package uz.pdp.model;
//Sevinch Abdisattorova 12/22/2021 2:49 PM

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class Product {
    private String name;
    private double price;
    private String imageUrl;
    private Category category;

    public Product(String name) {
        this.name = name;
    }
}
