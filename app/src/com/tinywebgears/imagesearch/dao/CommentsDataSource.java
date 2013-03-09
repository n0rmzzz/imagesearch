package com.tinywebgears.imagesearch.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tinywebgears.imagesearch.model.Comment;

/**
 * DAO class for {@link Comment}.
 */
public class CommentsDataSource
{
    private SQLiteDatabase mDatabase;
    private MySQLiteHelper mDbHelper;
    private String[] mAllColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_COMMENT };

    public CommentsDataSource(Context context)
    {
        mDbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException
    {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close()
    {
        mDbHelper.close();
    }

    public Comment createComment(String comment)
    {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
        long insertId = mDatabase.insert(MySQLiteHelper.TABLE_COMMENTS, null, values);
        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_COMMENTS, mAllColumns, MySQLiteHelper.COLUMN_ID + " = "
                + insertId, null, null, null, null);
        cursor.moveToFirst();
        Comment newComment = cursorToComment(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteComment(Comment comment)
    {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        mDatabase.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteAllComments()
    {
        mDatabase.delete(MySQLiteHelper.TABLE_COMMENTS, null, null);
    }

    public List<Comment> getAllComments()
    {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_COMMENTS, mAllColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Comment comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    private Comment cursorToComment(Cursor cursor)
    {
        Comment comment = new Comment();
        comment.setId(cursor.getLong(0));
        comment.setComment(cursor.getString(1));
        return comment;
    }
}
