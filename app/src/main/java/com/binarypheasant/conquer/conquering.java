package com.binarypheasant.conquer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class conquering extends AppCompatActivity {

    String statusCode;
    int score,time = 0;
    static String location;
    Timer timer = new Timer();
    TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conquering);

        conquer_map.activityList.add(this);

        location = conquer_map.location_name[(int)(Math.random()*10)%5];
        TextView locationView = findViewById(R.id.pointText);
        locationView.setText(location);

        timeView = findViewById(R.id.timeText);
        timer.schedule(task,1000,1000);

        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                sendRequestWithHttpURLConnection();
            }
        });
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    time++;
                    timeView.setText(String.format("%02d",time/60)+":"+String.format("%02d",time%60));
                }
            });
        }
    };

    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(conquering.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                score = time/2;
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/score/updatescore.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("userid="+log_in.userid+"&location="+location+"&group="+log_in.group+"&score="+String.valueOf(score)+"&token="+log_in.token);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //Log.v("HTTPResponse",response.toString());

                    JSONObject jsonresponse = new JSONObject(response.toString());
                    TryToGoNext(jsonresponse);
                    //showResponse(response.toString());
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if (reader != null){
                        try{
                            reader.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void TryToGoNext(final JSONObject response){
        //// parse the response
        try{
            statusCode = response.getString("status");
        }catch (JSONException e){
            e.printStackTrace();
        }
        //Toast.makeText(log_in.this, "statusCode:"+statusCode, Toast.LENGTH_LONG).show();
        //SendRet = false;

        if(statusCode.equals("1")){
            showResponse("用户不存在");
        }
        else if (statusCode.equals("0")){
            showResponse("上传成功");
            Intent GotoNext = new Intent(conquering.this, rank.class);
            GotoNext.putExtra("score",time/2);
            GotoNext.putExtra("location",location);
            startActivity(GotoNext);
        }
        else{
            showResponse("其他错误：返回值"+statusCode);
        }/**/
    }

}
