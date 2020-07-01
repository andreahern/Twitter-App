package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    private static final String TAG = "TweetsAdapter";
    Context context;
    List<Tweet> tweets;
    TwitterClient client;


    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        client = TwitterApp.getRestClient(context);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvDate;
        ImageView ivMedia;
        ImageView ivReply;
        ImageView ivRetweet;
        ImageView ivFavorite;
        TextView tvFavoriteCount;
        TextView tvRetweetCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            itemView.setOnClickListener(this);
        }

        public void bind(final Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvDate.setText(tweet.relativeDate);
            tvFavoriteCount.setText(Integer.toString(tweet.favoriteCount));
            tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);

            if (tweet.media != null) {
                Glide.with(context).load(tweet.media).into(ivMedia);
                ivMedia.setVisibility(View.VISIBLE);
            } else {
                ivMedia.setVisibility(View.GONE);
            }

            ivReply.setColorFilter(ContextCompat.getColor(context, R.color.inline_action));

            if (tweet.favorited) {
                Glide.with(context).load(R.drawable.ic_vector_heart).into(ivFavorite);
                ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.medium_red));
            }
            else {
                Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavorite);
                ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.inline_action));
            }

            if (tweet.retweeted) {
                Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                ivRetweet.setColorFilter(ContextCompat.getColor(context, R.color.medium_green));
            }
            else {
                Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                ivRetweet.setColorFilter(ContextCompat.getColor(context, R.color.inline_action));
            }

            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tweet.retweeted) {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            client.createRetweet(new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                        tweet.retweeted = true;
                                        tweet.retweetCount++;
                                        notifyItemChanged(getAdapterPosition());
                                        Glide.with(context).load(R.drawable.ic_vector_retweet).into(ivRetweet);
                                        ivRetweet.setColorFilter(ContextCompat.getColor(context, R.color.medium_green));
                                        Log.d(TAG, "onSuccess Create");
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.d(TAG, "onFailure Create");
                                }
                            }, tweet.id);
                        }
                    } else {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            client.destroyRetweet(new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                        tweet.retweeted = false;
                                        tweet.retweetCount--;
                                        notifyItemChanged(getAdapterPosition());
                                        Glide.with(context).load(R.drawable.ic_vector_retweet_stroke).into(ivRetweet);
                                        ivRetweet.setColorFilter(ContextCompat.getColor(context, R.color.inline_action));
                                        Log.d(TAG, "onSuccess Destroy");
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.d(TAG, "onFailure Destroy");
                                }
                            }, tweet.id);
                        }
                    }
                }
            });

            ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tweet.favorited) {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            client.createFavorite(new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    tweet.favorited = true;
                                    tweet.favoriteCount++;
                                    notifyItemChanged(getAdapterPosition());
                                    Glide.with(context).load(R.drawable.ic_vector_heart).into(ivFavorite);
                                    ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.medium_red));
                                    Log.d(TAG, "onSuccess Create");
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.d(TAG, "onFailure Create");
                                }
                            }, tweet.id);
                        }
                    } else {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            client.destroyFavorite(new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    tweet.favorited = false;
                                    tweet.favoriteCount--;
                                    notifyItemChanged(getAdapterPosition());
                                    Glide.with(context).load(R.drawable.ic_vector_heart_stroke).into(ivFavorite);
                                    ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.inline_action));
                                    Log.d(TAG, "onSuccess Destroy");
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.d(TAG, "onFailure Destroy");
                                }
                            }, tweet.id);
                        }
                    }
                }
            });
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailsActivity.class);
                i.putExtra("id", tweet.id);
                context.startActivity(i);
            }
        }
    }

}
