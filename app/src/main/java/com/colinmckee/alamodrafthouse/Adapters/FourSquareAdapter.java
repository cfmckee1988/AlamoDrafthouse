package com.colinmckee.alamodrafthouse.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.colinmckee.alamodrafthouse.Listeners.OnFourSquareClick;
import com.colinmckee.alamodrafthouse.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class FourSquareAdapter extends RecyclerView.Adapter<FourSquareAdapter.ViewHolder>
{
    private static final int NOT_FAVORITED_ITEM = 0;
    private static final int FAVORITED_ITEM = 1;

    private Context _context;
    private Drawable _favorite;
    private Drawable _notFavorite;
    // List of FourSquare items for the adapter
    private List<FourSquare> _fourSquareList = new ArrayList<>();
    // LayoutInflator for ViewHolder
    private LayoutInflater _inflater;
    // Typeface for custom font
    private Typeface _font;
    // OnClickListener for Adapter items
    private OnFourSquareClick _onFourSquareClickListener;
    // OnLongClickListener for Adapter items
    private DisplayImageOptions _options;
    private ImageLoadingListener _animateFirstListener = new AnimateFirstDisplayListener();

    /**
     * Custom adapter for setting up list's cells
     * @param context context of calling class
     * @param fourSquares list of FourSquare results
     */
    public FourSquareAdapter(@NonNull Context context, @NonNull List<FourSquare> fourSquares) {
        // More efficient to initialize the inflater once rather than every time
        // onCreateViewHolder is called.
        _context = context;
        _inflater = LayoutInflater.from(context);
        _fourSquareList = fourSquares;

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());

        _options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.gray_bg)
                .showImageForEmptyUri(R.drawable.gray_bg)
                .showImageOnFail(R.drawable.gray_bg)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();

        Resources resources = _context.getResources();
        final int favoriteId = resources.getIdentifier("baseline_favorite", "drawable",
                _context.getPackageName());
        final int notFavoriteId = resources.getIdentifier("baseline_favorite_border", "drawable",
                _context.getPackageName());
        _favorite = resources.getDrawable(favoriteId, null);
        _notFavorite = resources.getDrawable(notFavoriteId, null);

    }

    /**
     * Custom ViewHolder class
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        RelativeLayout container;
        ImageView icon;
        TextView name;
        TextView category;
        TextView distance;
        ImageView favorite;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            category = (TextView) itemView.findViewById(R.id.category);
            distance = (TextView) itemView.findViewById(R.id.distance);
            favorite = (ImageView) itemView.findViewById(R.id.favorite);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition(); // gets item position
                    if (position != RecyclerView.NO_POSITION && _fourSquareList.size() > position) {
                        FourSquare fs = _fourSquareList.get(position);
                        if (_onFourSquareClickListener != null) _onFourSquareClickListener.onClick(ViewHolder.this, fs, position);
                    }
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Log.d("Favorite", "Favorited position "+position);
                    if (position != RecyclerView.NO_POSITION && _fourSquareList.size() > position) {
                        FourSquare fs = _fourSquareList.get(position);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                        if (fs.getIsFavorited()) {
                            fs.setIsFavorited(false);
                            prefs.edit().putBoolean("fav_"+fs.getId(), false).commit();
                        } else {
                            fs.setIsFavorited(true);
                            prefs.edit().putBoolean("fav_"+fs.getId(), true).commit();
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * Provide a custom font for text in the ViewHolders
     * @param font custom font
     */
    public void setCustomFont(@NonNull Typeface font) {
        _font = font;
    }

    /**
     * Provide a custom click event listener.
     * @param listener click event listener
     */
    public void setOnFourSquareClickListener(@NonNull OnFourSquareClick listener) {
        _onFourSquareClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        // This is done to increase the performance of the adapter, as this allows
        // us to position the layout of the view inside onCreateViewHolder rather than
        // in onBindViewHolder.
        FourSquare fs = _fourSquareList.get(position);

        if (fs != null && fs.getIsFavorited()) {
            return FAVORITED_ITEM;
        }

        return NOT_FAVORITED_ITEM;
    }

    @Override
    public int getItemCount() {
        return _fourSquareList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Return a new holder instance
        final ViewHolder holder;

        switch (viewType) {
            case FAVORITED_ITEM: {
                View v = _inflater.inflate(R.layout.foursquare_cell, parent, false);
                holder = new ViewHolder(v);
                break;
            }
            case NOT_FAVORITED_ITEM: {
                View v = _inflater.inflate(R.layout.foursquare_cell, parent, false);
                holder = new ViewHolder(v);
                break;
            }
            default: {
                View v = _inflater.inflate(R.layout.foursquare_cell, parent, false);
                holder = new ViewHolder(v);
                break;
            }
        }

        // Set the custom font here if provided
        if (_font != null) holder.name.setTypeface(_font);
        if (_font != null) holder.category.setTypeface(_font);
        if (_font != null) holder.distance.setTypeface(_font);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FourSquare fourSquare = _fourSquareList.get(position);
        switch (holder.getItemViewType()) {
            case FAVORITED_ITEM: {
                holder.favorite.setImageDrawable(_favorite);
                break;
            }
            case NOT_FAVORITED_ITEM: {
                holder.favorite.setImageDrawable(_notFavorite);
                break;
            }
            default: {
                holder.favorite.setImageDrawable(_notFavorite);
                break;
            }
        }

        holder.name.setText(fourSquare.getName());
        holder.category.setText(fourSquare.getCategory());
        holder.distance.setText(fourSquare.getDistance());

        if (fourSquare.getIconURI() != null) {
            ImageLoader.getInstance().displayImage(fourSquare.getIconURI(), holder.icon, _options, _animateFirstListener);
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
