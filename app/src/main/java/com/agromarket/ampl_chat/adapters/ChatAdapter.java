package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.ChatScreenActivity;
import com.agromarket.ampl_chat.R;
import com.agromarket.ampl_chat.models.ChatItem;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatItem> originalList = new ArrayList<>();
    private final List<ChatItem> displayList = new ArrayList<>();
    private final Context context;

    public interface OnSearchResultListener {
        void onSearchResult(int count);
    }

    private OnSearchResultListener searchListener;

    public void setOnSearchResultListener(OnSearchResultListener listener) {
        this.searchListener = listener;
    }

    public ChatAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.row_chat_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = displayList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtMessage.setText(item.getLastMessage());
        holder.txtTime.setText(item.getTime());

        if (item.getUnreadCount() > 0) {
            holder.txtUnread.setVisibility(View.VISIBLE);
            holder.txtUnread.setText(String.valueOf(item.getUnreadCount()));

            holder.txtName.setTypeface(null, Typeface.BOLD);
            holder.txtMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.txtUnread.setVisibility(View.GONE);

            holder.txtName.setTypeface(null, Typeface.NORMAL);
            holder.txtMessage.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatScreenActivity.class);
            i.putExtra("customer_id", item.getCustomerId());
            i.putExtra("name", item.getName());
            i.putExtra("email", item.getEmail());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtMessage, txtTime, txtUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnread = itemView.findViewById(R.id.txtUnread);
        }
    }

    // Replace entire dataset
    public void submitList(List<ChatItem> newList) {
        originalList.clear();
        originalList.addAll(newList);

        displayList.clear();
        displayList.addAll(newList);

        notifyDataSetChanged();
        notifySearchResult();
    }

    // Filter safely
    public void filter(String query) {
        displayList.clear();

        if (query.isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String lower = query.toLowerCase();
            for (ChatItem item : originalList) {
                if (item.getName().toLowerCase().contains(lower)) {
                    displayList.add(item);
                }
            }
        }

        notifyDataSetChanged();
        notifySearchResult();
    }

    private void notifySearchResult() {
        if (searchListener != null) {
            searchListener.onSearchResult(displayList.size());
        }
    }
}