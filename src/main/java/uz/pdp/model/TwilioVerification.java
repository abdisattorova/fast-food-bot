package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.model.absClasses.BotUser;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwilioVerification {
    private UUID id=UUID.randomUUID();
    private BotUser user;
    private String code;
    private boolean verified;

    public TwilioVerification(BotUser user) {
        this.user = user;
    }
}

