package com.code.touragentbot.services;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotService {
    BotApiMethod<?> getUpdate(Update update, TelegramWebhookBot bot) throws TelegramApiException;

}
