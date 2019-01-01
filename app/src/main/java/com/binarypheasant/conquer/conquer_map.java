package com.binarypheasant.conquer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.LinkedList;
import java.util.List;

public class conquer_map extends AppCompatActivity {

    String statusCode;
    static public String[] location_name,location_group;
    int[] location_score;
    int location_num;

    public static List<Activity> activityList = new LinkedList();

    public void exit()
    {
        for(Activity act:activityList)
        {
            act.finish();
        }
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conquer_map);

        conquer_map.activityList.add(this);

        initMap();
        sendRequestWithHttpURLConnection();

        Button conquerButton = (Button) findViewById(R.id.conquerButton);
        conquerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GotoConquering = new Intent(conquer_map.this, conquering.class);
                startActivity(GotoConquering);
            }
        });
        Button exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GotoProfile = new Intent(conquer_map.this, profile.class);
                startActivity(GotoProfile);
            }
        });
    }

    private void initMap() {
        location_num = 5;
        location_name = new String[50];
        location_group = new String[50];
        location_score = new int[50];
        location_name[0] = "TeachingBuilding1";
        location_name[1] = "TeachingBuilding2";
        location_name[2] = "TeachingBuilding3";
        location_name[3] = "ScienceBuilding";
        location_name[4] = "PekingLibrary";
        for(int i=0;i<location_num;++i){
            location_group[i] = "null";
            location_score[i] = 0;
        }
        //TODO 等数据库完善后初始化更改为链接数据库获取地点基本信息
    }

    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/score/highestgroup.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("token="+log_in.token);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //Log.v("HTTPResponse",response.toString());

                    JSONObject jsonresponse = new JSONObject(response.toString());
                    UpdateMap(jsonresponse);
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
                Toast.makeText(conquer_map.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void UpdateMap(final JSONObject response){
        //// parse the response
        JSONArray result = null;
        JSONObject temp;
        boolean should_continue  = true;
        int i = 0;
        try{
            statusCode = response.getString("status");
            result = new JSONArray(response.getString("result"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        if (statusCode.equals("0")) showResponse("地图加载成功");
        else{
            showResponse("其他错误，返回值"+String.valueOf(statusCode));
            return;
        }
        while(should_continue){
            try{
                temp = result.getJSONObject(i++);
                if (temp == null||temp.getString("location")==null) {should_continue = false;break;}
                else{
                    UpdateLocation(temp.getString("location"),temp.getString("group"),temp.getString("totalscore"));
                }
            }catch (JSONException e){e.printStackTrace();}
            if (i>=location_num) break;
        }
    }

    private void UpdateLocation(String locationName,final String group, String totalscore) {
        int i;
        for(i=0;i<location_num;++i)
            if(location_name[i].equals(locationName)){
                location_group[i] = group;
                location_score[i] = Integer.valueOf(totalscore);
                break;
            }
        if (i == location_num) return;
        final int index = i;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView team_image = findViewById(R.id.image1 + index);
                if (group.equals("white")) team_image.setImageResource(R.drawable.team_blue);
                else if (group.equals("black")) team_image.setImageResource(R.drawable.team_red);
                else Toast.makeText(conquer_map.this, "未定义队伍:"+group, Toast.LENGTH_LONG).show();
            }
        });
    }
}
