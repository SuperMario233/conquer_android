package com.binarypheasant.conquer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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

public class profile extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private  Home fragment1;
    private PersonRank fragment2;
    private Message fragment3;
    private Fragment[] fragments;
    private int lastfragment;
    private TextView mTextMessage;
    public static int[] scorelist;
    public static String[] namelist;
    public static int person_num,myscore;
    String statusCode;

    private void initFragment()
    {

        fragment1 = new Home();
        fragment2 = new PersonRank();
        fragment3 = new Message();
        fragments = new Fragment[]{fragment1,fragment2,fragment3};
        lastfragment=0;
        getSupportFragmentManager().beginTransaction().replace(R.id.mainview,fragment1).show(fragment1).commit();
        bottomNavigationView=(BottomNavigationView)findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(changeFragment);
    }

    private void switchFragment(int lastfragment,int index)
    {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastfragment]);//隐藏上个Fragment
        if(fragments[index].isAdded()==false)
        {
            transaction.add(R.id.mainview,fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener changeFragment
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                   if(lastfragment != 0){
                       switchFragment(lastfragment,0);
                       lastfragment = 0;
                   }
                    return true;
                case R.id.navigation_dashboard:
                    if(lastfragment != 1){
                        switchFragment(lastfragment,1);
                        lastfragment = 1;
                    }
                    return true;
                case R.id.navigation_notifications:
                    if(lastfragment != 2){
                        switchFragment(lastfragment,2);
                        lastfragment = 2;
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        namelist = new String[50];
        scorelist = new int[50];
        sendRequestWithHttpURLConnection();

        initFragment();
    }

    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/score/dailyrank.php");
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
                    UpdateScoreTable(jsonresponse);
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

    public void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(profile.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void UpdateScoreTable(final JSONObject response){
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
        if (!statusCode.equals("0")){
            showResponse("其他错误，返回值"+String.valueOf(statusCode));
            return;
        }
        person_num = result.length();
        if (person_num > 50) person_num = 50;
        for(i=0;i<person_num;i++){
            try{
                temp = result.getJSONObject(i);
                namelist[i] = temp.getString("nickname");
                scorelist[i] = temp.getInt("totalscore");
                if (namelist[i].equals(log_in.nickname)){
                    myscore = scorelist[i];
                    //TextView scoreText = findViewById(R.id.score);
                    //scoreText.setText(String.valueOf(myscore));
                }
            }catch (JSONException e){e.printStackTrace();}
        }
        for(i=0;i<person_num;++i)
            for(int j=i+1;j<person_num;++j)
                if(scorelist[i] < scorelist[j]){
                    int tempscore = scorelist[j];
                    scorelist[j] = scorelist[i];
                    scorelist[i] = tempscore;
                    String tempname = namelist[j];
                    namelist[j] = namelist[i];
                    namelist[i] = tempname;
                }
        showResponse("排名数目："+person_num);
    }
}
