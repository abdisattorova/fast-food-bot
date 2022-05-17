package uz.pdp.services;
//Sevinch Abdisattorova 01/01/2022 4:03 PM

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.model.Admin;
import uz.pdp.model.Client;
import uz.pdp.model.absClasses.BotUser;
import uz.pdp.services.interfaces.BotService;

import static uz.pdp.util.Db.*;

public class BotServiceImpl implements BotService {
    @Override
    public void updateReceived(Update update) {

        if (update.hasMessage()) {
            updateHasMessage(update);
        }
        if (update.hasCallbackQuery()) {
            updateHasCallBackQuery(update);
        }
    }

    @Override
    public void updateHasMessage(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));

        String text = message.getText();


        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));


        BotUser user = generalService.getCurrentUser(chatId, message);
        if (message.hasContact()) {
            messageHasContact(user, message, sendMessage, update);
            return;
        }
        if (user.getCurrentRound() == null) {
            if (update.getMessage().getText().equals("/start"))
                user.setAuthRound(1);
            authService.authorize(sendMessage, user, update);
            return;
        }

        if (user.isAdmin())
            adminService.adminHasMessage(((Admin) user), message, sendMessage, sendDocument, sendPhoto);
        else
            clientService.clientHasMessage(((Client) user), message, text, sendMessage, sendDocument, sendPhoto);


    }

    @Override
    public void updateHasCallBackQuery(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        long chatId = callbackQuery.getMessage().getChatId();
        int msgId = callbackQuery.getMessage().getMessageId();

        String text = callbackQuery.getMessage().getCaption();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        BotUser user = generalService.getCurrentUser(chatId, callbackQuery.getMessage());
        generalService.checkUserIsStarted(chatId, sendMessage, user, text);

        if (user.isAdmin())
            adminService.adminHasCallBackQuery(((Admin) user), callbackQuery);
        clientService.clientHasCallBackQuery(((Client) user), callbackQuery);

    }

    @Override
    public void messageHasContact(BotUser user, Message message, SendMessage sendMessage, Update update) {
        Contact contact = message.getContact();
        user.setPhoneNumber(contact.getPhoneNumber());
        user.setAuthRound(2);
        authService.authorize(sendMessage, user, update);
    }


}
