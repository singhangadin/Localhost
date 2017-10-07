package com.github.angads25.internalnavigation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * <p>
 * Created by Angad on 07-10-2017.
 * </p>
 */

class WifiCheckListAdapter extends RecyclerView.Adapter<WifiCheckListAdapter.WifiListHolder>
    implements CompoundButton.OnCheckedChangeListener {
    private ArrayList<SortListItem> scanList;
    private Context context;

    WifiCheckListAdapter(ArrayList<SortListItem> scanList, Context context) {
        this.scanList = scanList;
        this.context = context;
    }

    @Override
    public WifiListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_wifi_scan, parent, false);
        return new WifiListHolder(view);
    }

    @Override
    public void onBindViewHolder(WifiListHolder holder, int position) {
        holder.ssidLabel.setText(scanList.get(position).getSsid());
        holder.checkBox.setTag(position);
        if(scanList.get(position).isSelected()) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return scanList.size();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.checkList : {
                int position = (int) compoundButton.getTag();
                scanList.get(position).setSelected(compoundButton.isChecked());
                break;
            }
        }
    }

    class WifiListHolder extends RecyclerView.ViewHolder {
        TextView ssidLabel;
        CheckBox checkBox;

        WifiListHolder(View itemView) {
            super(itemView);
            ssidLabel = itemView.findViewById(R.id.wifi_name);
            checkBox = itemView.findViewById(R.id.checkList);
            checkBox.setOnCheckedChangeListener(WifiCheckListAdapter.this);
        }
    }
}
