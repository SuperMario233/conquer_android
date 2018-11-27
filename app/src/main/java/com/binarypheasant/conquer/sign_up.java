package com.binarypheasant.conquer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class sign_up extends AppCompatActivity {

    String account,password,group,nickname,statusCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        conquer_map.activityList.add(this);

        Button signupButton = (Button) findViewById(R.id.signupButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText user_emailText = findViewById(R.id.accountText);
                EditText user_passwordText = findViewById(R.id.passwordText);
                EditText user_group = findViewById(R.id.teamText);
                EditText user_nickname = findViewById(R.id.nicknameText);
                account = user_emailText.getText().toString();
                password = user_passwordText.getText().toString();
                group = user_group.getText().toString();
                nickname = user_nickname.getText().toString();
                if(TextUtils.isEmpty(account)){
                    user_emailText.setError("用户名不能为空");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    user_passwordText.setError("密码不能为空");
                    return;
                }
                if(TextUtils.isEmpty(group)){
                    //TODO 阵营需要改为下拉选单
                    user_group.setError("阵营不能为空");
                    return;
                }
                if(TextUtils.isEmpty(nickname)){
                    user_nickname.setError("昵称不能为空");
                    return;
                }
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
                    URL url = new URL("https://ingfo.huyunfan.cn/user/signup.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("account="+account+"&password="+password+"&group="+group+"&nickname="+nickname);
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
                Toast.makeText(sign_up.this, response, Toast.LENGTH_LONG).show();
            }
        });
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

        if(statusCode.equals("1")) showResponse("用户已存在");
        else if (statusCode.equals("0")){
            showResponse("注册成功");
            Intent GotoNext = new Intent(sign_up.this, log_in.class);
            startActivity(GotoNext);
        }
        else showResponse("其他错误，返回值"+String.valueOf(statusCode));
    }

}
