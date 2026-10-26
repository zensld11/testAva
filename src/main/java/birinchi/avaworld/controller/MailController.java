package birinchi.avaworld.controller;

import birinchi.avaworld.service.MailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
@AllArgsConstructor
public class MailController {
    private final MailService mailService;

    @GetMapping("/sent")
    public ResponseEntity<String> sentEmail(@RequestParam("id") Long id) {
        mailService.sendEmailWithDocs(id);
        return ResponseEntity.ok("Отправлено письмо на почту");
    }
}
