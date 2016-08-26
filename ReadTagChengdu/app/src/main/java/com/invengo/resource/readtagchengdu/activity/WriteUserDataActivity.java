package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readlibrary.LogUtils;
import com.example.readlibrary.ReadTagLastActivity;
import com.invengo.resource.readtagchengdu.R;

/**
 * Created by shengjunhao on 16/7/18.
 * <p/>
 * 写入数据到epc里面，只需要设置一下
 */
public class WriteUserDataActivity extends ReadTagLastActivity {

    /**
     * 显示数据结果
     */
    private TextView tv_reasult;

    /**
     * 输入的数据
     */
    private EditText et_data;

    /**
     * 开始位置
     */
    private EditText et_begin;

    /**
     * 结束位置
     */
    private EditText et_end;

    private boolean isWrite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_user);
        et_begin = (EditText) findViewById(R.id.et_begin);
        et_data = (EditText) findViewById(R.id.et_data);
        et_end = (EditText) findViewById(R.id.et_end);
        tv_reasult = (TextView) findViewById(R.id.tv_reasult);
    }

    /**
     * 开始执行操作
     *
     * @param view
     */
    public void onAction(View view) {

        isWrite = true;

        String  begin = et_begin.getText().toString();
        int be = begin.length()>0 ? Integer.parseInt(begin): 0;
        String data = et_data.getText().toString();
        String pwd = et_end.getText().toString();

        //调用写入的方法

        LogUtils.i("test","begin:"+begin+"****data"+data+"****pwd"+pwd);
        writeUser(be, data, pwd, new WriteTagDataListenser() {
            @Override
            public void onSuccess() {
                Toast.makeText(WriteUserDataActivity.this,"写入成功",Toast.LENGTH_SHORT).show();

                handler.sendEmptyMessageDelayed(1,500);
            }

            @Override
            public void onFailed(String msg, int errorCode) {

            }
        });

    }

    /**
     * 再次读取user数据
     */
    private void readUserData(){

        String  begin = et_begin.getText().toString();
        int be = begin.length()>0 ? Integer.parseInt(begin): 0;

        readUser(be+1, new ReadTagDataListenser() {
            @Override
            public void onSuccess(String data) {
                tv_reasult.setText("读取结果：" + data);
            }

            @Override
            public void onFailed(String msg, int errorCode) {
                tv_reasult.setText("读取结果：" + msg);
            }
        });

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            readUserData();
        }
    };

}
