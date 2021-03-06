package com.ybxiang.driver.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import com.maogousoft.logisticsmobile.driver.Constants;
import com.maogousoft.logisticsmobile.driver.R;
import com.maogousoft.logisticsmobile.driver.activity.BaseListActivity;
import com.maogousoft.logisticsmobile.driver.adapter.FocusLineInfoListAdapter;
import com.maogousoft.logisticsmobile.driver.api.AjaxCallBack;
import com.maogousoft.logisticsmobile.driver.api.ApiClient;
import com.maogousoft.logisticsmobile.driver.api.ResultCode;
import com.ybxiang.driver.model.FocusLineInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 已关注路线的Adapter
 *
 * @author ybxiang
 */
public class FocusLineInfoActivity extends BaseListActivity implements
        OnClickListener, OnScrollListener {
    private Button mTitleBarMore;
    private int searchType;
    // 底部更多
    private View mFootView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initData();
    }

    private void initViews() {

        ((TextView) findViewById(R.id.titlebar_id_content)).setText("已关注线路");
        // 返回按钮生效

        // 更多按钮隐藏
        mTitleBarMore = (Button) findViewById(R.id.titlebar_id_more);
        mTitleBarMore.setVisibility(View.GONE);

        // 数据加载中进度条
        mFootView = getLayoutInflater().inflate(R.layout.listview_footview,
                null);
        mFootView.setClickable(false);
        mFootProgress = (ProgressBar) mFootView
                .findViewById(android.R.id.progress);
        mFootMsg = (TextView) mFootView.findViewById(android.R.id.text1);
        mListView.addFooterView(mFootView);

        // 初始化FocusLineInfoListAdapter的adapter
        mAdapter = new FocusLineInfoListAdapter(mContext);
        setListAdapter(mAdapter);
        // list未加载数据不显示
        setListShown(false);
    }

    private void initData() {
        searchType = getIntent().getIntExtra(Constants.COMMON_KEY, Constants.SOURCE_SEARCH_TYPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter.isEmpty()) {
            pageIndex = 1;
            getData(pageIndex);
        }
        mAdapter.notifyDataSetChanged();
    }

    // 请求指定页数的数据
    private void getData(int page) {
        try {
            state = ISREFRESHING;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.ACTION, Constants.QUERY_ALL_FOCUS_LINE);
            jsonObject.put(Constants.TOKEN, application.getToken());
            jsonObject.put(Constants.JSON, new JSONObject().put("page", page)
                    .put("type", application.getUserType())
                    .toString());

            ApiClient.doWithObject(Constants.DRIVER_SERVER_URL, jsonObject,
                    FocusLineInfo.class, new AjaxCallBack() {

                        @Override
                        public void receive(int code, Object result) {
                            setListShown(true);
                            switch (code) {
                                case ResultCode.RESULT_OK:
                                    if (result instanceof List) {
                                        List<FocusLineInfo> mList = (List<FocusLineInfo>) result;
                                        if (mList == null || mList.isEmpty()) {
                                            load_all = true;
                                            mFootProgress.setVisibility(View.GONE);
                                            mFootMsg.setText("已加载全部");
                                        } else {
                                            if (mList.size() < 20) {
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
                                    if (result instanceof String)
                                        showMsg(result.toString());
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
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(position == l.getCount() - 1) {
            return;
        }
        super.onListItemClick(l, v, position, id);
        FocusLineInfo focusLineInfo = (FocusLineInfo) v.getTag(R.id.common_city_selected);
        if(searchType == Constants.SOURCE_SEARCH_TYPE) {
            //正常搜索货源
            fastSearchSource(focusLineInfo);
        } else if(searchType == Constants.SOURCE_SEARCH_TYPE_SPECIAL) {
            //搜索零担货源
            fastSearchSource(focusLineInfo);
        } else {
            fastSearchCar(focusLineInfo);
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
