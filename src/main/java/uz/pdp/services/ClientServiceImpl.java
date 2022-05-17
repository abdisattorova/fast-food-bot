package uz.pdp.services;
//Sevinch Abdisattorova 01/01/2022 4:27 PM

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.model.*;
import uz.pdp.services.interfaces.ClientService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uz.pdp.util.Constants.absPath;
import static uz.pdp.util.Constants.textChoose;
import static uz.pdp.util.Db.*;

public class ClientServiceImpl implements ClientService {


    @Override
    public void clientHasMessage(Client user,
                                 Message message,
                                 String text,
                                 SendMessage sendMessage,
                                 SendDocument sendDocument,
                                 SendPhoto sendPhoto) {
        Long chatId = message.getChatId();


        if (message.hasLocation()) {
            messageHasLocation(user, message, sendMessage);
        }
        switch (text) {
            case "👈 Back":
                user.setCurrentRound(user.getCurrentRound() - 2);
                sendMessage.setText(textChoose);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                break;
            case "☎ Share contact":
                sendMessage.setText("Please, share your contact!");
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                break;
            case "\uD83C\uDF74 Menu":
            case "\uD83C\uDF74 Main menu":
                user.setProductQuantity(1);
                user.setCurrentRound(1);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                sendMessage.setText(textChoose);
                break;
            case "✍ Comments":
                user.setCurrentRound(10);
                sendMessage.setText("Write your comment below \uD83D\uDC47");
                break;
            case "\uD83D\uDECD My cart":
                String s1 = showMyCart(user);
                sendMessage.setText(s1);
                if (!s1.equals("Your cart is empty!!!")) {
                    sendMessage.setReplyMarkup(inlineButtonsMyCart(user));
                    try {
                        user.setCurrentRound(bot.execute(sendMessage).getMessageId());
                        return;
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                break;
            case "📝 My order histories":
                List<OrderHistory> myOrderHistories = getMyOrderHistories(user);
                if (myOrderHistories.isEmpty()) {
                    sendMessage.setText("No order histories yet!!!");
                    user.setCurrentRound(0);
                    sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                    break;
                }
                writeInOrderHistoriesFile(myOrderHistories);
                sendMessage.setText("Please download pdf file \uD83D\uDC47");
                user.setCurrentRound(9);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                break;
            case "\uD83D\uDCCE Download pdf":
                sendDocument.setDocument(new InputFile(new File(absPath + "pdfFile.pdf")));
                try {
                    bot.execute(sendDocument);
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;

            case "Ok \uD83D\uDE46":
                String textCheck = getCheckInfo(user);
                sendMessage.setText(textCheck);
                user.setCurrentRound(8);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                break;
            case "\uD83D\uDCCE Download check pdf":
                sendDocument.setDocument(new InputFile(new File(absPath + "check.pdf")));
                sendDocument.setCaption("Thanks 😉😉😉");
                user.setCurrentRound(0);
                sendDocument.setReplyMarkup(generalService.getReplyKeyboard(user));
                try {
                    bot.execute(sendDocument);
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            default:
                int round = user.getCurrentRound();
                switch (round) {
                    case 2:
                        if (!categoryList.contains(new Category(text)))
                            return;
                        user.setSelectedCategory(text);
                        String s = text.toLowerCase() + ".png";
                        sendPhoto.setPhoto(new InputFile(new File(absPath + s)));
                        sendPhoto.setCaption(textChoose);
                        sendPhoto.setReplyMarkup(generalService.getReplyKeyboard(user));
                        break;
                    case 3:
                        Product chosenProduct = getChosenProduct(text);
                        if (chosenProduct == null) return;
                        String pathname = absPath + chosenProduct.getImageUrl();
                        sendMessage.setText("Please,select!");
                        sendPhoto.setPhoto(new InputFile(new File(pathname)));
                        sendPhoto.setCaption(info(chosenProduct));
                        sendPhoto.setReplyMarkup(inlineMarkupAddToCartOrCancel(user));
                        user.setSelectedProduct(chosenProduct);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        break;

                    case 6:
                        PayType chosenPayType = getChosenPayType(text);
                        if (chosenPayType == null) {
                            sendMessage.setText(textChoose);
                            sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        }
                        user.setChosenPayType(chosenPayType);
                        user.setCurrentRound(7);
                        assert chosenPayType != null;
                        sendMessage.setText("\n\t\t~~~~~ Chosen pay type ~~~~~\n\nName: " +
                                chosenPayType.getName() + "\nCommission fee: " +
                                chosenPayType.getCommissionFee() + " %");
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        try {
                            bot.execute(sendMessage);
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        return;
                    case 10:
                        Comment comment = new Comment(user, text, false, LocalDateTime.now());
                        commentList.add(comment);
                        user.setCurrentRound(0);
                        sendMessage.setText("Successfully send,Please choose \uD83D\uDC47");
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));

                        try {
                            bot.execute(sendMessage);
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                try {
                    if (user.getCurrentRound() == 2
                            || user.getCurrentRound() == 3)
                        bot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                break;

        }
        try {
            if (sendMessage.getText().isEmpty())
                return;
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clientHasCallBackQuery(Client user, CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int msgId = callbackQuery.getMessage().getMessageId();
        user.setEditedMsgId(msgId);
        String text = callbackQuery.getMessage().getCaption();
        String inlineMessageId = callbackQuery.getInlineMessageId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        EditMessageText editMsgText = new EditMessageText();
        editMsgText.setChatId(String.valueOf(chatId));
        editMsgText.setMessageId(msgId);
        editMsgText.setInlineMessageId(inlineMessageId);

        EditMessageCaption editedMsg = new EditMessageCaption();
        editedMsg.setChatId(String.valueOf(chatId));
        editedMsg.setMessageId(msgId);
        editedMsg.setInlineMessageId(inlineMessageId);

        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(user.getEditedMsgId());


        switch (data) {
            case "<":
                if (user.getProductQuantity() == 1)
                    return;
                user.setProductQuantity(user.getProductQuantity() - 1);
                editedMsg.setCaption(text);
                editedMsg.setReplyMarkup(inlineMarkupAddToCartOrCancel(user));
                break;
            case ">":
                user.setProductQuantity(user.getProductQuantity() + 1);
                editedMsg.setCaption(text);
                editedMsg.setReplyMarkup(inlineMarkupAddToCartOrCancel(user));
                break;
            case "0":
                return;
            case "cart":
                Thread thread = new Thread(() -> {
                    editedMsg.setCaption("Adding to the cart!!!");
                    for (int i = 5; i > 0; i--) {
                        editedMsg.setReplyMarkup(generalService.replyKeyboardCancel(i));

                        try {
                            if (user.getEditedMsgId() == 0)
                                return;
                            bot.execute(editedMsg);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    try {
                        if (user.getEditedMsgId() != 0) {
                            bot.execute(deleteMessage);
                            user.setEditedMsgId(0);
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    OrderedFood orderedFood = new OrderedFood();
                    orderedFood.setClient(user);
                    orderedFood.setProduct(user.getSelectedProduct());
                    orderedFood.setQuantity(user.getProductQuantity());
                    orderedFood.setOrdered_at(LocalDateTime.now());
                    orderedFood.setOrderStatus(new OrderStatus("NEW"));
                    List<OrderedFood> myCart = new ArrayList<>(user.getMyCart());
                    myCart.add(orderedFood);
                    user.setMyCart(myCart);
                    user.setProductQuantity(1);
                    user.setSelectedCategory(null);
                    user.setSelectedProduct(null);

                });
                thread.start();
                return;
            case "cancel":
                try {
                    if (user.getEditedMsgId() != 0) {
                        bot.execute(deleteMessage);
                        user.setEditedMsgId(0);
                        sendMessage.setText("Operation cancelled!");
                        user.setCurrentRound(3);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        bot.execute(sendMessage);
                    }
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "removeAll":
                Thread thread1 = new Thread(() -> {
                    editMsgText.setText("Removing...!!!");
                    for (int i = 5; i > 0; i--) {
                        editMsgText.setReplyMarkup(generalService.replyKeyboardCancel(i));
                        try {
                            if (user.getEditedMsgId() == 0)
                                return;
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        if (user.getEditedMsgId() != 0)
                            bot.execute(deleteMessage);
                        user.setEditedMsgId(0);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    user.setMyCart(new ArrayList<>());
                });
                thread1.start();
                return;
            case "order":
                sendMessage.setText("Please,share your location \uD83D\uDCCD");
                user.setCurrentRound(5);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                try {
                    bot.execute(sendMessage);
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "remove":
                List<OrderedFood> myCart = user.getMyCart();
                OrderedFood orderedFood1 = myCart.stream().filter(orderedFood ->
                                orderedFood.getProduct().
                                        equals(user.
                                                getSelectedProduct())).
                        findFirst().orElse(null);
                myCart.remove(orderedFood1);
                editedMsg.setCaption("Successfully removed!");
                deleteMessage.setMessageId(user.getEditedMsgId());
                sendMessage.setText(textChoose);
                user.setCurrentRound(3);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                try {
                    bot.execute(editedMsg);
                    Thread.sleep(3000);
                    bot.execute(deleteMessage);
                    bot.execute(sendMessage);
                    return;
                } catch (InterruptedException | TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "save":
                List<OrderedFood> myCart1 = user.getMyCart();
                for (OrderedFood orderedFood : myCart1) {
                    if (orderedFood.getProduct().equals(user.getSelectedProduct())) {
                        orderedFood.setQuantity(user.getProductQuantity());
                        editedMsg.setMessageId(user.getEditedMsgId());
                        editedMsg.setCaption("Successfully saved!");
                        sendMessage.setText(textChoose);
                        user.setCurrentRound(3);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(user));
                        try {
                            bot.execute(editedMsg);
                            deleteMessage.setMessageId(user.getEditedMsgId());
                            Thread.sleep(3000);
                            bot.execute(deleteMessage);
                            bot.execute(sendMessage);
                            return;
                        } catch (TelegramApiException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            default:
                OrderedFood chosenOrderedFood = getChosenOrderedFood(user, data);
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(String.valueOf(chatId));
                sendPhoto.setPhoto(new InputFile(
                        new File(absPath +
                                chosenOrderedFood.
                                        getProduct().
                                        getImageUrl())));
                sendPhoto.setCaption(info(chosenOrderedFood.getProduct()));
                user.setCurrentRound(4);
                sendPhoto.setReplyMarkup(inlineMarkupAddToCartOrCancel(user));

                try {
                    if (user.getEditedMsgId() != 0) {
                        deleteMessage.setMessageId(user.getEditedMsgId());
                        bot.execute(deleteMessage);
                        user.setEditedMsgId(0);
                    }
                    user.setEditedMsgId(bot.execute(sendPhoto).getMessageId());
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }


        }
        try {
            if (editMsgText.getText() != null) {
                bot.execute(editMsgText);
                return;
            }
            bot.execute(editedMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String showMyCart(Client user) {
        if (user.getMyCart().isEmpty()) {
            return "Your cart is empty!!!";
        }
        StringBuilder text = new StringBuilder("\n\t\t~~~~~ MY CART ~~~~~\n");
        double totalSum = 0;

        for (int i = 0; i < user.getMyCart().size(); i++) {
            OrderedFood orderedFood = user.getMyCart().get(i);
            text.append("\n").append(i + 1).append(". Name: ").append(orderedFood.getProduct().getName()).append("\nPrice: ").append(orderedFood.getProduct().getPrice()).append("\nQuantity: ").append(orderedFood.getQuantity()).append("\n\n\t\t~~~~~~~~~~~~~~~~~~~");

            totalSum += orderedFood.getQuantity() * orderedFood.getProduct().getPrice();
        }
        text.append("\n\nTotal sum: ").append(totalSum);
        return text.toString();
    }

    @Override
    public String info(Product chosenProduct) {
        return "\nName: " + chosenProduct.getName()
                + "\nPrice: " + chosenProduct.getPrice();
    }

    @Override
    public void messageHasLocation(Client client, Message message, SendMessage sendMessage) {
        Location location = message.getLocation();
        client.setLocation(location);
        sendMessage.setText("Please choose pay type \uD83D\uDC47");
        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(client));
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeInOrderHistoriesFile(List<OrderHistory> myOrderHistories) {
        try (PdfWriter pdfWriter = new PdfWriter("src/main/resources/pdfFile.pdf")) {
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A5);
            pdfDocument.addNewPage();
            Document document = new Document(pdfDocument);
            Paragraph paragraph = new Paragraph();
            paragraph.add("MY ORDER HISTORIES").setBackgroundColor(Color.LIGHT_GRAY);
            document.add(paragraph);
            Paragraph paragraph1 = new Paragraph();
            StringBuilder text = new StringBuilder();
            for (OrderHistory myOrderHistory : myOrderHistories) {
                List<OrderedFood> orderedFoodList = myOrderHistory.getOrderedFoodList();
                text.append("\n\nClient: ").append(orderedFoodList.get(0).
                        getClient().getFullName()).append("\nProducts: ");
                for (OrderedFood orderedFood : orderedFoodList) {
                    text.append("\nName: ").append(orderedFood.getProduct().
                                    getName()).append("\nPrice: ").append(orderedFood.
                                    getProduct().getPrice()).append("\nQuantity: ").
                            append(orderedFood.getQuantity()).append("\nOrder status: ").
                            append(orderedFood.getOrderStatus().getName()).
                            append("\nOrdered at: ").append(orderedFood.getOrdered_at().
                                    format(DateTimeFormatter.ofPattern("HH:mm , dd.MM.yyyy\n")));
                }
                double totalSum = myOrderHistory.getTotalSum();
                double comFee = totalSum * myOrderHistory.getPayType()
                        .getCommissionFee() / 100;
                text.append("\nPay type: ").append(myOrderHistory.getPayType().getName()).append("\nCom fee: ").append(comFee).append("\nTotal sum: ").append(totalSum + comFee).append("\n").append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
            }
            paragraph1.add(text.toString());
            document.add(paragraph1);
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrderHistory> getMyOrderHistories(Client user) {

        return orderHistoryList.stream().
                filter(orderHistory ->
                        orderHistory.getOrderedFoodList().
                                get(0).getClient().getChatId()
                                == user.getChatId()
                ).collect(Collectors.toList());
    }

    @Override
    public String getCheckInfo(Client user) {
        StringBuilder text = new StringBuilder("\nClient: " + user.getFullName() + "\n");
        double totalSum = 0;
        PayType payType = user.getChosenPayType();
        for (int i = 0; i < user.getMyCart().size(); i++) {
            OrderedFood orderedFood = user.getMyCart().get(i);
            text.append("\n").append(i + 1).append(". Name: ").append(orderedFood.getProduct().getName()).append("\nPrice: ").append(orderedFood.getProduct().getPrice()).append("\nQuantity: ").append(orderedFood.getQuantity()).append("\n~~~~~~~~~~~~~~~~~~~");

            totalSum += orderedFood.getQuantity() * orderedFood.getProduct().getPrice();
        }
        double comFee = totalSum * payType.getCommissionFee() / 100;
        text.append("\nTotal sum: ").append(totalSum + comFee);
        text.append("\nChosen pay type: ").append(payType.getName()).append("\n").append("Commission fee: ").append(comFee).append("\n\n").append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"))).append("\n\n~~~~~ Thank you ~~~~~");
        writeInPdfCheck(text.toString());
        orderHistoryList.add(new OrderHistory(user.getMyCart(), payType, totalSum));
        user.setMyCart(new ArrayList<>());
        user.setSelectedProduct(null);
        user.setSelectedCategory(null);
        user.setChosenPayType(null);
        return text.toString();
    }

    @Override
    public void writeInPdfCheck(String text) {
        try (PdfWriter pdfWriter = new PdfWriter("src/main/resources/check.pdf")) {
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A5);
            pdfDocument.addNewPage();
            com.itextpdf.layout.Document document = new Document(pdfDocument);
            Paragraph paragraph = new Paragraph();
            paragraph.add("Purchase check").setBackgroundColor(Color.LIGHT_GRAY);
            document.add(paragraph);
            Paragraph paragraph1 = new Paragraph();
            paragraph1.add(text);
            document.add(paragraph1);
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PayType getChosenPayType(String text) {
        return payTypeList.stream().filter(payType ->
                        payType.getName().equals(text)).
                findFirst().orElse(null);
    }

    @Override
    public OrderedFood getChosenOrderedFood(Client user, String data) {
        List<OrderedFood> myCart = user.getMyCart();
        OrderedFood orderedFood = myCart.get(Integer.parseInt(data) - 1);
        user.setSelectedProduct(orderedFood.getProduct());
        user.setProductQuantity(orderedFood.getQuantity());
        return orderedFood;
    }

    @Override
    public InlineKeyboardMarkup inlineButtonsMyCart(Client user) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow2 = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow3 = new ArrayList<>();


        List<OrderedFood> myCart = user.getMyCart();

        for (int i = 0; i < myCart.size(); i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(i + 1));
            inlineKeyboardButton.setCallbackData(String.valueOf((i + 1)));
            inLineBtnRow.add(inlineKeyboardButton);
            if (i % 2 != 0) {
                inlineKeyBtnList.add(inLineBtnRow);
                inLineBtnRow = new ArrayList<>();
            }
        }
        InlineKeyboardButton b3 = new InlineKeyboardButton();
        b3.setText("❌ Remove all");
        b3.setCallbackData("removeAll");
        InlineKeyboardButton b4 = new InlineKeyboardButton();
        b4.setText("✅ Order");
        b4.setCallbackData("order");
        inLineBtnRow3.add(b3);
        inLineBtnRow3.add(b4);

        inlineKeyBtnList.add(inLineBtnRow);
        inlineKeyBtnList.add(inLineBtnRow2);
        inlineKeyBtnList.add(inLineBtnRow3);
        return inlineKeyboardMarkup;
    }

    @Override
    public Product getChosenProduct(String text) {
        return productList.stream().filter(product ->
                product.getName().equals(text)
        ).findFirst().orElse(null);

    }


    @Override
    public InlineKeyboardMarkup inlineMarkupAddToCartOrCancel(Client user) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow2 = new ArrayList<>();
        int count = user.getProductQuantity();

        InlineKeyboardButton b1 = new InlineKeyboardButton("<");
        b1.setCallbackData("<");
        InlineKeyboardButton b2 = new InlineKeyboardButton("" + count);
        b2.setCallbackData("0");
        InlineKeyboardButton b3 = new InlineKeyboardButton(">");
        b3.setCallbackData(">");

        inLineBtnRow.add(b1);
        inLineBtnRow.add(b2);
        inLineBtnRow.add(b3);
        InlineKeyboardButton b4 = new InlineKeyboardButton();
        if (user.getCurrentRound() == 3) {
            b4.setText("Add to cart \uD83D\uDED2");
            b4.setCallbackData("cart");
        } else {
            InlineKeyboardButton b5 = new InlineKeyboardButton();
            b4.setText("❌ Remove");
            b4.setCallbackData("remove");
            b5.setText("☑ Save");
            b5.setCallbackData("save");
            inLineBtnRow2.add(b5);
        }
        inLineBtnRow2.add(b4);

        inlineKeyBtnList.add(inLineBtnRow);
        inlineKeyBtnList.add(inLineBtnRow2);

        return inlineKeyboardMarkup;
    }


}
