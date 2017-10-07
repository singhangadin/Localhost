package com.github.angads25.internalnavigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Created by Angad on 07-10-2017.
 * </p>
 */

public class SortActivity extends AppCompatActivity {
    private WifiManager mainWifiObj;
    private WifiScanReceiver wifiReciever;

    private ArrayList<SortListItem> scanResult;
    private WifiCheckListAdapter listAdapter;
    private HashSet<String> ssids;

    private Set<String> savedSSIDS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);

        scanResult = new ArrayList<>();
        ssids = new HashSet<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SortActivity.this);
        savedSSIDS = preferences.getStringSet("SSIDS", new HashSet<String>());

        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        listAdapter = new WifiCheckListAdapter(scanResult, SortActivity.this);

        RecyclerView wifiList = (RecyclerView) findViewById(R.id.wifi_list);
        wifiList.setLayoutManager(new LinearLayoutManager(SortActivity.this));
        wifiList.addItemDecoration(new DividerItemDecoration(SortActivity.this, DividerItemDecoration.VERTICAL));
        wifiList.setAdapter(listAdapter);

        Button save = (Button) findViewById(R.id.action_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashSet<String> set = new HashSet<>();
                for(SortListItem item : scanResult) {
                    if(item.isSelected()) {
                        set.add(item.getSsid());
                    }
                }
                SharedPreferences ss = PreferenceManager.getDefaultSharedPreferences(SortActivity.this);
                SharedPreferences.Editor edit = ss.edit();
                edit.putStringSet("SSIDS", set);
                edit.apply();
                finish();
            }
        });
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifiObj.startScan();
        super.onResume();
    }

    class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            scanResult.clear();
            for(ScanResult result: wifiScanList) {
                if(!ssids.contains(result.SSID)) {
                    SortListItem item = new SortListItem();
                    item.setSsid(result.SSID);
                    if(!savedSSIDS.contains(result.SSID)) {
                        item.setSelected(false);
                    } else {
                        item.setSelected(true);
                    }
                    scanResult.add(item);
                    ssids.add(result.SSID);
                }
            }
            listAdapter.notifyDataSetChanged();
        }
    }
}
