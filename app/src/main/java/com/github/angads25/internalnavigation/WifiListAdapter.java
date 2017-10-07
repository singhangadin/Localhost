package com.github.angads25.internalnavigation;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * <p>
 * Created by Angad on 07-10-2017.
 * </p>
 */

class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiListHolder> {
    private ArrayList<ScanResult> scanList;
    private Context context;

    WifiListAdapter(ArrayList<ScanResult> scanList, Context context) {
        this.scanList = scanList;
        this.context = context;
    }

    @Override
    public WifiListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_wifi, parent, false);
        return new WifiListHolder(view);
    }

    @Override
    public void onBindViewHolder(WifiListHolder holder, int position) {
        holder.ssidLabel.setText(scanList.get(position).SSID);
        holder.macLabel.setText(scanList.get(position).BSSID);
        holder.strengthLabel.setText(scanList.get(position).level + "");
    }

    @Override
    public int getItemCount() {
        return scanList.size();
    }

    class WifiListHolder extends RecyclerView.ViewHolder {
        TextView ssidLabel;
        TextView macLabel;
        TextView strengthLabel;

        WifiListHolder(View itemView) {
            super(itemView);
            ssidLabel = itemView.findViewById(R.id.wifi_name);
            macLabel = itemView.findViewById(R.id.mac_address);
            strengthLabel = itemView.findViewById(R.id.level);
        }
    }
}
