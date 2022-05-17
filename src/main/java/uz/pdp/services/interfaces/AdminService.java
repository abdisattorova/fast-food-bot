package uz.pdp.services.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.model.Admin;

public interface AdminService {

    void adminHasMessage(Admin user, Message text, SendMessage sendMessage, SendDocument sendDocument, SendPhoto sendPhoto);

    void adminHasCallBackQuery(Admin user, CallbackQuery callbackQuery);
}
