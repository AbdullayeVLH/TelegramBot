package com.code.touragentbot.bots;

import com.code.touragentbot.services.BotService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class TourBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botToken;
    private final String botPath;

    private final BotService botService;

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return botService.getUpdate(update, this);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
