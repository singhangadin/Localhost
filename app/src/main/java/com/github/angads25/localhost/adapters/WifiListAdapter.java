package com.github.angads25.localhost.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.angads25.localhost.R;
import com.github.angads25.localhost.models.RouterListItem;

import java.util.ArrayList;

/**
 * <p>
 * Created by Angad on 07-10-2017.
 * </p>
 */

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiListHolder> {
    private ArrayList<RouterListItem> scanList;
    private Context context;

    public WifiListAdapter(ArrayList<RouterListItem> scanList, Context context) {
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
        holder.ssidLabel.setText(scanList.get(position).getSSID());
        holder.macLabel.setText(scanList.get(position).getBSSID());
        holder.strengthLabel.setText(scanList.get(position).getStrength() + "");
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
