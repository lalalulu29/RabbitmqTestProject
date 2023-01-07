package ru.kirill98.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kirill98.service.UpdateProducer;
import ru.kirill98.utils.MessageUtils;

import static ru.kirill98.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if(update == null) {
            log.error("Received update is null");
            return;
        }
        if(update.hasMessage()) {
            distributeMessagesByType(update);
        } else {
            log.error(String.format("Received unsupported message type: %s", update));
        }
    }

    private void distributeMessagesByType(Update update) {
        Message message = update.getMessage();
        if(message.hasText()) {
            processTextMessage(update);
        } else {
            sendUnsupportedMessageType(update);
        }
    }

    private void sendUnsupportedMessageType(Update update) {
        SendMessage message = messageUtils.generateSendMessageWithText(update, "Неподдерживаемый тип сообщения");
        setView(message);
    }


    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    public void setView(SendMessage message) {
        telegramBot.sendAnswerMessage(message);
    }
}
