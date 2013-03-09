package com.tinywebgears.imagesearch.view;

import roboguice.inject.ContentView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.contentprovider.CommentsContentProvider;

/**
 * An activity representing a single Item detail screen. This activity is only
 * used on handsets.
 */
@ContentView(R.layout.activity_item_detail)
public class ItemDetailActivity extends RoboSherlockFragmentActivity implements
		ItemDetailFragment.Callbacks {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Show the Up button in the action bar.
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putParcelable(
					CommentsContentProvider.CONTENT_ITEM_TYPE,
					getIntent().getParcelableExtra(
							CommentsContentProvider.CONTENT_ITEM_TYPE));
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.item_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, ItemListActivity.class));
			return true;
		}
		return false;
	}

	@Override
	public void onCommentRemvoed() {
		Uri listUri = CommentsContentProvider.CONTENT_URI;
		Intent listIntent = new Intent(this, ItemListActivity.class);
		listIntent.putExtra(CommentsContentProvider.CONTENT_TYPE, listUri);
		startActivity(listIntent);
	}
}
