package com.tinywebgears.imagesearch.view;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.contentprovider.CommentsContentProvider;
import com.tinywebgears.imagesearch.dao.MySQLiteHelper;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends SherlockFragment {
	private static Callbacks sDummyCallbacks = new Callbacks() {
		public void onCommentRemvoed() {
		}
	};

	private Uri mItemUri;

	private TextView mItemDetailsTextView;

	private Callbacks mCallbacks = sDummyCallbacks;

	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mItemUri = (savedInstanceState == null) ? null
				: (Uri) savedInstanceState
						.getParcelable(CommentsContentProvider.CONTENT_ITEM_TYPE);
		if (getArguments() != null
				&& getArguments().containsKey(
						CommentsContentProvider.CONTENT_ITEM_TYPE))
			mItemUri = getArguments().getParcelable(
					CommentsContentProvider.CONTENT_ITEM_TYPE);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
		inflator.inflate(R.menu.item_details, menu);
		super.onCreateOptionsMenu(menu, inflator);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);
		mItemDetailsTextView = ((TextView) rootView
				.findViewById(R.id.item_detail));
		if (mItemUri != null)
			fillData(mItemUri);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof Callbacks))
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallbacks = sDummyCallbacks;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_remove:
			removeComment();
			mCallbacks.onCommentRemvoed();
			Toast.makeText(getActivity(), R.string.text_item_removed,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	private void fillData(Uri uri) {
		String[] projection = { MySQLiteHelper.COLUMN_ID,
				MySQLiteHelper.COLUMN_COMMENT };
		Cursor cursor = getActivity().getContentResolver().query(uri,
				projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			Long id = cursor.getLong(cursor
					.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_ID));
			String comment = cursor.getString(cursor
					.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_COMMENT));
			mItemDetailsTextView.setText("ID: " + id + "\n" + comment);
			cursor.close();
		}
	}

	private void removeComment() {
		if (mItemUri != null)
			getActivity().getContentResolver().delete(mItemUri, null, null);
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * deletion.
	 */
	public static interface Callbacks {
		public void onCommentRemvoed();
	}
}
