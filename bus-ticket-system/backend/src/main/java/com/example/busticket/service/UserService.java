package com.example.busticket.service;

import com.example.busticket.dto.UserResponse;
import com.example.busticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MapperService mapperService;

    public UserService(UserRepository userRepository, MapperService mapperService) {
        this.userRepository = userRepository;
        this.mapperService = mapperService;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(mapperService::toUserResponse).toList();
    }
}

