package com.addamistry.addamistry.dto;

import com.addamistry.addamistry.collection.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class SignupDTO{


    private String firstname;
    private Role role;
    private String lastname;
    @NonNull
    @Size(max = 60)
    private String email;

    private String phonenumber;

    @Size(min = 6, max = 60)
    private String password;
}
