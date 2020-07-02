package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.FragmentComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComposeDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComposeDialogFragment extends DialogFragment {
    public static final int MAX_TWEET_LENGTH = 280;

    private static final String TAG = "ComposeDialogFragment";
    EditText etCompose;
    TextView tvCharacters;
    Button btnTweet;
    TwitterClient client;

    public ComposeDialogFragment() {
        // Required empty public constructor
    }

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Intent i);
    }


    public static ComposeDialogFragment newInstance() {
        ComposeDialogFragment fragment = new ComposeDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etCompose = view.findViewById(R.id.etCompose);
        tvCharacters = view.findViewById(R.id.tvCharacters);
        btnTweet = view.findViewById(R.id.btnTweet);
        client  = TwitterApp.getRestClient(getActivity());

        etCompose.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                tvCharacters.setText(Integer.toString(MAX_TWEET_LENGTH - etCompose.getText().length()));
                if (Integer.parseInt(tvCharacters.getText().toString()) < 0) {
                    tvCharacters.setTextColor(Color.RED);
                }
                return false;
            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(getContext(), "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                } else if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getContext(), "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                client.publishTweet(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                            Intent i = new Intent();
                            i.putExtra("tweet", Parcels.wrap(tweet));
                            listener.onFinishComposeDialog(i);
                            dismiss();
                        } catch (JSONException e) {
                            Log.e(TAG, "onException: ", e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure: ", throwable);
                    }
                }, tweetContent);
            }
        });

    }
}