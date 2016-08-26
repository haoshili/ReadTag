package com.atid.app.rfid;

import com.atid.app.rfid.view.InventoryView;
import com.atid.app.rfid.view.LockMemoryView;
import com.atid.app.rfid.view.OptionView;
import com.atid.app.rfid.view.ReadMemoryView;
import com.atid.app.rfid.view.WriteMemoryView;
import com.atid.app.rfidtwo.R;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.util.SysUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("ShowToast")
public class MainActivity extends Activity implements OnClickListener, RfidReaderEventListener {

	private final String TAG = MainActivity.class.getSimpleName();
	private final String LOG_PATH = "Log";
	private static final boolean ENABLE_LOG = true;

	private static final int INVENTORY_VIEW = 0;
	private static final int READ_MEMORY_VIEW = 1;
	private static final int WRITE_MEMORY_VIEW = 2;
	private static final int LOCK_MEMORY_VIEW = 3;
	private static final int OPTION_VIEW = 4;

	private ATRfidReader mReader;

	private TextView txtDemoVersion;
	private TextView txtFirmwareVersion;
	private Button btnInventory;
	private Button btnReadMemory;
	private Button btnWriteMemory;
	private Button btnLockMemory;
	private Button btnOption;
	private ImageView imgLogo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String appName = SysUtil.getAppName(this);
		if (ENABLE_LOG)
			ATLog.startUp(LOG_PATH, appName);

		// Setup always wake up
		SysUtil.wakeLock(this, appName);

		mReader = ATRfidManager.getInstance();

		// Initialize Widgets
		this.txtDemoVersion = (TextView) findViewById(R.id.demo_version);
		this.txtFirmwareVersion = (TextView) findViewById(R.id.firmware_version);
		this.btnInventory = (Button) findViewById(R.id.inventory);
		this.btnInventory.setOnClickListener(this);
		this.btnReadMemory = (Button) findViewById(R.id.read_memory);
		this.btnReadMemory.setOnClickListener(this);
		this.btnWriteMemory = (Button) findViewById(R.id.write_memory);
		this.btnWriteMemory.setOnClickListener(this);
		this.btnLockMemory = (Button) findViewById(R.id.lock_memory);
		this.btnLockMemory.setOnClickListener(this);
		this.btnOption = (Button) findViewById(R.id.option);
		this.btnOption.setOnClickListener(this);
		this.imgLogo = (ImageView) findViewById(R.id.app_logo);

		// Display Version
		String packageName = getPackageName();
		String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName;
		} catch (NameNotFoundException e) {
		}
		this.txtDemoVersion.setText(versionName);

		enableButtons(false);

		ATLog.i(TAG, "INFO. onCreate()");
	}

	@SuppressLint("Wakelock")
	@Override
	protected void onDestroy() {

		mReader.removeEventListener(this);

		ATRfidManager.onDestroy();

		// Setup basic wake up state
		SysUtil.wakeUnlock();

		ATLog.i(TAG, "INFO. onDestroy()");

		ATLog.shutdown();
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
		switch (requestCode) {
		case INVENTORY_VIEW:
		case READ_MEMORY_VIEW:
		case WRITE_MEMORY_VIEW:
		case LOCK_MEMORY_VIEW:
		case OPTION_VIEW:
			enableButtons(true);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;

		enableButtons(false);

		switch (v.getId()) {
		case R.id.inventory:
			intent = new Intent(this, InventoryView.class);
			startActivityForResult(intent, INVENTORY_VIEW);
			break;
		case R.id.read_memory:
			intent = new Intent(this, ReadMemoryView.class);
			startActivityForResult(intent, READ_MEMORY_VIEW);
			break;
		case R.id.write_memory:
			intent = new Intent(this, WriteMemoryView.class);
			startActivityForResult(intent, WRITE_MEMORY_VIEW);
			break;
		case R.id.lock_memory:
			intent = new Intent(this, LockMemoryView.class);
			startActivityForResult(intent, LOCK_MEMORY_VIEW);
			break;
		case R.id.option:
			intent = new Intent(this, OptionView.class);
			startActivityForResult(intent, OPTION_VIEW);
			break;
		}
	}

	// ------------------------------------------------------------------------
	// Reader Event Handler Methods
	// ------------------------------------------------------------------------

	@Override
	public void onReaderStateChanged(ATRfidReader reader, ConnectionState state) {
		ATLog.i(TAG, "EVENT. onReaderStateChanged(%s)", state);
		updateReaderState(state);
	}

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.i(TAG, "EVENT. onReaderActionChanged(%s)", action);
	}

	@Override
	public void onReaderReadTag(ATRfidReader reader, String tag, float rssi) {
		ATLog.i(TAG, "EVENT. onReaderReadTag([%s], %.1f)", tag, rssi);
	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data) {
		ATLog.i(TAG, "EVENT. onReaderResult(%s, %s, [%s], [%s])", code, action, epc, data);
	}

	// ------------------------------------------------------------------------
	// Internal Helper Methods
	// ------------------------------------------------------------------------

	// Connection State Update
	private void updateReaderState(ConnectionState state) {
		if (state == ConnectionState.Connected) {
			String version = "";
			try {
				version = mReader.getFirmwareVersion();
			} catch (ATRfidReaderException e) {
			}

			if (version.equals("")) {
				ATLog.e(TAG, "ERROR. updateReaderState(%s) - Failed to get firmware version", state);
				imgLogo.setImageResource(R.drawable.ic_disconnected_logo);
				enableButtons(false);
				return;
			}
			txtFirmwareVersion.setText(version);
			imgLogo.setImageResource(R.drawable.ic_connected_logo);
			enableButtons(true);
		} else {
			imgLogo.setImageResource(R.drawable.ic_disconnected_logo);
			enableButtons(false);
		}

	}

	// Enable/Disable Button
	private void enableButtons(boolean enabled) {
		this.btnInventory.setEnabled(enabled);
		this.btnOption.setEnabled(enabled);
		this.btnReadMemory.setEnabled(enabled);
		this.btnWriteMemory.setEnabled(enabled);
		this.btnLockMemory.setEnabled(enabled);
	}

}
