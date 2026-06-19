package K.K.K.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class AnonymousOrderDTO extends BaseDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String email;
    private List<ProductDTO> items;
}
