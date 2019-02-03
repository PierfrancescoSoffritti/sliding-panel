package com.pierfrancescosoffritti.slidingdrawer_sample.old.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.slidingdrawer_sample.R;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.adapters.RecyclerViewAdapter;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.DummyListItems;

public class ListFragment extends Fragment {

    private static final String COLUMN_COUNT = "column-count";
    private int columnCount = 1;

    public static ListFragment newInstance(int columnCount) {
        ListFragment fragment = new ListFragment();

        Bundle args = new Bundle();
        args.putInt(COLUMN_COUNT, columnCount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            columnCount = getArguments().getInt(COLUMN_COUNT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) view;

        if (columnCount <= 1)
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        else
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnCount));

        recyclerView.setAdapter(new RecyclerViewAdapter(DummyListItems.ITEMS));

        return view;
    }
}
