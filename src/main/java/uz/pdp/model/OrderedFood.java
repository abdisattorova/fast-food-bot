package uz.pdp.model;
//Sevinch Abdisattorova 12/22/2021 2:51 PM

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class OrderedFood {
    private Client client;
    private Product product;
    private int quantity;
    private LocalDateTime ordered_at;
    private OrderStatus orderStatus ;

    @Override
    public String toString() {
        return "OrderedFood{" +
                "client=" + client +
                ", product=" + product +
                ", quantity=" + quantity +
                ", ordered_at=" + ordered_at +
                ", orderStatus=" + orderStatus +
                '}';
    }
}
