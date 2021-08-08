package com.code.touragentbot.services;

import com.code.touragentbot.models.*;
import com.code.touragentbot.repositories.*;
import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Component
public class BotServiceImpl implements BotService {

    private final ActionRepository actionRepo;
    private final LocaleRepository localeRepo;
    private final QuestionRepository questionRepo;
    private final SessionRepository sessionRepo;
    private final RabbitMQService rabbitMQService;
    private final ImageService imageService;
    private final OfferRepository offerRepo;


    public BotServiceImpl(ActionRepository actionRepo, LocaleRepository localeRepo,
                          QuestionRepository questionRepo,
                          SessionRepository sessionRepo, RabbitMQService rabbitMQService, ImageService imageService, OfferRepository offerRepo) {
        this.actionRepo = actionRepo;
        this.localeRepo = localeRepo;
        this.questionRepo = questionRepo;
        this.sessionRepo = sessionRepo;
        this.rabbitMQService = rabbitMQService;
        this.imageService = imageService;
        this.offerRepo = offerRepo;
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
                return new SendMessage(update.getMessage().getChatId().toString(), "You have active order. Please stop it first by /stop command");
            }
            session = new Session();
            session = session.toBuilder().sessionId(UUID.randomUUID()).chatId(update.getMessage().getChatId()).build();
            sessionRepo.save(session);
            return askQuestions(update, bot);
        } else if (update.getMessage().getText().equals("/stop")) {
            if (sessionRepo.find(update.getMessage().getChatId()) == null) {
                return new SendMessage(update.getMessage().getChatId().toString(), "You don't have active order. Please start it first by /start command");
            }
            Session stopSession = sessionRepo.find(update.getMessage().getChatId());
            rabbitMQService.sendToStopQueue(stopSession);
            return new SendMessage(update.getMessage().getChatId().toString(), sessionRepo.delete(update.getMessage().getChatId()));
        }
        return new SendMessage(update.getMessage().getChatId().toString(), "Incorrect command");
    }

    private BotApiMethod<?> manageMessages(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        if (sessionRepo.find(update.getMessage().getChatId()) == null) {
            return new SendMessage(update.getMessage().getChatId().toString(), "You don't have active order. Please start it first by /start command");
        }

        sendAcceptOffer(update);
        return askQuestions(update, bot);
    }

    private BotApiMethod<?> askQuestions(Update update, TelegramWebhookBot bot) throws TelegramApiException {
        StringBuilder questions = new StringBuilder();
        Session session = sessionRepo.find(update.getMessage().getChatId());
        Question question;
        if (session.getAction() == null) {
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
        } else {
            if (session.getLang() == null) {
                String message = update.getMessage().getText();
                question = questionRepo.getQuestionsById(1L);
                if (message.matches(question.getRegex())) {
                    session.setLang(message);
                    session.setData(question.getKey(), message);
                }else {
                    StringBuilder errorBuilder = new StringBuilder();
                    List<Locale> locales = localeRepo.getLocalesByKey("q.error");
                    locales.forEach(locale -> errorBuilder.append(locale.getContext()).append("\n"));
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(update.getMessage().getChatId().toString());
                    errorMessage.setText(errorBuilder.toString());
                    return errorMessage;
                }
            } else {
                question = questionRepo.getQuestionsById(session.getAction().getNextId());
                if (question!=null) {
                    session.setData(question.getKey(), update.getMessage().getText());
                    session.getAction().setNextId(actionRepo.getActionByActionKeyword(question.getKey()));
                }else {
                    return new SendMessage(update.getMessage().getChatId().toString(), localeRepo.getLocaleByKeyAndLang("q.offer", session.getLang()).getContext());
                }
            }
            sessionRepo.save(session);
        }
        String lang = session.getLang();
        Long nextId = session.getAction().getNextId();
        if (nextId != null) {
            question = questionRepo.getQuestionsById(nextId);
            Locale locale = localeRepo.getLocaleByKeyAndLang(question.getKey(), lang);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText(locale.getContext());
            sendMessage.setReplyMarkup(answerButtons(lang, question.getKey()));
            bot.execute(sendMessage);
            sessionRepo.save(session);
            if (actionRepo.getActionByActionKeyword(question.getKey()) == null) {
                rabbitMQService.sendToQueue(session);
                bot.execute(new SendMessage(update.getMessage().getChatId().toString(), session.getData().toString()));
                SendMessage offerMessage = new SendMessage();
                offerMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                offerMessage.setText(localeRepo.getLocaleByKeyAndLang("q.offer", session.getLang()).getContext());
                return offerMessage;
            }
        }
        return null;
    }

    private void sendAcceptOffer(Update update){
        if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().hasPhoto()) {
            Session session = sessionRepo.find(update.getMessage().getChatId());
            Offer offer = offerRepo.getOfferByRequestId(session.getSessionId());
            Accepted acceptedOffer = Accepted.builder()
                    .requestId(offer.getRequestId())
                    .contactInfo(update.getMessage().getText())
                    .agentEmail(offer.getUserEmail())
                    .build();
            rabbitMQService.sendToAcceptedQueue(acceptedOffer);
        }
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
