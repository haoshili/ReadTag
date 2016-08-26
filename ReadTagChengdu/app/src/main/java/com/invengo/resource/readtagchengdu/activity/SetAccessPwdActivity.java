package com.invengo.resource.readtagchengdu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.readlibrary.ReadTagLastActivity;
import com.invengo.resource.readtagchengdu.R;



/**
 * Created by shengjunhao on 16/7/28.
 *
 * 设置标签的访问密码，在这样的情况下，就可以使用带密码读取标签的功能了
 */
public class SetAccessPwdActivity extends ReadTagLastActivity {


    private EditText et_access_pwd;
    private EditText et_lock_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd);

        et_access_pwd = (EditText)findViewById(R.id.et_access_pwd);
        et_lock_pwd = (EditText)findViewById(R.id.et_lock_pwd);
    }

    /**
     * 处理相关的东西
     * @param view
     */
    public void readTag(View view){

        String access = et_access_pwd.getText().toString();
        String locPwd = et_lock_pwd.getText().toString();

        if(access.length() != 8){
            Toast.makeText(SetAccessPwdActivity.this,"必须输入8位的访问密码",Toast.LENGTH_SHORT).show();
            return;
        }

        //开始执行
        writeSetAccessPwd(access, locPwd, new WriteTagDataListenser() {
            @Override
            public void onSuccess() {
                Toast.makeText(SetAccessPwdActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String msg, int errorCode) {
                Toast.makeText(SetAccessPwdActivity.this,"设置失败"+msg,Toast.LENGTH_SHORT).show();
            }
        });


    }
}
