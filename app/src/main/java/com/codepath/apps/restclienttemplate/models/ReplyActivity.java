package com.codepath.apps.restclienttemplate.models;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityReplyBinding;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {
    private static final String TAG = "ReplyActivity";

    ActivityReplyBinding binding;
    EditText etCompose;
    Button btnReply;
    TextView tvCharacters;

    long id;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        etCompose = binding.etCompose;
        btnReply = binding.btnReply;
        tvCharacters = binding.tvCharacters;

        id = getIntent().getLongExtra("id", 0);
        username = getIntent().getStringExtra("username");

        Log.d(TAG, "onCreate: " + id + " " + username);
        etCompose.setText("@" + username + " ");
    }

    public void onReply(View view) {
        TwitterClient client = TwitterApp.getRestClient(this);

        client.reply(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    Log.i(TAG, "onSuccess: " + tweet);
                    Intent i = new Intent();
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    setResult(30, i);
                    finish();
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: ", throwable);
            }
        }, id, etCompose.getText().toString());
    }
}