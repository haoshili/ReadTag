package com.atid.app.rfid.view;

import com.atid.app.rfidtwo.R;
import com.atid.app.rfid.adapter.SpinnerAdapter;
import com.atid.app.rfid.dialog.PasswordDialog;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.LockParam;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.LockType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.media.SoundPlayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class LockMemoryView extends Activity
		implements OnItemSelectedListener, OnClickListener, RfidReaderEventListener {

	private static final String TAG = LockMemoryView.class.getSimpleName();

	private static final int DISPLAY_DELAYED_TIME = 500;

	private static final int SELECTION_MASK_VIEW = 1;

	private static final int OFFSET_KILL_PASSWORD = 0;
	private static final int OFFSET_ACCESS_PASSWORD = 2;

	private ATRfidReader mReader = null;

	private TextView txtSelect;
	private ProgressBar progWait;
	private TextView txtMessage;
	private LinearLayout layoutBackground;
	private CheckBox chkKillPassword;
	private CheckBox chkAccessPassword;
	private CheckBox chkEpc;
	private CheckBox chkTid;
	private CheckBox chkUser;
	private EditText edtPassword;
	private Spinner spnPower;
	private EditText edtOperationTime;
	private Button btnLock;
	private Button btnUnlock;
	private Button btnPermalock;
	private Button btnKill;
	private Button btnSetAccessPwd;
	private Button btnSetKillPwd;
	private Button btnClear;
	private Button btnMask;

	private SpinnerAdapter adpPower;

	private SoundPlayer mSound;

	private Handler mHandler;
	private ActionType mActType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_lock_memory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mHandler = new Handler();
		// Initialize Sound Player
		mSound = new SoundPlayer(this, R.raw.beep);
		mActType = ActionType.Stop;

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		// Initialize Widgets
		txtSelect = (TextView) findViewById(R.id.selection);

		progWait = (ProgressBar) findViewById(R.id.progress_bar);

		txtMessage = (TextView) findViewById(R.id.message);

		layoutBackground = (LinearLayout) findViewById(R.id.background);

		chkKillPassword = (CheckBox) findViewById(R.id.kill_password);

		chkAccessPassword = (CheckBox) findViewById(R.id.access_password);

		chkEpc = (CheckBox) findViewById(R.id.epc);

		chkTid = (CheckBox) findViewById(R.id.tid);

		chkUser = (CheckBox) findViewById(R.id.user);

		edtPassword = (EditText) findViewById(R.id.password);

		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);
		spnPower.setOnItemSelectedListener(this);

		edtOperationTime = (EditText) findViewById(R.id.operation_time);

		btnLock = (Button) findViewById(R.id.action_lock);
		btnLock.setOnClickListener(this);

		btnUnlock = (Button) findViewById(R.id.action_unlock);
		btnUnlock.setOnClickListener(this);

		btnPermalock = (Button) findViewById(R.id.action_perma_lock);
		btnPermalock.setOnClickListener(this);

		btnKill = (Button) findViewById(R.id.action_kill);
		btnKill.setOnClickListener(this);

		btnSetAccessPwd = (Button) findViewById(R.id.action_access_password);
		btnSetAccessPwd.setOnClickListener(this);

		btnSetKillPwd = (Button) findViewById(R.id.action_kill_password);
		btnSetKillPwd.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);

		btnMask = (Button) findViewById(R.id.mask);
		btnMask.setOnClickListener(this);

		// Initialize Power Gain
		RangeValue powerRange;
		int time = 0;
		int i = 0;

		try {
			powerRange = mReader.getPowerRange();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get power range");
			powerRange = new RangeValue();
		}
		for (i = powerRange.getMax(); i >= powerRange.getMin(); i -= 10) {
			adpPower.addItem(i, String.format("%.1f dBm", (i / 10.0)));
		}
		adpPower.notifyDataSetChanged();
		int power = 0;
		try {
			power = mReader.getPower();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get power");
		}
		spnPower.setSelection(adpPower.indexOf(power));

		// Initialize operation time
		try {
			time = mReader.getOperationTime();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get operation time");
			time = 0;
		}
		// Initialize Operation Time
		edtOperationTime.setText(String.format("%d", time));

		clear();

		// Enable Widgets
		enableWidgets(true);

		mHandler = new Handler();

		// Initialize Sound Player
		mSound = new SoundPlayer(this, R.raw.beep);

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		ATLog.i(TAG, "INFO. onCreate()");
	}

	@Override
	protected void onDestroy() {

		mReader.removeEventListener(this);
		ATRfidManager.onDestroy();

		ATLog.i(TAG, "INFO. onDestroy()");

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		ATRfidManager.wakeUp();

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// Enable Widgets
				mActType = ActionType.Stop;
				enableWidgets(true);
			}

		}, DISPLAY_DELAYED_TIME);

		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATRfidManager.sleep();

		if (mActType != ActionType.Stop) {
			enableWidgets(false);
			btnLock.setEnabled(false);
			btnUnlock.setEnabled(false);
			btnPermalock.setEnabled(false);
			btnKill.setEnabled(false);
		}

		ATLog.i(TAG, "INFO. onStop()");

		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mReader.setEventListener(this);

		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {

		mReader.removeEventListener(this);

		ATLog.i(TAG, "INFO. onPause()");

		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.i(TAG, "INFO. onActivityResult()");
		mActType = ActionType.Stop;
		enableWidgets(true);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_lock:
			if (mReader.getAction() == ActionState.Stop)
				startActionLock();
			else
				stopAction();
			break;
		case R.id.action_unlock:
			if (mReader.getAction() == ActionState.Stop)
				startActionUnlock();
			else
				stopAction();
			break;
		case R.id.action_perma_lock:
			if (mReader.getAction() == ActionState.Stop)
				startActionPermalock();
			else
				stopAction();
			break;
		case R.id.action_kill:
			if (mReader.getAction() == ActionState.Stop)
				startActionKill();
			else
				stopAction();
			break;
		case R.id.action_access_password:
			setAccessPassword();
			break;
		case R.id.action_kill_password:
			setKillPassword();
			break;
		case R.id.clear:
			clear();
			break;
		case R.id.mask:
			enableWidgets(false);
			Intent intent = new Intent(this, SelectionMaskView.class);
			startActivityForResult(intent, SELECTION_MASK_VIEW);
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.power_gain:
			ATLog.i(TAG, "INFO. onItemSelected(R.id.power_gain, %d)", position);
			int power = adpPower.getItem(position);
			try {
				mReader.setPower(power);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onItemSelected(%d) - Failed to set power", position);
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onReaderStateChanged(ATRfidReader reader, ConnectionState state) {

		ATLog.i(TAG, "EVENT. onReaderStateChanged(%s)", state);

	}

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {

		ATLog.i(TAG, "EVENT. onReaderActionChanged(%s)", action);

		if (action == ActionState.Stop) {
			mActType = ActionType.Stop;
			enableWidgets(true);
		}
	}

	@Override
	public void onReaderReadTag(ATRfidReader reader, String tag, float rssi) {

		ATLog.i(TAG, "EVENT. onReaderReadTag([%s], %.1f)", tag, rssi);

	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data) {

		ATLog.i(TAG, "EVENT. onReaderResult(%s, %s, [%s], [%s])", code, action, epc, data);

		txtSelect.setText(epc);

		if (code == ResultCode.NoError) {
			outputMessage(getResources().getString(R.string.result_success), true);
		} else {
			outputMessage(code.toString(), false);
		}
		beep();
	}

	private void startActionLock() {

		clear();

		ResultCode res;
		int operationTime = getOperationTime();
		LockParam param = new LockParam(chkKillPassword.isChecked() ? LockType.Lock : LockType.NoChange,
				chkAccessPassword.isChecked() ? LockType.Lock : LockType.NoChange,
				chkEpc.isChecked() ? LockType.Lock : LockType.NoChange,
				chkTid.isChecked() ? LockType.Lock : LockType.NoChange,
				chkUser.isChecked() ? LockType.Lock : LockType.NoChange);
		String password = edtPassword.getText().toString();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startActionLock() - Failed to set operation time {%d}", operationTime);
		}
		if ((res = mReader.lock6c(param, password)) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startActionLock() - Failed to lock {[%s], [%s]} - [%s]", param, password, res);
			return;
		}

		mActType = ActionType.Lock;

		enableWidgets(false);

		ATLog.i(TAG, "INFO. startActionLock()");
	}

	private void startActionUnlock() {

		clear();

		ResultCode res;
		int operationTime = getOperationTime();
		LockParam param = new LockParam(chkKillPassword.isChecked() ? LockType.Unlock : LockType.NoChange,
				chkAccessPassword.isChecked() ? LockType.Unlock : LockType.NoChange,
				chkEpc.isChecked() ? LockType.Unlock : LockType.NoChange,
				chkTid.isChecked() ? LockType.Unlock : LockType.NoChange,
				chkUser.isChecked() ? LockType.Unlock : LockType.NoChange);
		String password = edtPassword.getText().toString();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startActionUnlock() - Failed to set operation time {%d}", operationTime);
		}
		if ((res = mReader.lock6c(param, password)) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startActionUnlock() - Failed to unlock {[%s], [%s]} - [%s]", param, password, res);
			return;
		}

		mActType = ActionType.Unlock;

		enableWidgets(false);

		ATLog.i(TAG, "INFO. startActionUnlock()");
	}

	private void startActionPermalock() {

		clear();

		ResultCode res;
		int operationTime = getOperationTime();
		LockParam param = new LockParam(chkKillPassword.isChecked() ? LockType.PermaLock : LockType.NoChange,
				chkAccessPassword.isChecked() ? LockType.PermaLock : LockType.NoChange,
				chkEpc.isChecked() ? LockType.PermaLock : LockType.NoChange,
				chkTid.isChecked() ? LockType.PermaLock : LockType.NoChange,
				chkUser.isChecked() ? LockType.PermaLock : LockType.NoChange);
		String password = edtPassword.getText().toString();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startActionPermalock() - Failed to set operation time {%d}", operationTime);
		}
		if ((res = mReader.lock6c(param, password)) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startActionPermalock() - Failed to permalock {[%s], [%s]} - [%s]", param, password,
					res);
			return;
		}

		mActType = ActionType.Permalock;

		enableWidgets(false);

		ATLog.i(TAG, "INFO. startActionPermalock()");
	}

	private void startActionKill() {

		 
		clear();

		ResultCode res;
		int operationTime = getOperationTime();
		String password = edtPassword.getText().toString();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startActionKill() - Failed to set operation time {%d}", operationTime);
		}
		if ((res = mReader.kill6c(password)) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startActionKill() - Failed to kill {[%s]} - [%s]", password, res);
			return;
		}

		mActType = ActionType.Kill;

		enableWidgets(false);

		ATLog.i(TAG, "INFO. startActionKill()");
	}

	private void stopAction() {

		clear();

		mActType = ActionType.Stop;

		mReader.stop();

		enableWidgets(false);

		ATLog.i(TAG, "INFO. stopAction()");
	}

	// clear
	private void clear() {
		txtSelect.setText("");
		outputMessage("");
	}

	// Enable/Disable Widgets
	private void enableWidgets(boolean enabled) {
		ActionState action = mReader.getAction();

		if (action == ActionState.Stop) {
			progWait.setVisibility(View.GONE);
			chkKillPassword.setEnabled(enabled);
			chkAccessPassword.setEnabled(enabled);
			chkEpc.setEnabled(enabled);
			chkTid.setEnabled(enabled);
			chkUser.setEnabled(enabled);
			edtPassword.setEnabled(enabled);
			spnPower.setEnabled(enabled);
			edtOperationTime.setEnabled(enabled);
			btnLock.setEnabled(enabled);
			btnLock.setText(R.string.action_lock);
			btnUnlock.setEnabled(enabled);
			btnUnlock.setText(R.string.action_unlock);
			btnPermalock.setEnabled(enabled);
			btnPermalock.setText(R.string.action_perma_lock);
			btnKill.setEnabled(enabled);
			btnKill.setText(R.string.action_kill);
			btnSetAccessPwd.setEnabled(enabled);
			btnSetKillPwd.setEnabled(enabled);
			btnClear.setEnabled(enabled);
			btnMask.setEnabled(enabled);
		} else {
			progWait.setVisibility(View.VISIBLE);
			switch (action) {
			case Lock:
				switch (mActType) {
				case Lock:
					btnLock.setEnabled(true);
					btnLock.setText(R.string.action_stop);
					btnUnlock.setEnabled(enabled);
					btnPermalock.setEnabled(enabled);
					btnKill.setEnabled(enabled);
					break;
				case Unlock:
					btnUnlock.setEnabled(true);
					btnUnlock.setText(R.string.action_stop);
					btnLock.setEnabled(enabled);
					btnPermalock.setEnabled(enabled);
					btnKill.setEnabled(enabled);
					break;
				case Permalock:
					btnPermalock.setEnabled(true);
					btnPermalock.setText(R.string.action_stop);
					btnLock.setEnabled(enabled);
					btnUnlock.setEnabled(enabled);
					btnKill.setEnabled(enabled);
					break;
				}
				break;
			case Kill:
				btnKill.setEnabled(true);
				btnKill.setText(R.string.action_stop);
				btnLock.setEnabled(enabled);
				btnUnlock.setEnabled(enabled);
				btnPermalock.setEnabled(enabled);
				break;
			}
		}
	}

	// Beep & Vibrate
	protected void beep() {
		mSound.play();
	}

	// Output Message
	private void outputMessage(String msg) {
		progWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		txtMessage.setTextColor(getResources().getColor(R.color.black));
		layoutBackground.setBackgroundColor(getResources().getColor(R.color.message_background));
	}

	private void outputMessage(String msg, boolean success) {
		progWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		if (success) {
			txtMessage.setTextColor(getResources().getColor(R.color.white));
			layoutBackground.setBackgroundColor(getResources().getColor(R.color.blue));
		} else {
			txtMessage.setTextColor(getResources().getColor(R.color.white));
			layoutBackground.setBackgroundColor(getResources().getColor(R.color.red));
		}
	}

	// Set Access Password
	private void setAccessPassword() {
		enableWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this, R.string.set_access_password_title);
		dlg.setResultListener(new PasswordDialog.IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {

				clear();

				ResultCode res;
				String data = dlg.getPassword();
				String password = edtPassword.getText().toString();
				int time = getOperationTime();
				try {
					mReader.setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. setAccessPassword() - Failed to set operation time(%d)", time);
				}
				if ((res = mReader.writeMemory6c(BankType.Reserved, OFFSET_ACCESS_PASSWORD, data,
						password)) != ResultCode.NoError) {
					ATLog.e(TAG, "ERROR. setAccessPassword() - Failed to write memory {[%s], [%s]} - [%s]", data,
							password, res);
					return;
				}

				mActType = ActionType.SetAccessPassword;

				enableWidgets(false);

				ATLog.i(TAG, "INFO. setAccessPassword()");
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				enableWidgets(true);
			}
		});
		dlg.show();
	}

	// Set Kill Password
	private void setKillPassword() {
		enableWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this, R.string.set_kill_password_title);
		dlg.setResultListener(new PasswordDialog.IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {

				clear();

				ResultCode res;
				String data = dlg.getPassword();
				String password = edtPassword.getText().toString();
				int time = getOperationTime();
				try {
					mReader.setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. setKillPassword() - Failed to set operation time(%d)", time);
				}
				if ((res = mReader.writeMemory6c(BankType.Reserved, OFFSET_KILL_PASSWORD, data,
						password)) != ResultCode.NoError) {
					ATLog.e(TAG, "ERROR. setKillPassword() - Failed to write memory {[%s], [%s]} - [%s]", data,
							password, res);
					return;
				}

				mActType = ActionType.SetAccessPassword;

				enableWidgets(false);

				ATLog.i(TAG, "INFO. setKillPassword()");
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				enableWidgets(true);
			}
		});
		dlg.show();
	}

	private enum ActionType {
		Stop, Lock, Unlock, Permalock, Kill, SetAccessPassword, SetKillPassword
	}

	private int getOperationTime() {
		int time = 0;

		try {
			time = Integer.parseInt(edtOperationTime.getText().toString());
		} catch (Exception e) {
			time = 0;
		}
		return time;
	}

	private void setOperationTime(int time) {
		edtOperationTime.setText(String.format("%d", time));
	}
}
