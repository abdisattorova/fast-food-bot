package uz.pdp.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.model.TwilioVerification;
import uz.pdp.model.absClasses.BotUser;
import uz.pdp.util.Constants;

import static uz.pdp.util.Constants.*;
import static uz.pdp.util.Db.twilioVerificationList;


public class TwilioService {

    public static String code = "";

    public static TwilioVerification getOrCreateTwilioVerification(BotUser user) {
        for (TwilioVerification verification : twilioVerificationList) {
            if (verification.getUser().getChatId() == (user.getChatId())) {
                return verification;
            }
        }
        TwilioVerification verification = new TwilioVerification(user);
        twilioVerificationList.add(verification);
        return verification;
    }

    public static String sendCode(BotUser user) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        code = String.valueOf((int) ((Math.random() * (999999 - 100000)) + 100000));
        try {
            Message message = Message.creator(
                    new PhoneNumber(user.getPhoneNumber()),
                    new PhoneNumber(TWILIO_PHONE),
                    "Confirmation code : " + code
            ).create();
            TwilioVerification verification = getOrCreateTwilioVerification(user);
            verification.setCode(code);
            verification.setVerified(false);
            saveVerificationChanges(verification);
            return code;
        } catch (Exception e) {
            TwilioVerification verification = getOrCreateTwilioVerification(user);
            verification.setCode(code);
            verification.setVerified(false);
            saveVerificationChanges(verification);
            return code;
        }
    }

    public static void saveVerificationChanges(TwilioVerification changedVerification) {
        for (TwilioVerification verification : twilioVerificationList) {
            if (verification.getId().equals(changedVerification.getId())) {
                twilioVerificationList.remove(verification);
                twilioVerificationList.add(changedVerification);
            }
        }
    }

    public static boolean getVerifiedCode(Update update, BotUser user) {
        TwilioVerification verification = getOrCreateTwilioVerification(user);
        if (verification.getCode().equals(update.getMessage().getText())) {
            verification.setVerified(true);
            saveVerificationChanges(verification);
            return true;
        }
        return false;

    }

}
