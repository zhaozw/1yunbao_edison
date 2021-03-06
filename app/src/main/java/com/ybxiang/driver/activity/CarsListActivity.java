package com.ybxiang.driver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import com.maogousoft.logisticsmobile.driver.Constants;
import com.maogousoft.logisticsmobile.driver.R;
import com.maogousoft.logisticsmobile.driver.activity.BaseListActivity;
import com.maogousoft.logisticsmobile.driver.activity.CarCloudSearchActivity;
import com.maogousoft.logisticsmobile.driver.activity.home.SearchCarSourceActivity;
import com.maogousoft.logisticsmobile.driver.activity.share.ShareActivity;
import com.maogousoft.logisticsmobile.driver.adapter.MyCarInfoListAdapter;
import com.maogousoft.logisticsmobile.driver.adapter.SearchCarInfoListAdapter;
import com.maogousoft.logisticsmobile.driver.api.AjaxCallBack;
import com.maogousoft.logisticsmobile.driver.api.ApiClient;
import com.maogousoft.logisticsmobile.driver.api.ResultCode;
import com.maogousoft.logisticsmobile.driver.model.CarInfo;
import com.maogousoft.logisticsmobile.driver.model.CarInfoList;
import com.maogousoft.logisticsmobile.driver.utils.MyAlertDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CarsListActivity extends BaseListActivity implements
        OnClickListener, OnScrollListener {
    private Button mTitleBarMore;
    private TextView mTitleBarContent;
    // 底部更多
    private View mFootView, mHeaderView;
    private ProgressBar mFootProgress;
    private TextView mFootMsg;
    // 当前模式
    private int state = WAIT;
    // 当前页码
    private int pageIndex = 1;
    // 滑动状态
    private boolean state_idle = false;
    // 已加载全部
    private boolean load_all = false;
    //查询类型
    private String queryAction = Constants.QUERY_MY_FLEET; //默认为我的车队
    private JSONObject queryParams = new JSONObject();
    private boolean isMyCarsFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initData(getIntent());
    }

    private void initViews() {
        mTitleBarContent = ((TextView) findViewById(R.id.titlebar_id_content));
        mTitleBarContent.setText("我的车队");
        // 返回按钮生效

        // 更多按钮隐藏
        mTitleBarMore = (Button) findViewById(R.id.titlebar_id_more);
        mTitleBarMore.setText("添加车辆");
        mTitleBarMore.setOnClickListener(this);

        // 数据加载中进度条
        mFootView = getLayoutInflater().inflate(R.layout.listview_footview, null);
        mFootView.setClickable(false);
        mFootProgress = (ProgressBar) mFootView.findViewById(android.R.id.progress);
        mFootMsg = (TextView) mFootView.findViewById(android.R.id.text1);
        mListView.addFooterView(mFootView);
        mListView.setOnScrollListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    private void initData(Intent intent) {
        //从车源搜索页面过来的
        String params = intent.getStringExtra(Constants.COMMON_KEY);
        String action = intent.getStringExtra(Constants.COMMON_ACTION_KEY);
        isMyCarsFilter = intent.getBooleanExtra(Constants.MY_CARS_SEARCH, false);
        if(!TextUtils.isEmpty(params) && !TextUtils.isEmpty(action)) {
            try {
                queryParams = new JSONObject(params);
                queryAction = action;
                if(isMyCarsFilter) {
                    // 我的车队的adapter
                    mAdapter = new MyCarInfoListAdapter(mContext);
                    if(mListView.getHeaderViewsCount() <= 0) {
                        mHeaderView = getLayoutInflater().inflate(R.layout.listview_header_search_layout, null);
                        mHeaderView.setOnClickListener(this);
                        mListView.addHeaderView(mHeaderView);
                    } else {
                        mListView.removeHeaderView(mHeaderView);
                    }
                } else {
                    // 搜索车源的adapter
                    mAdapter = new SearchCarInfoListAdapter(mContext);
                    mTitleBarContent.setText("搜索车源列表");
                    if(Constants.QUERY_CAR_SOURCE.equals(action)) {
                        mTitleBarMore.setText("地图显示");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // 我的车队的adapter
            mAdapter = new MyCarInfoListAdapter(mContext);
            mHeaderView = getLayoutInflater().inflate(R.layout.listview_header_search_layout, null);
            mHeaderView.setOnClickListener(this);
            mListView.addHeaderView(mHeaderView);
        }
        setListAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // list未加载数据不显示
        // 重新刷新数据
        setListShown(false);
        mAdapter.removeAll();
        pageIndex = 1;
        load_all = false;
        getData(pageIndex);
    }

    // 请求指定页数的数据
    private void getData(final int page) {
        try {
            state = ISREFRESHING;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.ACTION, queryAction);
            jsonObject.put(Constants.TOKEN, application.getToken());
            jsonObject.put(Constants.JSON, queryParams.put("page", page).toString());
            ApiClient.doWithObject(Constants.DRIVER_SERVER_URL, jsonObject,
                    isMyCarsFilter ? CarInfoList.class : CarInfo.class, new AjaxCallBack() {
                        @Override
                        public void receive(int code, Object result) {
                            setListShown(true);
                            switch (code) {
                                case ResultCode.RESULT_OK:
                                    if (result instanceof CarInfoList) {
                                        CarInfoList carInfoList = (CarInfoList) result;
                                        List<CarInfo> mList = carInfoList.getList();
                                        if (mList == null || mList.isEmpty()) {
                                            load_all = true;
                                            mFootProgress.setVisibility(View.GONE);
                                            mFootMsg.setText("已加载全部");
                                        } else {
                                            if (mList.size() < 10) {
                                                load_all = true;
                                                mFootProgress.setVisibility(View.GONE);
                                                mFootMsg.setText("已加载全部");
                                            }
                                            mAdapter.addAll(mList);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                        //如果是我的车队,显示共有多少辆车
                                        if(queryAction.equals(Constants.QUERY_MY_FLEET)) {
                                            ((TextView) mHeaderView.findViewById(R.id.number)).setText(getString(R.string.string_car_number, carInfoList.getTotalRow()));
                                        }
                                    }
                                    if (result instanceof ArrayList) {
                                        List<CarInfo> mList = (List<CarInfo>) result;
                                        if (mList == null || mList.isEmpty()) {
                                            load_all = true;
                                            mFootProgress.setVisibility(View.GONE);
                                            mFootMsg.setText("已加载全部");
                                        } else {
                                            if (mList.size() < 10) {
                                                load_all = true;
                                                mFootProgress.setVisibility(View.GONE);
                                                mFootMsg.setText("已加载全部");
                                            }
                                            mAdapter.addAll(mList);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    break;
                                case ResultCode.RESULT_ERROR:
                                    if (result instanceof String)
                                        showMsg(result.toString());
                                    break;
                                case ResultCode.RESULT_FAILED:
                                    if (result instanceof String) {
                                        showMsg(result.toString());
                                    }
                                    break;
                                default:
                                    break;
                            }
                            if (mAdapter.isEmpty()) {
                                setEmptyText("没有找到数据哦");
                            }
                            state = WAIT;
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.titlebar_id_more:
                if(Constants.QUERY_CAR_SOURCE.equals(queryAction)) {
                    Intent intent = new Intent(mContext, CarCloudSearchActivity.class);
                    //地图显示车辆信息
                    if(mAdapter.getCount() > 0) {
                        intent.putExtra(Constants.COMMON_KEY, (Serializable) mAdapter.getList());
                    }
                    intent.putExtra(Constants.MY_CARS_SEARCH, true);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, AddCarActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_CODE);
                }
                break;
            case R.id.search:
                Intent searchIntent = new Intent(mContext, SearchCarSourceActivity.class);
                searchIntent.putExtra(Constants.COMMON_KEY, (java.io.Serializable) mAdapter.getList());
                searchIntent.putExtra(Constants.MY_CARS_SEARCH, true);
                startActivity(searchIntent);
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            return;
        }
        if (state != WAIT) {
            return;
        }
        this.state_idle = true;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!this.state_idle) {
            return;
        }
        if (firstVisibleItem == 0
                || (firstVisibleItem + visibleItemCount) != totalItemCount) {
            return;
        }
        // 如果当前没有加载数据
        if (state != ISREFRESHING && !load_all) {
            getData(++pageIndex);
            mFootProgress.setVisibility(View.VISIBLE);
            mFootMsg.setText(R.string.tips_isloading);
        }
    }
}
