package com.tinywebgears.imagesearch.view;

import javax.annotation.Nullable;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.contentprovider.CommentsContentProvider;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablets, including an {@link ItemListFragment}
 * and an {@link ItemDetailFragment} if it fits the screen.
 */
@ContentView(R.layout.activity_item_list)
public class ItemListActivity extends RoboSherlockFragmentActivity implements
		ItemListFragment.Callbacks, ItemDetailFragment.Callbacks {
	private boolean mTwoPane;

	@Nullable
	@InjectView(R.id.item_detail_container)
	FrameLayout itemDetailContainer;

	@Nullable
	@InjectFragment(R.id.item_list)
	Fragment itemListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (itemDetailContainer != null) {
			mTwoPane = true;
			// In two-pane mode, list items should be given the 'activated'
			// state when touched.
			((ItemListFragment) itemListFragment).setActivateOnItemClick(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
			return false;
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCommentSelected(long id) {
		Uri itemUri = Uri.parse(CommentsContentProvider.CONTENT_URI + "/" + id);
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by adding
			// or replacing the detail fragment using
			// a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putParcelable(CommentsContentProvider.CONTENT_ITEM_TYPE,
					itemUri);
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();
		} else {
			// In single-pane mode, simply start the detail activity for the
			// selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(CommentsContentProvider.CONTENT_ITEM_TYPE,
					itemUri);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onCommentRemvoed() {
		ItemDetailFragment fragment = new ItemDetailFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.item_detail_container, fragment).commit();
	}
}
