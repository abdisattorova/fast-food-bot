package uz.pdp;
//Sevinch Abdisattorova 12/21/2021 8:30 AM

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new Bot());
            System.out.println("Bot started!!!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
