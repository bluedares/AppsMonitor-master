package com.eterno.joshspy.helper;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.eterno.joshspy.AppConst;
import com.eterno.joshspy.ui.MainActivity;
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
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class SendDataToSheet extends AsyncTask<String, Void, String> {
  String reqType = "";
  public SendDataToSheet(String reqType){
    this.reqType = reqType;
  }

  private static final String TAG = "SendDataToSheet";

  protected void onPreExecute() {
  }

  protected String doInBackground(String... arg0) {
    try {
      URL url = new URL(AppConst.APP_SCRIPT_URL);
      JSONObject postDataParams = new JSONObject();

      postDataParams.put("user_id", AppUtil.getClientId());
      postDataParams.put("data_set", arg0[0]);
      postDataParams.put("req_type", reqType);

      if(reqType == "OPEN"){
        Log.d(TAG, " DDDDD "+reqType+"  ->  "+arg0[1]);
      }

      Log.d(TAG, "  "+reqType+"  ->  "+postDataParams.toString());

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

      } else {
        Log.d(TAG,  "  "+reqType+"  ->  "+("false : " + responseCode));
      }
    } catch (Exception e) {
      Log.d(TAG,  "  "+reqType+"  ->  "+("Exception: " + e.getMessage()));
    }
   return "";
  }

  @Override
  protected void onPostExecute(String result) {

    if(reqType == "USAGE"){
      Log.d(TAG, "COMPLETEDD");
      Intent i = new Intent(AppUtil.getApplication(), MainActivity.class);
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      AppUtil.getApplication().startActivity(i);
    }


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


