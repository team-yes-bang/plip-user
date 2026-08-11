package com.plip.user.adapter.out.email;

import com.plip.user.application.port.out.EmailSendPort;
import com.plip.user.global.config.ResendProperties;
import com.plip.user.global.exception.BusinessException;
import com.plip.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendEmailAdapter implements EmailSendPort {

	private final RestClient resendRestClient;
	private final ResendProperties resendProperties;

	@Override
	public void sendOtp(String toEmail, String otpCode) {
		Map<String, Object> body = Map.of(
				"from", resendProperties.getFromEmail(),
				"to", List.of(toEmail),
				"subject", "[PLIP] 이메일 인증번호 안내",
				"html", buildOtpHtml(otpCode)
		);

		try {
			resendRestClient.post()
					.uri("/emails")
					.body(body)
					.retrieve()
					.toBodilessEntity();
		} catch (Exception e) {
			log.error("Resend API 호출 실패: toEmail={}", toEmail, e);
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
