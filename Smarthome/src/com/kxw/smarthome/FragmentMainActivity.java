/*
 * 净喜智能模块，设置温度、用水、滤芯状态、tds值显示等fragment的装载activity，同时也是
 * 净喜智能模块顶部公共区域的数据处理模块
*/
package com.kxw.smarthome;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.DbManager.DaoConfig;
import org.xutils.x;
import org.xutils.ex.DbException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android_serialport_api.SerialPortUtil;

import com.kxw.smarthome.adapter.MyFragmentAdapter;
import com.kxw.smarthome.entity.BaseData;
import com.kxw.smarthome.entity.FilterLifeInfo;
import com.kxw.smarthome.entity.WeatherInfo;
import com.kxw.smarthome.fragment.FilterInfoFragment;
import com.kxw.smarthome.fragment.SetTemperatureFragment;
import com.kxw.smarthome.fragment.TDSInfoFragment;
import com.kxw.smarthome.fragment.UseWaterFragment;
import com.kxw.smarthome.utils.DBUtils;
import com.kxw.smarthome.utils.MyLogger;
import com.kxw.smarthome.utils.NetUtils;
import com.kxw.smarthome.utils.ToolsUtils;
import com.kxw.smarthome.utils.UseStateToast;
import com.kxw.smarthome.utils.Utils;
import com.kxw.smarthome.view.LazyViewPager;
import com.kxw.smarthome.view.MyViewPager;

@SuppressLint("NewApi")
public class FragmentMainActivity extends FragmentActivity implements OnClickListener, Runnable {

	private TextView current_time,weather_state,weather_temperature,set_temperature_tv,
	use_water_tv,equipment_info_tv,tds_info_tv,use_mode,value_surplus;
	private ImageView set_temperature_iv,use_water_iv,equipment_info_iv,tds_info_iv,wifi_state_iv;;
	private MyViewPager mvp_view;
	private MyFragmentAdapter adapter;
	private UseWaterFragment useWaterFragment;
	private FilterInfoFragment equipmentInfoFragment;
	private SetTemperatureFragment setTemperatureFragment;
	private TDSInfoFragment mTDSInfoFragment;
	private List<Fragment> fragmentList = new ArrayList<Fragment>();
	private LinearLayout back_ll,set_temperature,use_water,equipment_info,TDS_info;
	private Handler handler;
	private static WeatherInfo weatherInfo=null;

	private SerialPortUtil mSerialPortUtil;
	private BaseData mBaseData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_activity);

		initView();
		initData();

		initFragment();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				current_time.setText((String)msg.obj);
			}
		};

		mSerialPortUtil =MyApplication.getSerialPortUtil();
		new Thread(this).start();
	}

	private void initView() {
		// TODO Auto-generated method stub

		current_time = (TextView)findViewById(R.id.current_time_tv);
		weather_state = (TextView)findViewById(R.id.weather_state_tv);
		weather_temperature = (TextView)findViewById(R.id.weather_temperature_tv);
		back_ll = (LinearLayout)findViewById(R.id.title_back_ll);
		back_ll.setOnClickListener(this);
		use_mode= (TextView)findViewById(R.id.use_mode_tv);
		value_surplus= (TextView)findViewById(R.id.value_surplus_tv);
		wifi_state_iv = (ImageView) findViewById(R.id.wifi_state_iv);

		set_temperature=(LinearLayout)findViewById(R.id.set_temperature_ll);
		use_water=(LinearLayout)findViewById(R.id.use_water_ll);
		equipment_info=(LinearLayout)findViewById(R.id.equipment_info_ll);
		TDS_info=(LinearLayout)findViewById(R.id.tds_info_ll);
		mvp_view=(MyViewPager) findViewById(R.id.mvp_view);
		set_temperature.setOnClickListener(this);
		use_water.setOnClickListener(this);
		equipment_info.setOnClickListener(this);
		TDS_info.setOnClickListener(this);

		set_temperature_tv = (TextView)findViewById(R.id.set_temperature_tv);
		use_water_tv = (TextView)findViewById(R.id.use_water_tv);
		equipment_info_tv = (TextView)findViewById(R.id.equipment_info_tv);
		tds_info_tv = (TextView)findViewById(R.id.tds_info_tv);

		set_temperature_iv = (ImageView)findViewById(R.id.set_temperature_iv);
		use_water_iv = (ImageView)findViewById(R.id.use_water_iv);
		equipment_info_iv = (ImageView)findViewById(R.id.equipment_info_iv);
		tds_info_iv = (ImageView)findViewById(R.id.tds_info_iv);
	}	

	private void initData() { 
		// TODO Auto-generated method stub

		DaoConfig daoConfig=DBUtils.getDaoConfig(); 
		DbManager db = x.getDb(daoConfig);
		try {
			weatherInfo=db.findFirst(WeatherInfo.class);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (weatherInfo != null) {
			weather_state.setText(weatherInfo.getState());
			weather_temperature.setText(weatherInfo.getTemperature() + "℃");
		}
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (NetUtils.isConnected(this)) {
			wifi_state_iv.setImageResource(R.drawable.wifi_connect_img);
		} else {
			wifi_state_iv.setImageResource(R.drawable.wifi_disconnect_img);
		}
		
		mBaseData=mSerialPortUtil.returnBaseData();
		showPayInfo(mBaseData);
	}

	public void showPayInfo(BaseData mBaseData) {
		// TODO Auto-generated method stub
		if(mBaseData!=null){
			if(mBaseData.timeSurplus==65535){
				MyLogger.getInstance().e(" by quantity of flow");
				Utils.payment_type = 0;
				use_mode.setText(getString(R.string.title_total_flow_surplus));
				value_surplus.setText(Html.fromHtml(String.format(
						getString(R.string.title_total_flow_surplus_value),
						mBaseData.waterSurplus)));
			}else{
				MyLogger.getInstance().e(" by time");
				Utils.payment_type = 1;
				use_mode.setText(getString(R.string.total_type_month));
				value_surplus.setText(Html.fromHtml(String.format(
						getString(R.string.title_total_day_surplus_value),
						mBaseData.timeSurplus)));
			}
		}
	}

	private void initFragment() {
		// TODO Auto-generated method stub
		useWaterFragment = new UseWaterFragment();
		equipmentInfoFragment = new FilterInfoFragment(); 
		setTemperatureFragment = new SetTemperatureFragment();
		mTDSInfoFragment = new TDSInfoFragment();

		fragmentList.add(setTemperatureFragment);
		fragmentList.add(useWaterFragment);
		fragmentList.add(equipmentInfoFragment);
		fragmentList.add(mTDSInfoFragment);

		adapter = new MyFragmentAdapter(getSupportFragmentManager(), fragmentList);
		mvp_view.setAdapter(adapter);
		mvp_view.setCurrentItem(0);
		mvp_view.setOffscreenPageLimit(0);
		mvp_view.setOnPageChangeListener(new LazyViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.set_temperature_ll:
			if(!Utils.isUsing){
				mvp_view.setCurrentItem(0);	
				changeBackground();
				set_temperature_tv.setTextColor(getResources().getColor(R.color.navigation_bar_color));
				set_temperature_iv.setImageResource(R.drawable.set_temperature_p);
			}else{
				UseStateToast.getManager(this).showToast(getString(R.string.water_is_inuse));
			}
			break;

		case R.id.use_water_ll:
			mvp_view.setCurrentItem(1);	
			changeBackground();
			use_water_tv.setTextColor(getResources().getColor(R.color.navigation_bar_color));
			use_water_iv.setImageResource(R.drawable.use_water_p);
			break;

		case R.id.equipment_info_ll:
			if(!Utils.isUsing){
				mvp_view.setCurrentItem(2);
				changeBackground();
				equipment_info_tv.setTextColor(getResources().getColor(R.color.navigation_bar_color));
				equipment_info_iv.setImageResource(R.drawable.equipment_info_p);
			}else{
				UseStateToast.getManager(this).showToast(getString(R.string.water_is_inuse));
			}
			break;

		case R.id.tds_info_ll:
			if(!Utils.isUsing){
				mvp_view.setCurrentItem(3);
				changeBackground();
				tds_info_tv.setTextColor(getResources().getColor(R.color.navigation_bar_color));
				tds_info_iv.setImageResource(R.drawable.tds_info_p);
			}else{
				UseStateToast.getManager(this).showToast(getString(R.string.water_is_inuse));
			}
			break;

		case R.id.title_back_ll:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while(true){
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd    EEEE   HH:mm:ss");
				String str=sdf.format(new Date());
				handler.sendMessage(handler.obtainMessage(100,str));
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void changeBackground(){
		set_temperature_tv.setTextColor(getResources().getColor(R.color.text_color_white));
		use_water_tv.setTextColor(getResources().getColor(R.color.text_color_white));
		equipment_info_tv.setTextColor(getResources().getColor(R.color.text_color_white));
		tds_info_tv.setTextColor(getResources().getColor(R.color.text_color_white));
		set_temperature_iv.setImageResource(R.drawable.set_temperature_n);
		use_water_iv.setImageResource(R.drawable.use_water_n);
		equipment_info_iv.setImageResource(R.drawable.equipment_info_n);
		tds_info_iv.setImageResource(R.drawable.tds_info_n);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
		if (!ToolsUtils.isFastDoubleClick()) {
			Intent mIntent = new Intent("ON_TOUCH_ACTION");                 
			sendBroadcast(mIntent);  
		}	
		super.dispatchTouchEvent(ev);
        return false;
    }
}