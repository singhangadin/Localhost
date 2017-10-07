package com.github.angads25.internalnavigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {
    private WifiManager mainWifiObj;
    private WifiScanReceiver wifiReciever;

    private ArrayList<ScanResult> scanResult;
    private WifiListAdapter listAdapter;

    private Set<String> savedSSIDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanResult = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
        savedSSIDS = preferences.getStringSet("SSIDS", new HashSet<String>());

        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        listAdapter = new WifiListAdapter(scanResult, getBaseContext());

        RecyclerView wifiList = (RecyclerView) findViewById(R.id.wifi_list);
        wifiList.setLayoutManager(new LinearLayoutManager(ScanActivity.this));
        wifiList.addItemDecoration(new DividerItemDecoration(ScanActivity.this, DividerItemDecoration.VERTICAL));
        wifiList.setAdapter(listAdapter);
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
                if(savedSSIDS.contains(result.SSID)) {
                    scanResult.add(result);
                }
            }
            listAdapter.notifyDataSetChanged();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainWifiObj.startScan();
                }
            }, 500);
        }
    }
}
