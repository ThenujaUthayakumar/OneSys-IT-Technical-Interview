package com.onesys.onesys.util;

import com.google.gson.JsonParseException;
import com.onesys.onesys.entity.UserEntity;
import com.onesys.onesys.repository.UserRepository;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggedInUser {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public UserEntity validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            System.out.println("Processing Token: " + token);

            // Extract username from the token
            String username = jwtUtil.extractUsername(token);
            UserEntity userEntity = userRepository.findByUsername(username);

            if (userEntity == null) {
                throw new SecurityException("User not found!");
            }

            Integer roleId = userEntity.getRoleId().getId();
            if (!jwtUtil.validateToken(token, userEntity.getUsername(), roleId)) {
                throw new SecurityException("Invalid token!");
            }

            System.out.println("Validated User: " + userEntity.getUsername() + ", Role ID: " + userEntity.getRoleId());
            return userEntity;

        } catch (MalformedJwtException | JsonParseException e) {
            System.err.println("Invalid Token: " + token);
            throw new SecurityException("Token is malformed or invalid: " + token, e);
        }
    }
}
