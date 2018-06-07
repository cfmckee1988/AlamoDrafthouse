package com.colinmckee.alamodrafthouse.Listeners;

import android.support.annotation.NonNull;

import com.colinmckee.alamodrafthouse.Adapters.FourSquareAdapter;
import com.colinmckee.alamodrafthouse.DataModels.FourSquare;

public interface OnFourSquareClick
{
    /**
     * Returns the FourSquare object and position of the item clicked.
     * @param fourSquare FourSquare object of the item clicked
     * @param position position in the FourSquareAdapter
     */
    void onClick(FourSquareAdapter.ViewHolder holder, @NonNull FourSquare fourSquare, int position);
}
