package com.mingxiu.pulllayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mingxiu.pull.PullLayout;


public class MainActivity extends AppCompatActivity implements PullLayout.OnRefreshListener {
    PullLayout pullLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullLayout = (PullLayout) findViewById(R.id.pull_layout);
        pullLayout.setCanMore(true);
        pullLayout.setCanPull(true);
        pullLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        pullLayout.completeAll();
    }

    @Override
    public void onMore() {
        pullLayout.completeAll();
    }
}
