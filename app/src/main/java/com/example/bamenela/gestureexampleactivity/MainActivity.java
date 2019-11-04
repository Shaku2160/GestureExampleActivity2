package com.example.bamenela.gestureexampleactivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TouchExample view = new TouchExample(this);
        setContentView(view);

    }
}
