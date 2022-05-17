package uz.pdp.services.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.model.absClasses.BotUser;

public interface AuthService {

    void authorize(SendMessage sendMessage, BotUser user, Update update);

    ReplyKeyboardMarkup getReplyKeyboardMarkup();
}
