package com.campus.user.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.security.JwtTokenProvider;
import com.campus.log.repository.SystemLogRepository;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private SystemLogRepository systemLogRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, jwtTokenProvider, systemLogRepository);
    }

    @Test
    void registerStudent_shouldCreateUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUserType("STUDENT");
        req.setStudentId("2021001");
        req.setName("张三");
        req.setPassword("123456");
        req.setPhone("13800001111");

        when(userRepository.existsByStudentId("2021001")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        LoginResponse response = userService.register(req);
        assertNotNull(response.getUser());
        assertEquals("张三", response.getUser().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerDuplicateStudentId_shouldThrow() {
        RegisterRequest req = new RegisterRequest();
        req.setUserType("STUDENT");
        req.setStudentId("2021001");
        req.setName("张三");
        req.setPassword("123456");
        req.setPhone("13800001111");

        when(userRepository.existsByStudentId("2021001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.register(req));
    }

    @Test
    void loginSuccess_shouldReturnToken() {
        LoginRequest req = new LoginRequest();
        req.setUserType("STUDENT");
        req.setAccount("2021001");
        req.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setUsername("2021001");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUserType("STUDENT");
        user.setRole("USER");
        user.setEnabled(true);
        user.setName("张三");

        when(userRepository.findByStudentId("2021001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(1L, "2021001", "USER")).thenReturn("test.jwt.token");

        LoginResponse response = userService.login(req);
        assertEquals("test.jwt.token", response.getToken());
        assertEquals("张三", response.getUser().getName());
    }

    @Test
    void loginDisabledUser_shouldThrow() {
        LoginRequest req = new LoginRequest();
        req.setUserType("STUDENT");
        req.setAccount("2021001");
        req.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setEnabled(false);

        when(userRepository.findByStudentId("2021001")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> userService.login(req));
    }

    @Test
    void resetPasswordSuccess_shouldReturnNewPassword() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@campus.com");
        user.setUserType("STUDENT");
        user.setName("张三");
        user.setPassword(passwordEncoder.encode("oldpass"));

        when(userRepository.findByStudentId("2021001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        String newPassword = userService.resetPassword("test@campus.com", "2021001", null);
        assertNotNull(newPassword);
        assertEquals(8, newPassword.length());
    }

    @Test
    void resetPasswordWrongEmail_shouldThrow() {
        User user = new User();
        user.setId(1L);
        user.setEmail("real@campus.com");

        when(userRepository.findByStudentId("2021001")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class,
                () -> userService.resetPassword("wrong@campus.com", "2021001", null));
    }
}
