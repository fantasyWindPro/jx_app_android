package com.kxw.smarthome;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android_serialport_api.SerialPortUtil;

import com.kxw.smarthome.entity.BaseData;
import com.kxw.smarthome.entity.FilterLifeInfo;
import com.kxw.smarthome.entity.OptionDescriptionInfo;
import com.kxw.smarthome.entity.OptionDescriptions;
import com.kxw.smarthome.entity.VerificationData;
import com.kxw.smarthome.utils.ConfigUtils;
import com.kxw.smarthome.utils.DBUtils;
import com.kxw.smarthome.utils.MyToast;
import com.kxw.smarthome.utils.SharedPreferencesUtil;
import com.kxw.smarthome.utils.ToastUtil;
import com.kxw.smarthome.utils.Utils;

public class RegulatingMachineActivity extends BaseActivity implements OnClickListener {
	
	private TextView txt_regulating_pp_add, txt_regulating_pp_reduce, txt_cto_add, txt_cto_reduce, txt_ro_add, txt_ro_reduce, txt_t33_add, txt_t33_reduce;
	private EditText edit_regulating_pp, edit_cto_adjustment, edit_ro_adjustment, edit_t33_adjustment, pwd_et;
	private Button regulating_pp_bt, cto_adjustment_bt, ro_adjustment_bt, t33_adjustment_bt, login_bt, reset_bt;
	private LinearLayout layout_regulating, layout_login;
	
	//修正滤芯的句柄
	private Handler mRegulatingTemperatureHandler;
	private HandlerThread mRegulatingTemperatureHandlerThread;
	
	private SerialPortUtil mSerialPortUtil;
	private BaseData mBaseData;
	private int pp, cto, ro, t33;
	private boolean isSuccess;
	private OptionDescriptions optionDescriptions = new OptionDescriptions();
	private List<OptionDescriptionInfo> options = new ArrayList<OptionDescriptionInfo>();
	private OptionDescriptionInfo optionDescriptionInfo = new OptionDescriptionInfo();
	private FilterLifeInfo mDataFilterLifeInfo;
	private VerificationData verificationData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setBaseContentView(R.layout.activity_regulating_machine);
		
		txt_regulating_pp_add = (TextView) findViewById(R.id.txt_regulating_pp_add);
		txt_regulating_pp_reduce = (TextView) findViewById(R.id.txt_regulating_pp_reduce);
		txt_cto_add = (TextView) findViewById(R.id.txt_cto_add);
		txt_cto_reduce = (TextView) findViewById(R.id.txt_cto_reduce);
		txt_ro_add = (TextView) findViewById(R.id.txt_ro_add);
		txt_ro_reduce = (TextView) findViewById(R.id.txt_ro_reduce);
		txt_t33_add = (TextView) findViewById(R.id.txt_t33_add);
		txt_t33_reduce = (TextView) findViewById(R.id.txt_t33_reduce);
		
		edit_regulating_pp = (EditText) findViewById(R.id.edit_regulating_pp);
		edit_cto_adjustment = (EditText) findViewById(R.id.edit_cto_adjustment);
		edit_ro_adjustment = (EditText) findViewById(R.id.edit_ro_adjustment);
		edit_t33_adjustment = (EditText) findViewById(R.id.edit_t33_adjustment);
		pwd_et = (EditText) findViewById(R.id.pwd_et);
		
		regulating_pp_bt = (Button) findViewById(R.id.regulating_pp_bt);
		cto_adjustment_bt = (Button) findViewById(R.id.cto_adjustment_bt);
		ro_adjustment_bt = (Button) findViewById(R.id.ro_adjustment_bt);
		t33_adjustment_bt = (Button) findViewById(R.id.t33_adjustment_bt);
		regulating_pp_bt = (Button) findViewById(R.id.regulating_pp_bt);
		cto_adjustment_bt = (Button) findViewById(R.id.cto_adjustment_bt);
		ro_adjustment_bt = (Button) findViewById(R.id.ro_adjustment_bt);
		t33_adjustment_bt = (Button) findViewById(R.id.t33_adjustment_bt);
		reset_bt = (Button) findViewById(R.id.reset_bt);
		login_bt = (Button) findViewById(R.id.login_bt);
		
		layout_regulating = (LinearLayout) findViewById(R.id.layout_regulating);
		layout_login = (LinearLayout) findViewById(R.id.layout_login);
		
		txt_regulating_pp_add.setOnClickListener(this);
		txt_regulating_pp_reduce.setOnClickListener(this);
		txt_cto_add.setOnClickListener(this);
		txt_cto_reduce.setOnClickListener(this);
		txt_ro_add.setOnClickListener(this);
		txt_ro_reduce.setOnClickListener(this);
		txt_t33_add.setOnClickListener(this);
		txt_t33_reduce.setOnClickListener(this);
		regulating_pp_bt.setOnClickListener(this);
		cto_adjustment_bt.setOnClickListener(this);
		ro_adjustment_bt.setOnClickListener(this);
		t33_adjustment_bt.setOnClickListener(this);
		reset_bt.setOnClickListener(this);
		login_bt.setOnClickListener(this);
		
	    mRegulatingTemperatureHandlerThread = new HandlerThread("RegulatingTemperatureActivity_RegulatingTemperature", 5);  
	    mRegulatingTemperatureHandlerThread.start();  
	    mRegulatingTemperatureHandler = new Handler(mRegulatingTemperatureHandlerThread.getLooper()); 
	    
		mSerialPortUtil = MyApplication.getSerialPortUtil();
		mBaseData=mSerialPortUtil.returnBaseData();
		mDataFilterLifeInfo = DBUtils.getFirstData(FilterLifeInfo.class);
		verificationData = new VerificationData(RegulatingMachineActivity.this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId())
		{
			case R.id.txt_regulating_pp_add:
				pp ++;
				if(pp > mDataFilterLifeInfo.getPp())
				{
					pp = mDataFilterLifeInfo.getPp();
				}
				edit_regulating_pp.setText(String.valueOf(pp));
				break;
			case R.id.txt_regulating_pp_reduce:
				pp --;
				if(pp < 0)
				{
					pp = 0;
				}
				edit_regulating_pp.setText(String.valueOf(pp));
				break;
			case R.id.txt_cto_add:
				cto ++;
				if(cto > mDataFilterLifeInfo.getCto())
				{
					cto = mDataFilterLifeInfo.getCto();
				}
				edit_cto_adjustment.setText(String.valueOf(cto));
				break;
			case R.id.txt_cto_reduce:
				cto --;
				if(cto < 0)
				{
					cto = 0;
				}
				edit_cto_adjustment.setText(String.valueOf(cto));
				break;
			case R.id.txt_ro_add:
				ro ++;
				if(ro > mDataFilterLifeInfo.getRo())
				{
					ro = mDataFilterLifeInfo.getRo();
				}
				edit_ro_adjustment.setText(String.valueOf(ro));
				break;
			case R.id.txt_ro_reduce:
				ro --;
				if(ro < 0)
				{
					ro = 0;
				}
				edit_ro_adjustment.setText(String.valueOf(ro));
				break;
			case R.id.txt_t33_add:
				t33 ++;
				if(t33 > mDataFilterLifeInfo.getT33())
				{
					t33 = mDataFilterLifeInfo.getT33();
				}
				edit_t33_adjustment.setText(String.valueOf(t33));
				break;
			case R.id.txt_t33_reduce:
				t33 --;
				if(t33 < 0)
				{
					t33 = 0;
				}
				edit_t33_adjustment.setText(String.valueOf(t33));
				break;
			case R.id.regulating_pp_bt:
			case R.id.cto_adjustment_bt:
			case R.id.ro_adjustment_bt:
			case R.id.t33_adjustment_bt:
				regulationMachine();
				break;
			case R.id.login_bt:
				if(pwd_et.getText().toString() == null)
				{
					MyToast.getManager(getApplicationContext()).show("请输入密码");
				}
				else if(!pwd_et.getText().toString().equals("jxsmart20160830"))
				{
					MyToast.getManager(getApplicationContext()).show("密码错误");
				}
				else
				{
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
					// 强制隐藏软键盘
					imm.hideSoftInputFromWindow(pwd_et.getWindowToken(), 0); 
					layout_login.setVisibility(View.GONE);
					layout_regulating.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.title_back_ll:
				finish();
			case R.id.reset_bt:
				showHintDialog(5);
				break;
		}
	}
	
	/**
	 * 显示操作提示框
	 * @param type
	 */
	private void showHintDialog(int type)
	{
		Intent intent = new Intent(RegulatingMachineActivity.this, HintDialogActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("type", type);
		startActivityForResult(intent, 100);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 105)
		{
			setResult(101);
			finish();
		}
	}
	
	private void regulationMachine() {
		if(mDataFilterLifeInfo != null  && mBaseData != null && verificationData != null)
		{	
			isSuccess = false;
			mRegulatingTemperatureHandler.post(mFilterLifeRunnable);
		}
	}
	
	//设置滤芯使用寿命	
		private Runnable mFilterLifeRunnable = new Runnable() {  
		    @Override  
		    public void run() {
				if(!Utils.inuse){//串口没有使用
					Utils.inuse = true;
					int try_times=0;
					int life[] = {pp<0?0:pp, cto<0?0:cto, ro<0?0:ro, t33<0?0:t33, 2000};
					System.out.println("==fuck you== "+life[0]+" , "+life[1]+" , "+life[2]+" , "+life[3]+" , "+life[4]);
					
					while(try_times<2){
						if(mSerialPortUtil.setFilterLife(life, life.length)>0 && mSerialPortUtil.getReturn()>=0){
							try_times++;
							isSuccess = true;
						}else{
							try_times++;
						}
					};
					
					Utils.inuse = false;
					
					if(isSuccess)
					{		
						//用新的数据来验证
						verificationData.setFirstFilter(pp);
						verificationData.setSecondFilter(cto);
						verificationData.setThirdFilter(ro);
						verificationData.setFourthFilter(t33);
						verificationData.setFivethFilter(2000);
						
						ToastUtil.showLongToast("滤芯寿命调整成功");
						optionDescriptionInfo.setId("1");
						optionDescriptionInfo.setParam("code:"+SharedPreferencesUtil.getStringData(RegulatingMachineActivity.this, "province", ""));
						optionDescriptionInfo.setOption("RegulatingMachineActivity:机器修正调整滤芯寿命操作");
						optionDescriptionInfo.setLocalDate("原来的BaseData数据："+mBaseData.toString() +"; 验证的数据verificationData变更："+verificationData.toString());
						optionDescriptionInfo.setNetDate(null);
						options.clear();
						options.add(optionDescriptionInfo);
						optionDescriptions.setDates(options);
						
						Intent intent = new Intent(ConfigUtils.upload_option_description_action);
						intent.putExtra("options", optionDescriptions);
						sendBroadcast(intent);
					}
					else
					{
						ToastUtil.showLongToast("滤芯寿命调整失败");
					}
				}
			}  
		}; 

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mRegulatingTemperatureHandler.removeCallbacks(mFilterLifeRunnable);  
	}
}
