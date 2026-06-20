package K.K.K.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SimpleProductDTO extends BaseDTO {

    private String name;
    private BigDecimal price;
    private String imageUrl;
}
