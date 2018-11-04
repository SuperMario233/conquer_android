package com.binarypheasant.conquer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.LinkedList;
import java.util.List;

public class conquer_map extends AppCompatActivity {

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

        Button conquerButton = (Button) findViewById(R.id.conquerButton);
        conquerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GotoConquering = new Intent(conquer_map.this, conquering.class);
                startActivity(GotoConquering);
                /*MySignUp();*/
            }
        });
        Button exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });
    }
}
