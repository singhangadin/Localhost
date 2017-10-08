package com.github.angads25.localhost;

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
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.angads25.localhost.models.RouterListItem;
import com.github.angads25.localhost.utils.AppConstants;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Created by Angad on 08-10-2017.
 * </p>
 */

public class DetectActivity extends AppCompatActivity {
    private WifiManager mainWifiObj;
    private WifiScanReceiver wifiReciever;

    private Set<String> savedSSIDS;
    private AppCompatTextView locationLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_location);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DetectActivity.this);
        savedSSIDS = preferences.getStringSet("SSIDS", new HashSet<String>());

        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        locationLabel.findViewById(R.id.location_label);
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
            final JSONObject object = new JSONObject();
            for(ScanResult result: wifiScanList) {
                if(savedSSIDS.contains(result.SSID)) {
                    RouterListItem item = new RouterListItem();
                    item.setSSID(result.SSID);
                    item.setBSSID(result.BSSID);
                    item.setStrength(result.level);
                    try {
                        object.put(result.SSID + " " + result.BSSID, Math.abs(result.level));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            String url = AppConstants.BASE_URL + "/" + AppConstants.PREDICT_URL;
            RequestQueue queue = Volley.newRequestQueue(DetectActivity.this);
            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("TAG", response);
                            locationLabel.setText(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  header = new HashMap<>();
                    String token = FirebaseInstanceId.getInstance().getToken();
                    header.put("fcmToken", token);
                    header.put("Content-Type", "application/x-www-form-urlencoded");
                    return header;
                }

                @Override
                public String getBodyContentType() {
                    return "application/text; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        Log.e("TAG", "Body: " + object.toString());
                        return object.toString() == null ? null : object.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
            stringRequest.setShouldCache(false);
            queue.add(stringRequest);
        }
    }
}
