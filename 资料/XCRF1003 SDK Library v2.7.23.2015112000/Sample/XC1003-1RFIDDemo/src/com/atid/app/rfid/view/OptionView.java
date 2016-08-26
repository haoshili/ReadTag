package com.atid.app.rfid.view;

import com.atid.app.rfidtwo.R;
import com.atid.app.rfid.adapter.SpinnerAdapter;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.diagnostics.ATLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

@SuppressLint("NewApi")
public class OptionView extends Activity implements OnClickListener {

	private static final String TAG = OptionView.class.getSimpleName();

	private ATRfidReader mReader = null;

	private AutoCompleteTextView txtInventoryTime;
	private AutoCompleteTextView txtIdleTime;
	private Spinner spnPower;

	private ArrayAdapter<String> adpInventoryTime;
	private ArrayAdapter<String> adpIdleTime;
	private SpinnerAdapter adpPower;

	private Button btnSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_option);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		txtInventoryTime = (AutoCompleteTextView) findViewById(R.id.inventory_time);
		adpInventoryTime = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.reader_time));
		txtInventoryTime.setAdapter(adpInventoryTime);

		txtIdleTime = (AutoCompleteTextView) findViewById(R.id.idle_time);
		adpIdleTime = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.reader_time));
		txtIdleTime.setAdapter(adpIdleTime);

		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		// Initialize Inventory Time
		int inventoryTime = 0;
		try {
			inventoryTime = mReader.getInventoryTime();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get inventory time");
		}
		txtInventoryTime.setText("" + inventoryTime);

		// Initialize Idle Time
		int idleTime = 0;
		try {
			idleTime = mReader.getIdleTime();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get idle time");
		}
		txtIdleTime.setText("" + idleTime);

		RangeValue range;
		try {
			range = mReader.getPowerRange();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get power range");
			range = new RangeValue();
		}
		// Initialize Power Gain
		for (int i = range.getMax(); i >= range.getMin(); i -= 10) {
			adpPower.addItem(i, String.format("%.1f dBm", (i / 10.0)));
		}
		adpPower.notifyDataSetChanged();

		int power = 0;
		try {
			power = mReader.getPower();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onCreate() - Failed to get power");
		}
		int position = adpPower.indexOf(power);
		spnPower.setSelection(position);

		enableWidgets(true);

	}

	@Override
	protected void onDestroy() {

		ATRfidManager.onDestroy();

		ATLog.i(TAG, "INFO. onDestroy()");

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		ATRfidManager.wakeUp();

		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATRfidManager.sleep();

		ATLog.i(TAG, "INFO. onStop()");

		super.onStop();
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
		case R.id.save:
			enableWidgets(false);
			try {
				int inventoryTime = Integer.parseInt(txtInventoryTime.getText().toString());
				mReader.setInventoryTime(inventoryTime);
			} catch (Exception e) {
				ATLog.e(TAG, e, "ERROR. onClick() - Failed to invalidate value inventory time");
				enableWidgets(true);
				return;
			}
			try {
				int idleTime = Integer.parseInt(txtIdleTime.getText().toString());
				mReader.setIdleTime(idleTime);
			} catch (Exception e) {
				ATLog.e(TAG, e, "ERROR. onClick() - Failed to invalidate value idle time");
				enableWidgets(true);
				return;
			}
			try {
				int power = adpPower.getItem(spnPower.getSelectedItemPosition());
				mReader.setPower(power);
			} catch (Exception e) {
				ATLog.e(TAG, e, "ERROR. onClick() - Failed to invalidate value power");
				enableWidgets(true);
				return;
			}
			finish();
			break;
		}
	}

	// Enable/Disable Widgets
	private void enableWidgets(boolean enabled) {
		txtInventoryTime.setEnabled(enabled);
		txtIdleTime.setEnabled(enabled);
		spnPower.setEnabled(enabled);
		btnSave.setEnabled(enabled);
	}
}
