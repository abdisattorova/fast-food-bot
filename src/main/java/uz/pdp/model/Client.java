package uz.pdp.model;
//Sevinch Abdisattorova 12/22/2021 2:47 PM

import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Location;
import uz.pdp.model.absClasses.BotUser;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data


public class Client extends BotUser {
    private List<OrderedFood> myCart = new ArrayList<>();
    private Product selectedProduct;
    private PayType chosenPayType;
    private int productQuantity = 1;
    private Location location;

    public Client(String fullName,
                  long chatId,
                  boolean isStarted) {
        super(fullName, chatId, isStarted);
    }

    public Client() {
    }

    public Client(String fullName,
                  String phoneNumber,
                  long chatId,
                  int editedMsgId,
                  Integer currentRound,
                  int currentPage,
                  boolean isStarted,
                  String selectedCategory,
                  int authRound,
                  boolean isAdmin) {
        super(fullName,
                phoneNumber,
                chatId,
                editedMsgId,
                currentRound,
                currentPage,
                isStarted,
                selectedCategory,
                authRound,
                isAdmin);
    }

    @Override
    public String toString() {
        return "Client{" +
                "myCart=" + myCart +
                ", selectedProduct=" + selectedProduct +
                ", productQuantity=" + productQuantity +
                ", location=" + location +
                '}';
    }
}
