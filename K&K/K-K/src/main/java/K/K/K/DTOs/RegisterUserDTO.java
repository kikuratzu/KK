package K.K.K.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RegisterUserDTO extends BaseDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;

}
