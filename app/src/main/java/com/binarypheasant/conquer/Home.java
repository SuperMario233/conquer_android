package com.binarypheasant.conquer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Home extends Fragment {
    private Button button;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        button = (Button)getActivity().findViewById(R.id.logoutButton);
        TextView nicknameText = getActivity().findViewById(R.id.nickName);
        nicknameText.setText(log_in.nickname);
        TextView groupText = getActivity().findViewById(R.id.groupName);
        groupText.setText(log_in.group);
        TextView scoreText = getActivity().findViewById(R.id.score);
        scoreText.setText(String.valueOf(profile.myscore));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GotoNext = new Intent(getActivity(), MainActivity.class);
                startActivity(GotoNext);
            }
        });

    }
}