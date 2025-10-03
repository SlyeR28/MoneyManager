package org.moneymanagement.Repository;

import org.moneymanagement.Entity.Income;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByProfileIdOrderByDateDesc(Long profileId);

    List<Income>findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT  SUM(e.amount) FROM Income e WHERE e.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    List<Income>findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
            Long profileId,
            LocalDate startDate,
            LocalDate endDate,
            String keyword,
            Sort sort
    );
    List<Income>findByProfileIdAndDateBetween(long profileId, LocalDate startDate, LocalDate endDate);
}
