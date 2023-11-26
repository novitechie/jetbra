
package win.novice.li.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Product {
    @NotBlank
    private String code;
    @NotBlank
    private String fallbackDate = "2025-12-31";
    @NotBlank
    private String paidUpTo = "2025-12-31";
    @NotNull
    private Boolean extended = false;
}