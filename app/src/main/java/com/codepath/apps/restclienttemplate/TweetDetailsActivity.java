package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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
    ImageView ivMedia;
    Button btnFavorite;
    Button btnRetweet;
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
        ivMedia = binding.ivMedia;
        btnFavorite = binding.btnFavorite;
        btnRetweet = binding.btnRetweet;
        id = getIntent().getLongExtra("id", 0);
        client = TwitterApp.getRestClient(this);

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
                    btnRetweet.setText("Unretweet");
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
                    btnRetweet.setText("Retweet");
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
                    btnFavorite.setText("Unfavorite");
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
                    btnFavorite.setText("Favorite");
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
                    Log.d(TAG, "onSuccess TweetDetails: " + json.jsonObject);
                    tweet = fromJson(json.jsonObject);
                    tvScreenName.setText(tweet.user.screenName);
                    tvBody.setText(tweet.body);
                    btnFavorite.setText(tweet.favorited ? "Unfavorite" : "Favorite");
                    btnRetweet.setText(tweet.retweeted ? "UnRetweet" : "Retweet");
                    Glide.with(TweetDetailsActivity.this).load(tweet.user.profileImageUrl).into(ivProfileImage);

                    if (tweet.media != null) {
                        Glide.with(TweetDetailsActivity.this).load(tweet.media).into(ivMedia);
                        ivMedia.setVisibility(View.VISIBLE);
                    } else {
                        ivMedia.setVisibility(View.GONE);
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

        if (jsonObject.has("extended_entities")) {
            String type = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("type");
            if (type.equals("photo")) {
                tweet.media = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
            }
        }

        return tweet;
    }
}