package com.SunDragon.RoutePlanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchSuggestionsViewHolder extends RecyclerView.ViewHolder {

    TextView mSearchResult;
    ImageView mPlaceType;
    ImageView mCopySuggestion;

    public SearchSuggestionsViewHolder(@NonNull View itemView) {
        super(itemView);

        mSearchResult = itemView.findViewById(R.id.suggestedLocation);
        mPlaceType = itemView.findViewById(R.id.placeType);
        mCopySuggestion = itemView.findViewById(R.id.copySuggestion);
    }
}
