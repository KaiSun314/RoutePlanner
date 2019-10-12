package com.SunDragon.RoutePlanner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsViewHolder> {

    private MapsActivity mActivity;
    private ArrayList<String> mPlaceDescriptions;
    private ArrayList<String> mPlaceDescriptionsCache = new ArrayList<>();
    private ArrayList<String> mPlaceIds;
    private SearchView mSearchView;

    public SearchSuggestionsAdapter(MapsActivity activity, ArrayList<String> placeDescriptions,
                                    ArrayList<String> placeIds,
                                    SearchView searchView) {
        mActivity = activity;
        mPlaceDescriptions = placeDescriptions;
        mPlaceIds = placeIds;
        mSearchView = searchView;
    }

    @NonNull
    @Override
    public SearchSuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.search_suggestion_view, viewGroup, false);

        return new SearchSuggestionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchSuggestionsViewHolder viewHolder,
                                 int position) {
        viewHolder.mSearchResult.setText(mPlaceDescriptions.get(position));

        if (TextUtils.isEmpty(mPlaceIds.get(position))) {
            viewHolder.mPlaceType.setImageResource(R.drawable.ic_search_grey);
        } else {
            viewHolder.mPlaceType.setImageResource(R.drawable.ic_place_grey);
        }

        viewHolder.mSearchResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setQuery(mPlaceDescriptions.get(viewHolder.getAdapterPosition()), true);
                mActivity.mPlaceIdsAdd = mPlaceIds.get(viewHolder.getAdapterPosition());
            }
        });

        viewHolder.mCopySuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setQuery(mPlaceDescriptions.get(viewHolder.getAdapterPosition()), false);
                mActivity.mPlaceIdsAdd = mPlaceIds.get(viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaceDescriptions.size();
    }

    public void setPlaceDescription(List<String> data, boolean expanded) {
        if (expanded) {
            mPlaceDescriptions.clear();
            mPlaceDescriptions.addAll(data);
        } else {
            mPlaceDescriptionsCache.clear();
            mPlaceDescriptionsCache.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setPlaceIds(List<String> placeIds) {
        mPlaceIds.clear();
        mPlaceIds.addAll(placeIds);
    }

    public void expandLess() {
        mPlaceDescriptionsCache.clear();
        mPlaceDescriptionsCache.addAll(mPlaceDescriptions);
        mPlaceDescriptions.clear();

        notifyItemRangeRemoved(0, mPlaceDescriptionsCache.size());
    }

    public void expandMore() {
        mPlaceDescriptions.clear();
        mPlaceDescriptions.addAll(mPlaceDescriptionsCache);

        notifyItemRangeInserted(0, mPlaceDescriptions.size());
    }
}
