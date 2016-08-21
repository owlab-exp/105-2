package com.example.android.calendartest6;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        HourLineLayout tlv = (HourLineLayout)findViewById(R.id.timeLineView);

        FrameLayout.LayoutParams testParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button btn = new Button(this);
        btn.setText("Test Child");
        btn.setLayoutParams(testParams);
        //tlv.addView(btn, testParams);
        tlv.addView(btn, 1000, 1000);

    }
}
