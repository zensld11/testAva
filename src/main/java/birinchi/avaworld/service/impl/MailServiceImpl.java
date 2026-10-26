package birinchi.avaworld.service.impl;

import birinchi.avaworld.model.entity.WhiteListCounterparty;
import birinchi.avaworld.repository.WhiteListCounterpartyRepository;
import birinchi.avaworld.service.MailService;
import birinchi.avaworld.service.WhiteListService;
import birinchi.avaworld.util.DocumentFormater;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender javaMailSender;
    private final WhiteListCounterpartyRepository repository;
    private final WhiteListService whiteListService;

    @Value("${spring.mail.from}")
    private String mailFrom;

    @Value("${spring.mail.to}")
    private String mailTo;

    @Override
    public void sendEmailWithDocs(Long id) {
        WhiteListCounterparty entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        S3Object front = null;
        S3Object back = null;
        if (entity.getFrontUri() != null && entity.getBackUri() != null) {
            front = whiteListService.getDocFromS3(entity.getFrontUri());
            back = whiteListService.getDocFromS3(entity.getBackUri());
        }

        byte[] byteFront = front != null ? getByteDocs(front) : null;
        byte[] byteBack = back != null ? getByteDocs(back) : null;

        try {
            String text = buildEmailBody();

            MimeMessage mailMessage = createMailMessage(text, byteFront, byteBack);
            javaMailSender.send(mailMessage);
            log.info("[#sendEmailWithDocs] Email sent successfully: {}", mailMessage.getDescription());
        } catch (Exception e) {
            log.error("[#sendEmailWithDocs] Error sending email: {}", e.getMessage());
        }
    }

    private MimeMessage createMailMessage(String text, byte[] byteFront, byte[] byteBack) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        text = text.replace("${front}", "cid:front");
        text = text.replace("${back}", "cid:back");

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailFrom);
        helper.setTo(mailTo);
        helper.setSubject("Уведомление для проверки изображений");
        helper.setText(text);

        helper.addInline("front",
                new ByteArrayResource(byteFront),
                DocumentFormater.detectType(byteFront));
        helper.addInline("back",
                new ByteArrayResource(byteBack),
                DocumentFormater.detectType(byteBack));
        return message;
    }

    private byte[] getByteDocs(S3Object docs) {
        if (docs != null) {
            try {
                byte[] doc = docs.getObjectContent().readAllBytes();
                return doc;
            } catch (IOException e) {
                log.warn("Error while make byte[] from s3 object {}", e.getMessage());
            }
        }
        return null;
    }

    private String buildEmailBody() {

        return null;
    }
}