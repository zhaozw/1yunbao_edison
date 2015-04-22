package com.maogousoft.logisticsmobile.driver.activity;

import android.view.View;
import android.widget.TextView;
import com.baidu.mapapi.SDKInitializer;
import com.maogousoft.logisticsmobile.driver.activity.home.*;
import com.maogousoft.logisticsmobile.driver.activity.other.OthersActivity;
import com.maogousoft.logisticsmobile.driver.utils.LogUtil;
import com.ybxiang.driver.activity.PublishGoodsSourceActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.maogousoft.logisticsmobile.driver.Constants;
import com.maogousoft.logisticsmobile.driver.MGApplication;
import com.maogousoft.logisticsmobile.driver.R;
import com.maogousoft.logisticsmobile.driver.activity.info.LoginActivity;
import com.maogousoft.logisticsmobile.driver.api.AjaxCallBack;
import com.maogousoft.logisticsmobile.driver.api.ApiClient;
import com.maogousoft.logisticsmobile.driver.api.ResultCode;
import com.maogousoft.logisticsmobile.driver.model.AbcInfo;
import com.maogousoft.logisticsmobile.driver.utils.LocHelper;
import com.umeng.update.UmengUpdateAgent;

/**
 * 登录之后显示的主页
 *
 * @author ybxiang
 */
public class MainActivity extends TabActivity {

    private static final String TAG = "MainActivity";
    private Context mContext; // PR105
    private int userType; // PR112 账户身份
    /**
     * 切换到主页tab
     */
    public static final String ACTION_SWITCH_MAINACTIVITY = "com.maogousoft.logisticsmobile.driver.activity.MainActivity";

    private TabHost mTabHost;
    private RadioGroup mRadioGroup;
    private MGApplication application;
    private BroadcastReceiver switchMainActivityReceiver;
    private BaiduSDKReceiver baiduSDKReceiver;

    @Override
    public CharSequence onCreateDescription() {
        return super.onCreateDescription();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;// PR105
        initViews();

        LocHelper.getInstance(this).init();
        switchMainActivityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                mRadioGroup.check(R.id.main_id_radio_home);
            }
        };
        registerReceiver(switchMainActivityReceiver, new IntentFilter(
                ACTION_SWITCH_MAINACTIVITY));
        getABCInfo();
        //注册百度sdk广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        baiduSDKReceiver = new BaiduSDKReceiver();
        registerReceiver(baiduSDKReceiver, iFilter);
    }

    @Override
    protected void onDestroy() {
        if (switchMainActivityReceiver != null) {
            unregisterReceiver(switchMainActivityReceiver);
            // 取消监听 SDK 广播
            unregisterReceiver(baiduSDKReceiver);
        }
        super.onDestroy();
    }

    private void initViews() {
        application = (MGApplication) getApplication();
        userType = application.getUserType();
        mTabHost = getTabHost();
        switch (userType) {
            // 司机主页
            case Constants.USER_DRIVER:
                mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("首页")
                        .setContent(new Intent(this, HomeDriverActivity.class)));
                break;
            // 货主主页
            case Constants.USER_SHIPPER:
                mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("首页")
                        .setContent(new Intent(this, HomeShipperActivity.class)));
                break;
            default:
                break;
        }

//        mTabHost.addTab(mTabHost.newTabSpec("share").setIndicator("分享") // PR113 改分享为礼品
//                .setContent(new Intent(this, ShareActivity.class)));
        if(application.getUserType() == Constants.USER_DRIVER) {
            mTabHost.addTab(mTabHost.newTabSpec("search").setIndicator("搜索")
                    .setContent(new Intent(this, SearchSourceActivity.class)));
            findViewById(R.id.main_id_radio_publish).setVisibility(View.GONE);
        } else {
            mTabHost.addTab(mTabHost.newTabSpec("publish").setIndicator("发布")
                    .setContent(new Intent(this, PublishGoodsSourceActivity.class)));
            findViewById(R.id.main_id_radio_search).setVisibility(View.GONE);
        }
        mTabHost.addTab(mTabHost.newTabSpec("tool").setIndicator("工具")
                .setContent(new Intent(this, OthersActivity.class)));
        if(application.getUserType() == Constants.USER_DRIVER) {
            mTabHost.addTab(mTabHost.newTabSpec("others").setIndicator("我X")
                    .setContent(new Intent(this, MyabcActivityDriver.class)));
        } else {
            mTabHost.addTab(mTabHost.newTabSpec("others").setIndicator("我X")
                    .setContent(new Intent(this, MyabcActivityShipper.class)));
        }
        // OthersActivity -->MyabcActivity;
        // PR 102 OthersActivity-->

        mRadioGroup = (RadioGroup) findViewById(R.id.main_id_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_id_radio_home:
                        mTabHost.setCurrentTab(0);
                        break;
                    case R.id.main_id_radio_share:
                        //mTabHost.setCurrentTab(1);
                        share();
                        break;
                    case R.id.main_id_radio_search:
                        mTabHost.setCurrentTab(1);
                        break;
                    case R.id.main_id_radio_publish:
                        mTabHost.setCurrentTab(1);
                        break;
                    case R.id.main_id_radio_tool:
                        mTabHost.setCurrentTab(2);
                        break;
                    case R.id.main_id_radio_other:
                        mTabHost.setCurrentTab(3);
                        break;
                    default:
                        break;
                }
            }
        });
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setOnDownloadListener(null);
        UmengUpdateAgent.update(this);
    }

    // 获取我的abc信息
    private void getABCInfo() {

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.ACTION, Constants.DRIVER_PROFILE);
            jsonObject.put(Constants.TOKEN, application.getToken());
            jsonObject.put(Constants.JSON, "");
            ApiClient.doWithObject(Constants.DRIVER_SERVER_URL, jsonObject,
                    AbcInfo.class, new AjaxCallBack() {

                @Override
                public void receive(int code, Object result) {
                    switch (code) {
                        case ResultCode.RESULT_OK:
                            if (result != null) {
                                AbcInfo mAbcInfo = (AbcInfo) result;
                                application.setAbcInfo(mAbcInfo);
                                String idCard = mAbcInfo.getId_card();// 身份证号码
                                String name = mAbcInfo.getName();// 司机姓名

                                if (!TextUtils.isEmpty(idCard)) {
                                    application.writeIsThroughRezheng(true);
                                } else {
                                    application.writeIsThroughRezheng(false);
                                }

                                if (!TextUtils.isEmpty(name)) {
                                    application.writeIsRegOptional(true);
                                } else {
                                    application.writeIsRegOptional(false);
                                }

                            }
                            break;
                        case ResultCode.RESULT_ERROR:
                            // if (result != null)
                            // showMsg(result.toString());
                            break;
                        case ResultCode.RESULT_FAILED:
                            // if (result != null)
                            // showMsg(result.toString());
                            break;

                        default:
                            break;
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 1, "切换账户【所有】"); //PR112
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // PR112 begin
            case 1:
                // 跳转到登陆界面,此时需要让自动登陆失效
                application.writeAutoLogin(false);
                Intent loginIntent = new Intent();
                loginIntent.setClass(mContext, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    public void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.string_share_tips));
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class BaiduSDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            LogUtil.d(TAG, "action: " + s);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                LogUtil.d(TAG, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s
                    .equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                LogUtil.d(TAG, "网络出错");
            }
        }
    }
}