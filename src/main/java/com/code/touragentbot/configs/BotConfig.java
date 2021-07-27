package com.code.touragentbot.configs;

import com.code.touragentbot.bots.TourBot;
import com.code.touragentbot.services.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BotConfig {
    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.path}")
    private String botPath;

    private final BotService botService;

    @Bean
    TourBot tourBot() throws TelegramApiException {
        TourBot bot = new TourBot(botUsername, botToken, botPath, botService);
        List<BotCommand> commands = new ArrayList<>();
        bot.execute(SetWebhook.builder().url(botPath).build());
        commands.add(BotCommand.builder().command("start").description("Stat bot").build());
        commands.add(BotCommand.builder().command("stop").description("Stop bot").build());
        bot.execute(SetMyCommands.builder().commands(commands).build());
        return bot;
    }
}
