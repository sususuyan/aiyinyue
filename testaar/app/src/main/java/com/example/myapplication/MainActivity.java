package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
public class MainActivity extends AppCompatActivity{
    @Override
protected void onCreate(Bundle saveInstanceState){
super.onCreate(saveInstanceState);
setContentView(R.layout.activity_main);
try{
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setComponent(new ComponentName("com.zeusee.main.hyperlandmark","com.zeusee.main.hyperlandmark.MainActivity"));
    startActivity(intent);
}catch(Exception e){
    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
}
}
}
