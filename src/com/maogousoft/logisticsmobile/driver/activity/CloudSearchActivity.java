package com.maogousoft.logisticsmobile.driver.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.baidu.mapapi.cloud.*;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.maogousoft.logisticsmobile.driver.Constants;
import com.maogousoft.logisticsmobile.driver.R;

public class CloudSearchActivity extends Activity implements CloudListener {
	private static final String LTAG = "CloudSearchActivity";
	private MapView mMapView;
	private BaiduMap mBaiduMap;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_lbssearch);
		CloudManager.getInstance().init(CloudSearchActivity.this);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		findViewById(R.id.regionSearch).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						LocalSearchInfo info = new LocalSearchInfo();
						info.ak = "fGahmvRg6YiiqNTY72uoWE9W";
						info.geoTableId = 31869;
						info.tags = "";
						info.q = "天安门";
						info.region = "北京市";
						CloudManager.getInstance().localSearch(info);
					}
				});
		findViewById(R.id.nearbySearch).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						NearbySearchInfo info = new NearbySearchInfo();
						info.ak = "fGahmvRg6YiiqNTY72uoWE9W";
						info.geoTableId = Constants.BAIDU_LBS_TABLE_ID;
						info.radius = 30000;
						info.location = "30.563362, 104.042326";
						CloudManager.getInstance().nearbySearch(info);
					}
				});

		findViewById(R.id.boundsSearch).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						BoundSearchInfo info = new BoundSearchInfo();
						info.ak = "fGahmvRg6YiiqNTY72uoWE9W";
						info.geoTableId = 31869;
						info.q = "天安门";
						info.bound = "116.401663,39.913961;116.406529,39.917396";
						CloudManager.getInstance().boundSearch(info);
					}
				});
		findViewById(R.id.detailsSearch).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						DetailSearchInfo info = new DetailSearchInfo();
						info.ak = "fGahmvRg6YiiqNTY72uoWE9W";
						info.geoTableId = 31869;
						info.uid = 18622266;
						CloudManager.getInstance().detailSearch(info);
					}
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		CloudManager.getInstance().destroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	public void onGetDetailSearchResult(DetailSearchResult result, int error) {
		if (result != null) {
			if (result.poiInfo != null) {
				Toast.makeText(CloudSearchActivity.this, result.poiInfo.title,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(CloudSearchActivity.this,
						"status:" + result.status, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void onGetSearchResult(CloudSearchResult result, int error) {
		if (result != null && result.poiList != null
				&& result.poiList.size() > 0) {
			Log.d(LTAG, "onGetSearchResult, result length: " + result.poiList.size());
			mBaiduMap.clear();
			BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
			LatLng ll;
			LatLngBounds.Builder builder = new Builder();
			for (CloudPoiInfo info : result.poiList) {
				ll = new LatLng(info.latitude, info.longitude);
				OverlayOptions oo = new MarkerOptions().icon(bd).position(ll);
				mBaiduMap.addOverlay(oo);
				builder.include(ll);
			}
			LatLngBounds bounds = builder.build();
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
			mBaiduMap.animateMapStatus(u);
		}
	}
}

