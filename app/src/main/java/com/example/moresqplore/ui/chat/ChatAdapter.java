package com.example.moresqplore.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moresqplore.R;
import com.example.moresqplore.data.model.ChatMessage;
import io.noties.markwon.Markwon;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;
    private static final int VIEW_TYPE_LOADING = 3;

    private List<ChatMessage> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Markwon markwon;

    public ChatAdapter(Markwon markwon) {
        this.markwon = markwon;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);

        if (message.isLoading()) {
            return VIEW_TYPE_LOADING;
        } else if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ASSISTANT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageHolder(view);
        } else if (viewType == VIEW_TYPE_ASSISTANT) {
            View view = inflater.inflate(R.layout.item_chat_assistant, parent, false);
            return new AssistantMessageHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_loading, parent, false);
            return new LoadingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageHolder) {
            ((UserMessageHolder) holder).bind(message, markwon);
        } else if (holder instanceof AssistantMessageHolder) {
            ((AssistantMessageHolder) holder).bind(message, markwon);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMessageHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final TextView textTime;

        UserMessageHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessage message, Markwon markwon) {
            // Use Markwon to render markdown
            if (markwon != null) {
                markwon.setMarkdown(textMessage, message.getContent());
            } else {
                textMessage.setText(message.getContent());
            }
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textTime.setText(format.format(message.getTimestamp()));
        }
    }

    static class AssistantMessageHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final TextView textTime;

        AssistantMessageHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessage message, Markwon markwon) {
            // Use Markwon to render markdown
            if (markwon != null) {
                markwon.setMarkdown(textMessage, message.getContent());
            } else {
                textMessage.setText(message.getContent());
            }
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textTime.setText(format.format(message.getTimestamp()));
        }
    }

    static class LoadingHolder extends RecyclerView.ViewHolder {
        LoadingHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
