package com.kxw.smarthome.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.DbManager.DaoConfig;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.util.Log;
import android_serialport_api.SerialPortUtil;

import com.baidu.apistore.sdk.ApiCallBack;
import com.baidu.apistore.sdk.ApiStoreSDK;
import com.baidu.apistore.sdk.network.Parameters;
import com.kxw.smarthome.AdvMainActivity;
import com.kxw.smarthome.MyApplication;
import com.kxw.smarthome.entity.AdvInfo;
import com.kxw.smarthome.entity.BaseData;
import com.kxw.smarthome.entity.UserInfo;

public class RequestUtils {
	private static SerialPortUtil mSerialPortUtil;
	private static BaseData mBaseData;
	

	public static void getWeatherInfo(String city) {

		Parameters para = new Parameters();
		para.put("city", city);
		ApiStoreSDK.execute("http://apis.baidu.com/heweather/weather/free", 
				ApiStoreSDK.GET, 
				para, 
				new ApiCallBack() {

			@Override
			public void onSuccess(int status, String responseString) {  
				JsonUtils.getWeather(responseString);
			}

			@Override
			public void onComplete() {
			}

			@Override
			public void onError(int status, String responseString, Exception e) {
			}

		});
	}


	public static void downloadMedia(String url){

		File file=new File(ConfigUtils.file_path);
		if(file.exists()){
			file.delete();
		}
		RequestParams params = new RequestParams(url); // 文件在服务器上的存放路径  
		//  params.setExecutor(executor);   // 可以通过该方法设置下载的线程数  
		// 为请求创建新的线程, 取消时请求线程被立即中断; false: 请求建立过程可能不被立即终止  
		params.setCancelFast(true);  
		params.setAutoResume(true);
		params.setSaveFilePath(ConfigUtils.file_path);  

		x.http().get(params, new CommonCallback<File>(){

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(File response) {
				// TODO Auto-generated method stub
				//cancelable.cancel();
				AdvMainActivity.changeMediaPlayBt();
			}
		});
	}
	
   
	public static void getAdvUrl(){

		RequestParams params= new RequestParams(ConfigUtils.get_adv_url);
		MyLogger.getInstance().e(DBUtils.getAdvId());
		JSONObject jObj = new JSONObject();
		try {
			jObj.accumulate("type", -1);
			//			jObj.accumulate("pro_no", userInfo.getPro_no());
		} catch (Exception e) {
		}
		MyLogger.getInstance().e(jObj.toString());
		params.setBodyContent(DataProcessingUtils.encrypt(jObj.toString()));
		x.http().post(params, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub

			}
			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				MyLogger.getInstance().e("onError Throwable="+arg0+"  boolean="+arg1);
			}
			@Override
			public void onFinished() {
				// TODO Auto-generated method stub

			}
			@Override
			public void onSuccess(String response) {
				// TODO Auto-generated method stub
				MyLogger.getInstance().e(response);

				if(JsonUtils.result(response)==0){					
					List<AdvInfo> list= new ArrayList<>();
					try {
						MyLogger.getInstance().e(DataProcessingUtils.decode(new JSONObject(response).getString("data")));
						list=JsonUtils.jsonToArrayList(DataProcessingUtils.decode(new JSONObject(response).getString("data")),AdvInfo.class);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

//					MyLogger.getInstance().e("  list ="+list.size());
					/*if(list!=null&&list.size()>0){
						for(int len = 0;len <list.size();len++){
							int type=list.get(len).getType();
							if(type==0){
								downloadMedia(list.get(len).adv_imgurl);
							}
						}
					}*/
					if(list!=null&&list.size()>0){
						if(DBUtils.deleteAll(AdvInfo.class)){
							/*boolean save =	DBUtils.replaceAdvUrlInfoList(list);
						MyLogger.getInstance().e("boolean  save ="+save);*/
							if(DBUtils.replaceAdvUrlInfoList(list)){
								AdvMainActivity.changeAdv();
							}
						}
					}
				}
			}
		});
	}
}