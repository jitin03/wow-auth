package com.addamistry.addamistry.collection;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document
@Data
public class RefreshToken {

    @Id
    private String id;
    @DocumentReference
    private Users owner;
}
