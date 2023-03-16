package com.addamistry.addamistry.service;

import com.addamistry.addamistry.collection.Users;
import com.addamistry.addamistry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository repository;
//    @Override
//    public UserDetails loadUserByUsername(String phoneNo) throws UsernameNotFoundException {
//        return  repository.findByEmail(phoneNo)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }

    @Override
    public UserDetails loadUserByUsername(String phoneNo) throws UsernameNotFoundException {
        return  repository.findByPhonenumber(phoneNo)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public Users findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("user id not found"));
    }
}
