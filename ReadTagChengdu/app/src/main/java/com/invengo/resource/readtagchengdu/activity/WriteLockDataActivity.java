package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.invengo.resource.readtagchengdu.R;
import com.example.readlibrary.ReadTagLastActivity;
/**
 * Created by shengjunhao on 16/8/1.
 * <p/>
 * 锁定标签
 */
public class WriteLockDataActivity extends ReadTagLastActivity {


    private Switch sw_access;
    private Switch sw_epc;
    private Switch sw_user;
    private EditText tv_pwd;

    private int[] checkReasult = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_lock_data);
        sw_access = (Switch) findViewById(R.id.sw_access);
        sw_epc = (Switch) findViewById(R.id.sw_epc);
        sw_user = (Switch) findViewById(R.id.sw_user);
        tv_pwd = (EditText) findViewById(R.id.tv_pwd);

        sw_access.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //循环遍历是否被点击过，
                if (isChecked) {
                    checkReasult[0] = LOCK_ACCESS_PASSWORD;
                } else {
                    checkReasult[0] = LOCK_NO;
                }

            }
        });

        sw_epc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //循环遍历是否被点击过，
                if (isChecked) {
                    checkReasult[1] = LOCK_EPC;
                } else {
                    checkReasult[1] = LOCK_NO;
                }

            }
        });

        sw_user.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //循环遍历是否被点击过，
                if (isChecked) {
                    checkReasult[2] = LOCK_USER;
                } else {
                    checkReasult[2] = LOCK_NO;
                }

            }
        });
    }

    /**
     * 读取标签
     *
     * @param view
     */
    public void readTag(View view) {

        String pwd = tv_pwd.getText().toString();


        //传递进入的数组长度不一定是3，长度是不固定的，也对重复没有限制，可以根据自己的实际情况传参。详情参看手册。

        writeLockData(checkReasult, pwd, new WriteTagDataListenser() {
            @Override
            public void onSuccess() {
                Toast.makeText(WriteLockDataActivity.this, "锁定成功", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailed(String msg, int errorCode) {
                Toast.makeText(WriteLockDataActivity.this, "锁定失败" + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 解锁标签
     *
     * @param view
     */
    public void unLockTag(View view) {

        String pwd = tv_pwd.getText().toString();

        writeUnLockData(checkReasult, pwd, new WriteTagDataListenser() {

            @Override
            public void onSuccess() {
                Toast.makeText(WriteLockDataActivity.this, "解锁标签成功", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailed(String msg, int errorCode) {
                Toast.makeText(WriteLockDataActivity.this, "解锁失败" + msg, Toast.LENGTH_SHORT).show();

            }

        });
    }

}
