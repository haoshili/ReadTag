package com.atid.app.rfid.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class WaitDialog {

	private static ProgressDialog dialog = null;

	// Show wait dialog
	public static void show(Context context, String title, String message,
			DialogInterface.OnCancelListener listener) {

		hide();

		dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		if (null != title) {
			dialog.setTitle(title);
		}
		if (null != message) {
			dialog.setMessage(message);
		}
		if (null != listener) {
			dialog.setCancelable(true);
			dialog.setOnCancelListener(listener);
		} else {
			dialog.setCancelable(false);
		}
		dialog.show();
	}

	public static void show(Context context, String title, String message) {
		show(context, title, message, null);
	}

	public static void show(Context context, String message,
			DialogInterface.OnCancelListener listener) {
		show(context, null, message, listener);
	}

	public static void show(Context context, String message) {
		show(context, null, message, null);
	}
	
	// Hide wiat dialog
	public static synchronized void hide() {
		if (null == dialog) {
			return;
		}
		dialog.dismiss();
		dialog = null;
	}
}
