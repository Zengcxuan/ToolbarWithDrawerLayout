package com.tcl.easybill.ui.activity;


import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import com.tcl.easybill.R;
import com.tcl.easybill.Utils.LockViewUtil;
import com.tcl.easybill.ui.widget.LockView;

import static com.tcl.easybill.ui.widget.LockView.STATUS_NO_PWD;

public class LockViewUi extends BaseActivity{
    @BindView(R.id.lockview)
    LockView lockView;
    @BindView(R.id.lockview_text)
    TextView lockViewText;
    @BindView(R.id.switch_lockview)
    Switch lockViewSwh;
    private Boolean isVerify = false;
    @Override
    protected int getLayout() {
        return R.layout.activity_lockview;
    }

    @Override
    protected void initEventAndData() {
        if(LockViewUtil.getIslock(mContext)){
            lockViewSwh.setChecked(true);
        }
        if(lockView.getCurrentStatus() == STATUS_NO_PWD){
            lockViewText.setText("当前无手势密码,请绘制你的手势密码");
        }else{
            lockViewText.setText("你已经设置了手势密码,请输入你的手势密码");
        }
        swhHandle();
        setLockView();
    }

    @OnClick ({R.id.switch_lockview, R.id.modify_lockview, R.id.back_persional})
    protected void onClick(View v){
        switch (v.getId()) {
            //login out this activity
            case R.id.back_persional:
                finish();
                break;
            //switch button
//            case R.id.switch_lockview:
//                swhHandle();
//                break;
            /*after user revise gesture password,click this button will clear the gesture password */
            case R.id.modify_lockview:
                modifyHandle();
                break;
        }
    }
    /**
     * React according to current state
     */
    private void setLockView(){
        lockView.setOnLockListener(new LockView.OnLockListener() {
            @Override
            public void onTypeInOnce(String input) {
                lockViewText.setText("再次输入密码");
            }

            @Override
            public void onTypeInTwice(String input, boolean isSuccess) {
                lockViewText.setText("成功保存密码");
            }

            @Override
            public void onUnLock(String input, boolean isSuccess) {
                if(isSuccess){
                    lockViewText.setText("验证成功");
                    isVerify = true;
                }else{
                    lockViewText.setText("密码错误");
                }
            }

            @Override
            public void onError() {
                lockViewText.setText("密码长度不够");
            }
        });
    }

    /**
     * Switch
     * Click on Open to get the current status of the gesture password,
     * no password click invalid; password to see if it has been verified,
     * no validation invalid, then turn on the gesture password click Close direct stop gesture password
     * (not clear record)
     */
    private void swhHandle(){
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(isVerify){
                        if (lockView.getCurrentStatus() == STATUS_NO_PWD) {
                        Toast.makeText(mContext, "当前没有设置手势密码", Toast.LENGTH_SHORT).show();
                        lockViewSwh.setChecked(false);
                        }else {
                        lockViewSwh.setChecked(true);
                        LockViewUtil.setIslock(mContext, true);
                        Toast.makeText(mContext, "手势密码已启用", Toast.LENGTH_SHORT).show();
                       }
                    }else {
                        lockViewSwh.setChecked(false);
                        Toast.makeText(mContext, "请先验证密码", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    lockViewSwh.setChecked(false);
                    LockViewUtil.setIslock(mContext, false);
                    Toast.makeText(mContext, "手势密码已取消", Toast.LENGTH_SHORT).show();
                }
            }
        };
        lockViewSwh.setOnCheckedChangeListener(listener);
    }

    /**
     *Used to modify the password,
     * has been verified to click to clear the current password,
     * did not verify clicks invalid.
     */
    private void modifyHandle(){
        if(lockView.getCurrentStatus() == STATUS_NO_PWD){
            Toast.makeText(mContext, "你没有设置密码", Toast.LENGTH_SHORT).show();
        }else {
            if(isVerify) {
                LockViewUtil.clearPwd(mContext);
                lockView.initStatus();
                lockViewText.setText("当前无手势密码,请绘制你的手势密码");
                setLockView();
            }else {
                Toast.makeText(mContext, "请先验证密码", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
