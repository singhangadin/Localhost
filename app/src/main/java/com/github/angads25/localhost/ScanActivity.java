package com.github.angads25.localhost;

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
import android.util.Log;

import com.github.angads25.localhost.adapters.WifiListAdapter;
import com.github.angads25.localhost.models.RouterListItem;
import com.github.angads25.localhost.utils.AppConstants;
import com.github.angads25.promise.PromiseDBHelper;
import com.github.angads25.promise.Request;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {
    private WifiManager mainWifiObj;
    private WifiScanReceiver wifiReciever;

    private ArrayList<RouterListItem> scanResult;
    private WifiListAdapter listAdapter;

    private Set<String> savedSSIDS;

    private int count = 0;
    StringBuffer buffer = new StringBuffer();

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

        RecyclerView wifiList = findViewById(R.id.wifi_list);
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
            JSONObject object = new JSONObject();
            for(ScanResult result: wifiScanList) {
                if(savedSSIDS.contains(result.SSID)) {
                    RouterListItem item = new RouterListItem();
                    item.setSSID(result.SSID);
                    item.setBSSID(result.BSSID);
                    item.setStrength(result.level);
                    scanResult.add(item);
                    if(count < 1) {
                        try {
                            object.put(result.SSID + " " +result.BSSID, Math.abs(result.level));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(count < 1) {
                buffer.append(object.toString());
                count++;
            } else if(count == 1) {
                Log.e("TAG", buffer.toString());
                Request request = new Request();
                request.setUrl(AppConstants.BASE_URL + "/" + AppConstants.LEARN_URL);
                request.setMethod(com.android.volley.Request.Method.POST);
                request.setBody(buffer.toString());
                JSONObject header = new JSONObject();
                try {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    header.put("fcmToken", token);
                    header.put("Content-Type", "application/x-www-form-urlencoded");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                request.setHeader(header.toString());
                PromiseDBHelper helper = new PromiseDBHelper(ScanActivity.this);
                helper.addRequest(request);
                count++;
            }
            Collections.sort(scanResult);
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
