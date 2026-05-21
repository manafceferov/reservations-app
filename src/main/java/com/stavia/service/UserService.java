package com.stavia.service;

import com.stavia.dto.user.UserEditDto;
import com.stavia.dto.user.UserRegisterDto;
import com.stavia.dto.user.UserResponseDto;
import com.stavia.entity.User;
import com.stavia.enums.Role;
import com.stavia.exception.AlreadyExistsException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.UserMapper;
import com.stavia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto register(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new AlreadyExistsException("Bu email artıq mövcuddur");

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.GUEST);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    public UserResponseDto getById(Long id) {
        return userMapper.toResponseDto(findById(id));
    }

    @Transactional
    public UserResponseDto edit(Long id, UserEditDto dto) {
        User user = findById(id);
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı"));
    }
}