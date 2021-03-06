package com.maogousoft.logisticsmobile.driver.activity.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.maogousoft.logisticsmobile.driver.Constants;
import com.maogousoft.logisticsmobile.driver.R;
import com.maogousoft.logisticsmobile.driver.activity.BaseActivity;
import com.maogousoft.logisticsmobile.driver.activity.MainActivity;
import com.maogousoft.logisticsmobile.driver.activity.info.ChargeActivity;
import com.maogousoft.logisticsmobile.driver.activity.share.ShareActivity;
import com.maogousoft.logisticsmobile.driver.activity.vender.VenderShopListActivity;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 其它
 * 
 * @author lenovo
 */
public class OthersActivity extends BaseActivity implements
		OnItemClickListener, OnClickListener {

	private GridView mGridView;
	private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_others);
		((TextView) findViewById(R.id.titlebar_id_content)).setText("工具");
        findViewById(R.id.titlebar_id_more).setOnClickListener(this);
		initViews();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
        switch (v.getId()) {
            case R.id.titlebar_id_more:
                startActivity(new Intent(mContext, ShareActivity.class));
                break;
        }
	}

	// 初始化视图
	private void initViews() {
		mGridView = (GridView) findViewById(R.id.other_id_gridview);

		List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
		String[] arrays = getResources().getStringArray(R.array.others);
		int[] icons = {
				R.drawable.ic_other_hytq,
				R.drawable.selector_others_help,
				R.drawable.selector_others_charge,
				R.drawable.selector_others_stay,
				R.drawable.selector_others_oil,
				R.drawable.selector_others_bank,
				R.drawable.selector_others_update,
				R.drawable.selector_others_advice,
				R.drawable.selector_others_contact};
		final int lenght = arrays.length;
		for (int i = 0; i < lenght; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("icon", icons[i]);
			map.put("title", arrays[i]);
			mList.add(map);
		}
		mAdapter = new SimpleAdapter(mContext, mList, R.layout.griditem_others,
				new String[] { "icon", "title" }, new int[] {
						R.id.other_id_item_icon, R.id.other_id_item_title });
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:
			startActivity(new Intent(mContext, VenderShopListActivity.class));
			break;
		case 1:
			startActivity(new Intent(mContext, HelpActivity.class));
			break;
		case 2:
			startActivity(new Intent(mContext, ChargeActivity.class));
			break;
		case 3:
			startActivity(new Intent(mContext, MapActivity.class).putExtra(
                    Constants.MAP_TYPE, Constants.MAP_TYPE_HOTEL));
			break;
		case 4:
			startActivity(new Intent(mContext, MapActivity.class).putExtra(
                    Constants.MAP_TYPE, Constants.MAP_TYPE_GAS));
			break;
		case 5:
			startActivity(new Intent(mContext, MapActivity.class).putExtra(
                    Constants.MAP_TYPE, Constants.MAP_TYPE_BANK));
			break;
		case 6:
			update();
			break;
		case 7:
			startActivity(new Intent(mContext, FeedBackActivity.class));
			break;
		case 8:
			startActivity(new Intent(mContext, AboutActivity.class));
			break;
		case 9:
			startActivity(new Intent(mContext, DistanceCalcActivity.class));
			break;
		default:
			break;
		}
	}

	UmengUpdateListener updateListener = new UmengUpdateListener() {
		@Override
		public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
			dismissProgress();
			switch (updateStatus) {
			case 0:
				UmengUpdateAgent.showUpdateDialog(getParent(), updateInfo);
				break;
			case 1:
				showMsg("当前已是最新版本");
				break;
			case 2:
				showMsg("没有wifi连接， 只在wifi下更新");
				break;
			case 3:
				showMsg("超时");
				break;
			case 4:
				break;
			}

		}
	};

	// 检查版本更新
	private void update() {
		showProgress("正在检查更新");
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(updateListener);

		UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

			@Override
			public void OnDownloadEnd(int result) {
			}

		});

		UmengUpdateAgent.update(getParent());
	}

    @Override
    public void onBackPressed() {
        application.finishAllActivity();
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
    }
}
