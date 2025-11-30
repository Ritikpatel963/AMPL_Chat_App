package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.content.Intent;
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

    private List<ChatItem> list;
    private List<ChatItem> filteredList;
    private Context context;

    public interface OnSearchResultListener {
        void onSearchResult(int count);
    }

    private OnSearchResultListener searchListener;

    public void setOnSearchResultListener(OnSearchResultListener listener) {
        this.searchListener = listener;
    }

    public ChatAdapter(Context context, List<ChatItem> list) {
        this.context = context;
        this.list = list;
        this.filteredList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_chat_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        ChatItem item = filteredList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtMessage.setText(item.getLastMessage());
        holder.txtTime.setText(item.getTime());

        if (item.getUnreadCount() > 0) {
            holder.txtUnread.setText(String.valueOf(item.getUnreadCount()));
            holder.txtUnread.setVisibility(View.VISIBLE);
        } else {
            holder.txtUnread.setVisibility(View.GONE);
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
        return filteredList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtMessage, txtTime, txtUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnread = itemView.findViewById(R.id.txtUnread);
        }
    }

    public void updateList(List<ChatItem> newList) {
        list.clear();
        list.addAll(newList);

        filteredList.clear();
        filteredList.addAll(newList);

        notifyDataSetChanged();

        if (searchListener != null) {
            searchListener.onSearchResult(filteredList.size());
        }
    }

    public void filter(String text) {
        filteredList.clear();

        if (text.isEmpty()) {
            filteredList.addAll(list);
        } else {
            text = text.toLowerCase();
            for (ChatItem c : list) {
                if (c.getName().toLowerCase().contains(text)) {
                    filteredList.add(c);
                }
            }
        }

        notifyDataSetChanged();

        if (searchListener != null) {
            searchListener.onSearchResult(filteredList.size());
        }
    }
}