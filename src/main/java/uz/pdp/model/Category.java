package uz.pdp.model;
//Sevinch Abdisattorova 12/30/2021 11:23 AM

import lombok.Data;


@Data
public class Category {
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }
}
