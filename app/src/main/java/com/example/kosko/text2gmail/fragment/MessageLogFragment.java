package com.example.kosko.text2gmail.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.kosko.text2gmail.adapter.LogEntryAdapter;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.LogEntry;
import com.example.kosko.text2gmail.util.Util;

import java.util.HashMap;
import java.util.List;

public class MessageLogFragment extends ListFragment implements View.OnClickListener{

    public static final String REFRESH_INTENT = "REFRESH_INTENT";

    private LogUpdateBroadcastReceiver logUpdateBroadcastReceiver;
    private Spinner spinnerSortLog;

    private enum LogEntryOperation {
        REFRESH,
        CLEAR
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_log_fragment, container, false);
        Button buttonClearLog = view.findViewById(R.id.buttonClearLog);
        spinnerSortLog = view.findViewById(R.id.spinnerSortLog);
        spinnerSortLog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { refreshLog(); }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        buttonClearLog.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLog();
        logUpdateBroadcastReceiver = new LogUpdateBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(logUpdateBroadcastReceiver, new IntentFilter(REFRESH_INTENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(logUpdateBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonClearLog:
                clearLog();
                break;
        }
    }

    public void clearLog(){
        new LogEntryTask(LogEntryOperation.CLEAR).execute();
    }

    public void refreshLog(){
        new LogEntryTask(LogEntryOperation.REFRESH).execute();
    }

    private class LogEntryTask extends AsyncTask<Void, Void, List<LogEntry>> {
        private LogEntryOperation operation;
        private String sortOption = spinnerSortLog.getSelectedItem().toString();
        private HashMap<String, String> contactNameMap = new HashMap<>();

        public LogEntryTask(LogEntryOperation operation) {
            this.operation = operation;
        }

        @Override
        protected List<LogEntry> doInBackground(Void... voids) {
            if(operation == LogEntryOperation.CLEAR) {
                AppDatabase.getInstance(getActivity()).logEntryDao().deleteAll();
            }

            List<LogEntry> entries;
            if (sortOption.equals(getResources().getString(R.string.sender))) {
                entries = Util.sortLogEntriesByContactName(getActivity(), AppDatabase.getInstance(getActivity()).logEntryDao().getAllBySender());
            } else {
                entries = AppDatabase.getInstance(getActivity()).logEntryDao().getAllByTimestamp();
            }

            for (LogEntry entry : entries) contactNameMap.put(entry.getSenderNumber(), Util.findContactNameByNumber(getActivity(), entry.getSenderNumber()));
            return entries;
        }

        @Override
        protected void onPostExecute(List<LogEntry> logEntries) {
            LogEntryAdapter adapter = new LogEntryAdapter(getActivity(), R.layout.log_entry, logEntries, contactNameMap);
            setListAdapter(adapter);
        }
    }

    private class LogUpdateBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshLog();
        }
    }

}
