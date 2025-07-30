package com.project.InsightPrep.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.auth.entity.EmailVerification;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.EmailMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "username", "noreply@yourdomain.com");
    }

    @Test
    @DisplayName("이메일 전송 실패")
    void sendEmail_throwsRuntimeException_whenEmailSenderFails() throws MessagingException {
        // given
        String toEmail = "user@test.com";
        String title = "제목";
        String content = "내용";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mockMessage);

        doThrow(new RuntimeException("메일 전송 실패")).when(emailSender).send(any(MimeMessage.class));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendEmail(toEmail, title, content);
        });
        System.out.println("Actual exception message: " + exception.getMessage());

        assertTrue(exception.getMessage().contains("Unable to send in sendEmail"));
    }

    @Test
    @DisplayName("이메일 폼 생성 테스트")
    void createEmailForm_shouldReturnCorrectSimpleMailMessage() {
        // given
        String toEmail = "test@insightprep.com";
        String title = "테스트 제목";
        String text = "본문 내용";

        // when
        SimpleMailMessage result = emailService.createEmailForm(toEmail, title, text);

        // then
        assertEquals(toEmail, result.getTo()[0]);
        assertEquals(title, result.getSubject());
        assertEquals(text, result.getText());
    }

    @Test
    @DisplayName("이메일 중복 - 중복일 경우 예외 발생")
    void existEmail_Duplicate_ThrowsException() {
        String email = "test@example.com";
        given(authMapper.existEmail(email)).willReturn(true);

        assertThrows(AuthException.class, () -> emailService.existEmail(email));
    }

    @Test
    @DisplayName("이메일 인증 코드 만료 - 삭제 수행")
    void currentEmailExisting_ExpiredCode_Deletes() throws Exception {
        // given
        String email = "test@example.com";
        EmailVerification expired = EmailVerification.builder()
                .email(email)
                .expiresTime(LocalDateTime.now().minusMinutes(1))
                .build();
        given(emailMapper.findByEmail(email)).willReturn(expired);

        // 필요 mock 설정
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(emailSender.createMimeMessage()).willReturn(mimeMessage);
        doNothing().when(emailSender).send(any(MimeMessage.class));

        // username 필드 수동 주입
        Field usernameField = EmailServiceImpl.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        usernameField.set(emailService, "noreply@example.com");

        // when
        emailService.sendCodeToEmail(email);

        // then
        verify(emailMapper).deleteByEmail(email);
    }

    @Test
    @DisplayName("이메일 인증 코드 존재하지만 아직 유효 - 예외 발생")
    void currentEmailExisting_NotExpired_ThrowsException() {
        String email = "test@example.com";
        EmailVerification notExpired = EmailVerification.builder()
                .email(email)
                .expiresTime(LocalDateTime.now().plusMinutes(5))
                .build();
        given(emailMapper.findByEmail(email)).willReturn(notExpired);

        assertThrows(AuthException.class, () -> emailService.sendCodeToEmail(email));
    }

    @Test
    @DisplayName("인증 코드 일치하고 유효 - 인증 성공")
    void verifyCode_Valid_Success() {
        String email = "test@example.com";
        String code = "ABC123";
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresTime(LocalDateTime.now().plusMinutes(1))
                .build();
        given(emailMapper.findByEmailAndCode(email, code)).willReturn(Optional.of(verification));

        boolean result = emailService.verifyCode(email, code);

        assertTrue(result);
        verify(emailMapper).updateVerified(email, code);
    }

    @Test
    @DisplayName("인증 코드 만료 - 예외 발생")
    void verifyCode_Expired_ThrowsException() {
        String email = "test@example.com";
        String code = "ABC123";
        EmailVerification expired = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresTime(LocalDateTime.now().minusMinutes(1))
                .build();
        given(emailMapper.findByEmailAndCode(email, code)).willReturn(Optional.of(expired));

        assertThrows(AuthException.class, () -> emailService.verifyCode(email, code));
    }

    @Test
    @DisplayName("인증 코드 불일치 - 예외 발생")
    void verifyCode_NotFound_ThrowsException() {
        given(emailMapper.findByEmailAndCode(anyString(), anyString())).willReturn(Optional.empty());

        assertThrows(AuthException.class, () -> emailService.verifyCode("test@example.com", "ABC123"));
    }

    @Test
    @DisplayName("인증된 이메일 검증 - 실패 시 예외 발생")
    void validateEmailVerified_Invalid_ThrowsException() {
        given(emailMapper.findByEmail(anyString())).willReturn(null);

        assertThrows(AuthException.class, () -> emailService.validateEmailVerified("test@example.com"));
    }

    @Test
    @DisplayName("인증 코드 만료 테스트")
    void deleteExpiredVerificationCodes_shouldCallEmailMapperOnce() {
        // when
        emailService.deleteExpiredVerificationCodes();

        // then
        verify(emailMapper, times(1)).deleteByExpiresTimeBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("이메일 인증 여부 검증 테스트 - 실패")
    void validateEmailVerified_shouldThrowException_whenNotVerified() {
        // given
        String email = "user@test.com";
        EmailVerification notVerified = new EmailVerification(1L, "test@example.com", "XWL9WS", false, LocalDateTime.now().plusMinutes(5));

        when(emailMapper.findByEmail(email)).thenReturn(notVerified);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () -> {
            emailService.validateEmailVerified(email);
        });

        assertEquals(AuthErrorCode.EMAIL_VERIFICATION_ERROR, exception.getErrorCode());
    }
}