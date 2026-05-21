package com.stavia.service;

import com.stavia.dto.user.UserEditDto;
import com.stavia.dto.user.UserRegisterDto;
import com.stavia.dto.user.UserResponseDto;
import com.stavia.entity.User;
import com.stavia.exception.AlreadyExistsException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.UserMapper;
import com.stavia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    @Test
    void registerSuccess() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@test.com");
        dto.setPassword("123");

        when(repository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(repository.save(any())).thenReturn(new User());
        when(mapper.toResponseDto(any())).thenReturn(new UserResponseDto());

        UserResponseDto response = service.register(dto);

        assertNotNull(response);

        verify(repository).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenEmailExists() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@test.com");

        when(repository.existsByEmail(any())).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.register(dto));
    }

    @Test
    void editSuccess() {
        User user = new User();

        UserEditDto dto = new UserEditDto();
        dto.setFirstName("John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.save(any())).thenReturn(user);
        when(mapper.toResponseDto(any())).thenReturn(new UserResponseDto());

        UserResponseDto response = service.edit(1L, dto);

        assertNotNull(response);

        verify(repository).save(user);
    }

    @Test
    void findByEmailShouldThrow() {
        when(repository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findByEmail("a@test.com"));
    }

    @Test
    void getByIdSuccess() {
        User user = new User();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponseDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto response = service.getById(1L);

        assertNotNull(response);
        verify(repository).findById(1L);
    }

    @Test
    void deleteSuccess() {
        User user = new User();
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        assertTrue(user.getDeleted());
        verify(repository).save(user);
    }
}