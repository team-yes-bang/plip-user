package com.plip.user.adapter.out.email;

import com.plip.user.application.port.out.EmailSendPort;
import com.plip.user.global.config.MailProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SmtpEmailAdapter implements EmailSendPort {

	private final JavaMailSender mailSender;
	private final MailProperties mailProperties;

	@Override
	public void sendOtp(String toEmail, String otpCode) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(mailProperties.getFrom());
			helper.setTo(toEmail);
			helper.setSubject("[PLIP] 이메일 인증번호 안내");
			helper.setText(buildOtpHtml(otpCode), true);
			mailSender.send(message);
		} catch (Exception e) {
			log.error("SMTP 메일 발송 실패: toEmail={}", toEmail, e);
			throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	private String buildOtpHtml(String otpCode) {
		return "<div style=\"font-family:sans-serif;max-width:400px;margin:0 auto;padding:24px\">"
				+ "<h2>PLIP 이메일 인증</h2>"
				+ "<p>아래 인증번호를 입력해주세요.</p>"
				+ "<div style=\"font-size:32px;font-weight:bold;letter-spacing:8px;"
				+ "text-align:center;padding:16px;background:#f4f4f4;border-radius:8px\">"
				+ otpCode
				+ "</div>"
				+ "<p style=\"color:#888;font-size:13px;margin-top:16px\">"
				+ "인증번호는 2분간 유효합니다.</p>"
				+ "</div>";
	}
}
