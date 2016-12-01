package com.example.user.joe120101;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {
    TextView tv,tv2;
    GetData t = new GetData();
    CustomGauge gauge;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView2);
        gauge = (CustomGauge) findViewById(R.id.gauge3);
    }
    //抓取樹莓派透過藍芽上傳至firebase的溫度感測資料
        public void click1(View v)
        {
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            //網址是  目標資料庫的網址
            StringRequest request = new StringRequest("https://raspberrytemp-abf6c.firebaseio.com/temp.json",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);//
                                double temp = obj.getDouble("temp");
                                tv.setText(String.valueOf(temp));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            queue.add(request);
            queue.start();
    }


    public void click2(View v)
    {

        t.start();
    }

    //運用多重執行緒抓 firebase溫度感測資料
    private class GetData extends Thread {
        InputStream inputStream = null;
        @Override
        public void run()
        {
            URL url = null;
            while(true) { //利用while使app每隔一秒抓取資料
                try {
                    url = new URL("https://raspberrytemp-abf6c.firebaseio.com/temp.json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();
                    inputStream = conn.getInputStream();

                    //==============
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    String str = result.toString();
                    JSONObject obj = new JSONObject(str);
                    final double temp = obj.getDouble("temp");
                    final String time = obj.getString("time");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(String.valueOf(temp));
                            //運用SimpleDateFormat 在textview2中顯示時間
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//                            Date now = new Date();
//                            String strDate = sdf.format(now);
//                            tv2.setText(strDate);
                            tv2.setText(time);
                            gauge.setValue((int)(temp*10)); //運用別人做好的元件(儀錶板顯示),來顯示溫度資料
                        }
                    });
                    Thread.sleep(1000);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
