package com.atid.app.rfid.view;

import com.atid.app.rfidtwo.R;
import com.atid.app.rfid.adapter.SpinnerAdapter;
import com.atid.app.rfid.adapter.TagListAdapter;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.media.SoundPlayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("NewApi")
public class InventoryView extends Activity
		implements RfidReaderEventListener, OnItemSelectedListener, OnCheckedChangeListener, OnClickListener {

	private static final String TAG = InventoryView.class.getSimpleName();

	private static final String OPTIONS_NAME = "Inventory";

	private static final String KEY_DISPLAY_PC = "Display PC";
	private static final String KEY_CONTINUOUS_MODE = "Continuous Mode";
	private static final boolean DEFAULT_DISPLAY_PC = true;
	private static final boolean DEFAULT_CONTINUOUS_MODE = true;

	private static final int DISPLAY_DELAYED_TIME = 500;

	private static final int SELECTION_MASK_VIEW = 1;

	private ATRfidReader mReader = null;

	private ListView lstTags;
	private Spinner spnPower;
	private CheckBox chkDisplayPc;
	private CheckBox chkContinuousMode;
	private EditText edtOperationTime;
	private TextView txtCount;
	private Button btnAction;
	private Button btnClear;
	private Button btnMask;

	private TagListAdapter adpTags;
	private SpinnerAdapter adpPower;

	private SoundPlayer mSound;

	private Handler mHandler;
	private boolean mAction = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_inventory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mHandler = new Handler();

		// Initialize Sound Player
		mSound = new SoundPlayer(this, R.raw.beep);

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		// Create Tag ListView
		lstTags = (ListView) findViewById(R.id.tag_list);
		adpTags = new TagListAdapter(this);
		lstTags.setAdapter(adpTags);
		lstTags.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lstTags.setFastScrollEnabled(true);

		// Create Power Gain Spinner
		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);
		spnPower.setOnItemSelectedListener(this);

		// Create Display PC CheckBox
		chkDisplayPc = (CheckBox) findViewById(R.id.display_pc);
		chkDisplayPc.setOnCheckedChangeListener(this);

		// Create Continuous Mode CheckBox
		chkContinuousMode = (CheckBox) findViewById(R.id.continue_mode);
		chkContinuousMode.setOnCheckedChangeListener(this);

		// Create Operation EditText
		edtOperationTime = (EditText) findViewById(R.id.operation_time);

		// Create Inventory Tag Count Text View
		txtCount = (TextView) findViewById(R.id.tag_count);

		// Create Inventory/Stop Button
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);

		// Create Clear Button
		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);

		// Create Mask Button
		btnMask = (Button) findViewById(R.id.mask);
		btnMask.setOnClickListener(this);

		// Initialize Power Gain
		RangeValue powerRange;
		int time = 0;

		try {
			powerRange = mReader.getPowerRange();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get power range");
			powerRange = new RangeValue();
		}
		for (int i = powerRange.getMax(); i >= powerRange.getMin(); i -= 10) {
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

		// Load Options
		loadOptions();

		clear();

		ATLog.i(TAG, "INFO. onCreate()");
	}

	@Override
	protected void onDestroy() {

		mReader.removeEventListener(this);
		ATRfidManager.onDestroy();

		saveOptions();

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action:
			ATLog.i(TAG, "INFO. onClick(R.id.inventory)");
			// if (mReader.getState() == ReaderState.Stop) {
			if (mReader.getAction() == ActionState.Stop)
				startAction();
			else
				stopAction();
			break;
		case R.id.clear:
			ATLog.i(TAG, "INFO. onClick(R.id.clear)");
			clear();
			break;
		case R.id.mask:
			enableWidgets(false);
			Intent intent = new Intent(this, SelectionMaskView.class);
			startActivityForResult(intent, SELECTION_MASK_VIEW);
			ATLog.i(TAG, "INFO. onClick(R.id.mask)");
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.display_pc:
			ATLog.i(TAG, "INFO. onCheckedChanged(R.id.display_pc, %s)", isChecked);
			adpTags.setDisplayPc(isChecked);
			adpTags.notifyDataSetChanged();
			saveOptions();
			break;
		case R.id.continue_mode:
			ATLog.i(TAG, "INFO. onCheckedChanged(R.id.continue_mode, %s)", isChecked);
			saveOptions();
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

		// Play Beep
		beep();
		// Add Tag List
		adpTags.addItem(tag);
		// Display Tag Count
		txtCount.setText("" + adpTags.getCount());

		adpTags.notifyDataSetChanged();
	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data) {
		ATLog.i(TAG, "EVENT. onReaderResult(%s, %s, [%s], [%s])", code, action, epc, data);
	}

	// ------------------------------------------------------------------------
	// Inner Helper Methods
	// ------------------------------------------------------------------------

	// Start Action
	private void startAction() {

		ResultCode res;
		int operationTime = getOperationTime();

		try {
			mReader.setOperationTime(operationTime);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startAction() - Failed to set operation time {%d}", operationTime);
		}
		if (getContinuousMode()) {
			// Multi Tag Inventory...
			if ((res = mReader.inventory6cTag()) != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. startAction() - Failed to inventory [%s]", res);
				return;
			}
		} else {
			// Single Tag Inventory...
			if ((res = mReader.readEpc6cTag()) != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. startAction() - Failed to read epc [%s]", res);
				return;
			}
		}
		mAction = true;
		enableWidgets(false);

		ATLog.i(TAG, "INFO. startAction()");
	}

	// Stop Action
	private void stopAction() {

		mAction = false;

		mReader.stop();

		enableWidgets(false);

		ATLog.i(TAG, "INFO. stopAction()");
	}

	// clear
	private void clear() {
		adpTags.clear();
		txtCount.setText("" + adpTags.getCount());
		adpTags.notifyDataSetChanged();
	}

	// Get ContinuousMode
	private boolean getContinuousMode() {
		return chkContinuousMode.isChecked();
	}

	// Enable/Disable Widgets
	private void enableWidgets(boolean enabled) {

		spnPower.setEnabled(enabled);
		chkDisplayPc.setEnabled(enabled);
		chkContinuousMode.setEnabled(enabled);
		edtOperationTime.setEnabled(enabled);
		btnClear.setEnabled(enabled);
		btnMask.setEnabled(enabled);

		if (mReader.getAction() == ActionState.Stop) {
			btnAction.setText(R.string.action_inventory);
			btnAction.setEnabled(enabled);
		} else {
			btnAction.setText(R.string.action_stop);
			btnAction.setEnabled(true);
		}
	}

	// Beep & Vibrate
	protected void beep() {
		mSound.play();
	}

	// Load Last Option
	private void loadOptions() {

		ATLog.i(TAG, "loadOptions()");

		SharedPreferences prefs = getSharedPreferences(OPTIONS_NAME, MODE_PRIVATE);

		boolean isDisplayPc = prefs.getBoolean(KEY_DISPLAY_PC, DEFAULT_DISPLAY_PC);
		boolean isContinuousMode = prefs.getBoolean(KEY_CONTINUOUS_MODE, DEFAULT_CONTINUOUS_MODE);

		chkDisplayPc.setChecked(isDisplayPc);
		chkContinuousMode.setChecked(isContinuousMode);
	}

	// Save Last Option
	private void saveOptions() {

		ATLog.i(TAG, "saveOptions()");

		SharedPreferences prefs = getSharedPreferences(OPTIONS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		boolean isDisplayPc = chkDisplayPc.isChecked();
		boolean isContinuousMode = chkContinuousMode.isChecked();

		editor.putBoolean(KEY_DISPLAY_PC, isDisplayPc);
		editor.putBoolean(KEY_CONTINUOUS_MODE, isContinuousMode);
		editor.commit();
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
