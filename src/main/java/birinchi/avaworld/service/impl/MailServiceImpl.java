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
import org.springframework.ui.freemarker.SpringTemplateLoader;
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

//    @Value("${spring.mail.to}")
//    private String mailTo;

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
            log.error("[#sendEmailWithDocs] Error sending email", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось отправить письмо",
                    e
            );
        }
    }

    private MimeMessage createMailMessage(String text, byte[] byteFront, byte[] byteBack) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        text = text.replace("${front}", "cid:front");
        text = text.replace("${back}", "cid:back");

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailFrom);
        helper.setTo("killopowqwert@gmail.com");
        helper.setSubject("Уведомление для проверки изображений");
        helper.setText(text, true);

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
        String htmlText = """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Совпадение по Белому Списку</title>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; margin: 0; padding: 0; }
                        .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                        h1, h2 { color: #0056b3; }
                        h1 { font-size: 24px; margin-bottom: 10px; }
                        h2 { font-size: 18px; margin-bottom: 10px; }
                        ul { list-style-type: none; padding: 0; }
                        li { margin-bottom: 8px; }
                        .section { margin-top: 20px; }
                        .footer { margin-top: 30px; font-size: 14px; color: #555; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Добрый день!</h1>
                        <h2>В приложении выявлено совпадение по Контрагенту</h2>
                
                        <div class="section">
                            <h2>Данные по Контрагенту</h2>
                            <ul>
                                <li><strong>Название/ФИО:</strong> ${fullName}</li>
                            </ul>
                        </div>
                
                        <div class="section">
                            <h2>Сведения о субъекте в ЧС:</h2>
                            <ul>
                                <li><strong>ФИО:</strong> ${personFullName}</li>
                            </ul>
                        </div>
                
                        <div class="section">
                            <h2>Данные паспорта:</h2>
                            <img src="${front}" alt="Лицевая сторона" width="400"/>
                            <img src="${back}" alt="Оборотная сторона" width="400"/>
                        </div>
                    </div>
                </body>
                </html>
                """;
        String htmlBody = htmlText
                .replace("${fullName}", "Adelina Chto-to Tam")
                .replace("${personFullName}", "Chto-to");
        return htmlBody;
    }
}