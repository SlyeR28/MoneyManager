package org.moneymanagement.Payload.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    @NotNull(message = "Profile ID cannot be null")
    private Long profileId;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Type cannot be blank")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;


    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Icon must be a valid URL")
    private String icon;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
