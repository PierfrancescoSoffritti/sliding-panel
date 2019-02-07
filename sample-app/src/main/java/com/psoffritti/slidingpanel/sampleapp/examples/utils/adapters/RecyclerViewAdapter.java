package com.psoffritti.slidingpanel.sampleapp.examples.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.psoffritti.slidingpanel.sampleapp.R;
import com.psoffritti.slidingpanel.sampleapp.examples.utils.DummyListItems;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<DummyListItems.DummyItem> items;

    public RecyclerViewAdapter(List<DummyListItems.DummyItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_sample_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final DummyListItems.DummyItem item = items.get(position);
        holder.item = item;
        holder.view.setText(item.content);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Clicked: " +item.content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView view;
        DummyListItems.DummyItem item;

        ViewHolder(View view) {
            super(view);
            this.view = (TextView) view;
        }
    }
}
