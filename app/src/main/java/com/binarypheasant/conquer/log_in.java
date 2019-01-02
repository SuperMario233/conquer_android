package com.binarypheasant.conquer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class log_in extends AppCompatActivity {

    public String statusCode,account,password;
    public static String userid,group,nickname,token;
    boolean SendRet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        conquer_map.activityList.add(this);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText user_emailText = findViewById(R.id.accountText);
                EditText user_passwordText = findViewById(R.id.passwordText);
                String user_email = user_emailText.getText().toString();
                String user_password = user_passwordText.getText().toString();
                if(TextUtils.isEmpty(user_email)){
                    user_emailText.setError("用户名不能为空");
                    return;
                }
                if(TextUtils.isEmpty(user_password)){
                    user_passwordText.setError("密码不能为空");
                    return;
                }
                account = user_email;
                password = user_password;
                sendRequestWithHttpURLConnection();
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
                    URL url = new URL("https://ingfo.huyunfan.cn/user/signin.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("account="+account+"&password="+password);
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
            userid = response.getString("userid");
            nickname = response.getString("nickname");
            group = response.getString("group");
            token = response.getString("token");
        }catch (JSONException e){
            e.printStackTrace();
        }
        if(statusCode.equals("1")) showResponse("用户不存在");
        else if (statusCode.equals("2")) showResponse("密码错误");
        else if (statusCode.equals("0")){
            showResponse("登录成功");
            Intent GotoMap = new Intent(log_in.this, conquer_map.class);
            startActivity(GotoMap);
        }
        else showResponse("其他错误，返回值"+String.valueOf(statusCode));
    }

    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(log_in.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean SendToSever(String email,String password){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://ingfo.huyunfan.cn/user/signin.php";
        JSONObject sign_inJSON = new JSONObject();
        try {
            sign_inJSON.put("account", email);
            sign_inJSON.put("password", password);
        } catch (JSONException e){
            e.printStackTrace();
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, sign_inJSON,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 需要判断返回码
                        //// parse the response
                        try{
                            statusCode = response.getString("status");
                            nickname = response.getString("nickname");
                            group = response.getString("group");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        //Toast.makeText(log_in.this, "statusCode:"+statusCode, Toast.LENGTH_LONG).show();
                        //SendRet = false;

                        if(statusCode.equals("1")) showResponse("用户不存在");
                        else if (statusCode.equals("2")) showResponse("密码错误");
                        else if (statusCode.equals("0")){
                            showResponse("登录成功");
                            Intent GotoMap = new Intent(log_in.this, conquer_map.class);
                            startActivity(GotoMap);
                        }
                        else showResponse("其他错误，返回值"+String.valueOf(statusCode));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(log_in.this, "Error "+error, Toast.LENGTH_LONG).show();
                //Intent GotoProfile = new Intent(log_in.this, profile_main.class);
                //startActivity(GotoProfile);
                error.printStackTrace();
                SendRet = false;
            }
        }
        );

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
        return SendRet;
    }

}
