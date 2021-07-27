package com.code.touragentbot.controller;

import com.code.touragentbot.bots.TourBot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/webhook")
public class BotController {
    private final TourBot tourBot;

    public BotController(TourBot tourBot) {
        this.tourBot = tourBot;
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handlerNotFoundException(Exception exception) {
//        System.out.println(exception.getMessage());
//        return new ResponseEntity<>(exception.getMessage(), HttpStatus.OK);
//    }

    @PostMapping
    public BotApiMethod<?> onWebhookUpdateReceived(@RequestBody Update update){
//        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId())).text("salam").build();
        return tourBot.onWebhookUpdateReceived(update);
    }
}
