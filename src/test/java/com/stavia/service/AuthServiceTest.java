package com.stavia.service;

import com.stavia.dto.auth.LoginRequestDto;
import com.stavia.dto.auth.LoginResponseDto;
import com.stavia.entity.User;
import com.stavia.enums.Role;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSuccess() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@test.com");
        dto.setPassword("12345");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setRole(Role.GUEST);

        when(userService.findByEmail("test@test.com")).thenReturn(user);
        when(passwordEncoder.matches("12345", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any()))
                .thenReturn("jwt-token");

        LoginResponseDto response = authService.login(dto);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole().name(), response.getRole());
    }

    @Test
    void loginShouldThrowWhenEmailNull() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setPassword("123");

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(dto));
    }

    @Test
    void loginShouldThrowWhenPasswordWrong() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@test.com");
        dto.setPassword("wrong");

        User user = new User();
        user.setPassword("encoded");

        when(userService.findByEmail(any())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(dto));
    }
}