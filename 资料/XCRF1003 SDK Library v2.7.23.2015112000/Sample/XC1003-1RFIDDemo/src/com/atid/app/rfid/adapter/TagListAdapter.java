package com.atid.app.rfid.adapter;

import java.util.ArrayList;

import com.atid.app.rfidtwo.R;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.util.SysUtil;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



public class TagListAdapter extends BaseAdapter {

	private static final String TAG = TagListAdapter.class.getSimpleName();
	
	private static final int UPDATE_TIME = 300;
	
	public static final int PC_LEN = 4;

	private LayoutInflater mInflater;
	private ArrayList<TagListItem> mList;
	private boolean mIsDisplayPc;

	private Handler mHandler;
	private Thread mThread;
	private boolean mIsAliveThread;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TagListAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mList = new ArrayList<TagListItem>();
		mIsDisplayPc = true;
		
		mHandler = new Handler();
		mThread = null;
		mIsAliveThread = false;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	public void start() {
		mThread = new Thread(mUpdateThread);
		mThread.start();
	}
	
	public void shutdown() {
		if (mThread == null)
			return;
		
		if (mThread.isAlive()) {
			mIsAliveThread = false;
			try {
				mThread.join();
			} catch (InterruptedException e) {
			}
		}
		mThread = null;
	}

	public void clear() {
		this.mList.clear();
		//this.notifyDataSetChanged();
	}

	public boolean getDisplayPc() {
		return this.mIsDisplayPc;
	}

	public void setDisplayPc(boolean show) {
		this.mIsDisplayPc = show;
		//this.notifyDataSetChanged();
	}

	public void addItem(String tag) {
		TagListItem item = null;

		ATLog.d(TAG, "DEBUG. addItem([%s])", tag);

		if ((item = findItem(tag)) == null) {
			item = new TagListItem(tag);
			this.mList.add(item);
			
		} else {
			item.increase();
		}
		//this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.mList.size();
	}

	@Override
	public String getItem(int position) {
		return this.mList.get(position).getTag();
	}

	public String getItem(int position, boolean displayPc) {
		return displayPc ? this.mList.get(position).getTag() : this.mList
				.get(position).getTag().substring(PC_LEN);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TagListViewHolder holder;

		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_tag_list, parent,
					false);
			holder = new TagListViewHolder(convertView);
		} else {
			holder = (TagListViewHolder) convertView.getTag();
		}
		holder.setItem(this.mList.get(position), this.mIsDisplayPc);

		return convertView;
	}

	// Find tag list item from tag
	private TagListItem findItem(String tag) {
		for (TagListItem item : this.mList) {
			if (item.equals(tag)) {
				return item;
			}
		}
		return null;
	}
	
	private Runnable mUpdateThread = new Runnable() {

		@Override
		public void run() {
			mIsAliveThread = true;
			
			while (mIsAliveThread) {
				mHandler.post(mUpdateProc);
				SysUtil.sleep(UPDATE_TIME);
			}
		}
		
	};
	
	private Runnable mUpdateProc = new Runnable() {

		@Override
		public void run() {
			notifyDataSetChanged();
		}
		
	};

	// ------------------------------------------------------------------------
	// Internal Class TagListItem
	// ------------------------------------------------------------------------

	private class TagListItem {

		private String tag;
		private int count;

		public TagListItem(String tag) {
			this.tag = tag;
			this.count = 1;
		}

		public String getTag() {
			return this.tag;
		}

		public int getCount() {
			return this.count;
		}

		public boolean equals(String tag) {
			return this.tag.equals(tag);
		}

		public void increase() {
			this.count++;
		}
	}

	// ------------------------------------------------------------------------
	// Internal Class TagListViewHolder
	// ------------------------------------------------------------------------

	private class TagListViewHolder {

		private TextView tag;
		private TextView count;

		public TagListViewHolder(View parent) {
			this.tag = (TextView) parent.findViewById(R.id.tag_value);
			this.count = (TextView) parent.findViewById(R.id.tag_count);
			parent.setTag(this);
		}

		public void setItem(TagListItem item, boolean displayPc) {
			if (displayPc) {
				this.tag.setText(item.getTag());
			} else {
				this.tag.setText(item.getTag().substring(PC_LEN));
			}
			this.count.setText("" + item.getCount());
		}
	}

}
