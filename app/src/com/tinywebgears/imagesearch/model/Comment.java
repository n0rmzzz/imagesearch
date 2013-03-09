package com.tinywebgears.imagesearch.model;

/**
 * Basic model class which contains an ID and a simple text.
 */
public class Comment
{
    private long mId;
    private String mComment;

    public long getId()
    {
        return mId;
    }

    public void setId(long id)
    {
        this.mId = id;
    }

    public String getComment()
    {
        return mComment;
    }

    public void setComment(String comment)
    {
        this.mComment = comment;
    }

    @Override
    public String toString()
    {
        return mComment;
    }
}
