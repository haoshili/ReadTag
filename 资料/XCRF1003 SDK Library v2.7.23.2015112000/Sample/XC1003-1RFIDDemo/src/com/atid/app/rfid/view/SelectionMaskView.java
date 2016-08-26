package com.atid.app.rfid.view;

import com.atid.app.rfidtwo.R;
import com.atid.app.rfid.adapter.SelectionMask6cAdapter;
import com.atid.app.rfid.adapter.SpinnerAdapter;
import com.atid.app.rfid.dialog.SelectionMaskDialog;
import com.atid.app.rfid.dialog.SelectionMaskDialog.ISelectionMaskListener;
import com.atid.app.rfid.dialog.WaitDialog;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.SelectionMask6c;
import com.atid.lib.dev.rfid.type.InventorySession;
import com.atid.lib.dev.rfid.type.InventoryTarget;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.util.SysUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;

public class SelectionMaskView extends Activity
		implements OnClickListener, OnItemLongClickListener, ISelectionMaskListener, OnCheckedChangeListener {

	private static final String TAG = SelectionMaskView.class.getSimpleName();

	private static final int MAX_SELECTION_MASK_COUNT = 8;

	private ATRfidReader mReader = null;

	private CheckBox chkUseSelectionMask;
	private ListView lstMasks;
	private Spinner spnSession;
	private Spinner spnTarget;
	private Button btnSave;
	private Button btnCancel;

	private SelectionMask6cAdapter adpMasks;
	private SpinnerAdapter adpSession;
	private SpinnerAdapter adpTarget;

	private boolean mIsUseSelectionMask;
	private SelectionMask6c[] mMasks;
	private InventorySession mSession;
	private InventoryTarget mTarget;

	private SelectionMaskDialog mMaskDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_selection_mask);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mIsUseSelectionMask = false;
		mMasks = new SelectionMask6c[MAX_SELECTION_MASK_COUNT];
		mSession = InventorySession.S0;
		mTarget = InventoryTarget.All;

		mMaskDialog = new SelectionMaskDialog(this, this);

		// Initialize Reader
		if ((mReader = ATRfidManager.getInstance()) == null) {
			ATLog.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
		}

		chkUseSelectionMask = (CheckBox) findViewById(R.id.use_selection_mask);
		chkUseSelectionMask.setChecked(mIsUseSelectionMask);
		chkUseSelectionMask.setOnCheckedChangeListener(this);

		lstMasks = (ListView) findViewById(R.id.mask_list);
		adpMasks = new SelectionMask6cAdapter(this);
		lstMasks.setAdapter(adpMasks);
		lstMasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lstMasks.setOnItemLongClickListener(this);

		spnSession = (Spinner) findViewById(R.id.session);
		adpSession = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnSession.setAdapter(adpSession);

		spnTarget = (Spinner) findViewById(R.id.target);
		adpTarget = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnTarget.setAdapter(adpTarget);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);

		// Initialzie Session
		for (InventorySession item : InventorySession.values()) {
			adpSession.addItem(item.getValue(), item.toString());
		}
		adpSession.notifyDataSetChanged();
		spnSession.setSelection(adpSession.indexOf(mSession.getValue()));

		// Initialize Target
		for (InventoryTarget item : InventoryTarget.values()) {
			adpTarget.addItem(item.getValue(), item.toString());
		}
		adpTarget.notifyDataSetChanged();
		spnTarget.setSelection(adpTarget.indexOf(mTarget.getValue()));

		ATLog.i(TAG, "INFO. onCreate()");
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

		WaitDialog.show(this, "Loading Selection Mask\r\nPlease Wait...");
		new Thread(mLoading).start();

		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATRfidManager.sleep();

		ATLog.i(TAG, "INFO. onStop()");

		super.onStop();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		lstMasks.setEnabled(isChecked);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			WaitDialog.show(this, "Saving Selection Mask\r\nPlease Wait...");
			mIsUseSelectionMask = chkUseSelectionMask.isChecked();
			for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
				mMasks[i] = adpMasks.getItem(i);
			}
			mSession = InventorySession.valueOf(adpSession.getValue(spnSession.getSelectedItemPosition()));
			mTarget = InventoryTarget.valueOf(adpTarget.getValue(spnTarget.getSelectedItemPosition()));
			new Thread(mSaving).start();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mMaskDialog.show(position, adpMasks.getItem(position));
		return true;
	}

	@Override
	public void onOkClick(SelectionMaskDialog dialog) {
		int position = dialog.getPosition();
		adpMasks.setItem(position, dialog.getSelectionMask());
		adpMasks.notifyDataSetChanged();
	}

	@Override
	public void onCancelClick(SelectionMaskDialog dialog) {
	}

	public Runnable mLoading = new Runnable() {

		@Override
		public void run() {

			try {
				mIsUseSelectionMask = mReader.getUseSelectionMask();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Loading() - Failed to get use selection mask");
			}
			for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
				try {
					mMasks[i] = mReader.getSelectionMask6c(i);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. Loading() - Failed to get selection mask {%d}", i);
				}
				SysUtil.sleep(10);
			}

			try {
				mSession = mReader.getInventorySession();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Loading() - Failed to get inventory session");
			}
			try {
				mTarget = mReader.getInventoryTarget();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Loading() - Failed to get inventory target");
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					chkUseSelectionMask.setChecked(mIsUseSelectionMask);
					lstMasks.setEnabled(mIsUseSelectionMask);
					for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
						adpMasks.setItem(i, mMasks[i]);
					}
					adpMasks.notifyDataSetChanged();
					spnSession.setSelection(adpSession.indexOf(mSession.getValue()));
					spnTarget.setSelection(adpTarget.indexOf(mTarget.getValue()));

					WaitDialog.hide();
				}

			});
		}

	};

	public Runnable mSaving = new Runnable() {

		@Override
		public void run() {
			SelectionMask6c mask = null;

			try {
				mReader.setUseSelectionMask(mIsUseSelectionMask);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Saving() - Failed to set use selection mask {%s}", mIsUseSelectionMask);
			}

			for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
				try {
					mReader.setSelectionMask6c(i, mMasks[i]);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. Saving() - Failed to set selection mask {%d, [%s]}", i, mask);
				}
			}

			try {
				mReader.setInventorySession(mSession);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Saving() - Failed to set inventory session {%s}", mSession);
			}
			try {
				mReader.setInventoryTarget(mTarget);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Saving() - Failed to set inventory target {%s}", mTarget);
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WaitDialog.hide();
					finish();
				}

			});
		}

	};
}
