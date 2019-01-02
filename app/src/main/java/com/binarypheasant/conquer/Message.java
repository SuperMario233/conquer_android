package com.binarypheasant.conquer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;

public class Message extends Fragment {
    private ListView listView;
    private ArrayAdapter adapter;
    public static String message[];
    final ArrayList<String> list = new ArrayList<>();
    String statusCode;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_message,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        message = new String[20];
        listView = getActivity().findViewById(R.id.messagelist);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        getMessage();
        Button finishButton = (Button) getActivity().findViewById(R.id.send);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView messageText = getActivity().findViewById(R.id.messageText);
                String message = messageText.getText().toString();
                if(TextUtils.isEmpty(message)){
                    messageText.setError("消息不为空");
                    return;
                }
                sendMessage(message);
                list.add(log_in.nickname+": "+message);
                messageText.setText("");
            }
        });
    }

    private void getMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/info/getinfo.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("mygroup="+log_in.group+"&token="+log_in.token);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //Log.v("HTTPResponse",response.toString());

                    JSONObject jsonresponse = new JSONObject(response.toString());
                    UpdateMessage(jsonresponse);
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

    private void sendMessage(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("https://ingfo.huyunfan.cn/info/updateinfo.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("message="+message+"&token="+log_in.token);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //Log.v("HTTPResponse",response.toString());

                    JSONObject jsonresponse = new JSONObject(response.toString());
                    RefreshMessage(jsonresponse);
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

    private void RefreshMessage(final JSONObject response){
        try{
            statusCode = response.getString("status");
        }catch (JSONException e){e.printStackTrace();}
        if(statusCode.equals("0")){
            getMessage();
        }
        else{
            showResponse("发送信息失败"+statusCode);
        }
    }

    public void showResponse(final String response){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void UpdateMessage(final JSONObject response){
        //// parse the response
        JSONArray result = null;
        JSONObject temp;
        int i = 0;
        try{
            statusCode = response.getString("status");
            result = new JSONArray(response.getString("result"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        if (!statusCode.equals("0")){
            showResponse("消息列表拉取失败："+statusCode);
            return;
        }
        else{
            showResponse("拉取"+result.length()+"条消息");
        }
        for(i=0;i<result.length();i++){
            try{
                temp = result.getJSONObject(i);
                message[i] = temp.getString("nickname") + "\t: "+temp.getString("message");
            }catch (JSONException e){e.printStackTrace();}
        }
        //list.clear();
        for(int j=0;j<i;++j){
            list.add(message[j]);
        }
    }
}