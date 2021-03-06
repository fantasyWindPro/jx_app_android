/*
 * 自定义的提示框，包含升级提示、到期与即将到期的提示
*/
package com.kxw.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android_serialport_api.SerialPortUtil;
import android_serialport_api.SerialPortUtil.OnDataReceiveListener;

import com.kxw.smarthome.entity.FilterLifeInfo;
import com.kxw.smarthome.entity.UserInfo;
import com.kxw.smarthome.entity.VerificationData;
import com.kxw.smarthome.service.UpdateService;
import com.kxw.smarthome.utils.ConfigUtils;
import com.kxw.smarthome.utils.DBUtils;
import com.kxw.smarthome.utils.MyLogger;
import com.kxw.smarthome.utils.SharedPreferencesUtil;


public class HintDialogActivity extends Activity implements OnClickListener, OnDataReceiveListener{

	private LinearLayout root_view_ll;
	private Button cancel,confirm;
	private TextView hint_tv;
	private int type=-1,must = 0;
	private String url=null,apkName=null;

	byte[] mBuffer;
	private static int temperature,this_value,surplus_value=0,sum_value=0,tds=0;
	private static int sum=0,highbit=0,lowbit=0;
	private SerialPortUtil mSerialPortUtil;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_dialog_activity);
		this.setFinishOnTouchOutside(false); 

		initView();
		initData();
		mSerialPortUtil=SerialPortUtil.getInstance();
		mSerialPortUtil.setOnDataReceiveListener(this);
	}

	private void initView() {
		// TODO Auto-generated method stub
		//		root_view_ll=(LinearLayout)findViewById(R.id.root_view_ll);
		//		root_view_ll.getBackground().setAlpha(200);
		cancel=(Button)findViewById(R.id.cancel_bt);
		confirm=(Button)findViewById(R.id.confirm_bt);
		hint_tv=(TextView)findViewById(R.id.hint_tv);
		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}


	private void initData() {
		// TODO Auto-generated method stub
		Intent intent=getIntent();
		MyLogger.getInstance().e("intent = "+intent);

		if (intent != null) {

			type = intent.getIntExtra("type", -1);
			must  = intent.getIntExtra("must", -1);
			if(type==0){  //版本升级
				cancel.setVisibility(View.VISIBLE);
				hint_tv.setText(getString(R.string.upgrade_msg));
				url = intent.getStringExtra("downloadURL");
				apkName = intent.getStringExtra("apkName");
				MyLogger.getInstance().e("url = "+url+"apkname = "+apkName);
			}
			else if(type==1){//即将到期提示
				cancel.setVisibility(View.GONE);
				hint_tv.setText(getString(R.string.coming_due_time));
			}
			else if(type==2){//已到期提示
				cancel.setVisibility(View.GONE);
				hint_tv.setText(getString(R.string.has_due_time));
			}
			else if(type==3){//滤芯即将到期提示
				cancel.setVisibility(View.GONE);
				hint_tv.setText(getString(R.string.coming_due_lvxin));
			}
			else if(type==4){//滤芯已到期提示
				cancel.setVisibility(View.GONE);
				hint_tv.setText(getString(R.string.has_due_lvxin));
			}
			else if(type==5){//恢复出厂设置
				cancel.setVisibility(View.VISIBLE);
				hint_tv.setText(getString(R.string.reset_params));
			}
			else if(type==6){//解除绑定
				cancel.setVisibility(View.VISIBLE);
				hint_tv.setText(getString(R.string.do_unbind));
			}
			else if(type==7){//续费
				cancel.setVisibility(View.VISIBLE);
				hint_tv.setText(getString(R.string.do_renew));
			}
			else if(type==8)//单片机还有套餐在里面，换了新的平板，点击绑定
			{
				cancel.setVisibility(View.VISIBLE);
				hint_tv.setText(getString(R.string.do_binddevice));
			}
			else if(type==-1){
				finish();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.cancel_bt:
			if(must != 1)
				finish();
			break;

		case R.id.confirm_bt:
			if(!TextUtils.isEmpty(url)&&!TextUtils.isEmpty(apkName) && type == 0){
				try {
					download(url,apkName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(type == 5)//恢复出厂值
			{
				Intent reset_intent = new Intent(ConfigUtils.reset_device_alarm);
				sendBroadcast(reset_intent);
				DBUtils.deleteAll(UserInfo.class);
				DBUtils.deleteAll(FilterLifeInfo.class);
				//清除下机器码
				SharedPreferencesUtil.saveStringData(
						HintDialogActivity.this, "pro_no", "");
				
				//清除下订单是否能解绑的标记
				SharedPreferencesUtil.saveIntData(
						HintDialogActivity.this, "tag", -1);
				
				//清除用来验证同步回调的久订单号码
				SharedPreferencesUtil.saveStringData(
						HintDialogActivity.this, "oldOrderno", "");
				
				//清楚别名
				SharedPreferencesUtil.saveStringData(
						HintDialogActivity.this, "alias", "");
				
				//清除机器机型
				SharedPreferencesUtil.saveIntData(
						HintDialogActivity.this, "model", -1);
				
				//清除验证的数据
				VerificationData verificationData = new VerificationData(HintDialogActivity.this);
				verificationData.clearVerificationData();
				
				setResult(105);
			}
			else if(type == 6)//解除绑定
			{
				setResult(106);
			}
			else if(type == 7)//续费
			{
				setResult(107);
			}
			else if(type == 8)
			{
				Intent reset_intent = new Intent(ConfigUtils.reset_device_alarm);
				sendBroadcast(reset_intent);
				
				//清除下订单是否能解绑的标记
				SharedPreferencesUtil.saveIntData(
						HintDialogActivity.this, "tag", -1);
				
				//清除用来验证同步回调的久订单号码
				SharedPreferencesUtil.saveStringData(
						HintDialogActivity.this, "oldOrderno", "");
				
				setResult(101);
			}
			finish();
			break;

		default:
			break;
		}

	}
	private void download(String url,String apkName) throws Exception {
		Intent service = new Intent(this, UpdateService.class);
		MyLogger.getInstance().e("url = "+url+"apkname = "+apkName);
		service.putExtra("DownloadURL", url);
		service.putExtra("apkName", apkName);
		startService(service);
	}


	@Override
	public void onDataReceive(byte[] buffer, int size) {
		// TODO Auto-generated method stub

	}


	
}