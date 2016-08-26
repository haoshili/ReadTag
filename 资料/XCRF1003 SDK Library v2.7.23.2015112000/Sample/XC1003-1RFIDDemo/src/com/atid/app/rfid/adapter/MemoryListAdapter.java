package com.atid.app.rfid.adapter;

import java.util.ArrayList;

import com.atid.app.rfidtwo.R;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.util.StringUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MemoryListAdapter extends BaseAdapter {

	private static final String TAG = "MemooryListAdapter";

	private static final int MAX_COL = 4;
	private static final int WORD_LENGTH = 16;

	private LayoutInflater inflater;
	private ArrayList<MemoryListItem> list;
	private int offset;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public MemoryListAdapter(Context context) {
		super();

		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<MemoryListItem>();
		this.offset = 0;
		MemoryListItem item = new MemoryListItem();
		this.list.add(item);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	// Clear Adapter List
	public void clear() {
		this.offset = 0;
		this.list.clear();
		MemoryListItem item = new MemoryListItem();
		this.list.add(item);
		this.notifyDataSetChanged();

		ATLog.d(TAG, "DEBUG. clear(%s)", item);
	}

	// Set Display Offset
	public void setOffset(int offset) {
		this.offset = offset * WORD_LENGTH;
	}

	// Set Memory Value
	public void setValue(String tag) {
		int row = tag.length() / MemoryListItem.MAX_DISPLAY_LENGTH;
		int i = 0;

		this.list.clear();

		for (i = 0; i < row; i++) {
			this.list.add(new MemoryListItem((i * WORD_LENGTH) + this.offset,
					tag.substring(i * MemoryListItem.MAX_DISPLAY_LENGTH,
							(i * MemoryListItem.MAX_DISPLAY_LENGTH) + MemoryListItem.MAX_DISPLAY_LENGTH)));
		}
		if (tag.length() % MemoryListItem.MAX_DISPLAY_LENGTH != 0) {
			this.list.add(new MemoryListItem((i * WORD_LENGTH) + this.offset,
					tag.substring(i * MemoryListItem.MAX_DISPLAY_LENGTH)));
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MemoryListViewHolder holder;

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.item_memory_list, parent, false);
			holder = new MemoryListViewHolder(convertView);
		} else {
			holder = (MemoryListViewHolder) convertView.getTag();
		}
		holder.setItem(this.list.get(position));
		ATLog.d(TAG, "DEBUG. getView(%s)", holder);
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal Class MemoryListListItem
	// ------------------------------------------------------------------------

	private class MemoryListItem {

		public static final int MAX_DISPLAY_LENGTH = 16;
		private static final int DISPLAY_VALUE_LENGTH = 4;

		private String[] address;
		private String[] value;

		public MemoryListItem() {
			this.address = new String[] { "0bit", "16bit", "32bit", "48bit" };
			this.value = new String[] { "0000", "0000", "0000", "0000" };
		}

		public MemoryListItem(int offset, String tag) {
			String data = StringUtil.padRight(tag, MAX_DISPLAY_LENGTH, '0');

			this.address = new String[MAX_COL];
			this.value = new String[MAX_COL];

			for (int i = 0; i < MAX_COL; i++) {
				this.address[i] = String.format("%dbit", offset + (i * WORD_LENGTH));
				this.value[i] = data.substring(i * MAX_COL, (i * MAX_COL) + DISPLAY_VALUE_LENGTH);
			}
		}

		public String getAddress(int index) {
			return this.address[index];
		}

		public String getValue(int index) {
			return this.value[index];
		}

		@Override
		public String toString() {
			return "{{" + address[0] + ", " + address[1] + ", " + address[2] + ", " + address[3] + "}, {" + value[0]
					+ ", " + value[1] + ", " + value[2] + ", " + value[3] + "}}";
		}
	}

	// ------------------------------------------------------------------------
	// Internal Class MemoryListViewHolder
	// ------------------------------------------------------------------------

	private class MemoryListViewHolder {

		private TextView[] address;
		private TextView[] value;

		public MemoryListViewHolder(View parent) {
			this.address = new TextView[] { (TextView) parent.findViewById(R.id.address1),
					(TextView) parent.findViewById(R.id.address2), (TextView) parent.findViewById(R.id.address3),
					(TextView) parent.findViewById(R.id.address4) };
			this.value = new TextView[] { (TextView) parent.findViewById(R.id.value1),
					(TextView) parent.findViewById(R.id.value2), (TextView) parent.findViewById(R.id.value3),
					(TextView) parent.findViewById(R.id.value4) };
			parent.setTag(this);
		}

		public void setItem(MemoryListItem item) {
			ATLog.d(TAG, "DEBUG. setItem(%s)", item);

			for (int i = 0; i < MAX_COL; i++) {
				this.address[i].setText(item.getAddress(i));
				this.value[i].setText(item.getValue(i));
			}
		}

		@Override
		public String toString() {
			return "{{" + address[0].getText() + ", " + address[1].getText() + ", " + address[2].getText() + ", "
					+ address[3].getText() + "}, {" + value[0].getText() + ", " + value[1].getText() + ", "
					+ value[2].getText() + ", " + value[3].getText() + "}}";
		}
	}
}
