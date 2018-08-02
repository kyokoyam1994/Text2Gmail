package com.example.kosko.text2gmail.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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

import com.example.kosko.text2gmail.LogEntryAdapter;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.LogEntry;

import java.util.List;

public class MessageLogFragment extends ListFragment implements View.OnClickListener{

    private final static String CLEAR_OPERATION = "CLEAR_OPERATION";
    public final static String REFRESH_INTENT = "REFRESH_INTENT";

    private LogUpdateBroadcastReceiver logUpdateBroadcastReceiver;
    private Spinner spinnerSortLog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_log_fragment, container, false);
        Button buttonClearLog = view.findViewById(R.id.buttonClearLog);
        spinnerSortLog = view.findViewById(R.id.spinnerSortLog);
        spinnerSortLog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {refreshLog();}

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
        System.out.println("Registering...");
        logUpdateBroadcastReceiver = new LogUpdateBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(logUpdateBroadcastReceiver, new IntentFilter(REFRESH_INTENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Unregistering...");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(logUpdateBroadcastReceiver);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonClearLog:
                clearLog(v);
                break;
        }
    }

    public void clearLog(View v){
        new LogEntryTask().execute(CLEAR_OPERATION);
    }

    public void refreshLog(){
        new LogEntryTask().execute();
    }

    private class LogEntryTask extends AsyncTask<String, Void, List<LogEntry>> {

        private String sortOption = spinnerSortLog.getSelectedItem().toString();

        @Override
        protected List<LogEntry> doInBackground(String... strings) {
            if(strings.length > 0 && CLEAR_OPERATION.equals(strings[0])) AppDatabase.getInstance(getActivity()).logEntryDao().deleteAll();
            if(sortOption.equals(getString(R.string.sender))) return AppDatabase.getInstance(getActivity()).logEntryDao().getAllBySender();
            else return AppDatabase.getInstance(getActivity()).logEntryDao().getAllByTimestamp();
        }

        @Override
        protected void onPostExecute(List<LogEntry> logEntries) {
            LogEntryAdapter adapter = new LogEntryAdapter(getActivity(), R.layout.log_entry, logEntries);
            setListAdapter(adapter);
        }
    }

    private class LogUpdateBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Log update called!!!");
            refreshLog();
        }
    }

}
