package com.invengo.resource.readtagchengdu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.invengo.resource.readtagchengdu.R;

/*

标签的锁定，解锁，杀死，属于使用频率比较低的功能，没有实际环境做结果测试，所以现在不做封装，如果日后有用到该功能，

可以考虑再次进行封装。
 */

public class MainLessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_less);
     }


    /**
     * 设置标签的访问密码
     *
     * @param view
     */
    public void setAccessPwd(View view) {

        Intent intent = new Intent(this, SetAccessPwdActivity.class);
        startActivity(intent);

    }

    /**
     * 锁定标签
     *
     * @param view
     */
    public void lockTag(View view) {
        Intent intent = new Intent(this, WriteLockDataActivity.class);
        startActivity(intent);
    }
}
