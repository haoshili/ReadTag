package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.readlibrary.ReadTagLastActivity;
import com.invengo.resource.readtagchengdu.R;

public class ReadEPCActivity extends ReadTagLastActivity {

    private EditText et_count;
    private TextView tv_reasult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_epc);
        et_count = (EditText) findViewById(R.id.et_count);
        tv_reasult = (TextView) findViewById(R.id.tv_reasult);
    }

    /**
     * 读取标签
     *
     * @param view
     */
    public void readTag(View view) {

        showLog("读取标签EPC####");

        readEpc(et_count.getText().toString().trim(), new ReadTagDataListenser() {

            @Override
            public void onSuccess(String data) {
                tv_reasult.setText(data);
            }

            @Override
            public void onFailed(String msg, int errorCode) {
                tv_reasult.setText(msg+"*****code***"+errorCode);
            }
        });

    }


}
