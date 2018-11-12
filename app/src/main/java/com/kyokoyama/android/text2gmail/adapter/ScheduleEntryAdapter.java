package com.kyokoyama.android.text2gmail.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kyokoyama.android.text2gmail.R;
import com.kyokoyama.android.text2gmail.ScheduleEntry;

import java.util.List;

public class ScheduleEntryAdapter extends ArrayAdapter<ScheduleEntry> {

    public ScheduleEntryAdapter(@NonNull Context context, int resource, @NonNull List<ScheduleEntry> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.card_view_schedule, parent, false);
        }

        ScheduleEntry entry = getItem(position);
        TextView textViewDayOfWeek = v.findViewById(R.id.textViewDayOfWeek);
        TextView textViewScheduledStartTime = v.findViewById(R.id.textViewScheduledStartTime);
        TextView textViewScheduledEndTime = v.findViewById(R.id.textViewScheduledEndTime);

        textViewDayOfWeek.setText(entry.getDayOfTheWeek().getName());
        textViewScheduledStartTime.setText(entry.getStartTime());
        textViewScheduledEndTime.setText(entry.getEndTime());
        return v;
    }

}
