package com.atid.app.rfid.view;

import com.atid.app.rfidtwo.R;
import com.atid.app.rfid.adapter.MemoryListAdapter;
import com.atid.app.rfid.adapter.SpinnerAdapter;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.media.SoundPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ReadMemoryView extends Activity implements OnItemSelectedListener,
		OnClickListener, RfidReaderEventListener {

	private static final String TAG = ReadMemoryView.class.getSimpleName();

	private static final int MAX_OFFSET = 16;
	private static final int MAX_LENGTH = 16;
	
	private static final int DISPLAY_DELAYED_TIME = 500;

	private static final int SELECTION_MASK_VIEW = 1;
	
	private ATRfidReader mReader = null;

	private TextView txtSelect;
	private ProgressBar progWait;
	private TextView txtMessage;
	private LinearLayout layoutBackground;
	private ListView lstReadValue;
	private Spinner spnBank;
	private Spinner spnOffset;
	private Spinner spnLength;
	private EditText edtPassword;
	private Spinner spnPower;
	private EditText edtOperationTime;
	private Button btnAction;
	private Button btnClear;
	private Button btnMask;

	private MemoryListAdapter adpReadValue;
	private SpinnerAdapter adpTagType;
	private SpinnerAdapter adpBank;
	private SpinnerAdapter adpOffset;
	private SpinnerAdapter adpLength;
	private SpinnerAdapter adpPower;

	private SoundPlayer mSound;

	private Handler mHandler;
	private boolean mAction = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_read_memory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mHandler = new Handler();

		// Initialize Sound Player
		mSound = new SoundPlayer(this, R.raw.beep);

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		txtSelect = (TextView) findViewById(R.id.selection);

		progWait = (ProgressBar) findViewById(R.id.progress_bar);

		txtMessage = (TextView) findViewById(R.id.message);

		layoutBackground = (LinearLayout) findViewById(R.id.background);

		// Create Read Memory ListView
		lstReadValue = (ListView) findViewById(R.id.read_memory);
		adpReadValue = new MemoryListAdapter(this);
		lstReadValue.setAdapter(adpReadValue);

		// Create Bank Type Spinner
		spnBank = (Spinner) findViewById(R.id.bank);
		adpBank = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnBank.setAdapter(adpBank);
		spnBank.setOnItemSelectedListener(this);

		// Create Offset Spinner
		spnOffset = (Spinner) findViewById(R.id.offset);
		adpOffset = new SpinnerAdapter(this,
				android.R.layout.simple_list_item_1);
		spnOffset.setAdapter(adpOffset);
		spnOffset.setOnItemSelectedListener(this);

		// Create Length Spinner
		spnLength = (Spinner) findViewById(R.id.length);
		adpLength = new SpinnerAdapter(this,
				android.R.layout.simple_list_item_1);
		spnLength.setAdapter(adpLength);
		spnLength.setOnItemSelectedListener(this);

		// Create Password EditText
		edtPassword = (EditText) findViewById(R.id.password);

		// Create Power Gain Spinner
		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);
		spnPower.setOnItemSelectedListener(this);

		// Create Operation EditText
		edtOperationTime = (EditText) findViewById(R.id.operation_time);

		// Create Action Button
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);

		// Create Clear Button
		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);

		// Create Mask Button
		btnMask = (Button) findViewById(R.id.mask);
		btnMask.setOnClickListener(this);

		// Initialize bank
		for (BankType item : BankType.values()) {
			adpBank.addItem(item.getValue(), item.toString());
		}
		adpBank.notifyDataSetChanged();
		spnBank.setSelection(adpBank.indexOf(BankType.EPC.getValue()));

		int i = 0;
		// Initialize offset
		for (i = 0; i < MAX_OFFSET; i++) {
			adpOffset.addItem(i, String.format("%d word", i));
		}
		adpOffset.notifyDataSetChanged();
		spnOffset.setSelection(adpOffset.indexOf(2));

		// Initialize length
		for (i = 1; i <= MAX_LENGTH; i++) {
			adpLength.addItem(i, String.format("%d word", i));
		}
		adpLength.notifyDataSetChanged();
		spnLength.setSelection(adpLength.indexOf(2));

		// Initialize Password
		edtPassword.setText("");

		// Initialize Power Gain
		RangeValue powerRange;
		int time = 0;

		try {
			powerRange = mReader.getPowerRange();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, 
					"ERROR. onCreate() - Failed to get power range");
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
				mAction = false;
				enableWidgets(true);
			}

		}, DISPLAY_DELAYED_TIME);

		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATRfidManager.sleep();

		if (mAction) {
			enableWidgets(false);
			btnAction.setEnabled(false);
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
		mAction = false;
		enableWidgets(true);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.power_gain:
			ATLog.i(TAG, "INFO. onItemSelected(R.id.power_gain, %d)", position);
			int power = adpPower.getItem(position);
			try {
				mReader.setPower(power);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onItemSelected(%d) - Failed to set power",
						position);
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action:
			if (mReader.getAction() == ActionState.Stop)
				startAction();
			else
				stopAction();
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

	// ------------------------------------------------------------------------
	// Reader Event Handler Methods
	// ------------------------------------------------------------------------

	@Override
	public void onReaderStateChanged(ATRfidReader reader, ConnectionState state) {

		ATLog.i(TAG, "EVENT. onReaderStateChanged(%s)", state);

	}

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {

		ATLog.i(TAG, "EVENT. onReaderActionChanged(%s)", action);

		if (action == ActionState.Stop) {
			mAction = false;
			enableWidgets(true);
		}
	}

	@Override
	public void onReaderReadTag(ATRfidReader reader, String tag, float rssi) {

		ATLog.i(TAG, "EVENT. onReaderReadTag([%s], %.1f)", tag, rssi);
	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code,
			ActionState action, String epc, String data) {

		ATLog.i(TAG, "EVENT. onReaderResult(%s, %s, [%s], [%s])",
				code, action, epc, data);

		txtSelect.setText(epc);
		
		if (code == ResultCode.NoError) {
			adpReadValue.setValue(data);
			outputMessage(getResources().getString(R.string.result_success), true);
		} else {
			adpReadValue.clear();
			outputMessage(code.toString(), false);
		}
		beep();
	}

	private void startAction() {
		
		clear();
		
		ResultCode res;
		int operationTime = getOperationTime();
		BankType bank = BankType.valueOf(adpBank.getValue(spnBank
				.getSelectedItemPosition()));
		int offset = adpOffset.getValue(spnOffset.getSelectedItemPosition());
		int length = adpLength.getValue(spnLength.getSelectedItemPosition());
		String password = edtPassword.getText().toString();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startAction() - Failed to set operation time {%d}",
							operationTime);
		}
		if ((res = mReader.readMemory6c(bank, offset, length, password)) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startAction() - Failed to read memory {%s, %d, %d, [%s]} - [%s]",
							bank, offset, length, (password == null ? ""
									: password), res);
			mAction = false;
			return;
		}

		mAction = true;

		enableWidgets(false);

		ATLog.i(TAG, "INFO. startAction() - {%s, %d, %d, [%s]}",
				bank, offset, length, (password == null ? "" : password));
	}

	private void stopAction() {

		mAction = false;

		mReader.stop();

		// enableWidgets(false);
		enableWidgets(false);

		ATLog.i(TAG, "INFO. stopAction()");
	}

	private void clear() {
		txtSelect.setText("");
		outputMessage("");
		adpReadValue.clear();
	}

	private void enableWidgets(boolean enabled) {
		
		lstReadValue.setEnabled(enabled);
		spnBank.setEnabled(enabled);
		spnOffset.setEnabled(enabled);
		spnLength.setEnabled(enabled);
		edtPassword.setEnabled(enabled);
		spnPower.setEnabled(enabled);
		edtOperationTime.setEnabled(enabled);
		btnClear.setEnabled(enabled);
		btnMask.setEnabled(enabled);

		if (mReader.getAction() == ActionState.Stop) {
			progWait.setVisibility(View.GONE);
			btnAction.setEnabled(enabled);
			btnAction.setText(R.string.action_read);
		} else {
			progWait.setVisibility(View.VISIBLE);
			btnAction.setEnabled(true);
			btnAction.setText(R.string.action_stop);
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
		layoutBackground.setBackgroundColor(getResources().getColor(
				R.color.message_background));
	}

	private void outputMessage(String msg, boolean success) {
		progWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		if (success) {
			txtMessage.setTextColor(getResources().getColor(R.color.white));
			layoutBackground.setBackgroundColor(getResources().getColor(
					R.color.blue));
		} else {
			txtMessage.setTextColor(getResources().getColor(R.color.white));
			layoutBackground.setBackgroundColor(getResources().getColor(
					R.color.red));
		}
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
