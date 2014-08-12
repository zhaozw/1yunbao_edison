package com.ybxiang.driver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.maogousoft.logisticsmobile.driver.R;
import com.maogousoft.logisticsmobile.driver.activity.BaseActivity;
import com.maogousoft.logisticsmobile.driver.activity.info.ChargeActivity;

/**
 * Created by aliang on 2014/8/10.
 */
public class CheckCardActivity extends BaseActivity {

    private Button mTitleBarBack, mTitleBarMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_card_layout);
        initView();
    }

    private void initView() {
        ((TextView) findViewById(R.id.titlebar_id_content)).setText("证件验证");
        // 返回按钮生效
        mTitleBarBack = (Button) findViewById(R.id.titlebar_id_back);
        mTitleBarBack.setOnClickListener(this);
        // 更多按钮隐藏
        mTitleBarMore = (Button) findViewById(R.id.titlebar_id_more);
        mTitleBarMore.setText("验证记录");
        mTitleBarMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.titlebar_id_more:
                Toast.makeText(context, "验证记录", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 帐号充值
     *
     * @param view
     */
    public void onCharge(View view) {
        startActivity(new Intent(context, ChargeActivity.class));
    }
}