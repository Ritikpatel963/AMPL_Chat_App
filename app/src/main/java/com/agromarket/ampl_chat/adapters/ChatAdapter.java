package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.models.ChatItem;
import com.agromarket.ampl_chat.ChatScreenActivity;
import com.agromarket.ampl_chat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<ChatItem> list;
    Context context;

    public ChatAdapter(Context context, List<ChatItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_chat_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = list.get(position);

        holder.txtName.setText(item.getName());
        holder.txtMessage.setText(item.getMessage());
        holder.txtTime.setText(item.getTime());

        if(item.getUnreadCount() > 0){
            holder.txtUnread.setText(String.valueOf(item.getUnreadCount()));
            holder.txtUnread.setVisibility(View.VISIBLE);
        } else {
            holder.txtUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatScreenActivity.class);
            i.putExtra("name", item.getName());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtMessage, txtTime, txtUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnread = itemView.findViewById(R.id.txtUnread);
        }
    }
}
