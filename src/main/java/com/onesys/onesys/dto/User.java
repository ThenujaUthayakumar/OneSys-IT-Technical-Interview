package com.onesys.onesys.dto;

import com.onesys.onesys.entity.RoleEntity;
import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private RoleEntity roleId;
    private String firstName;
    private String lastName;
    private String email;
    private String searchLike;
}
