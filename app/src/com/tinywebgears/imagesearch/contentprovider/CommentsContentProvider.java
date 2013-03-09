package com.tinywebgears.imagesearch.contentprovider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.tinywebgears.imagesearch.dao.MySQLiteHelper;

/**
 * Main content provider class which is a wrapper on the SQLite database.
 */
public class CommentsContentProvider extends ContentProvider
{
    // Used for the UriMacher
    private static final int COMMENTS = 10;
    private static final int COMMENT_ID = 20;

    private static final String AUTHORITY = "com.tinywebgears.imagesearch.contentprovider";

    private static final String BASE_PATH = "comments";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/comments";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/comment";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMMENTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", COMMENT_ID);
    }

    // database
    private MySQLiteHelper mDatabase;

    @Override
    public boolean onCreate()
    {
        mDatabase = new MySQLiteHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(MySQLiteHelper.TABLE_COMMENTS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType)
        {
        case COMMENTS:
            break;
        case COMMENT_ID:
            // Adding the ID to the original query
            queryBuilder.appendWhere(MySQLiteHelper.COLUMN_ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        long id = 0;
        switch (uriType)
        {
        case COMMENTS:
            id = sqlDB.insert(MySQLiteHelper.TABLE_COMMENTS, null, values);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType)
        {
        case COMMENTS:
            rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_COMMENTS, selection, selectionArgs);
            break;
        case COMMENT_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID + "=" + id, null);
            else
                rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID + "=" + id + " and "
                        + selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType)
        {
        case COMMENTS:
            rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_COMMENTS, values, selection, selectionArgs);
            break;
        case COMMENT_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_COMMENTS, values, MySQLiteHelper.COLUMN_ID + "=" + id,
                        null);
            else
                rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_COMMENTS, values, MySQLiteHelper.COLUMN_ID + "=" + id
                        + " and " + selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        String[] available = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_COMMENT };
        if (projection != null)
        {
            Set<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            Set<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns))
                throw new IllegalArgumentException("Unknown columns in projection");
        }
    }
}
