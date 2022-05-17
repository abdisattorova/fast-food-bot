package uz.pdp.services.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.model.absClasses.BotUser;

public interface GeneralService {

    BotUser getCurrentUser(long chatId, Message message);

    void checkUserIsStarted(long chatId1, SendMessage sendMessage, BotUser currentUser, String text);

    ReplyKeyboard getReplyKeyboard(BotUser user);
}
