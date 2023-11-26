package win.novice.li.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class License {
    private String licenseId;
    @NotBlank
    private String licenseeName = "Test";
    @NotBlank
    private String assigneeName = "novice.li";
    @NotNull
    private String assigneeEmail = "";
    @NotNull
    private String licenseRestriction = "";
    @NotNull
    private Boolean checkConcurrentUse = false;

    @NotEmpty
    private List<@Valid Product> products;
    @NotBlank
    private String metadata = "0120230102PPAA013009";
    @NotBlank
    private String hash = "41472961/0:1563609451";

    @NotNull
    @Min(1)
    private Integer gracePeriodDays = 7;
    @NotNull
    private Boolean autoProlongated = true;
    @NotNull
    private Boolean isAutoProlongated = true;
}
