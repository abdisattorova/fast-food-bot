package uz.pdp;
//Sevinch Abdisattorova 12/21/2021 8:31 AM

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.services.BotServiceImpl;

import static uz.pdp.util.Constants.botToken;
import static uz.pdp.util.Constants.botUsername;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        BotServiceImpl botService = new BotServiceImpl();
        botService.updateReceived(update);

    }

}



