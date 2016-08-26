package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readlibrary.ReadTagLastActivity;
import com.invengo.resource.readtagchengdu.R;


public class ReadTidActivity extends ReadTagLastActivity {

    private EditText et_count;
    private TextView tv_reasult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_tid);

        et_count = (EditText) findViewById(R.id.et_count);
        tv_reasult = (TextView) findViewById(R.id.tv_reasult);
    }

    /**
     * 读取标签
     *
     * @param view
     */
    public void readTag(View view) {

        if (et_count.getText().toString().trim().length() > 0) {
            setReadLength(Integer.parseInt(et_count.getText().toString()));
        }

        //调用读tid的方法

        readTid(new ReadTagLastActivity.ReadTagDataListenser() {
            @Override
            public void onSuccess(String data) {
                //读取标签成功，请将标签数据
                tv_reasult.setText(data);
            }

            @Override
            public void onFailed(String msg, int errorCode) {
                tv_reasult.setText(msg+"***code***"+errorCode);
            }
        });
    }

}
