package uz.pdp.model.absClasses;
//Sevinch Abdisattorova 12/22/2021 2:42 PM

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data

public abstract class BotUser {
    private String fullName;
    private String phoneNumber;
    private long chatId;
    private int editedMsgId;
    private Integer currentRound;
    private int currentPage = 1;
    private boolean isStarted;
    private String selectedCategory;
    private int authRound;
    private boolean isAdmin;

    public BotUser(String fullName, long chatId, boolean isStarted) {
        this.fullName = fullName;
        this.chatId = chatId;
        this.isStarted = isStarted;
    }

    public BotUser(String fullName, String phoneNumber, boolean isAdmin) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.isAdmin = isAdmin;
    }

    public BotUser() {
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", chatId=" + chatId +
                ", editedMsgId=" + editedMsgId +
                ", currentRound=" + currentRound +
                ", currentPage=" + currentPage +
                ", isStarted=" + isStarted +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
