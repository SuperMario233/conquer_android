package com.binarypheasant.conquer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class rank extends AppCompatActivity {

    String statusCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        conquer_map.activityList.add(this);

        TextView scoreView = findViewById(R.id.scoreText);
        TextView locationView = findViewById(R.id.pointText);
        int myscore = getIntent().getIntExtra("score",0);
        scoreView.setText(String.valueOf(myscore));
        String location = getIntent().getStringExtra("location");
        locationView.setText(location);

        sendRequestWithHttpURLConnection();

        Button exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GotoNext = new Intent(rank.this, conquer_map.class);
                startActivity(GotoNext);
            }
        });
    }

    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/score/localranklist.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("location="+conquering.location+"&token="+log_in.token);
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

    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(rank.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void TryToGoNext(final JSONObject response){
        //// parse the response
        JSONArray result = null;
        JSONObject temp;
        int i=0;
        boolean should_continue = true;
        try{
            statusCode = response.getString("status");
            result = new JSONArray(response.getString("result"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        //Toast.makeText(log_in.this, "statusCode:"+statusCode, Toast.LENGTH_LONG).show();
        //SendRet = false;

        if(statusCode.equals("1")) {showResponse("地点缺失");return;}
        else if (statusCode.equals("0")) showResponse("表单拉取成功");
        else {showResponse("其他错误，返回值"+String.valueOf(statusCode));return;}
        while(should_continue){
            try{
                temp = result.getJSONObject(i++);
                if (temp == null||temp.getString("group")==null) {should_continue = false;break;}
                else{
                    UpdateScore(temp.getString("group"),temp.getString("totalscore"));
                }
            }catch (JSONException e){e.printStackTrace();}
            if (i>=1) break;
        }
    }

    private void UpdateScore(final String group, final String score) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView scoreText;
                if(group.equals("black")) scoreText = findViewById(R.id.team2Text);
                else scoreText = findViewById(R.id.team1Text);
                scoreText.setText(score);
            }
        });
    }
}
