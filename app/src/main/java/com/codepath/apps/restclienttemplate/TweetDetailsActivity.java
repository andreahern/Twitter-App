package com.codepath.apps.restclienttemplate;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailsBinding;
import com.codepath.apps.restclienttemplate.models.ReplyActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {
    private static final String TAG = "TweetDetailsActivity";

    TwitterClient client;
    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvName;
    ImageView ivMedia;
    ImageView ivFavorite;
    ImageView ivRetweet;
    TextView tvFavoriteCount;
    TextView tvRetweetCount;
    Tweet tweet;
    long id;

    ActivityTweetDetailsBinding binding;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        toolbar = binding.toolbar;
        ivProfileImage = binding.ivProfileImage;
        tvBody = binding.tvBody;
        tvScreenName = binding.tvScreenName;
        tvName = binding.tvUserName;
        ivMedia = binding.ivMedia;
        ivFavorite = binding.ivFavorite;
        ivRetweet = binding.ivRetweet;
        tvFavoriteCount = binding.tvFavoriteCount;
        tvRetweetCount = binding.tvRetweetCount;

        id = getIntent().getLongExtra("id", 0);
        client = TwitterApp.getRestClient(this);

        Glide.with(this).load(R.drawable.ic_vector_heart_stroke).into(ivFavorite);
        Glide.with(this).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);

        getTweetDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        toolbar.setTitle("");
        return true;
    }

    public void goHome(View view) {
        finish();
    }

    public void handleReply(View view) {
        Intent i = new Intent(this, ReplyActivity.class);
        i.putExtra("id", tweet.id);
        i.putExtra("username", tweet.user.screenName);
        startActivity(i);
        finish();
    }

    public void handleFavorite(View view) {
        if (tweet.favorited) onUnFavorite();
        else onFavorite();
    }

    public void handleRetweet(View view) {
        if (tweet.retweeted) onUnRetweet();
        else onRetweet();
    }

    public void onRetweet() {
        client.createRetweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "onSuccess onRetweet: " + json.toString());
                    tweet.retweeted = json.jsonObject.getBoolean("retweeted");
                    Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                    ivRetweet.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.medium_green));
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure onRetweet: ", throwable);
            }
        }, id);
    }

    public void onUnRetweet() {
        client.destroyRetweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "onSuccess onUnRetweet: " + json.toString());
                    tweet.retweeted = json.jsonObject.getBoolean("retweeted");
                    Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                    ivRetweet.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.inline_action));
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure onUnRetweet: ", throwable);
            }
        }, id);
    }

    public void onFavorite() {
        client.createFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "onSuccess onFavorite: " + json.toString());
                    tweet.favorited = json.jsonObject.getBoolean("favorited");
                    Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_heart).into(ivFavorite);
                    ivFavorite.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.medium_red));
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure onFavorite: ", throwable);
            }
        }, id);
    }

    public void onUnFavorite() {
        client.destroyFavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "onSuccess onUnfavorite: " + json.toString());
                    tweet.favorited = json.jsonObject.getBoolean("favorited");
                    Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_heart_stroke).into(ivFavorite);
                    ivFavorite.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.inline_action));
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure onUnfavorite: ", throwable);
            }
        }, id);
    }

    public void getTweetDetails() {
        client.getDetails(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    tweet = fromJson(json.jsonObject);
                    Log.d(TAG, "onSuccess: " + tweet);
                    tvScreenName.setText(tweet.user.name);
                    tvName.setText("@" + tweet.user.screenName);
                    tvBody.setText(tweet.body);
                    tvFavoriteCount.setText("" + tweet.favoriteCount);
                    tvRetweetCount.setText("" + tweet.retweetCount);

                    Glide.with(TweetDetailsActivity.this).load(tweet.user.profileImageUrl).into(ivProfileImage);

                    if (tweet.media != null) {
                        Glide.with(TweetDetailsActivity.this).load(tweet.media).into(ivMedia);
                        ivMedia.setVisibility(View.VISIBLE);
                    } else {
                        ivMedia.setVisibility(View.GONE);
                    }

                    if (tweet.favorited) {
                        Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_heart).into(ivFavorite);
                        ivFavorite.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.medium_red));
                    } else {
                        Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_heart_stroke).into(ivFavorite);
                        ivFavorite.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.inline_action));
                    }

                    if (tweet.retweeted) {
                        Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                        ivRetweet.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.medium_green));
                    } else {
                        Glide.with(TweetDetailsActivity.this).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                        ivRetweet.setColorFilter(ContextCompat.getColor(TweetDetailsActivity.this, R.color.inline_action));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure onFavorite: ", throwable);
            }
        }, id);
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("full_text");
        Log.d(TAG, "fromJson: " + tweet.body);
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.relativeDate = Tweet.getRelativeTimeAgo(tweet.createdAt);
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.id = jsonObject.getLong("id");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.retweetCount = jsonObject.getInt("retweet_count");

        if (jsonObject.has("extended_entities")) {
            String type = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("type");
            if (type.equals("photo")) {
                tweet.media = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
            }
        }

        return tweet;
    }
}