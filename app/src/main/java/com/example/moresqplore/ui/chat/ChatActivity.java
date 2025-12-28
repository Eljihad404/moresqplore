package com.example.moresqplore.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupInputArea();
        setupQuickActions();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle("Atlas AI Assistant");
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        binding.recyclerViewMessages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewMessages.setAdapter(chatAdapter);

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                binding.recyclerViewMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void setupInputArea() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                binding.editTextMessage.setText("");
            }
        });
    }

    private void setupQuickActions() {
        binding.btnQuickAsk1.setOnClickListener(v -> {
            viewModel.sendMessage("What are the best monuments to visit in Morocco?");
        });

        binding.btnQuickAsk2.setOnClickListener(v -> {
            viewModel.sendMessage("How do I travel between cities in Morocco? What are the options?");
        });

        binding.btnQuickAsk3.setOnClickListener(v -> {
            viewModel.sendMessage("What traditional Moroccan food should I try?");
        });

        binding.btnQuickAsk4.setOnClickListener(v -> {
            viewModel.sendMessage("Give me some travel tips for Morocco");
        });
    }

    private void setupObservers() {
        viewModel.getMessages().observe(this, messages -> {
            if (messages != null) {
                chatAdapter.setMessages(messages);
                binding.emptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!isLoading);

            if (isLoading) {
                binding.textTyping.setVisibility(View.VISIBLE);
            } else {
                binding.textTyping.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}