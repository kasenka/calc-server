package org.example.calc_server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String username;
    private String encryptedPassword;
    private String role;
}
