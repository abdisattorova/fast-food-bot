package uz.pdp.services;
//Sevinch Abdisattorova 01/01/2022 4:34 PM

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.model.Category;
import uz.pdp.model.Client;
import uz.pdp.model.PayType;
import uz.pdp.model.Product;
import uz.pdp.model.absClasses.BotUser;
import uz.pdp.services.interfaces.GeneralService;
import uz.pdp.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uz.pdp.util.Db.*;

public class GeneralServiceImpl implements GeneralService {


    @Override
    public BotUser getCurrentUser(long chatId, Message message) {

        for (BotUser user : userList) {
            int authRound = user.getAuthRound();
            if (user.getChatId() == chatId) {
                if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(Constants.adminPhoneNum)) {
                    admin.setStarted(true);
                    admin.setAuthRound(authRound);
                    admin.setChatId(chatId);
                    return admin;
                }
                return user;
            }
        }
        org.telegram.telegrambots.meta.api.objects.User from = message.getFrom();
        String fullName = from.getFirstName() +
                (from.getLastName() != null ? " " + from.getLastName() : "");

        Client client = new Client(fullName, chatId, false);
        userList.add(client);
        return client;
    }

    @Override
    public void checkUserIsStarted(long chatId1, SendMessage sendMessage, BotUser user, String text) {

        if (!user.isStarted() && !text.equals("/start")) {
            sendMessage.setText("Please /start first!");
            sendMessage.getText();
            try {
                user.setEditedMsgId(bot.execute(sendMessage).getMessageId());
                Thread.sleep(5000);
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId1),
                        user.getEditedMsgId());
                bot.execute(deleteMessage);
                sendMessage.setText(null);
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ReplyKeyboard getReplyKeyboard(BotUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        KeyboardRow row1 = new KeyboardRow();
        if (user.isAdmin()) {
            if (user.getCurrentRound() == 0) {
                row1.add("Category menu");
                row1.add("Order status menu");
                KeyboardRow row2 = new KeyboardRow();
                row2.add("Product menu");
                row2.add("PayType menu");
                KeyboardRow row3 = new KeyboardRow();
                row3.add("\uD83D\uDCC8 Change order status");
                row3.add("\uD83D\uDCCE Order histories");
                KeyboardRow row4 = new KeyboardRow();
                row4.add("\uD83D\uDDD2 Users list");
                row4.add("\uD83D\uDCAC Comments");
                keyboardRows.add(row1);
                keyboardRows.add(row2);
                keyboardRows.add(row3);
                keyboardRows.add(row4);
                return replyKeyboardMarkup;
            }
        } else {
            Client client = (Client) user;
            switch ((int) client.getCurrentRound()) {
                case 0:
                    row1.add("\uD83C\uDF74 Menu");
                    row1.add("\uD83D\uDECD My cart");
                    KeyboardRow row2 = new KeyboardRow();
                    row2.add("✍ Comments");
                    KeyboardRow row3 = new KeyboardRow();
                    row3.add("📝 My order histories");
                    keyboardRows.add(row1);
                    keyboardRows.add(row2);
                    keyboardRows.add(row3);
                    return replyKeyboardMarkup;
                case 1:
                    client.setCurrentRound(2);
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = categoryList.get(i);
                        row1.add(category.getName());
                        if (i % 2 == 0) {
                            keyboardRows.add(row1);
                        } else {
                            row1 = new KeyboardRow();
                        }
                    }
                    break;
                case 2:
                    client.setCurrentRound(3);
                    String selectedCategory = client.getSelectedCategory();
                    List<Product> collect = productList.stream()
                            .filter(product ->
                                    product
                                            .getCategory()
                                            .getName()
                                            .equals(selectedCategory)).
                            collect(Collectors.toList());

                    for (int i = 0; i < collect.size(); i++) {
                        Product product = collect.get(i);
                        row1.add(product.getName());
                        if (i % 2 == 0) {
                            keyboardRows.add(row1);
                        } else {
                            row1 = new KeyboardRow();
                        }
                    }
                    break;


                case 8:
                    KeyboardRow row = new KeyboardRow();
                    row.add("\uD83D\uDCCE Download check pdf");
                    keyboardRows.add(row);

                case 3:
                    row1.add("\uD83C\uDF74 Main menu");
                    row1.add("\uD83D\uDECD My cart");
                    keyboardRows.add(row1);
                    return replyKeyboardMarkup;

                case 5:
                    client.setCurrentRound(6);
                    KeyboardButton button1 = new KeyboardButton("\uD83D\uDCCD Share location");
                    button1.setRequestLocation(true);
                    row1.add(button1);
                    keyboardRows.add(row1);
                    return replyKeyboardMarkup;
                case 6:
                    for (int i = 0; i < payTypeList.size(); i++) {
                        PayType payType = payTypeList.get(i);
                        row1.add(payType.getName());
                        if (i % 2 == 0) {
                            keyboardRows.add(row1);
                        } else {
                            row1 = new KeyboardRow();
                        }
                    }
                    break;

                case 7:
                    row1.add("Ok \uD83D\uDE46");
                    keyboardRows.add(row1);
                    break;
                case 9:
                    client.setCurrentRound(0);
                    KeyboardRow r = new KeyboardRow();
                    r.add("\uD83D\uDCCE Download pdf");
                    keyboardRows.add(r);
                    return replyKeyboardMarkup;


            }
            KeyboardRow row2 = new KeyboardRow();
            row2.add("👈 Back");
            keyboardRows.add(row2);
            return replyKeyboardMarkup;

        }
        return replyKeyboardMarkup;


    }

    public InlineKeyboardMarkup replyKeyboardCancel(int count) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();

        InlineKeyboardButton b1 = new InlineKeyboardButton("Cancel ❌  " + count);
        if (count != 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        b1.setCallbackData("cancel");
        inLineBtnRow.add(b1);

        inlineKeyBtnList.add(inLineBtnRow);
        return inlineKeyboardMarkup;
    }

}
