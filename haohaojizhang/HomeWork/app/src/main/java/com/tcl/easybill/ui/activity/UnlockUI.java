package com.tcl.easybill.ui.activity;

import android.content.Intent;
import android.widget.Toast;

import butterknife.BindView;
import com.tcl.easybill.R;
import com.tcl.easybill.ui.widget.LockView;


public class UnlockUI extends BaseActivity {
    @BindView(R.id.unlock_lockview)
    LockView lockView;
    @Override
    protected int getLayout() {
        return R.layout.activity_unlock_ui;
    }

    /**
     * unlock UI
     */
    @Override
    protected void initEventAndData() {
        lockView.setOnLockListener(new LockView.OnLockListener() {
            @Override
            public void onTypeInOnce(String input) {

            }

            @Override
            public void onTypeInTwice(String input, boolean isSuccess) {

            }

            @Override
            public void onUnLock(String input, boolean isSuccess) {
               if(isSuccess){
                   startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                   Toast.makeText(mContext, "解锁成功,正在加载数据", Toast.LENGTH_SHORT).show();
               }else {
                   Toast.makeText(mContext, "错误", Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void onError() {
                Toast.makeText(mContext, "密码长度不够", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
