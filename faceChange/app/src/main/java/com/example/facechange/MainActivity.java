package com.example.facechange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.zeusee.main.hyperlandmark.hxy_facechange.FC_Activity;

public class MainActivity extends AppCompatActivity {

    private Button btn_faceChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //btn点击跳转activity到aar包内
        btn_faceChange =(Button)findViewById(R.id.btn_facechange);
        btn_faceChange.setOnClickListener(  v -> {
            try {
                Intent intent = new Intent();
                //btn点击跳转到aar包内activity
                intent.setClass(MainActivity.this, FC_Activity.class);

                //btn点击跳转到当前app的FaceChangeActivity，需要迁移对应layout：face_change
                //intent.setClass(MainActivity.this, FaceChangeActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}