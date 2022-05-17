package uz.pdp.model;
//Sevinch Abdisattorova 12/30/2021 4:16 PM

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class PayType {
    private String name;
    private double commissionFee;
}
