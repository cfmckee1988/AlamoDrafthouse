package com.colinmckee.alamodrafthouse;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;

import com.colinmckee.alamodrafthouse.Adapters.FourSquareAdapter;
import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.colinmckee.alamodrafthouse.Listeners.OnFourSquareClick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * This is the core class for this library. It is the RecyclerView
 * that displays our list of messages and sets up our custom adapter
 * optimized for performance. It also aides in configuring how the message
 * list should look by configuring timestamps and their visibility along with
 * the visibility of displaying names. Additional methods have been provided
 * to give user's flexibility for using this class.
 */
public class FourSquareView extends RecyclerView
{
    private OnFourSquareClick _onFourSquareClickListener;

    // Tag for log messages
    private static final String TAG = "FourSquareView";
    // Context of application
    private Context _context;
    // List of current FourSquare items
    private List<FourSquare> _fourSquareList = new ArrayList<>();
    // Custom Adapter for the recycler view
    private FourSquareAdapter _adapter;
    // Custom LinearLayoutManager for the recycler view
    private FourSquareLayoutManager _llm;
    // Custom Typeface for font
    private Typeface _font;

    public FourSquareView(Context context) {
        super(context);
        init(context);
    }

    public FourSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FourSquareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Custom init method
     * @param context context requesting class
     */
    public void init(Context context) {
        _context = context;
        _llm = new FourSquareLayoutManager(_context);
        setLayoutManager(_llm);
        // Default message animator
        setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Helper method that initiates the FourSquareAdapter and any other
     * variables that need to be initiated with it.
     */
    private void initAdapter() {
        _adapter = new FourSquareAdapter(_context, _fourSquareList);
        // Set the apapter's custom font
        if (_font != null) {
            _adapter.setCustomFont(_font);
            // Setting it to null prevents us from repeatedly setting this font
            _font = null;
        }
        // Set the adapter's custom onClickListener
        if (_onFourSquareClickListener != null) {
            _adapter.setOnFourSquareClickListener(_onFourSquareClickListener);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        // Because we cannot prevent users from using this function, we must account for it
        // by ensuring they provide an FourSquareAdapter object. We also want to check that the
        // adapter provided isn't already equal to our current adapter to prevent thrashing of
        // messages.
        if (adapter != null && adapter instanceof FourSquareAdapter) {
            FourSquareAdapter temp = (FourSquareAdapter) adapter;
            if (_adapter != null) {
                int tempHash = temp.hashCode();
                int hash = _adapter.hashCode();
                // setAdapter was called by an external class, change our current
                // adapter if the hash are unequal
                if (tempHash != hash) {
                    _adapter = temp;
                }
            }
            else {
                // Adapter is being set for the first time
                _adapter = temp;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // changed is true any time the keyboard is opened or closed
        if (changed && !_fourSquareList.isEmpty()) {
        //    smoothScrollToPosition(_fourSquareList.size()-1);
        }
    }

    /**
     * Provide a custom font for FourSquareView TextViews.
     * @param font custom font
     */
    public void setCustomFont(@NonNull Typeface font) {
        if (_adapter != null) {
            _adapter.setCustomFont(font);
        }
        else {
            // Adapter hasn't been set yet, store the font
            // for now.
            _font = font;
        }
    }

    /**
     * Provide a custom click event listener.
     * @param listener click event listener
     */
    public void setOnFourSquareClickListener(@NonNull OnFourSquareClick listener) {
        if (_adapter != null) {
            _adapter.setOnFourSquareClickListener(listener);
        }
        else {
            // In case this is called before the adapter has been set
            _onFourSquareClickListener = listener;
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout)
    {
        super.setLayoutManager(layout);
    }

    /**
     * Provide a list of FourSquare objects
     * @param list list of all current FourSquare objects
     */
    public void setFourSquareList(@NonNull List<FourSquare> list) {
        _fourSquareList = list;
        initAdapter();
        setAdapter(_adapter);
    }

    public void updateFourSquareItem(FourSquare fs) {
        if (_adapter == null) return;

        for (int i = 0; i < _fourSquareList.size(); i++) {
            FourSquare fsl = _fourSquareList.get(i);
            if (fsl.getId().equals(fs.getId())) {
                fsl.setIconURI(fs.getIconURI());
                fsl.setUrl(fs.getUrl());
                _adapter.notifyItemChanged(i);
                break;
            }
        }
    }


    /**
     * Clear all items
     * @return true if items were cleared, false otherwise
     */
    public boolean clearAllItems() {
        if (!_fourSquareList.isEmpty() && _adapter != null) {
            int size = _fourSquareList.size();
            _fourSquareList.clear();
            _adapter.notifyItemRangeRemoved(0, size);
            return true;
        }
        return false;
    }

    /**
     * Determine if an animation should occur.
     * @param position index position of the last item before new item add
     * @return true if the adapter should animate, false otherwise
     */
    private boolean shouldAnimateNewItem(int position) {
        int lastVisibleItem = _llm.findLastVisibleItemPosition();
        return position < 0 || position == lastVisibleItem;
    }
}

