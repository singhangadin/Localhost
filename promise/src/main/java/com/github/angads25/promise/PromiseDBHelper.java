package com.github.angads25.promise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * Created by Angad on 08-10-2017.
 * </p>
 */

public class PromiseDBHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "promise.db";
    private static final int VERSION = 1;

    private Context context;

    private static final String CREATE_QUERY =
    "create table " + PromiseEntry.TABLE_NAME +" ("+
        PromiseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//        PromiseEntry.REQUEST_ID + " TEXT UNIQUE," +
        PromiseEntry.REQUEST_URL + " TEXT NOT NULL," +
        PromiseEntry.REQUEST_BODY + " TEXT," +
        PromiseEntry.REQUEST_HEADER + " TEXT," +
        PromiseEntry.REQUEST_METHOD + " INTEGER"+
    ")";

    public PromiseDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    private int countRows() {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(PromiseEntry.TABLE_NAME, null, null, null, null, null, null);
        int result = cursor.getCount();
        cursor.close();
        return result;
    }

    private Request getRequestData() {
        Request request = new Request();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(PromiseEntry.TABLE_NAME, new String[] {
                PromiseEntry.REQUEST_URL,
                PromiseEntry.REQUEST_HEADER,
                PromiseEntry.REQUEST_METHOD,
                PromiseEntry.REQUEST_BODY,
                PromiseEntry._ID
        }, null, null, null, null, null, "1");
        cursor.moveToFirst();
        request.setUrl(cursor.getString(cursor.getColumnIndex(PromiseEntry.REQUEST_URL)));
        request.setHeader(cursor.getString(cursor.getColumnIndex(PromiseEntry.REQUEST_HEADER)));
        request.setMethod(cursor.getInt(cursor.getColumnIndex(PromiseEntry.REQUEST_METHOD)));
        request.setBody(cursor.getString(cursor.getColumnIndex(PromiseEntry.REQUEST_BODY)));
        request.setRequestId(cursor.getString(cursor.getColumnIndex(PromiseEntry._ID)));
        cursor.close();
        return request;
    }

    public void addRequest(Request request) {
        ContentValues values = new ContentValues();
        values.put(PromiseEntry.REQUEST_URL, request.getUrl());
        values.put(PromiseEntry.REQUEST_BODY, request.getBody());
        values.put(PromiseEntry.REQUEST_HEADER, request.getHeader());
        values.put(PromiseEntry.REQUEST_METHOD, request.getMethod());

        SQLiteDatabase database = getWritableDatabase();
        database.insert(PromiseEntry.TABLE_NAME, null, values);

        Log.e("TAG", "Values: " + values.toString());

        commenceTransport();
    }

    public void cancelRequest(String request_id) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(PromiseEntry.TABLE_NAME, PromiseEntry._ID + " = ?", new String[]{request_id});
    }

    public void commenceTransport() {
        int rowCount = countRows();
        Log.e("TAG", "Row Size: " + rowCount);
        while (rowCount > 0) {
            final Request request = getRequestData();
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(request.getMethod(), request.getUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            cancelRequest(request.getRequestId());
                            Log.e("TAG", response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<>();
                    try {
                        JSONObject object = new JSONObject(request.getHeader());
                        Iterator<?> keys = object.keys();
                        while (keys.hasNext()) {
                            String key = (String)keys.next();
                            params.put(key, object.getString(key));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "application/text; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        Log.e("TAG", "Body: " + request.getBody());
                        return request.getBody() == null ? null : request.getBody().getBytes("utf-8");
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
            rowCount--;
        }
    }
}
