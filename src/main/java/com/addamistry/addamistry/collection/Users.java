package com.addamistry.addamistry.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Document
@Data
public class Users implements UserDetails {

    @Id
    String id;

    @Enumerated(EnumType.STRING)
    private Role role;
    private String firstname;

    private String lastname;
    
    
    private String email;

    private String phonenumber;

    @JsonIgnore
    private String password;

    public Users(String firstname, String lastname, String email, String password,String phonenumber,Role role) {
        this.firstname = firstname;
        this.lastname=lastname;
        this.email=email;
        this.password =password;
        this.phonenumber= phonenumber;
        this.role =role;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return phonenumber;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
