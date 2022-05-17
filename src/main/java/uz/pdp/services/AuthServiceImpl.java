package uz.pdp.services;
//Sevinch Abdisattorova 01/06/2022 3:52 PM

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.model.absClasses.BotUser;
import uz.pdp.services.interfaces.AuthService;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.util.Constants.textChoose;
import static uz.pdp.util.Constants.twilioRegisteredNum;
import static uz.pdp.util.Db.*;

public class AuthServiceImpl implements AuthService {


    @Override
    public void authorize(SendMessage sendMessage, BotUser user, Update update) {
        Long chatId = update.getMessage().getChatId();
        Message message = update.getMessage();

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));

        String text = message.getText();

        switch (user.getAuthRound()) {
            case 1:
                user.setStarted(true);
                sendMessage.setText("Please share your contact: ");
                sendMessage.setReplyMarkup(authService.getReplyKeyboardMarkup());
                break;
            case 2:
                user.setAuthRound(3);
                String sendCode = TwilioService.sendCode(user);
                if (user.getPhoneNumber().equals(twilioRegisteredNum))
                    sendMessage.setText("Please enter the code in the message sent to your phone ");
                else
                    sendMessage.setText("Please enter the code in the message sent to your phone : " + TwilioService.code);

                break;
            case 3:
                boolean verifiedCode = TwilioService.getVerifiedCode(update, user);
                if (verifiedCode) {
                    BotUser currentUser = generalService.getCurrentUser(user.getChatId(), update.getMessage());
                    currentUser.setCurrentRound(0);
                    if (currentUser.isAdmin()) {
                        admin.setCurrentRound(0);
                        sendMessage.setText(textChoose);
                        admin.setStarted(true);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                    } else {
                        user.setCurrentRound(1);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        sendMessage.setText(textChoose);
                    }
                    break;
                } else {
                    sendMessage.setText(
                            "Confirmation message is wrong, please, enter again❗");
                    try {
                        bot.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    sendMessage.setText("We don't have token to" +
                            "real SMS service so we send code to bot : " + TwilioService.code);
                    break;
                }
            default:
                generalService.checkUserIsStarted(chatId, sendMessage, admin, text);
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    @Override
    public ReplyKeyboardMarkup getReplyKeyboardMarkup() {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton button = new KeyboardButton("☎ Share contact");
        button.setRequestContact(true);
        row1.add(button);
        keyboardRows.add(row1);
        return replyKeyboardMarkup;


    }
}
