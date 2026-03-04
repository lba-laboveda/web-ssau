package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.UserNotFoundException;
import ru.ssau.todo.mapper.UserMapper;
import ru.ssau.todo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return UserMapper.toDto(user);
    }
}