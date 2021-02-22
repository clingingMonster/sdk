package so.dian.export.sdk.core.sender;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import so.dian.export.sdk.core.model.SimpleMessage;

import java.util.Date;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
public class EmailSender implements Sender {

    private MailSender mailSender;

    private String fromName;


    public EmailSender(MailSender mailSender, String fromName) {
        this.fromName = fromName;
        this.mailSender = mailSender;
    }

    @Override
    public boolean sendMessage(SimpleMessage simpleMessage) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromName);
        simpleMailMessage.setText(simpleMessage.getContent());
        simpleMailMessage.setSentDate(new Date());
        simpleMailMessage.setSubject(simpleMessage.getTitle());
        simpleMailMessage.setTo(simpleMessage.getAddresses());
        mailSender.send(simpleMailMessage);
        return true;
    }
}
