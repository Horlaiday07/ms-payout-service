package com.remit.mellonsecure.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        log.info("Admin users list requested");
        return ResponseEntity.ok(List.of(
                Map.of("id", "1", "username", "admin", "role", "ADMIN")
        ));
    }
}
