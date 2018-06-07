package com.colinmckee.alamodrafthouse.REST;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

public class RestClient extends AsyncTask<RestRequest, Void, RestResponse> {

    public interface OnRestResponse {
        void onRestResponse(RestResponse response);
    }

    private OnRestResponse _callback;

    public RestClient(OnRestResponse callback) {
        _callback = callback;
    }

    @Override
    protected RestResponse doInBackground(RestRequest... requests) {
        RestResponse response = new RestResponse();
        RestRequest request = requests[0];
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        boolean responseError = false;

        try {
            conn = (HttpURLConnection) request.Url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(request.Data != null || request.Array != null);
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(request.Method.toUpperCase());
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", encodeBasicAuthentication(request));

            if (request.Data != null) {
                // To upload a JSON Object
                String data = request.Data.toString();
                conn.setFixedLengthStreamingMode(request.Data.toString().length());
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                OutputStreamWriter writer = new OutputStreamWriter(os);
                writer.write(request.Data.toString());
                writer.flush();
                writer.close();
            }

            if (request.Array != null) {
                // To upload a JSONArray
                conn.setFixedLengthStreamingMode(request.Array.toString().length());
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                OutputStreamWriter writer = new OutputStreamWriter(os);
                writer.write(request.Array.toString());
                writer.flush();
                writer.close();
            }

            response.code = conn.getResponseCode();
            //Log.d("RestClient","url: "+request.UrlString+" code: "+response.code);
            InputStream is;
            if (200 <= response.code && response.code <= 299) {
                is = conn.getInputStream();
            }
            else {
                //This is incomplete. As server might return 302 when psw is wrong.
                is = conn.getErrorStream();
                if (response.code / 400 == 1) {
                    responseError = true;
                }
            }

            if (is != null) {
                baos = new ByteArrayOutputStream();
                bis = new BufferedInputStream(is);
                byte[] buffer = new byte[8192];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                baos.flush();
                byte[] bytes = baos.toByteArray();
                response.setData(bytes, responseError);
            }
        }
        catch (SocketTimeoutException e) {
            // Used so we can distinguish a network timeout
            response.code = -1;
            response.responseError = true;
            response.msg = "Connection timed out. Please try again.";
            Log.w("RestClient", response.msg + " url: " + request.UrlString);
        }
        catch (Exception e)
        {
            response.responseError = true;
            response.msg = "There was a problem connecting to the server. " +
                    "Please try again.";
            Log.w("RestClient", response.msg + " url: " + request.UrlString);
        }
        finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (baos != null) {
                    baos.close();
                }
            }
            catch (IOException e) {
                Log.e("RestClient", "There was a problem closing the connections.");
            }

            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(RestResponse response) {
        if (_callback != null) {
            _callback.onRestResponse(response);
        }
    }

    private String encodeBasicAuthentication(RestRequest request) {
        String encodedAuthorization = null;
        if (request.Username != null && request.Username.length() > 0 && request.Password != null) {
            String userpassword = request.Username + ":" + request.Password;
            byte[] bytes = userpassword.getBytes();
            encodedAuthorization = "Basic " + Base64.encodeToString(bytes, Base64.NO_WRAP);
        }
        return encodedAuthorization;
    }
}
