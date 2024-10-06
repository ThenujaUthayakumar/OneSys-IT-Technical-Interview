package com.onesys.onesys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Employee {
    private Integer id;
    private String firstName;
    private String lastName;
    private String address;
    private String contactNumber;
    private String email;
    private List<String> attachment;
    private String dateOfBirth;
    private String currentAgeInDays;
    private String gender;
    private String referenceNumber;
    private String searchLike;
}
