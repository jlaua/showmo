package com.showmo.activity.login;

import ipc365.app.showmo.jni.JniClient;

import com.showmo.base.BaseActivity;
import com.showmo.network.NetWorkErrorCode;
import com.showmo.network.RequestCallBack;
import com.showmo.network.ResponseInfo;
import com.showmo.timer.TimerService;
import com.showmo.userManage.IUserObject;
import com.showmo.userManage.User;
import com.showmo.util.StringUtil;
import com.showmo.util.ToastUtil;
import com.showmo.widget.PasswordText;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.showmo.R;

public class ResetPswActivity extends BaseActivity{

	private EditText etAccount;
	private Button btnRecover;
	private EditText etVeri;
	private boolean isInChina ;

	private boolean isPhone ; //判断输入的账号是否为电话
	private boolean isClickVer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_psw);
		
		//判断国家
		String country = getResources().getConfiguration().locale.getCountry();
		Log.e("所在国家", country);
		if("CN".equals(country)){
			isInChina = true ;
		}else{
			isInChina = false ;
		}
		initView();
		initTimer();
		initNetwork();
	}

	//	private void test() {
	//		etAccount.setText("15068749510");
	//		etPsw.setText("887766");
	//		etPswRe.setText("887766");
	//	}

	private void initView() {
		setBarTitle(R.string.find_psw);
		findViewById(R.id.btn_bar_back).setOnClickListener(this);

		etAccount=(EditText)findViewById(R.id.et_reset_account);
		etVeri = (EditText)findViewById(R.id.et_veri_code);
		btnRecover = (Button)findViewById(R.id.btn_veri_recover);
		btnRecover.setOnClickListener(this);
		findViewAndSet(R.id.btn_reset_psw);
		if(isInChina){
			etAccount.setHint(R.string.input_your_phone_as_account);
			etAccount.setInputType(InputType.TYPE_CLASS_PHONE);
		}else{
			etAccount.setHint(R.string.input_your_email_as_account);
			etAccount.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
		
		etVeri.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_UP){
					switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						if (checkInputContent()) {
							netTaskVerifyUserExisted();
						}
						break;
					default:
						break;
					}
					//return false;
				}
				return false;
			}
		});

	}

	@Override
	protected void onClick(int viewId) {
		switch (viewId) {
		case R.id.btn_reset_psw:
			if (checkInputContent()) {
				netTaskVerifyUserExisted();
			}
				break;
		case R.id.btn_bar_back:
			onBackPressed();
			break;
		case R.id.btn_veri_recover:
			if (checkInputContent()) {
				isClickVer=true;
				 netTaskVerifyUserExisted();
			}
			break;

		default:
			break;
		}

	}
	private void netTaskVerifyUserExisted(){//确认用户名是否存在
		mNetHelper.newNetTask(new RequestCallBack() {

			@Override
			public void onSuccess(ResponseInfo info) {

				long l = info.getDateLong();
				Log.e("out", "dateLong-->"+l);
				if(l == User.USER_EXIST){
					if (isClickVer) {
						netTaskGetVeri();
						btnRecover.setClickable(false);
						isClickVer=false;
					}else {
						if(StringUtil.checkVerificationCode( etVeri.getText().toString())){//检测验证码的格式
							hideSoftInputMethod();
							netTaskResetPsw();
						}else{
							showToastShort(R.string.please_input_six_digits_verification_code);
						}
					}
				
					
				}else if(l == User.USER_NOT_EXIST){
					ToastUtil.toastShort(getContext(), R.string.account_have_not_existed);
				}else if(l==-1){
					netConnectionError();
				}
			}

			@Override
			public void onFailure(ResponseInfo info ) {
				int errorCode = (int) info.getErrorCode();
				if(handleNetConnectionError(errorCode)){
					return ;
				}
				
			}

			@Override
			public ResponseInfo doInBackground() {
				String accountTemp = null;
				if(!isInChina){
					accountTemp = StringUtil.email2LowerCase(etAccount.getText().toString().trim());
				}else{
					accountTemp = etAccount.getText().toString().trim() ;									
				}
				return getResponseInfo(mShowmoSys.userExistQuery(accountTemp));
			}

		});
	}
	private void netConnectionError(){
		mNetHelper.newNetTask(new RequestCallBack() {
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				super.onPrepare();
				
			}
			@Override
			public ResponseInfo doInBackground() {
				ResponseInfo resInfo=new ResponseInfo();
				handleNetConnectionError((int)JniClient.PW_NET_GetLastError());
				resInfo.setIsSuccess(true);
				return resInfo;
			}
			@Override
			public void onSuccess(ResponseInfo info) {
				
			}
			
			@Override
			public void onFailure(ResponseInfo info) {
				
			}
			@Override
			public void onFinally() {
				// TODO Auto-generated method stub
				super.onFinally();
				
			}
		});
	}
	private void netTaskResetPsw(){
		mNetHelper.newNetTask(new RequestCallBack() {
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				super.onPrepare();
				showLoadingDialog();
			}
			@Override
			public ResponseInfo doInBackground() {
				// 0:验证码正确 1：验证码不存在  2：验证码不存在  -1：失败
				String accountTemp = null;
				if(!isPhone){
					accountTemp = StringUtil.email2LowerCase(etAccount.getText().toString().trim());
				}else{
					accountTemp = etAccount.getText().toString().trim() ;
				}
				long checkRes=mShowmoSys.checkVerifyCode(accountTemp, etVeri.getText().toString().trim());
				return getResponseInfoCheckVerifyCode(checkRes);
			}

			@Override
			public void onFailure(ResponseInfo info) {
				int errorCode = (int) info.getErrorCode();
				if(handleNetConnectionError(errorCode)){
					return ;
				}
				switch (errorCode) {
				case NetWorkErrorCode.CSP_ERROR_VERIFYCODE_WRONG:
					showToastShort(R.string.verification_code_error);
					break;

				default:
					break;
				}
				switch ((int)info.getDateLong()) {
				case 1:
					showToastShort(R.string.verification_code_error);
					break;
				case 2:
					showToastShort(R.string.verification_code_error);
					break;
				case -1:
					showToastShort(R.string.verification_code_fail);
					break;
				default:
					break;
				}

			}
			@Override
			public void onSuccess(ResponseInfo info) {
				String accountTemp = null;
				if(!isPhone){
					accountTemp = StringUtil.email2LowerCase(etAccount.getText().toString().trim());
				}else{
					accountTemp = etAccount.getText().toString().trim() ;
				}
				User user = new User(accountTemp, etVeri.getText().toString(),User.ACTION_VERI_RESET_PSW);
				slideInFromRight(VerificationCodeActivity.class,user);
			}
			@Override
			public void onFinally() {
				// TODO Auto-generated method stub
				super.onFinally();
				closeLoadingDialog();
			}
		});
	}
	private void netTaskGetVeri(){
		mNetHelper.newNetTask(new RequestCallBack() {
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				super.onPrepare();
				//showLoadingDialog();
			}
			@Override
			public void onSuccess(ResponseInfo info) {
				sendTimeControlBroadcast(TimerService.TIME_START);
				isTimerRunning = true ;
			}

			@Override
			public void onFailure(ResponseInfo info) {
				int errorCode = (int) info.getErrorCode();
				if(handleNetConnectionError(errorCode)){
					btnRecover.setClickable(true);
					return ;
				}
				btnRecover.setClickable(true);
			}

			@Override
			public ResponseInfo doInBackground() {
				User user=new User();
				IUserObject userObject=user;
				String accountTemp = null;
				if(!isInChina){
					accountTemp = StringUtil.email2LowerCase(etAccount.getText().toString().trim());
				}else{
					accountTemp = etAccount.getText().toString().trim() ;
				}
				userObject.setUserName(accountTemp);
				boolean bres =mShowmoSys.getVerifyCode(userObject);
				ResponseInfo res=getResponseInfo(bres);
				return res;
			}
			@Override
			public void onFinally() {
				// TODO Auto-generated method stub
				super.onFinally();
				
			}
		});
	}

	/**
	 * 检验用户输入内容
	 * @return 
	 */
	private boolean checkInputContent() {

		String account =  etAccount.getText().toString().trim();		
		if(	 !(isPhone=StringUtil.checkPhoneNumber(account ) )&&
				!StringUtil.checkEmail(account)	){
			showToastShort(getString(R.string.account_format_error));
			return false ;
		}
		return true;
	}
	private  class UITimeReceiver extends BroadcastReceiver{  
		@Override
		public void onReceive(android.content.Context context, Intent intent) {
			String timeLeft = intent.getStringExtra(BaseActivity.INTENT_KEY_STRING);
			onTimeReceive(timeLeft);
		}  
	}
	private boolean isTimerRunning ;
	private boolean isTimerInit ;
	private UITimeReceiver timeReceive;
	/** 
	 * 发送时间控制广播
	 * TimerService提供静态命令参数
	 */  
	private void sendTimeControlBroadcast(String command){  
		Intent intent  = new Intent();
		intent.putExtra(BaseActivity.INTENT_KEY_STRING,  command);
		intent.setAction(BaseActivity.TIME_CONTROL_ACTION);  
		sendBroadcast(intent);  
	}  
	/**
	 * onCreate()中调用此方法完成初始化
	 * 重写onTimeReceive()完成对UI的操作
	 */
	private  void initTimer() {
		if(!isTimerInit){
			startService(new Intent(this,TimerService.class));
			sendTimeControlBroadcast(TimerService.TIME_RESET);
			registerTimerChangedReceiver();
			isTimerInit = true ;
		}
	}
	/** 
	 * 注册时间广播
	 * 接受时间数据 
	 */  
	private void registerTimerChangedReceiver(){  
		timeReceive = new UITimeReceiver();  
		IntentFilter filter = new IntentFilter(TIME_CHANGED_ACTION);  
		registerReceiver(timeReceive, filter);  
	}  
	
	/**
	 * 接收到时间时调用此方法
	 * @param timeLeft
	 */
	private void onTimeReceive(String timeLeft) {
		int time = Integer.parseInt(timeLeft);
		//-1 是初始值，service准备好之后发送的广播的标志
		if("-1".equals(timeLeft)){
			netTaskGetVeri();
		}else if(time == 0){
			btnRecover.setText(getString(R.string.recover));
			btnRecover.setClickable(true);
			isTimerRunning = false ;
		}else if(time > 0){
			btnRecover.setText(timeLeft + getString(R.string.can_recover_after_seconds));

		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e("isTimerInitonStop", "isTimerInit"+isTimerInit);
		if(isTimerInit){
			if(timeReceive != null){
				unregisterReceiver(timeReceive);
				timeReceive= null;
			}
		}
		Log.e("isTimerRunning", "isTimerRunning"+isTimerRunning);
		if(!isTimerRunning){
			Log.e("out", "stopService");
			stopService(new Intent(this,TimerService.class));
			isTimerRunning = false ;
		}
		super.onDestroy();
	}
}
