package com.colinmckee.alamodrafthouse.REST;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;

/**
 * REST request class.
 */
public class RestRequest {

    public interface OnComplete {
        void onComplete(boolean success, RestResponse response);
    }

    public String Method = null;
    public String UrlString = null;
    /** JSONObject that will be submitted.*/
    public JSONObject Data = null;
    /** JSONArray that will be submitted.*/
    public JSONArray Array = null;
    public boolean UseBasicAuthentication = false;
    public String Username = null;
    public String Password = null;
    public boolean UseHttps = false;
    /** URL object from UrlString.*/
    public URL Url = null;

    /** Basic constructor*/
    public RestRequest(String method, @NonNull String urlString, JSONObject data, JSONArray array,
                       boolean useBasicAuthentication, String username, String password)
    {
        Method = method;
        UrlString = urlString;
        Data = data;
        Array = array;
        UseBasicAuthentication = useBasicAuthentication;
        Username = username;
        Password = password;
        UseHttps = urlString.startsWith("https://");
    }

    /** Basic constructor*/
    public RestRequest(String method, @NonNull String urlString, String username, String password,
                       JSONObject data)
    {
        Method = method;
        UrlString = urlString;
        Data = data;
        Array = null;
        UseBasicAuthentication = true;
        Username = username;
        Password = password;
        UseHttps = urlString.startsWith("https://");
    }

    /** Basic constructor*/
    public RestRequest(String method, @NonNull String urlString, String username, String password,
                       JSONArray data) {
        Method = method;
        UrlString = urlString;
        Data = null;
        Array = data;
        UseBasicAuthentication = true;
        Username = username;
        Password = password;
        UseHttps = urlString.startsWith("https://");
    }

    /** Basic constructor*/
    public RestRequest(String method, @NonNull String urlString, String username, String password) {
        Method = method;
        UrlString = urlString;
        Data = null;
        Array = null;
        UseBasicAuthentication = true;
        Username = username;
        Password = password;
        UseHttps = urlString.startsWith("https://");
    }

    /** constructor with no useBasicAuthentication*/
    public RestRequest(String method, String urlString, JSONObject data, JSONArray array) {
        this(method, urlString, data, array, false, "", "");
    }

    public void asyncRequest(final OnComplete oc) {
        try {
            RestClient restClient = new RestClient(new RestClient.OnRestResponse() {
                @Override
                public void onRestResponse(RestResponse response) {
                    boolean success = (200 <= response.code && response.code <= 299);
                    oc.onComplete(success, response);
                }
            });
            // do URL conversion here as it requires exception processing
            Url = new URL(UrlString);
            // execute the task
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // Allows multiple restClient tasks to operate in parallel rather than
                // serially to prevent possibility of a large queue
                restClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
            }
            else {
                restClient.execute(this);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
