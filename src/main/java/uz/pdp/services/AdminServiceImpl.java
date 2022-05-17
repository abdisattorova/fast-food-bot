package uz.pdp.services;
//Sevinch Abdisattorova 01/01/2022 4:26 PM

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.model.*;
import uz.pdp.model.absClasses.BotUser;
import uz.pdp.services.interfaces.AdminService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uz.pdp.util.Constants.*;
import static uz.pdp.util.Db.*;

public class AdminServiceImpl implements AdminService {
    @Override
    public void adminHasMessage(Admin admin,
                                Message message,
                                SendMessage sendMessage,
                                SendDocument sendDocument,
                                SendPhoto sendPhoto) {
        Long chatId = message.getChatId();
        String text = message.getText();


        if (message.hasPhoto()) {
            Product product = admin.getProduct();
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize photo = photos.get(photos.size() - 1);
            GetFile getFile = new GetFile();
            getFile.setFileId(photo.getFileId());
            product.setImageUrl(photo.getFileUniqueId() + ".png");
            admin.setProduct(product);
            if (admin.getCurrentRound() == 6) {
                String text1 = "\nName: " + product.getName() +
                        "\nPrice: " + product.getPrice() +
                        "\nCategory: " + product.getCategory();
                sendPhoto.setCaption(text1);
                sendPhoto.setReplyMarkup(confirmationRM());
            }
            if (admin.getCurrentRound() == 9) {
                Product chosenProduct1 = productList.stream().filter(product1 ->
                                product1.equals(admin.getProduct())).
                        findFirst().orElse(null);
                String text1 = "Updated!!!\nName: " + product.getName() +
                        "\nPrice: " + product.getPrice() +
                        "\nCategory: " + product.getCategory();
                sendPhoto.setCaption(text1);
                sendPhoto.setReplyMarkup(infoIM());
                admin.setProduct(chosenProduct1);
                admin.setCurrentRound(6);
            }

            sendPhoto.setPhoto(new InputFile(new File(absPath + product.getImageUrl())));

            try {
                admin.setEditedMsgId(bot.execute(sendPhoto).getMessageId());
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        switch (text) {
            case "Main menu":
                admin.setCurrentRound(0);
                sendMessage.setText(textChoose);
                admin.setStarted(true);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                break;

            case "\uD83D\uDDD2 Users list":
                writeUsersInPdfFile();
                sendDocument.setDocument(new InputFile(new File(absPath + "users.pdf")));
                sendDocument.setCaption("Users list!");
                sendDocument.setReplyMarkup(generalService.getReplyKeyboard(admin));
                try {
                    bot.execute(sendDocument);
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "\uD83D\uDCAC Comments":
                if (commentList.isEmpty()) {
                    sendMessage.setText("List is empty!!!");
                    sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                    break;
                }
                String s = commentsFromList();
                sendMessage.setText(s);
                sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));

                break;
            case "\uD83D\uDCCE Order histories":
                if (orderHistoryList.isEmpty()) {
                    sendMessage.setText("List is empty!!!");
                    sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                    break;
                }
                writeOrderHistoriesInExcelFile();
                sendDocument.setDocument(new InputFile(new File(absPath + "orderHistory.xlsx")));
                sendDocument.setCaption("Order history list!");
                sendDocument.setReplyMarkup(generalService.getReplyKeyboard(admin));
                try {
                    bot.execute(sendDocument);
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "\uD83D\uDCC8 Change order status":
                sendMessage.setText("List is empty!!!");
                break;
            case "PayType menu":
                admin.setCurrentRound(1);
                crudService(admin, sendMessage);
                return;
            case "Product menu":
                admin.setCurrentRound(5);
                crudService(admin, sendMessage);
                return;
            case "Order status menu":
                admin.setCurrentRound(11);
                crudService(admin, sendMessage);
                return;
            case "Category menu":
                admin.setCurrentRound(10);
                crudService(admin, sendMessage);
                return;
            default:
                int round = admin.getCurrentRound();
                switch (round) {
                    case 2:
                        PayType payType = admin.getPayType();
                        if (payType.getName() == null) {
                            payType.setName(text);
                            admin.setPayType(payType);
                            sendMessage.setText("Ok, send commission fee(numbers): ");
                        } else {
                            int i = Integer.parseInt(text);
                            payType.setCommissionFee(i);
                            admin.setPayType(payType);
                            String text1 = "NEW PAY TYPE\nName: " +
                                    payType.getName() + "\nCommission fee: "
                                    + i + "%";
                            sendMessage.setText(text1);
                            sendMessage.setReplyMarkup(confirmationRM());
                        }
                        try {
                            admin.setEditedMsgId(bot.execute(sendMessage).getMessageId());
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        PayType chosenPayType = payTypeList.stream().filter(payType1 ->
                                        payType1.equals(admin.getPayType())).
                                findFirst().orElse(null);
                        if (chosenPayType != null) {
                            chosenPayType.setName(text);
                        }
                        assert chosenPayType != null;
                        sendMessage.setText("Updated!!!" + infoChosenPayType(chosenPayType));
                        sendMessage.setReplyMarkup(infoIM());
                        admin.setPayType(chosenPayType);
                        admin.setCurrentRound(2);
                        break;
                    case 4:
                        PayType chosenPayType1 = payTypeList.stream().filter(payType1 ->
                                        payType1.equals(admin.getPayType())).
                                findFirst().orElse(null);
                        if (chosenPayType1 != null) {
                            chosenPayType1.setCommissionFee(Integer.parseInt(text));
                        }
                        assert chosenPayType1 != null;
                        sendMessage.setText("Updated!!!\n\n" + infoChosenPayType(chosenPayType1));
                        sendMessage.setReplyMarkup(infoIM());
                        admin.setPayType(chosenPayType1);
                        admin.setCurrentRound(2);
                        break;
                    case 6:
                        Product product = admin.getProduct();
                        if (product.getName() == null) {
                            product.setName(text);
                            admin.setProduct(product);
                            sendMessage.setText("Ok, send " + text + " price ");
                        } else if (product.getPrice() == 0) {
                            double price = Double.parseDouble(text);
                            product.setPrice(price);
                            admin.setProduct(product);
                            sendMessage.setText("Ok, send " + product.getName() + " photo");
                            break;
                        }
                        break;
                    case 7:
                        Product chosenProduct = productList.stream().filter(product1 ->
                                        product1.equals(admin.getProduct())).
                                findFirst().orElse(null);
                        if (chosenProduct != null) {
                            chosenProduct.setName(text);
                            sendMessage.setText("Updated!!!" + infoChosenProduct(chosenProduct));
                            sendMessage.setReplyMarkup(infoIM());
                            admin.setProduct(chosenProduct);
                            admin.setCurrentRound(6);
                            break;
                        }
                    case 8:
                        Product chosenProduct1 = productList.stream().filter(product1 ->
                                        product1.equals(admin.getProduct())).
                                findFirst().orElse(null);
                        if (chosenProduct1 != null) {
                            chosenProduct1.setPrice(Double.parseDouble(text));
                            sendMessage.setText("Updated!!!" + infoChosenProduct(chosenProduct1));
                            sendMessage.setReplyMarkup(infoIM());
                            admin.setProduct(chosenProduct1);
                            admin.setCurrentRound(6);
                            break;
                        }
                    case 10:
                        if (admin.getCategory().getName() == null) {
                            admin.setCategory(new Category(text));
                            sendMessage.setText("New category\n\nName: " + text);
                            sendMessage.setReplyMarkup(confirmationRM());
                            break;
                        }
                        Category category1 = categoryList.stream().filter(category ->
                                        category.getName().equals(admin.getCategory().getName()))
                                .findFirst().orElse(null);
                        if (category1 != null) {
                            category1.setName(text);
                            sendMessage.setText("Updated!!!");
                            try {
                                bot.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            crudService(admin, sendMessage);
                            admin.setCategory(new Category());
                            return;
                        }
                    case 11:
                        if (admin.getOrderStatus().getName() == null) {
                            admin.setOrderStatus(new OrderStatus(text));
                            sendMessage.setText("New order status\n\nName: " + text);
                            sendMessage.setReplyMarkup(confirmationRM());
                            break;
                        }
                        OrderStatus orderStatus1 = orderStatusList.stream().filter(orderStatus ->
                                        orderStatus.getName().equals(admin.getOrderStatus().getName()))
                                .findFirst().orElse(null);
                        if (orderStatus1 != null) {
                            orderStatus1.setName(text);
                            sendMessage.setText("Updated!!!");
                            try {
                                bot.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            crudService(admin, sendMessage);
                            admin.setCategory(new Category());
                            return;
                        }
                }
                try {
                    admin.setEditedMsgId(bot.execute(sendMessage).getMessageId());
                    return;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
        }
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void adminHasCallBackQuery(Admin admin, CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int msgId = callbackQuery.getMessage().getMessageId();
        admin.setEditedMsgId(msgId);
        String inlineMessageId = callbackQuery.getInlineMessageId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));

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
        deleteMessage.setMessageId(msgId);

        int round = admin.getCurrentRound();

        Category category1 = categoryList.stream().filter(category ->
                        category.getName().equals(data)).
                findFirst().orElse(null);
        if (category1 != null) {
            admin.getProduct().setCategory(category1);
            sendMessage.setText("Ok, send product name: ");
        }

        switch (data) {
            case "prev":
                admin.setCurrentPage(admin.getCurrentPage() - 1);
                switch (round) {
                    case 1:
                        editMsgText.setText(allPayTypesFromList(admin));
                        editMsgText.setReplyMarkup(inlineMarkup(admin));
                        break;
                    case 5:
                        editMsgText.setText(allProductsFromList(admin));
                        editMsgText.setReplyMarkup(inlineMarkup(admin));
                        break;
                }
                break;
            case "next":
                admin.setCurrentPage(admin.getCurrentPage() + 1);
                switch (round) {
                    case 1:
                        editMsgText.setText(allPayTypesFromList(admin));
                        editMsgText.setReplyMarkup(inlineMarkup(admin));

                        break;
                    case 5:
                        editMsgText.setText(allProductsFromList(admin));
                        editMsgText.setReplyMarkup(inlineMarkup(admin));
                        break;
                }

                break;
            case "ok":
                switch (round) {
                    case 2:
                        payTypeList.add(admin.getPayType());
                        editMsgText.setText("Added to the list!!!");
                        admin.setPayType(new PayType());
                        admin.setCurrentRound(0);
                        sendMessage.setText(textChoose);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                        break;
                    case 6:
                        productList.add(admin.getProduct());
                        editedMsg.setCaption("Added to the list!!!");
                        admin.setProduct(new Product());
                        admin.setCurrentRound(0);
                        sendMessage.setText(textChoose);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                        break;
                    case 10:
                        categoryList.add(admin.getCategory());
                        editMsgText.setText("Added to the list!!!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setCategory(new Category());
                        crudService(admin, sendMessage);
                        return;
                    case 11:
                        OrderStatus orderStatus = admin.getOrderStatus();
                        orderStatusList.add(orderStatus);
                        editMsgText.setText("Added to the list!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setOrderStatus(new OrderStatus());
                        crudService(admin, sendMessage);
                        return;
                }
                break;
            case "back":
                admin.setCurrentRound(admin.getCurrentRound() - 1);
                round = admin.getCurrentRound();
                switch (round) {
                    case 1:
                        admin.setPayType(new PayType());
                        editMsgText.setText(allPayTypesFromList(admin));
                        editMsgText.setReplyMarkup(inlineMarkup(admin));
                        break;
                    case 5:
                        admin.setProduct(new Product());
                        try {
                            bot.execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        sendMessage.setText(allProductsFromList(admin));
                        sendMessage.setReplyMarkup(inlineMarkup(admin));
                        break;
                    case 9:
                    case 10:
                        admin.setCurrentRound(admin.getCurrentRound() + 1);
                        crudService(admin, sendMessage);
                        return;
                }
                break;
            case "cancel":
                switch (round) {
                    case 2:
                        editMsgText.setText("Cancelled!");
                        admin.setPayType(new PayType());
                        admin.setCurrentRound(0);
                        sendMessage.setText(textChoose);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                        break;
                    case 6:
                        editedMsg.setCaption("Cancelled!");
                        admin.setProduct(new Product());
                        admin.setCurrentRound(0);
                        sendMessage.setText(textChoose);
                        sendMessage.setReplyMarkup(generalService.getReplyKeyboard(admin));
                        break;
                    case 10:
                        editMsgText.setText("Cancelled!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setCategory(new Category());
                        crudService(admin, sendMessage);
                        return;
                    case 11:
                        editMsgText.setText("Cancelled!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setOrderStatus(new OrderStatus());
                        crudService(admin, sendMessage);
                        return;
                }
                break;
            case "update":
                switch (round) {
                    case 2:
                        editMsgText.setText("Please, choose");
                        editMsgText.setReplyMarkup(updateIM(admin));
                        break;
                    case 6:
                        editedMsg.setCaption("Please, choose");
                        editedMsg.setReplyMarkup(updateIM(admin));
                        break;
                    case 10:
                    case 11:
                        editMsgText.setText("Please,send new name");
                        break;

                }
                break;
            case "delete":
                switch (round) {
                    case 2:
                        PayType chosenPayType = payTypeList.stream().filter(payType1 ->
                                        payType1.equals(admin.getPayType())).
                                findFirst().orElse(null);
                        if (chosenPayType != null) {
                            payTypeList.remove(chosenPayType);
                            admin.setPayType(new PayType());
                        }
                        editMsgText.setText("Deleted!!!\n\n" + allPayTypesFromList(admin));
                        admin.setPayType(new PayType());
                        admin.setCurrentRound(1);
                        editMsgText.setReplyMarkup(inlineMarkup(admin));
                        break;
                    case 6:
                        Product chosenProduct1 = productList.stream().filter(product1 ->
                                        product1.equals(admin.getProduct())).
                                findFirst().orElse(null);
                        productList.remove(chosenProduct1);
                        sendMessage.setText("Deleted!!!\n\n");
                        admin.setProduct(new Product());
                        admin.setCurrentRound(5);
                        try {
                            bot.execute(deleteMessage);
                            admin.setEditedMsgId(bot.execute(sendMessage).getMessageId());
                            Thread.sleep(3000);
                            editMsgText.setMessageId(admin.getEditedMsgId());
                            editMsgText.setText(allProductsFromList(admin));
                            editMsgText.setReplyMarkup(inlineMarkup(admin));
                            sendMessage.setText(null);
                        } catch (TelegramApiException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        Category category2 = categoryList.stream().filter(category ->
                                category.getName().equals(admin.
                                        getCategory().getName())).findFirst().orElse(null);
                        categoryList.remove(category2);
                        editMsgText.setText("Deleted!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setCategory(new Category());
                        crudService(admin, sendMessage);
                        return;
                    case 11:
                        OrderStatus orderStatus = orderStatusList.stream().filter(status ->
                                status.getName().equals(admin.
                                        getOrderStatus().getName())).findFirst().orElse(null);
                        orderStatusList.remove(orderStatus);
                        editMsgText.setText("Deleted!");
                        try {
                            bot.execute(editMsgText);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        admin.setOrderStatus(new OrderStatus());
                        crudService(admin, sendMessage);
                        return;

                }
                break;
            case "addPayType":
                admin.setCurrentRound(2);
                editMsgText.setText("Ok, send new pay type name: ");
                break;
            case "updatePTName":
                admin.setCurrentRound(3);
                sendMessage.setText("Send new name: ");
                break;
            case "updatePTComFee":
                admin.setCurrentRound(4);
                sendMessage.setText("Send new commission fee: ");
                break;
            case "addProduct":
                admin.setCurrentRound(6);
                editMsgText.setText("Ok, select category");
                editMsgText.setReplyMarkup(inlineMarkup(admin));
                break;
            case "updateProductName":
                admin.setCurrentRound(7);
                sendMessage.setText("Send new name: ");
                break;
            case "updateProductPrice":
                admin.setCurrentRound(8);
                sendMessage.setText("Send new price: ");
                break;
            case "updateProductCategory":
                admin.setCurrentRound(7);
                sendMessage.setText("Select category");
                sendMessage.setReplyMarkup(inlineMarkup(admin));
                break;
            case "updateProductImage":
                admin.setCurrentRound(9);
                sendMessage.setText("Send new photo: ");
                break;
            case "addCategory":
                admin.setCategory(new Category());
                sendMessage.setText("Ok, send category name");
                break;
            case "addOrderStatus":
                admin.setOrderStatus(new OrderStatus());
                sendMessage.setText("Ok, send order status name");
                break;
            default:
                switch (round) {
                    case 1:
                        admin.setCurrentRound(2);
                        int i = Integer.parseInt(data) - 1;
                        PayType payType = payTypeList.get(i);
                        admin.setPayType(payType);
                        String info = infoChosenPayType(payType);
                        editMsgText.setText(info);
                        editMsgText.setReplyMarkup(infoIM());
                        break;
                    case 5:
                        admin.setCurrentRound(6);
                        try {
                            bot.execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        int productIndex = ((admin.getCurrentPage() - 1) * elementsInAPge) + Integer.parseInt(data) - 1;
                        Product product = productList.get(productIndex);
                        admin.setProduct(product);
                        String productInfo = infoChosenProduct(product);
                        sendPhoto.setPhoto(new InputFile(new File(absPath + product.getImageUrl())));
                        sendPhoto.setCaption(productInfo);
                        sendPhoto.setReplyMarkup(infoIM());
                        try {
                            bot.execute(sendPhoto);
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        int categoryInd = ((admin.getCurrentPage() - 1) * elementsInAPge) + Integer.parseInt(data) - 1;
                        Category category = categoryList.get(categoryInd);
                        admin.setCategory(category);
                        editMsgText.setText("Name: " + category.getName());
                        editMsgText.setReplyMarkup(infoIM());
                        try {
                            bot.execute(editMsgText);
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:
                        int statusInd = ((admin.getCurrentPage() - 1) * elementsInAPge) + Integer.parseInt(data) - 1;
                        OrderStatus orderStatus = orderStatusList.get(statusInd);
                        admin.setOrderStatus(orderStatus);
                        editMsgText.setText("Name: " + orderStatus.getName());
                        editMsgText.setReplyMarkup(infoIM());
                        try {
                            bot.execute(editMsgText);
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                }

        }
        try {
            if (editedMsg.getCaption() != null)
                bot.execute(editedMsg);
            if (editMsgText.getText() != null) {
                bot.execute(editMsgText);
            }
            if (sendMessage.getText() != null) {
                bot.execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private String infoChosenProduct(Product product) {
        return "\nName: " + product.getName() + "\nPrice: "
                + product.getPrice() + "\nCategory: " +
                product.getCategory().getName() + "\n";
    }

    private InlineKeyboardMarkup confirmationRM() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();
        InlineKeyboardButton b = new InlineKeyboardButton();
        InlineKeyboardButton b2 = new InlineKeyboardButton();

        b.setText("❌ cancel");
        b.setCallbackData("cancel");
        inLineBtnRow.add(b);

        b2.setText("✅ OK");
        b2.setCallbackData("ok");
        inLineBtnRow.add(b2);

        inlineKeyBtnList.add(inLineBtnRow);
        return inlineKeyboardMarkup;
    }

    private String infoChosenPayType(PayType payType) {
        return "~~~~~ PAY TYPE ~~~~~\n" +
                "Name: " + payType.getName() + "\n" +
                "Commission fee: " + payType.getCommissionFee() + "%\n";
    }

    private InlineKeyboardMarkup updateIM(Admin admin) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow2 = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow3 = new ArrayList<>();

        InlineKeyboardButton b = new InlineKeyboardButton();
        InlineKeyboardButton b2 = new InlineKeyboardButton();
        InlineKeyboardButton b3 = new InlineKeyboardButton();
        InlineKeyboardButton b4 = new InlineKeyboardButton();

        int round = admin.getCurrentRound();
        switch (round) {
            case 2:

                b.setText("Update name");
                b.setCallbackData("updatePTName");
                inLineBtnRow.add(b);


                b2.setText("Update com fee");
                b2.setCallbackData("updatePTComFee");
                inLineBtnRow.add(b2);


                b3.setText("👈 Back");
                b3.setCallbackData("back");
                inLineBtnRow2.add(b3);

                inlineKeyBtnList.add(inLineBtnRow);
                inlineKeyBtnList.add(inLineBtnRow2);
                return inlineKeyboardMarkup;

            case 6:
                b.setText("Update name");
                b.setCallbackData("updateProductName");
                inLineBtnRow.add(b);


                b2.setText("Update price");
                b2.setCallbackData("updateProductPrice");
                inLineBtnRow.add(b2);

                b4.setText("Update category");
                b4.setCallbackData("updateProductCategory");
                inLineBtnRow2.add(b4);

                InlineKeyboardButton b5 = new InlineKeyboardButton();
                b5.setText("Update image");
                b5.setCallbackData("updateProductImage");
                inLineBtnRow2.add(b5);

                b3.setText("👈 Back");
                b3.setCallbackData("back");
                inLineBtnRow3.add(b3);

                inlineKeyBtnList.add(inLineBtnRow);
                inlineKeyBtnList.add(inLineBtnRow2);
                inlineKeyBtnList.add(inLineBtnRow3);
                return inlineKeyboardMarkup;
        }
        return null;
    }


    private InlineKeyboardMarkup infoIM() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow2 = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();


        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText("\uD83D\uDCDD Update");
        b.setCallbackData("update");
        inLineBtnRow2.add(b);

        InlineKeyboardButton b2 = new InlineKeyboardButton();
        b2.setText("\uD83D\uDDD1 Delete");
        b2.setCallbackData("delete");
        inLineBtnRow2.add(b2);

        InlineKeyboardButton b3 = new InlineKeyboardButton();
        b3.setText("👈 Back");
        b3.setCallbackData("back");
        inLineBtnRow.add(b3);


        inlineKeyBtnList.add(inLineBtnRow2);
        inlineKeyBtnList.add(inLineBtnRow);
        return inlineKeyboardMarkup;
    }

    private void crudService(BotUser admin, SendMessage sendMessage) {
        int currentRound = admin.getCurrentRound();
        switch (currentRound) {
            case 1:
                String text = allPayTypesFromList(admin);
                sendMessage.setText(text);
                sendMessage.setReplyMarkup(inlineMarkup(admin));
                break;
            case 5:
                String products = allProductsFromList(admin);
                sendMessage.setText(products);
                sendMessage.setReplyMarkup(inlineMarkup(admin));
                break;
            case 10:
                String categories = allCategoriesFromList(admin);
                sendMessage.setText(categories);
                sendMessage.setReplyMarkup(inlineMarkup(admin));
                break;
            case 11:
                String orderStatus = allStatusesFromList(admin);
                sendMessage.setText(orderStatus);
                sendMessage.setReplyMarkup(inlineMarkup(admin));
                break;
        }
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private String allStatusesFromList(BotUser admin) {
        int page = admin.getCurrentPage() - 1;
        int totalPages = orderStatusList.size() % elementsInAPge > 0
                && orderStatusList.size() / elementsInAPge > 0 ? categoryList.size()
                / elementsInAPge + 1 : categoryList.size() / elementsInAPge;
        List<OrderStatus> collect = orderStatusList.stream().skip((long) page * elementsInAPge).
                limit(elementsInAPge).collect(Collectors.toList());
        StringBuilder text = new StringBuilder("\t\t\t\t\t~~~~~ ORDER STATUSES ~~~~~\n" + (totalPages > 0 ?
                "~~~~~ " + (page + 1) + " of " + totalPages + " ~~~~~\n\n" : "\n"));
        for (int i = 0; i < collect.size(); i++) {
            OrderStatus orderStatus = collect.get(i);
            text.append(i + 1).append(". Name: ").append(orderStatus.getName()).append("\n\n");
        }
        return text.toString();
    }

    private String allCategoriesFromList(BotUser admin) {
        int page = admin.getCurrentPage() - 1;
        int totalPages = categoryList.size() % elementsInAPge > 0
                && categoryList.size() / elementsInAPge > 0 ? categoryList.size()
                / elementsInAPge + 1 : categoryList.size() / elementsInAPge;
        List<Category> collect = categoryList.stream().skip((long) page * elementsInAPge).
                limit(elementsInAPge).collect(Collectors.toList());
        StringBuilder text = new StringBuilder("\t\t\t\t\t~~~~~ CATEGORIES ~~~~~\n" + (totalPages > 0 ?
                "~~~~~ " + (page + 1) + " of " + totalPages + " ~~~~~\n\n" : "\n"));
        for (int i = 0; i < collect.size(); i++) {
            Category category = collect.get(i);
            text.append(i + 1).append(". Name: ").append(category.getName()).append("\n\n");
        }
        return text.toString();
    }

    private String allProductsFromList(BotUser admin) {
        int page = admin.getCurrentPage() - 1;
        int totalPages = productList.size() % elementsInAPge > 0
                && productList.size() / elementsInAPge > 0 ? productList.size()
                / elementsInAPge + 1 : productList.size() / elementsInAPge;
        List<Product> collect = productList.stream().skip((long) page * elementsInAPge).
                limit(elementsInAPge).collect(Collectors.toList());
        StringBuilder text = new StringBuilder("\t\t\t\t\t~~~~~ PRODUCTS ~~~~~\n" +
                "~~~~~ " + (page + 1) + " of " + totalPages + " ~~~~~\n\n");
        for (int i = 0; i < collect.size(); i++) {
            Product product = collect.get(i);
            text.append(i + 1).append(". Name: ").append(product.getName()).append("\nPrice: ").append(product.getPrice()).append("\n").append("Category: ").append(product.getCategory()).append("\n").append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        }
        return text.toString();
    }

    private String allPayTypesFromList(BotUser admin) {
        int page = admin.getCurrentPage() - 1;
        List<PayType> collect = payTypeList.stream().skip((long) page * elementsInAPge).
                limit(elementsInAPge).collect(Collectors.toList());
        StringBuilder text = new StringBuilder("\t\t\t\t\t~~~~~ PAY TYPES ~~~~~\n");
        for (int i = 0; i < collect.size(); i++) {
            PayType payType = collect.get(i);
            text.append(i + 1).append(". Name: ").append(payType.getName()).append("\nCommission fee: ").append(payType.getCommissionFee()).append(" %\n\n");
        }
        return text.toString();
    }

    private InlineKeyboardMarkup inlineMarkup(BotUser admin) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyBtnList = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(inlineKeyBtnList);
        List<InlineKeyboardButton> inLineBtnRow = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow2 = new ArrayList<>();
        List<InlineKeyboardButton> inLineBtnRow3 = new ArrayList<>();
        InlineKeyboardButton b3 = new InlineKeyboardButton();

        int page = admin.getCurrentPage() - 1;
        int currentRound = admin.getCurrentRound();
        int totalPages = 0;
        int listSize = 0;
        switch (currentRound) {
            case 1:
                totalPages = payTypeList.size() % elementsInAPge > 0
                        && payTypeList.size() / elementsInAPge > 0 ? payTypeList.size()
                        / elementsInAPge + 1 : payTypeList.size() / elementsInAPge;
                List<PayType> collect = payTypeList.stream().
                        skip((long) page * elementsInAPge).
                        limit(elementsInAPge).
                        collect(Collectors.toList());
                listSize = collect.size();
                b3.setText("\uD83C\uDD95 Add new pay type");
                b3.setCallbackData("addPayType");
                break;
            case 5:
                totalPages = productList.size() % elementsInAPge > 0
                        && productList.size() / elementsInAPge > 0 ? productList.size()
                        / elementsInAPge + 1 : productList.size() / elementsInAPge;
                List<Product> products = productList.stream().
                        skip((long) page * elementsInAPge).
                        limit(elementsInAPge).
                        collect(Collectors.toList());
                listSize = products.size();
                b3.setText("\uD83C\uDD95 Add new product");
                b3.setCallbackData("addProduct");
                break;
            case 6:
            case 7:
                for (int i = 0; i < categoryList.size(); i++) {
                    Category category = categoryList.get(i);
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText(String.valueOf(category.getName()));
                    inlineKeyboardButton.setCallbackData(String.valueOf(category.getName()));
                    inLineBtnRow.add(inlineKeyboardButton);
                    if (i % 2 != 0) {
                        inlineKeyBtnList.add(inLineBtnRow);
                        inLineBtnRow = new ArrayList<>();
                    }
                }
                return inlineKeyboardMarkup;
            case 10:
                totalPages = categoryList.size() % elementsInAPge > 0
                        && categoryList.size() / elementsInAPge > 0 ? categoryList.size()
                        / elementsInAPge + 1 : categoryList.size() / elementsInAPge;
                List<Category> category = categoryList.stream().
                        skip((long) page * elementsInAPge).
                        limit(elementsInAPge).
                        collect(Collectors.toList());
                listSize = category.size();
                b3.setText("\uD83C\uDD95 Add new category");
                b3.setCallbackData("addCategory");
                break;
            case 11:
                totalPages = orderStatusList.size() % elementsInAPge > 0
                        && orderStatusList.size() / elementsInAPge > 0 ? orderStatusList.size()
                        / elementsInAPge + 1 : orderStatusList.size() / elementsInAPge;
                List<OrderStatus> orderStatus = orderStatusList.stream().
                        skip((long) page * elementsInAPge).
                        limit(elementsInAPge).
                        collect(Collectors.toList());
                listSize = orderStatus.size();
                b3.setText("\uD83C\uDD95 Add new order status");
                b3.setCallbackData("addOrderStatus");
                break;
        }


        for (int i = 0; i < listSize; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(i + 1));
            inlineKeyboardButton.setCallbackData(String.valueOf((i + 1)));
            inLineBtnRow.add(inlineKeyboardButton);
            if (i % 2 != 0) {
                inlineKeyBtnList.add(inLineBtnRow);
                inLineBtnRow = new ArrayList<>();
            }
        }
        if (page != 0) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText("Previous ⏮");
            b.setCallbackData("prev");
            inLineBtnRow2.add(b);
        }
        if (page + 1 != totalPages && totalPages != 0) {
            InlineKeyboardButton b2 = new InlineKeyboardButton();
            b2.setText("Next ⏭");
            b2.setCallbackData("next");
            inLineBtnRow2.add(b2);
        }
        inLineBtnRow3.add(b3);


        inlineKeyBtnList.add(inLineBtnRow);
        inlineKeyBtnList.add(inLineBtnRow2);
        inlineKeyBtnList.add(inLineBtnRow3);
        return inlineKeyboardMarkup;
    }


    private String commentsFromList() {
        StringBuilder text = new StringBuilder();
        for (Comment comment1 : commentList) {
            text.append(comment1.getComment()).
                    append("\n\t\t\t\t\t\t\t\t\t").
                    append(comment1.getClient().getFullName()).
                    append(comment1.getLocalDateTime().
                            format(DateTimeFormatter
                                    .ofPattern("HH:mm, dd.MM.yyyy")));
        }
        return text.toString();
    }

    private void writeUsersInPdfFile() {
        try (PdfWriter pdfWriter = new PdfWriter("src/main/resources/users.pdf")) {

            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A5);
            pdfDocument.addNewPage();
            Document document = new Document(pdfDocument);
            Paragraph paragraph = new Paragraph();
            paragraph.add("Clients list").setBackgroundColor(Color.LIGHT_GRAY);
            document.add(paragraph);

            float[] pointColumnWidths = {50F, 150F, 150F};
            Table table = new Table(pointColumnWidths);
            table.addCell(new Cell().add("T/R"));
            table.addCell(new Cell().add("Full name"));
            table.addCell(new Cell().add("Phone number"));

            for (int i = 1; i < userList.size(); i++) {
                BotUser admin = userList.get(i);
                table.addCell(i + ".");
                table.addCell(admin.getFullName());
                if (admin.getPhoneNumber() != null)
                    table.addCell(admin.getPhoneNumber());
                else
                    table.addCell("-");
            }

            document.add(table);
            document.close();

            pdfDocument.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void writeOrderHistoriesInExcelFile() {
        try (FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/orderHistory.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setDefaultColumnWidth(20);
            sheet.setDefaultRowHeightInPoints(50);
            XSSFRow row = sheet.createRow(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            row.createCell(0).setCellValue("T/R");
            row.getCell(0).setCellStyle(cellStyle);
            row.createCell(1).setCellValue("Client name");
            row.getCell(1).setCellStyle(cellStyle);
            row.createCell(2).setCellValue("Food name");
            row.getCell(2).setCellStyle(cellStyle);
            row.createCell(3).setCellValue("Pay type");
            row.getCell(3).setCellStyle(cellStyle);
            row.createCell(4).setCellValue("Total sum");
            row.getCell(4).setCellStyle(cellStyle);
            row.createCell(5).setCellValue("Ordered time");
            row.getCell(5).setCellStyle(cellStyle);

            for (int i = 0; i < orderHistoryList.size(); i++) {
                OrderHistory orderHistory = orderHistoryList.get(i);
                StringBuilder text = new StringBuilder();
                String time = "";
                for (int j = 0; j < orderHistory.getOrderedFoodList().size(); j++) {
                    OrderedFood orderedFood = orderHistory.getOrderedFoodList().get(j);
                    text.append("\n").append(j + 1).append(". ").append(orderedFood.getProduct().getName()).append(" (#").append(orderedFood.getQuantity()).append(")");
                    time = orderedFood.getOrdered_at().
                            format(DateTimeFormatter.
                                    ofPattern("HH:mm, dd.MM.yyyy"));
                }
                XSSFRow newRow = sheet.createRow(i + 1);
                newRow.createCell(0).setCellValue(i + 1);
                newRow.getCell(0).setCellStyle(cellStyle);
                newRow.createCell(1).setCellValue(orderHistory.
                        getOrderedFoodList().get(0).getClient().getFullName());
                newRow.getCell(1).setCellStyle(cellStyle);
                newRow.createCell(2).setCellValue(text.toString());
                newRow.getCell(2).setCellStyle(cellStyle);
                newRow.createCell(3).setCellValue(orderHistory.getPayType().getName());
                newRow.getCell(3).setCellStyle(cellStyle);
                newRow.createCell(4).setCellValue(orderHistory.getTotalSum());
                newRow.getCell(4).setCellStyle(cellStyle);
                newRow.createCell(5).setCellValue(time);
                newRow.getCell(5).setCellStyle(cellStyle);

            }
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
