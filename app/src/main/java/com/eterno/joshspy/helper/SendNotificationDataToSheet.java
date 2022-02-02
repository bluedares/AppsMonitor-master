package com.eterno.joshspy.helper;


import android.os.AsyncTask;
import android.util.Log;

import com.eterno.joshspy.AppConst;
import com.eterno.joshspy.util.AppUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class SendNotificationDataToSheet extends AsyncTask<String, Void, String> {

  private static String userId = AppUtil.getClientId();
  private static final String TAG = "SendNotifiDataToSheet";

  protected void onPreExecute() {
  }

  protected String doInBackground(String... arg0) {
    try {
      URL url = new URL(AppConst.APP_SCRIPT_URL);
      JSONObject postDataParams = new JSONObject();

      postDataParams.put("user_id", userId);
      postDataParams.put("app_name", arg0[0]);
      postDataParams.put("package", arg0[1]);
      postDataParams.put("time", arg0[2]);
      postDataParams.put("req_type", "NOTI");


      Log.d(TAG, postDataParams.toString());

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setReadTimeout(15000 /* milliseconds */);
      conn.setConnectTimeout(15000 /* milliseconds */);
      conn.setRequestMethod("POST");
      conn.setDoInput(true);
      conn.setDoOutput(true);

      OutputStream os = conn.getOutputStream();
      BufferedWriter writer = new BufferedWriter(
          new OutputStreamWriter(os, "UTF-8"));
      writer.write(getPostDataString(postDataParams));

      writer.flush();
      writer.close();
      os.close();

      int responseCode = conn.getResponseCode();

      if (responseCode == HttpsURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer("");
        String line = "";

        while ((line = in.readLine()) != null) {

          sb.append(line);
          break;
        }

        in.close();
        Log.d(TAG, sb.toString());

      } else {
        Log.d(TAG, ("false : " + responseCode));
      }
    } catch (Exception e) {
      Log.d(TAG, ("Exception: " + e.getMessage()));
    }
    return "";
  }

  @Override
  protected void onPostExecute(String result) {


  }


  public static String getPostDataString(JSONObject params) throws Exception {

    StringBuilder result = new StringBuilder();
    boolean first = true;

    Iterator<String> itr = params.keys();

    while (itr.hasNext()) {

      String key = itr.next();
      Object value = params.get(key);

      if (first) {
        first = false;
      } else {
        result.append("&");
      }

      result.append(URLEncoder.encode(key, "UTF-8"));
      result.append("=");
      result.append(URLEncoder.encode(value.toString(), "UTF-8"));
    }
    return result.toString();
  }
}


