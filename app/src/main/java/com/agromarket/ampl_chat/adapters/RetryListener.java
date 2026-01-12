package com.agromarket.ampl_chat.adapters;

import com.agromarket.ampl_chat.models.MessageItem;

public interface RetryListener {
    void onRetry(MessageItem item, int position);
}
