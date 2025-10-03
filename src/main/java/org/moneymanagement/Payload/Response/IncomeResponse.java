package org.moneymanagement.Payload.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeResponse {

    private Long id;
    private String name;
    private String icon;
    private LocalDate date;
    private BigDecimal amount;
    private LocalDateTime creationAt;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryName;

    private Long profileId;
    private String profileName;
}
