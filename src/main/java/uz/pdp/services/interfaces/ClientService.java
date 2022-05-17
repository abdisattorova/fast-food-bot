package uz.pdp.services.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.model.*;

import java.util.List;

public interface ClientService {

    void clientHasMessage(Client user, Message message, String text, SendMessage sendMessage, SendDocument sendDocument, SendPhoto sendPhoto);

    void clientHasCallBackQuery(Client client, CallbackQuery callbackQuery);

    String showMyCart(Client currentUser);

    String info(Product chosenProduct);

    void messageHasLocation(Client client, Message message, SendMessage sendMessage);

    void writeInOrderHistoriesFile(List<OrderHistory> myOrderHistories);

    List<OrderHistory> getMyOrderHistories(Client currentUser);

    String getCheckInfo(Client currentUser);

    void writeInPdfCheck(String text);

    PayType getChosenPayType(String text);

    OrderedFood getChosenOrderedFood(Client currentUser, String data);

    InlineKeyboardMarkup inlineButtonsMyCart(Client currentUser);

    Product getChosenProduct(String text);

    InlineKeyboardMarkup inlineMarkupAddToCartOrCancel(Client currentUser);


}
