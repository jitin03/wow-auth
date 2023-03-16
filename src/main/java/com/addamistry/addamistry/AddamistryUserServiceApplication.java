package com.addamistry.addamistry;

import com.addamistry.addamistry.config.TwilioConfig;
import com.twilio.Twilio;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@OpenAPIDefinition( info = @Info(title = "Spring boot otp authentication",version = "1.0.0"))
public class AddamistryUserServiceApplication {
	@Autowired
	private TwilioConfig twilioConfig;
	public static void main(String[] args) {
		SpringApplication.run(AddamistryUserServiceApplication.class, args);
	}



	@PostConstruct
	public void initTwilio(){
		Twilio.init(twilioConfig.getAccountSid(),twilioConfig.getAuthToken());
	}
}
