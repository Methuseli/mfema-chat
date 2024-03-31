package com.mfemachat.chatapp.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mfemachat.chatapp.dto.ConnectionUpdateDto;
import com.mfemachat.chatapp.models.Connection;
import com.mfemachat.chatapp.service.ConnectionService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/connections")
@AllArgsConstructor
public class ConnectionController {
    private ConnectionService connectionService;

    @PostMapping("/request-connection") 
    public Mono<ResponseEntity<Connection>> requestConnection(@RequestBody Connection connection){
        return connectionService.connectUsers(connection).map(
            newConnection -> ResponseEntity.status(HttpStatus.CREATED).body(newConnection)
        );
    }

    @PatchMapping("/update-connection/{id}")
    public Mono<ResponseEntity<Connection>> updateConnection(@PathVariable UUID id, @RequestBody ConnectionUpdateDto connectionDto) {
        return connectionService.updateConnection(id, connectionDto).map(
            update -> ResponseEntity.status(HttpStatus.OK).body(update)
        );
    }
    

    @GetMapping("/user-connections")
    public Flux<Connection> getUserConnections(
        @RequestParam(name = "user_id") UUID userId,  @RequestParam(name = "page") int page, @RequestParam(name ="size") int size
    ) {
        return connectionService.getConnections(userId, page, size);
    }
}
