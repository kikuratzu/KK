package K.K.Controllers;

import K.K.Entities.User;
import K.K.K.DTOs.ChangePasswordDTO;
import K.K.K.DTOs.LoginUserDTO;
import K.K.K.DTOs.RegisterUserDTO;
import K.K.K.DTOs.changeUsernameDTO;
import K.K.Services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/")
@Slf4j
@CrossOrigin(origins = "http://127.0.0.1:5500/")
public class UserController {

    @Autowired
    UserService service;

    @PostMapping("createUser")
    @ResponseStatus(value = HttpStatus.CREATED)
    public RegisterUserDTO createUser(@RequestBody RegisterUserDTO dto) {
        log.info("create a user");
        service.createUser(dto);
        return dto;
    }

    @PostMapping("login")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<UUID, String> login (@RequestBody LoginUserDTO dto) {
        log.info("user tries to log in");
        return service.verify(dto);
    }

    @GetMapping("getAllUsers")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        log.info("getting all users.");
        return service.getAllUsers();
    }

    @PostMapping("/api/logout")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> manualLogout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/hello")
    public HttpStatus hello(){
        service.hello();
        return HttpStatus.OK;
    }

    @PostMapping("/request-username-change")
    public ResponseEntity<Map<String, String>> requestChange(@RequestBody final changeUsernameDTO dto) {
        service.initiateUsernameChangeFlow(dto);
        return ResponseEntity.ok(Map.of("message", "Verification code sent to your email."));
    }

    @PatchMapping("/confirm-username-change")
    public ResponseEntity<Map<String, String>> confirmChange(@RequestBody final changeUsernameDTO dto,
                                                             @RequestParam final String code) {
        String newToken = service.changeUsername(dto, code);
        return ResponseEntity.ok(Map.of(
                "message", "Username updated successfully!",
                "token", newToken
        ));
    }

    @PostMapping ("/request-password-change")
    public ResponseEntity<Map<String, String>> requestPasswordChange(@RequestBody final ChangePasswordDTO dto){
        service.initiatePasswordChange(dto);
        return ResponseEntity.ok(Map.of("message", "Verification code sent to your email"));
    }

    @PatchMapping("/confirm-password-change")
    public ResponseEntity<Map<String, String>> confirmPasswordChange(@RequestBody final ChangePasswordDTO dto,
                                                                     @RequestParam final String code) {
        service.changePassword(dto, code);
        return ResponseEntity.ok(Map.of("message","Password updated successfully!"));
    }


}
