package com.invengo.resource.readtagchengdu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.invengo.resource.readtagchengdu.R;
import  com.example.readlibrary.ReadTagLastActivity;

public class MainActivity extends ReadTagLastActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 读取tid数据
     *
     * @param view
     */
    public void readTag(View view) {
        Intent intent = new Intent(this, ReadTidActivity.class);
        startActivity(intent);
    }

    /**
     * 读取epc数据
     *
     * @param v
     */
    public void readEpc(View v) {
        Intent intent = new Intent(this, ReadEPCActivity.class);
        startActivity(intent);
    }

    /**
     * 读取user数据区
     *
     * @param view
     */
    public void readUser(View view) {
        Intent intent = new Intent(this, ReadUserActivity.class);
        startActivity(intent);
    }

    /**
     * 更多功能
     *
     * @param view
     */
    public void mainLess(View view) {
        Intent intent = new Intent(this, SetAccessPwdActivity.class);
        startActivity(intent);
    }

    /**
     * 写入epc数据
     *
     * @param view
     */
    public void writeEpc(View view) {
        Intent intent = new Intent(this, WriteEpcDataActivity.class);
        startActivity(intent);
    }

    /**
     * 写入user数据
     *
     * @param view
     */
    public void writeUser(View view) {
        Intent intent = new Intent(this, WriteUserDataActivity.class);
        startActivity(intent);
    }
}
