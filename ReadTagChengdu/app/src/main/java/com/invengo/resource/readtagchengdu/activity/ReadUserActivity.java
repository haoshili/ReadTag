package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readlibrary.ReadTagLastActivity;
import com.invengo.resource.readtagchengdu.R;


public class ReadUserActivity extends ReadTagLastActivity {

    private EditText et_count;
    private TextView tv_reasult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_user);
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

            int count = Integer.parseInt(et_count.getText().toString());
            setReadLength(count);

            readUser(count, new ReadTagLastActivity.ReadTagDataListenser() {
                @Override
                public void onSuccess(String data) {
                    tv_reasult.setText(data);
                }

                @Override
                public void onFailed(String msg, int errorCode) {
                    tv_reasult.setText(msg);
                }
            });

        }else{

            Toast.makeText(this,"请输入需要读取的位数",Toast.LENGTH_SHORT).show();
        }

    }


}
