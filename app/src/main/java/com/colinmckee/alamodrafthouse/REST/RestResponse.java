package com.colinmckee.alamodrafthouse.REST;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

/**
 * REST Response data class to hold server response
 */
public class RestResponse
{
    private static final String TAG = "RestResponse";
    /** Server response code */
    public int code = 0;
    /** Server response as a JSONObject */
    public JSONObject jsonObject = null;
    /** Server response as a JSONArray */
    public JSONArray jsonArray = null;
    /** Server response as a String */
    public String msg = null;
    /** Server response as a byte array */
    public byte[] bytes = null;

    public boolean responseError;

    public RestResponse() {
        // Empty constructor
    }

    public void setData(byte[] data, boolean errorMsg) {
        bytes = data;
        responseError = errorMsg;
        setMessage(new String(bytes));
    }

    public void setMessage(String message) {

        if (message != null && message.length() > 1 && !responseError) {
            if (message.charAt(0) == '{' && message.charAt(message.length() - 1) == '}') {
                // msg can be decoded as a JSONObject
                try {
                    jsonObject = new JSONObject(message);
                }
                catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }

            }
            else if (message.charAt(0) == '[' && message.charAt(message.length() - 1) == ']') {
                // msg can be decoded as a JSONArray
                try {
                    jsonArray = new JSONArray(message);
                }
                catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
            else {
                // msg cannot be decoded as JSONArray or JSONObject, leave as string
                msg = message;
            }
        }
        else
        {
            // An error occurred, msg will now be an error message
            msg = message;
        }
    }
}
