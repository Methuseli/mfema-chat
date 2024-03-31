package com.mfemachat.chatapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mfemachat.chatapp.data.ConnectionRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.service.ConnectionService;
import com.mfemachat.chatapp.service.ConnectionServiceImpl;

@Configuration
public class ConnectionConfig {
    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Bean
    ConnectionService connectionService() {
        return new ConnectionServiceImpl(connectionRepository, userRepository);
    }

}
