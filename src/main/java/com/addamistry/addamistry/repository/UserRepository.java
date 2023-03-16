package com.addamistry.addamistry.repository;

import com.addamistry.addamistry.collection.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<Users,String> {

//    Users findByUserName(String username);
    Optional<Users> findByEmail(String email);

    Optional<Users> findByPhonenumber(String phoneNo);

}
