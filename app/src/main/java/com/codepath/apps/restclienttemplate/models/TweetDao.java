package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TweetDao {
    @Query("SELECT Tweet.body AS tweet_body, Tweet.createdAt as tweet_createdAt, Tweet.id as tweet_id," +
            " Tweet.relativeDate as tweet_relativeDate, Tweet.media as tweet_media, Tweet.favorited as tweet_favorited," +
            " Tweet.retweeted as tweet_retweeted, Tweet.retweetCount as tweet_retweetCount, Tweet.favoriteCount as tweet_favoriteCount," +
            " User.* FROM Tweet INNER JOIN User ON Tweet.userId = user.id ORDER BY Tweet.createdAt DESC LIMIT 25")
    List<TweetWithUser> recentItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);
}
