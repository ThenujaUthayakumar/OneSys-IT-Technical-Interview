package com.onesys.onesys.service;

import com.onesys.onesys.dto.User;
import com.onesys.onesys.entity.RoleEntity;
import com.onesys.onesys.entity.UserEntity;
import com.onesys.onesys.repository.RoleRepository;
import com.onesys.onesys.repository.UserRepository;
import com.onesys.onesys.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity register(User user) throws ParseException {
        UserEntity userEntity=new UserEntity();

        if(user.getFirstName() == null || user.getFirstName().isEmpty())
        {
            throw new IllegalStateException("Please Enter First Name !");
        }

        if(user.getLastName() == null || user.getLastName().isEmpty())
        {
            throw new IllegalStateException("Please Enter Last Name !");
        }

        if(user.getEmail() == null || user.getEmail().isEmpty())
        {
            throw new IllegalStateException("Please Enter Patient E-mail !");
        }

        if(user.getUsername() == null || user.getUsername().isEmpty())
        {
            throw new IllegalStateException("Please Enter Username !");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalStateException("Username Already Exists !");
        }

        Optional<RoleEntity> roleEntity=roleRepository.findById(user.getRoleId().getId());

        if(user.getRoleId() == null)
        {
            throw new IllegalStateException("Please Select User Role !");
        }

        if (roleEntity.isEmpty())
        {
            throw new IllegalStateException("User Role Not Found !");
        }

        if(user.getPassword() ==null)
        {
            throw new IllegalStateException("Please Enter Password !");
        }

        if((user.getPassword() != null) && (!user.getPassword().isEmpty())) {
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setEmail(user.getEmail());
        userEntity.setUsername(user.getUsername());
        userEntity.setRoleId(user.getRoleId());
        return userRepository.save(userEntity);
    }

    /*--------------------- USER LOGIN -------------------*/
    public ResponseEntity<Map<String, String>> login(UserEntity userEntity) {
        UserEntity user = userRepository.findByUsername(userEntity.getUsername());
        if (user != null && passwordEncoder.matches(userEntity.getPassword(), user.getPassword())) {
            String token = jwtUtil.createToken(user.getUsername(), user.getRoleId().getId());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("roleId", String.valueOf(user.getRoleId().getId()));

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /*-------------------- GET ALL USERS -------------------*/
    public Page<UserEntity> getAll(Integer pageNo, Integer pageSize, String orderBy, User user) {

        Pageable pageable = null;
        List<Sort.Order> sorts = new ArrayList<>();
        if (orderBy != null) {
            String[] split = orderBy.split("&");
            for (String s : split) {
                String[] orders = s.split(",");
                sorts.add(new Sort.Order(Sort.Direction.valueOf(orders[1]), orders[0]));
            }
        }
        if (pageNo != null && pageSize != null) {
            if (orderBy != null) {
                pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(pageNo, pageSize);
            }
        } else {
            if (orderBy != null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }
        }

        String searchLike = null;
        if(user.getSearchLike() != null){
            searchLike = "%"+user.getSearchLike()+"%";
        }
        Page<UserEntity> userEntities;

        userEntities = userRepository.findByColumns(pageable,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleId(),
                searchLike);
        return userEntities;
    }

    /*-------------------- USER UPDATE -----------------------*/
    @Transactional
    public UserEntity update(User user) throws ParseException {
        UserEntity userEntity=userRepository.findById(user.getId()).orElseThrow(()-> new IllegalStateException(
                "User with id " + user.getId() + "Doesn't Exists !"
        ));

        if(user.getFirstName() == null || user.getFirstName().isEmpty())
        {
            throw new IllegalStateException("Please Enter First Name !");
        }

        if(user.getLastName() == null || user.getLastName().isEmpty())
        {
            throw new IllegalStateException("Please Enter Last Name !");
        }

        if(user.getEmail() == null || user.getEmail().isEmpty())
        {
            throw new IllegalStateException("Please Enter Patient E-mail !");
        }

        if(user.getUsername() == null || user.getUsername().isEmpty())
        {
            throw new IllegalStateException("Please Enter Username !");
        }

        Optional<RoleEntity> roleEntity=roleRepository.findById(user.getRoleId().getId());

        if(user.getRoleId() == null)
        {
            throw new IllegalStateException("Please Select User Role !");
        }

        if (roleEntity.isEmpty())
        {
            throw new IllegalStateException("User Role Not Found !");
        }

        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setEmail(user.getEmail());
        userEntity.setUsername(user.getUsername());
        userEntity.setRoleId(user.getRoleId());
        return userRepository.save(userEntity);
    }
}