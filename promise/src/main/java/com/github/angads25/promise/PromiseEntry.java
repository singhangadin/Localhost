package com.github.angads25.promise;

import android.provider.BaseColumns;

/**
 * <p>
 * Created by Angad on 08-10-2017.
 * </p>
 */

public class PromiseEntry implements BaseColumns {

    public static final String TABLE_NAME = "promise";

    public static final String REQUEST_ID = "request_id";
    public static final String REQUEST_URL = "request_url";
    public static final String REQUEST_BODY = "request_body";
    public static final String REQUEST_HEADER = "request_header";
    public static final String REQUEST_METHOD = "request_method";
}
