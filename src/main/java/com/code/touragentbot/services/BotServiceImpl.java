package com.code.touragentbot.services;

import com.code.touragentbot.models.*;
import com.code.touragentbot.repositories.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Component
public class BotServiceImpl implements BotService {

    private final ActionRepository actionRepo;
    private final LocaleRepository localeRepo;
    private final QuestionRepository questionRepo;
    private final SessionRepository sessionRepo;
    private final RabbitMQService rabbitMQService;


    public BotServiceImpl(ActionRepository actionRepo, LocaleRepository localeRepo,
                          QuestionRepository questionRepo,
                          SessionRepository sessionRepo, RabbitMQService rabbitMQService) {
        this.actionRepo = actionRepo;
        this.localeRepo = localeRepo;
        this.questionRepo = questionRepo;
        this.sessionRepo = sessionRepo;
        this.rabbitMQService = rabbitMQService;
    }

    @Override
    public BotApiMethod<?> getUpdate(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        if (update.getMessage().getText().startsWith("/")) {
            return manageCommands(update, bot);
        }

            return manageMessages(update, bot);
    }

    private BotApiMethod<?> manageCommands(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        Session session;
        if (update.getMessage().getText().equals("/start")) {
            if (sessionRepo.find(update.getMessage().getChatId()) != null) {
                System.out.println("sending /start");
                return new SendMessage(update.getMessage().getChatId().toString(), "You have active order. Please stop it first by /stop command");
            }
            session = new Session();
            session = session.toBuilder().chatId(update.getMessage().getChatId()).build();
            sessionRepo.save(session);
            return askQuestions(update, bot);
        } else if (update.getMessage().getText().equals("/stop")) {
            if (sessionRepo.find(update.getMessage().getChatId()) == null) {
                return new SendMessage(update.getMessage().getChatId().toString(), "You don't have active order. Please start it first by /start command");
            }
            rabbitMQService.sendToStopQueue(sessionRepo.find(update.getMessage().getChatId()));
            return new SendMessage(update.getMessage().getChatId().toString(), sessionRepo.delete(update.getMessage().getChatId()));
        }
        return new SendMessage(update.getMessage().getChatId().toString(), "Incorrect command");
    }

    private BotApiMethod<?> manageMessages(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        if (sessionRepo.find(update.getMessage().getChatId())==null) {
            return new SendMessage(update.getMessage().getChatId().toString(), "You don't have active order. Please start it first by /start command");
        }
        return askQuestions(update, bot);
    }

    private BotApiMethod<?> askQuestions(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        StringBuilder questions = new StringBuilder();
        Session session = sessionRepo.find(update.getMessage().getChatId());
        Question question;
        if (session.getAction()==null) {
            question = questionRepo.getQuestionsById(1L);
            List<Locale> locales = localeRepo.getQuestionsByLangAndKey("empty", question.getKey());
            locales.forEach(locale -> questions.append(locale.getContext()).append("\n"));
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText(questions.toString());
            sendMessage.setReplyMarkup(answerButtons("empty", question.getKey()));
            bot.execute(sendMessage);
            session.setAction(new Action());
            session.getAction().setNextId(actionRepo.getActionByActionKeyword(question.getKey()));
            sessionRepo.save(session);
            return null;
        }else{
            if (session.getLang()==null){
                question = questionRepo.getQuestionsById(1L);
                session.setLang(update.getMessage().getText());
                session.setData(question.getKey(), update.getMessage().getText());
            }else{
                question = questionRepo.getQuestionsById(session.getAction().getNextId());
                session.setData(question.getKey(), update.getMessage().getText());
                session.getAction().setNextId(actionRepo.getActionByActionKeyword(question.getKey()));
            }
            sessionRepo.save(session);
        }
        String lang = session.getLang();
        Long nextId = session.getAction().getNextId();
        if (nextId != null) {
            question = questionRepo.getQuestionsById(nextId);
            Locale locale=localeRepo.getLocaleByKeyAndLang(question.getKey(),lang);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText(locale.getContext());
            sendMessage.setReplyMarkup(answerButtons(lang, question.getKey()));
            bot.execute(sendMessage);
            sessionRepo.save(session);
            if (actionRepo.getActionByActionKeyword(question.getKey())==null){
                rabbitMQService.sendToQueue(session);
                return new SendMessage(update.getMessage().getChatId().toString(), session.getData().toString());
            }
        }
        return null;
    }

    private ReplyKeyboardMarkup answerButtons(String lang, String key) {
        List<Locale> locales = localeRepo.getActionsByLangAndKey(lang, key);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (Locale locale : locales) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton();
            button.setText(locale.getContext());
            row.add(button);
            keyboardRows.add(row);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
