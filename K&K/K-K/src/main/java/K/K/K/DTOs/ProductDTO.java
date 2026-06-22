package K.K.K.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductDTO extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("size")
    private String size;

    @JsonProperty("imageUrl")
    private String imageUrl;


}
