
package win.novice.li.model;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Product {
    @NotBlank
    private String code;
    @NotBlank
    private String fallbackDate = "2030-12-31";
    @NotBlank
    private String paidUpTo = "2030-12-31";
    @NotNull
    private Boolean extended = false;
}