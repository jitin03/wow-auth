package com.addamistry.addamistry.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NonNull
    private String email;

    private Integer phoneNo;

    @NonNull
    private String password;
}
