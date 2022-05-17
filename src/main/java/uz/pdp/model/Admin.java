package uz.pdp.model;
//Sevinch Abdisattorova 01/01/2022 3:49 PM

import lombok.Data;
import uz.pdp.model.absClasses.BotUser;


@Data
public class Admin extends BotUser {
    private PayType payType = new PayType();
    private Category category = new Category();
    private Product product = new Product();
    private OrderStatus orderStatus = new OrderStatus();

    public Admin() {
    }


    public Admin(String fullName, String phoneNumber, boolean isAdmin) {
        super(fullName, phoneNumber, isAdmin);
    }

    public Admin(String fullName,
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

    public Admin(PayType payType, Category category, Product product, OrderStatus orderStatus) {
        this.payType = payType;
        this.category = category;
        this.product = product;
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "payType=" + payType +
                ", category=" + category +
                ", product=" + product +
                ", orderStatus=" + orderStatus +
                '}';
    }
}
