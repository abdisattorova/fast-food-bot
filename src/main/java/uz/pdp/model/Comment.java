package uz.pdp.model;
//Sevinch Abdisattorova 01/01/2022 10:06 AM

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class Comment {
    private Client client;
    private String comment;
    private boolean isRead;
    private LocalDateTime localDateTime;
}
