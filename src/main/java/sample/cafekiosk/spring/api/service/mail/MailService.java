package sample.cafekiosk.spring.api.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.history.MailSendHistory;
import sample.cafekiosk.spring.domain.history.MailSendHistoryRepository;

@RequiredArgsConstructor
@Service
public class MailService {

    private final MailSendClient mailSendClient;
    private final MailSendHistoryRepository mailSendHistoryRepository;

    public boolean sendMail(String fromEmail, String toEmail, String subject, String content) {
        boolean sendResult = mailSendClient.sendEmail(fromEmail, toEmail, subject, content);
        if (sendResult) {
            mailSendHistoryRepository.save(MailSendHistory.builder()
                .fromEmail(fromEmail)
                .toEmail(toEmail)
                .subject(subject)
                .content(content)
                .build()
            );
            return true;
        }
        return false;
    }
}
