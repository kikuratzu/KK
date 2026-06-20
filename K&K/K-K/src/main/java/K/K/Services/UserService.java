package K.K.Services;

import K.K.Configurations.JWTUtil;
import K.K.Entities.Role;
import K.K.Entities.User;
import K.K.Entities.VerificationCode;
import K.K.Enums.ERole;
import K.K.K.DTOs.ChangePasswordDTO;
import K.K.K.DTOs.LoginUserDTO;
import K.K.K.DTOs.RegisterUserDTO;
import K.K.K.DTOs.changeUsernameDTO;
import K.K.Repositories.RoleRepository;
import K.K.Repositories.UserRepository;
import K.K.Repositories.VerificationCodeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
@Slf4j
public class UserService {


    private final AuthenticationManager authManager;
    private final JWTUtil service;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, AuthenticationManager authManager, JWTUtil service, UserRepository userRepository, RoleRepository roleRepository, EmailService emailService, VerificationCodeRepository verificationCodeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.service = service;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> verify(LoginUserDTO dto)
    {
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        if (authentication.isAuthenticated()){
            log.info("%s successfully authenticated".formatted(dto.getUsername()));
            User user = userRepository.findByUsername(dto.getUsername());
            String token = service.generateToken(user.getUsername(), user.getId());
            Map<UUID, String> idStringMap = new HashMap<>();
            idStringMap.put(user.getId(), token);

            return idStringMap;
        }
        else {
            log.warn("%s failed to authenticate".formatted(dto.getUsername()));
            throw new IllegalArgumentException("fail");
        }
    }

    @Transactional
    public void createUser(RegisterUserDTO dto){

      Role userRole = dto.getUsername().equals("kristian")
              ? roleRepository.findByName(ERole.ADMIN).orElseThrow(()
              -> new RuntimeException("Error: Role ADMIN not found in database."))
              : roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER not found in database."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        try {
            userRepository.save(User.builder()
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                            .roles(roles)
                    .build());
            log.info("new user %s created".formatted(dto.getUsername()));
        }
        catch (IllegalArgumentException ex){
            log.warn("illegal arguments, user could not be created!");
        }
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }



    @Cacheable("hello")
    public String hello(){
        System.out.println("hello");
        return "hello";
    }

    @Transactional
    public void initiateUsernameChangeFlow(final changeUsernameDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if (userRepository.existsByUsername(dto.getNewUsername())) {
            throw new IllegalArgumentException("The new username is already taken.");
        }

        List<VerificationCode> oldCodes = verificationCodeRepository.findByUsername(dto.getUsername());

        if(!oldCodes.isEmpty()){
            verificationCodeRepository.deleteAll(oldCodes);
        }

        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));

        VerificationCode verificationData = new VerificationCode(pin, user.getUsername());
        verificationCodeRepository.save(verificationData);

        emailService.sendVerificationCode(user.getEmail(), "Your Identity Verification Code", pin);
    }


    @Transactional
    public String changeUsername(final changeUsernameDTO dto, final String code) {

        VerificationCode savedToken = verificationCodeRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));

        if (!savedToken.getUsername().equals(dto.getUsername())) {
            throw new IllegalArgumentException("Code does not match this user identity.");
        }

        if (userRepository.existsByUsername(dto.getNewUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        User user = userRepository.findByUsername(dto.getUsername());

        user.setUsername(dto.getNewUsername());
        SecurityContextHolder.clearContext();
        userRepository.saveAndFlush(user);

        verificationCodeRepository.delete(savedToken);

        return service.generateToken(user.getUsername(), user.getId());
    }

    @Transactional
    public void initiatePasswordChange(final ChangePasswordDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        List<VerificationCode> oldCodes = verificationCodeRepository.findByUsername(dto.getUsername());

        if(!oldCodes.isEmpty()){
            verificationCodeRepository.deleteAll(oldCodes);
        }

        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));

        VerificationCode verificationData = new VerificationCode(pin, user.getUsername());
        verificationCodeRepository.save(verificationData);

        emailService.sendVerificationCode(user.getEmail(), "Your Identity Verification Code", pin);
    }

    @Transactional
    public void changePassword(final ChangePasswordDTO dto, final String code) {

        VerificationCode savedToken = verificationCodeRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));

        if (!savedToken.getUsername().equals(dto.getUsername())) {
            throw new IllegalArgumentException("Code does not match this user identity.");
        }

        User user = userRepository.findByUsername(dto.getUsername());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.saveAndFlush(user);

        verificationCodeRepository.delete(savedToken);

    }

    @Transactional(readOnly = true)
    public Map<String, String> getEmailAndUsernameFromId(final UUID id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        );

    }




}
