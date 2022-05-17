package uz.pdp.util;
//Sevinch Abdisattorova 12/22/2021 3:11 PM

import uz.pdp.Bot;
import uz.pdp.model.*;
import uz.pdp.services.AdminServiceImpl;
import uz.pdp.services.AuthServiceImpl;
import uz.pdp.services.ClientServiceImpl;
import uz.pdp.services.GeneralServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Db {

    public static Bot bot = new Bot();

    public static ClientServiceImpl clientService = new ClientServiceImpl();

    public static AdminServiceImpl adminService = new AdminServiceImpl();

    public static GeneralServiceImpl generalService = new GeneralServiceImpl();

    public static AuthServiceImpl authService = new AuthServiceImpl();

    public static List<TwilioVerification> twilioVerificationList = new ArrayList<>();

    public static Admin admin = new Admin("Admin", Constants.adminPhoneNum, true);

    public static List<Client> userList = new ArrayList<>(
            Arrays.asList(
                    new Client("Client", 967906635, false)
            )
    );

    public static List<Product> productList = new ArrayList<>(
            Arrays.asList(
                    new Product("Combo+", 22000, "img_13.png", new Category("Set")),
                    new Product("Lavash", 19000, "img_10.png", new Category("Lavash")),
                    new Product("Lavash (chicken)", 20000, "img_11.png", new Category("Lavash")),
                    new Product("Lavash (chicken,cheese)", 17000, "img_11.png", new Category("Lavash")),
                    new Product("Lavash (cheese)", 23000, "img_12.png", new Category("Lavash")),
                    new Product("Lavash (pepper)", 22000, "img_10.png", new Category("Lavash")),
                    new Product("Lavash (chicken and pepper)", 20000, "img_11.png", new Category("Lavash")),
                    new Product("Fitter", 19000, "lavash1.png", new Category("Lavash")),
                    new Product("Shaurma (beef)", 18000, "img_9.png", new Category("Shaurma")),
                    new Product("Shaurma (chicken)", 18000, "img_8.png", new Category("Shaurma")),
                    new Product("Hot-dog (double baget)", 15000, "img_6.png", new Category("Hot-dog")),
                    new Product("Hot-dog (classic)", 15000, "img_7.png", new Category("Hot-dog")),
                    new Product("Hot-dog (kids)", 10000, "img_7.png", new Category("Hot-dog")),
                    new Product("Hot-dog (baget)", 15000, "img_7.png", new Category("Hot-dog")),
                    new Product("Hamburger", 17000, "img_4.png", new Category("Burger")),
                    new Product("Cheese burger", 18000, "img_5.png", new Category("Burger")),
                    new Product("Double burger", 20000, "img_5.png", new Category("Burger")),
                    new Product("Double cheese", 20000, "img_5.png", new Category("Burger")),
                    new Product("Kids' juice", 2500, "img.png", new Category("Beverages")),
                    new Product("Pepsi 0,5l", 7000, "img_1.png", new Category("Beverages")),
                    new Product("Pepsi 1,5l", 14000, "img_2.png", new Category("Beverages")),
                    new Product("Tea", 2500, "img_3.png", new Category("Beverages")),
                    new Product("Water", 2000, "img_15.png", new Category("Beverages")),
                    new Product("Coffee", 3000, "img_14.png", new Category("Beverages"))
            )
    );

    public static List<Category> categoryList = new ArrayList<>(
            Arrays.asList(
                    new Category("Set"),
                    new Category("Lavash"),
                    new Category("Shaurma"),
                    new Category("Burger"),
                    new Category("Hot-dog"),
                    new Category("Beverages")
            )
    );

    public static List<OrderStatus> orderStatusList = new ArrayList<>(
            Arrays.asList(
                    new OrderStatus("NEW"),
                    new OrderStatus("COMPLETED"),
                    new OrderStatus("DELIVERED")
            )

    );

    public static List<PayType> payTypeList = new ArrayList<>(
            Arrays.asList(
                    new PayType("Cash", 0),
                    new PayType("Apelsin", 0.5),
                    new PayType("PayMe", 0.7),
                    new PayType("Click", 1)
            )

    );
    public static List<OrderHistory> orderHistoryList = new ArrayList<>();

    public static List<Comment> commentList = new ArrayList<>();
}
