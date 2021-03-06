package com.kxw.smarthome;

import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android_serialport_api.SerialPortUtil;

import com.kxw.smarthome.entity.City;
import com.kxw.smarthome.utils.ConfigUtils;
import com.kxw.smarthome.utils.CountDownTimer;
import com.kxw.smarthome.utils.LocationAndWeatherUtils;
import com.kxw.smarthome.utils.MyLogger;
import com.kxw.smarthome.utils.ToolsUtils;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application  {

	private static Context context;
	private static MyApplication instance;
	private static SerialPortUtil mSerialPortUtil;
	private CountDownTimer timer;
	private List<City> mAllCities;
	private LocationAndWeatherUtils locationAndWeatherUtils;
	private AdvMainActivity activity;

	@Override
	public void onCreate() {
		super.onCreate();
		CrashReport.initCrashReport(getApplicationContext(), "55f92fbd73", false); 
		context = getApplicationContext();
		instance=this;
		mSerialPortUtil=SerialPortUtil.getInstance();
		locationAndWeatherUtils = new LocationAndWeatherUtils(context);
		//开发平台路径：http://apistore.baidu.com/apiworks/servicedetail/478.html
//		ApiStoreSDK.init(this, "96f19e6bf138f9ced87075d98a5dfca5"); //百度apistore中国和世界天气预报APIkey
		x.Ext.init(this); 
		x.Ext.setDebug(true); 
	}

	/**
	 * 显示图片（默认情况）
	 *
	 * @param imageView 图像控件
	 * @param iconUrl   图片地址
	 */
	public void display(ImageView imageView, String iconUrl) {
		
		if(!ToolsUtils.isEmpty(iconUrl))
		{
			if(iconUrl.contains("data.jx-inteligent.tech:15010/jx"))
			{
				iconUrl = iconUrl.replace("data.jx-inteligent.tech:15010/jx", "www.szjxzn.tech:8080/old_jx");
			}
			else if(iconUrl.contains("http://113.106.93.195:15010/jx")) 
			{
				iconUrl = iconUrl.replace("http://113.106.93.195:15010/jx", "www.szjxzn.tech:8080/old_jx");
			}
		}
		
		ImageOptions imageOptions = new ImageOptions.Builder()
		.setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
		.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
		.setFailureDrawableId(R.drawable.list_load_fail)
		.build();		
		x.image().bind(imageView, iconUrl, imageOptions);
	}

	public  static MyApplication getInstance() {
		return instance;
	}

	public static Context getInstances() {
		return context;
	}
	public static SerialPortUtil getSerialPortUtil() {
		return mSerialPortUtil;
	}
	public void closeSerialPort() {
		mSerialPortUtil.close();
	}
	public void resetSerialPort(){
		mSerialPortUtil = mSerialPortUtil.getNewInstance();
	}
	
	public LocationAndWeatherUtils getLocationAndWeatherUtils() {
		return locationAndWeatherUtils;
	}

	public void setLocationAndWeatherUtils(
			LocationAndWeatherUtils locationAndWeatherUtils) {
		this.locationAndWeatherUtils = locationAndWeatherUtils;
	}

	//长时间不操作跳转至广告界面的倒计时Timer
	public void setTimer(){
		if(timer!=null){
			timer.cancel();
		}
		timer= new CountDownTimer(ConfigUtils.ad_time, ConfigUtils.ad_time) {  

			@Override  
			public void onTick(long millisUntilFinished) {  
			}  

			@Override  
			public void onFinish() {    
				MyLogger.getInstance().e("is time to show ad ");
				Intent touchIntent = new Intent(context, AdvMainActivity.class);
				touchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(touchIntent);
			}  
		}; 
		timer.start();
	}

	public List<City> getmAllCities() {
		return mAllCities;
	}

	public void setmAllCities(List<City> mAllCities) {
		this.mAllCities = mAllCities;
	}

	public AdvMainActivity getActivity() {
		return activity;
	}

	public void setActivity(AdvMainActivity activity) {
		this.activity = activity;
	}
}
