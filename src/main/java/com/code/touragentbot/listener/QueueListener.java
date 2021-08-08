package com.code.touragentbot.listener;

import com.code.touragentbot.configs.RabbitMQConfig;
import com.code.touragentbot.models.Offer;
import com.code.touragentbot.models.SessionDB;
import com.code.touragentbot.repositories.OfferRepository;
import com.code.touragentbot.repositories.SessionRepository;
import com.code.touragentbot.services.ImageService;
import com.code.touragentbot.services.RabbitMQService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;


@Component
public class QueueListener {

    private final OfferRepository offerRepo;
    private final SessionRepository sessionRepo;
    private final ImageService imageService;
    private final RabbitMQService rabbitMQService;
    private final TelegramWebhookBot bot;

    public QueueListener(OfferRepository offerRepo, SessionRepository sessionRepo, ImageService imageService, RabbitMQService rabbitMQService, TelegramWebhookBot bot) {
        this.offerRepo = offerRepo;
        this.sessionRepo = sessionRepo;
        this.imageService = imageService;
        this.rabbitMQService = rabbitMQService;
        this.bot = bot;
    }

    @RabbitListener(queues = RabbitMQConfig.OFFER_QUEUE)
    public void offerListener(Offer offer) {
        Offer listenedOffer = Offer.builder()
                .id(offer.getId())
                .dateInterval(offer.getDateInterval())
                .tourInformation(offer.getTourInformation())
                .price(offer.getPrice())
                .requestId(offer.getRequestId())
                .userEmail(offer.getUserEmail())
                .build();
        offerRepo.save(listenedOffer);
        sendOffers(listenedOffer);
    }

    public void sendOffers(Offer offer) {
        Optional<SessionDB> session = sessionRepo.findBySessionId(offer.getRequestId());
        if (session.isPresent()) {
            try {
                sendImage(offer, session.get());
            } catch (FileNotFoundException | JRException | TelegramApiException e) {
                e.printStackTrace();
            }

        }

    }

    private void sendImage(Offer offer, SessionDB session) throws FileNotFoundException, JRException, TelegramApiException {
        SendPhoto picture = new SendPhoto();
        picture.setChatId(String.valueOf(session.getChatId()));
        File file = imageService.convertOfferToImage(offer);
        InputFile inputFile = new InputFile();
        inputFile.setMedia(file);
        picture.setPhoto(inputFile);
        bot.execute(picture);
        file.deleteOnExit();
    }


}
