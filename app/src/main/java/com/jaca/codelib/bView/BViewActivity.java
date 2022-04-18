package com.jaca.codelib.bView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;
import android.widget.Toast;

import com.jaca.codelib.R;
import com.zjn.apt.annotation.BView;

import static com.jaca.codelib.MainActivityKt.toast;

public class BViewActivity extends AppCompatActivity {

    @BView(R.id.tv_click) TextView mTvClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bview);
        BViewUtils.bind(this);
        mTvClick.setOnClickListener(v -> System.out.println("Test bView test"));
        toast(this);
    }
}