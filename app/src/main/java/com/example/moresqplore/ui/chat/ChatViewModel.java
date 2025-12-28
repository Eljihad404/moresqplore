package com.example.moresqplore.ui.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.moresqplore.data.model.ChatMessage;
import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.repository.GeminiChatRepository;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final GeminiChatRepository chatRepository;

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = GeminiChatRepository.getInstance();

        chatRepository.getConversationHistory().observeForever(this.messages::setValue);
        chatRepository.isLoading().observeForever(this.isLoading::setValue);
        chatRepository.getError().observeForever(this.error::setValue);
    }

    public void sendMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            chatRepository.sendMessage(message.trim(), null, null);
        }
    }

    public void sendMessageWithLocation(String message, double latitude, double longitude) {
        chatRepository.sendMessageWithLocation(message, latitude, longitude);
    }

    public void sendMessageWithPlaces(String message, List<Place> places) {
        chatRepository.sendMessageWithPlaces(message, places);
    }

    public void clearConversation() {
        chatRepository.clearConversation();
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}