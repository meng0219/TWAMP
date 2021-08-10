package com.example.PEGA_latency_merger_tool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void go2Ping(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,pingPage.class);
        startActivity(intent);
    }
    public void go2Twamp(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,twampPage.class);
        startActivity(intent);
    }
    public void go2Trace(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,tracePage.class);
        startActivity(intent);
    }
}