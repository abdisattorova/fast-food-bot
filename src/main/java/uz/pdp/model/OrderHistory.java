package uz.pdp.model;
//Sevinch Abdisattorova 12/30/2021 4:16 PM

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class OrderHistory {
    private List<OrderedFood> orderedFoodList;
    private PayType payType;
    private double totalSum;
}
