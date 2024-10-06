package com.onesys.onesys.controller;

import com.onesys.onesys.dto.User;
import com.onesys.onesys.entity.UserEntity;
import com.onesys.onesys.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /*-------------------- USER CREATE ---------------*/
    @PostMapping("/register")
    public UserEntity register(@RequestBody User user) throws ParseException {
        return userService.register(user);
    }

    /*------------------USER LOGIN ------------------*/
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserEntity userEntity) {
        return userService.login(userEntity);
    }

    /*-------------------- GET ALL USERS -------------------*/
    @GetMapping("/get-all")
    public Page<UserEntity> getUser(@RequestParam(required = false) Integer skip, @RequestParam(required = false) Integer limit, @RequestParam(required = false) String orderBy, User user) {
        return userService.getAll(skip, limit, orderBy, user);
    }
    /*-------------------- USER UPDATE -----------------------*/
    @PutMapping("/update")
    public UserEntity update(@RequestBody User user) throws ParseException {
        return userService.update(user);
    }
    /*-------------------- RESET PASSWORD -------------------*/
}
