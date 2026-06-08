package com.example.rentlyauth.dto;

import com.example.rentlyauth.model.UsersEntity;
import lombok.Data;

@Data
public class ReturnUserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public static ReturnUserDTO fromEntity(UsersEntity u) {
        ReturnUserDTO dto = new ReturnUserDTO();
        dto.id = u.getId();
        dto.username = u.getUsername();
        dto.email = u.getEmail();
        dto.firstName = u.getFirstName();
        dto.lastName = u.getLastName();
        return dto;
    }
}
