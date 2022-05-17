package uz.pdp.services.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.model.absClasses.BotUser;

public interface BotService {

    void updateReceived(Update update);

    void updateHasMessage(Update update);

    void updateHasCallBackQuery(Update update);

    void messageHasContact(BotUser user, Message message, SendMessage sendMessage,Update update);


}
