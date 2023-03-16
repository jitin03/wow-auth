package com.addamistry.addamistry.controller;

import com.addamistry.addamistry.collection.RefreshToken;
import com.addamistry.addamistry.collection.Users;
import com.addamistry.addamistry.config.JwtService;
import com.addamistry.addamistry.dto.AuthRequest;
import com.addamistry.addamistry.dto.LoginDTO;
import com.addamistry.addamistry.dto.SignupDTO;
import com.addamistry.addamistry.dto.TokenDTO;
import com.addamistry.addamistry.repository.RefreshTokenRepository;
import com.addamistry.addamistry.repository.UserRepository;
import com.addamistry.addamistry.service.OtpService;
import com.addamistry.addamistry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthRest {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtService jwtHelper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;

    @Autowired
    private OtpService otpService;

    @RequestMapping(value = "requestOtp/{phoneNo}",method = RequestMethod.GET)
    public Map<String,Object> getOtp(@PathVariable String phoneNo){
        Map<String,Object> returnMap=new HashMap<>();
        try{
            //generate OTP
            String otp = otpService.generateOtp(phoneNo);
            returnMap.put("otp", otp);
            returnMap.put("status","success");
            returnMap.put("message","Otp sent successfully");
        }catch (Exception e){
            returnMap.put("status","failed");
            returnMap.put("message",e.getMessage());
        }

        return returnMap;
    }

    @RequestMapping(value = "verifyOtp/",method = RequestMethod.POST)
    public Map<String,Object> verifyOtp(@RequestBody AuthRequest authenticationRequest){
        Map<String,Object> returnMap=new HashMap<>();
        try{
            //verify otp
            if(authenticationRequest.getOtp().equals(otpService.getCacheOtp(authenticationRequest.getPhoneNo()))){
                ArrayList<String> jwtToken = createAuthenticationToken(authenticationRequest);
                returnMap.put("status","success");
                returnMap.put("message","Otp verified successfully");
                returnMap.put("jwt",jwtToken.get(0));
                returnMap.put("userId",jwtToken.get(1));
                otpService.clearOtp(authenticationRequest.getPhoneNo());
            }else{
                returnMap.put("status","success");
                returnMap.put("message","Otp is either expired or incorrect");
            }

        } catch (Exception e){
            returnMap.put("status","failed");
            returnMap.put("message",e.getMessage());
        }

        return returnMap;
    }

    //create auth token
    public ArrayList<String> createAuthenticationToken(AuthRequest authenticationRequest) throws Exception {
        ArrayList<String> auths = new ArrayList<>();
        Users user;
        try {
            Authentication authentication =  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getPhoneNo(), "Password")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
             user = (Users) authentication.getPrincipal();
            System.out.println(user.getId());
        }
        catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userService
                .loadUserByUsername(authenticationRequest.getPhoneNo());
        auths.add(jwtHelper.generateToken(userDetails));
        auths.add(user.getId());

        return auths;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Users user = (Users) authentication.getPrincipal();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setOwner(user);
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtHelper.generateToken(user);
        String refreshTokenString = jwtHelper.generateRefreshToken(user,refreshToken);

        return ResponseEntity.ok(new TokenDTO(user.getId(), accessToken, refreshTokenString));
    }




    @PostMapping("signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupDTO dto) {
        Users user = new Users(dto.getFirstname(), dto.getLastname(),dto.getEmail(), passwordEncoder.encode("Password"),dto.getPhonenumber(),dto.getRole());
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setOwner(user);
        System.out.println(refreshToken);
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtHelper.generateToken(user);
        String refreshTokenString = jwtHelper.generateRefreshToken(user,refreshToken);

        return ResponseEntity.ok(new TokenDTO(user.getId(), accessToken, refreshTokenString));
    }



    @PostMapping("logout")
    public ResponseEntity<?> logout(@RequestBody TokenDTO dto) {
        String refreshTokenString = dto.getRefreshToken();
        if (refreshTokenRepository.existsById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString))) {
            // valid and exists in db
            refreshTokenRepository.deleteById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString));
            return ResponseEntity.ok().build();
        }

        throw new BadCredentialsException("invalid token");
    }

    @PostMapping("logout-all")
    public ResponseEntity<?> logoutAll(@RequestBody TokenDTO dto) {
        String refreshTokenString = dto.getRefreshToken();
        if ( refreshTokenRepository.existsById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString))) {
            // valid and exists in db

            refreshTokenRepository.deleteByOwner_Id(jwtHelper.getUserIdFromRefreshToken(refreshTokenString));
            return ResponseEntity.ok().build();
        }

        throw new BadCredentialsException("invalid token");
    }



    @PostMapping("access-token")
    public ResponseEntity<?> accessToken(@RequestBody TokenDTO dto) {
        String refreshTokenString = dto.getRefreshToken();
        if ( refreshTokenRepository.existsById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString))) {
            // valid and exists in db

            Users user = userService.findById(jwtHelper.getUserIdFromRefreshToken(refreshTokenString));
            String accessToken = jwtHelper.generateToken(user);

            return ResponseEntity.ok(new TokenDTO(user.getId(), accessToken, refreshTokenString));
        }

        throw new BadCredentialsException("invalid token");
    }

    @PostMapping("refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenDTO dto) {
        String refreshTokenString = dto.getRefreshToken();
        if ( refreshTokenRepository.existsById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString))) {
            // valid and exists in db

            refreshTokenRepository.deleteById(jwtHelper.getTokenIdFromRefreshToken(refreshTokenString));

            Users user = userService.findById(jwtHelper.getUserIdFromRefreshToken(refreshTokenString));

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setOwner(user);
            refreshTokenRepository.save(refreshToken);

            String accessToken = jwtHelper.generateToken(user);
            String newRefreshTokenString = jwtHelper.generateRefreshToken(user, refreshToken);

            return ResponseEntity.ok(new TokenDTO(user.getId(), accessToken, newRefreshTokenString));
        }

        throw new BadCredentialsException("invalid token");
    }
}
