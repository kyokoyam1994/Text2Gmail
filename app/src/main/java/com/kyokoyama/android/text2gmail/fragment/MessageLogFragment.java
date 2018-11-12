package com.kyokoyama.android.text2gmail.fragment;

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

import com.kyokoyama.android.text2gmail.adapter.LogEntryAdapter;
import com.kyokoyama.android.text2gmail.R;
import com.kyokoyama.android.text2gmail.database.AppDatabase;
import com.kyokoyama.android.text2gmail.database.entity.LogEntry;
import com.kyokoyama.android.text2gmail.util.Util;

import java.util.HashMap;
import java.util.List;

public class MessageLogFragment extends ListFragment implements View.OnClickListener {

    public static final String REFRESH_INTENT = "REFRESH_INTENT";

    private LogUpdateBroadcastReceiver logUpdateBroadcastReceiver;

    private enum LogEntryOperation {
        REFRESH,
        CLEAR
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_log_fragment, container, false);
        Button buttonClearLog = view.findViewById(R.id.buttonClearLog);
        Spinner spinnerSortLog = view.findViewById(R.id.spinnerSortLog);
        spinnerSortLog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { performLogOperation(LogEntryOperation.REFRESH); }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        buttonClearLog.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        performLogOperation(LogEntryOperation.REFRESH);
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
                performLogOperation(LogEntryOperation.CLEAR);
                break;
        }
    }

    private void performLogOperation(LogEntryOperation operation){
        View view = getView();
        if(view != null) {
            Spinner spinnerSortLog = view.findViewById(R.id.spinnerSortLog);
            new LogEntryTask(this, operation, spinnerSortLog.getSelectedItem().toString()).execute();
        }
    }

    private static class LogEntryTask extends AsyncTask<Void, Void, List<LogEntry>> {
        private MessageLogFragment fragment;
        private Context context;
        private LogEntryOperation operation;
        private String sortOption;
        private String sender;
        private HashMap<String, String> contactNameMap = new HashMap<>();

        LogEntryTask(MessageLogFragment fragment, LogEntryOperation operation, String sortOption) {
            this.fragment = fragment;
            this.operation = operation;
            this.sortOption = sortOption;
            sender = fragment.getResources().getString(R.string.sender);
            context = fragment.getContext().getApplicationContext();
        }

        @Override
        protected List<LogEntry> doInBackground(Void... voids) {
            if (operation == LogEntryOperation.CLEAR) {
                AppDatabase.getInstance(context).logEntryDao().deleteAll();
            }

            List<LogEntry> entries;
            if (sortOption.equals(sender)) {
                entries = Util.sortLogEntriesByContactName(context, AppDatabase.getInstance(context).logEntryDao().getAllBySender());
            } else {
                entries = AppDatabase.getInstance(context).logEntryDao().getAllByTimestamp();
            }

            for (LogEntry entry : entries) contactNameMap.put(entry.getSenderNumber(), Util.findContactNameByNumber(context, entry.getSenderNumber()));
            return entries;
        }

        @Override
        protected void onPostExecute(List<LogEntry> logEntries) {
            if (fragment != null && fragment.getActivity() != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                LogEntryAdapter adapter = new LogEntryAdapter(fragment.getActivity(), R.layout.log_entry, logEntries, contactNameMap);
                fragment.setListAdapter(adapter);
            }
        }
    }

    private class LogUpdateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            performLogOperation(LogEntryOperation.REFRESH);
        }
    }

}
