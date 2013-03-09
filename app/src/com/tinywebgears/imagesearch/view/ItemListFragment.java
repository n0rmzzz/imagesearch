package com.tinywebgears.imagesearch.view;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.contentprovider.CommentsContentProvider;
import com.tinywebgears.imagesearch.dao.MySQLiteHelper;

/**
 * A list fragment representing a list of Items.
 */
public class ItemListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onCommentSelected(long id) {
		}
	};

	private SimpleCursorAdapter mAdapter;

	private Callbacks mCallbacks = sDummyCallbacks;

	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(getActivity(),
				VersionDependentConstants.getListItemLayout(), null,
				new String[] { MySQLiteHelper.COLUMN_ID,
						MySQLiteHelper.COLUMN_COMMENT }, new int[] {
						android.R.id.text1, android.R.id.text2 }, 0);
		getLoaderManager().initLoader(0, null, this);
		setListAdapter(mAdapter);

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
		inflator.inflate(R.menu.item_list, menu);
		super.onCreateOptionsMenu(menu, inflator);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri = CommentsContentProvider.CONTENT_URI;
		// Create and return a CursorLoader that will take care of creating a
		// Cursor for the data being displayed.
		String select = "(" + MySQLiteHelper.COLUMN_COMMENT + " NOTNULL)";
		String[] projection = { MySQLiteHelper.COLUMN_ID,
				MySQLiteHelper.COLUMN_COMMENT };
		return new CursorLoader(getActivity(), baseUri, projection, select,
				null, MySQLiteHelper.COLUMN_ID + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap in a new Cursor, returning the old Cursor.
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Swap in a new Cursor, returning the old Cursor.
		mAdapter.swapCursor(null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
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
		case R.id.menu_create:
			createNewComment();
			Toast.makeText(getActivity(), R.string.text_item_created,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		mCallbacks.onCommentSelected(id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION)
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically give
		// items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION)
			getListView().setItemChecked(mActivatedPosition, false);
		else
			getListView().setItemChecked(position, true);
		mActivatedPosition = position;
	}

	private void createNewComment() {
		List<String> allComments = Arrays.asList("Cool", "Very Nice",
				"Hate It", "Awesome", "Beautiful", "Not So Bad");
		int randomInt = new Random().nextInt(allComments.size());
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_COMMENT, allComments.get(randomInt));
		getActivity().getContentResolver().insert(
				CommentsContentProvider.CONTENT_URI, values);
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		public void onCommentSelected(long id);
	}
}
