package org.moneymanagement.Repository;

import org.moneymanagement.Entity.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository  extends JpaRepository<Expense, Long> {

  List<Expense>findByProfileIdOrderByDateDesc(Long profileId);

  List<Expense>findTop5ByProfileIdOrderByDateDesc(Long profileId);

  @Query("SELECT  SUM(e.amount) FROM Expense e WHERE e.profile.id = :profileId")
  BigDecimal  findTotalExpenseByProfileId(@Param("profileId") Long profileId);

  List<Expense>findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
          Long profileId,
          LocalDate startDate,
          LocalDate endDate,
          String keyword,
          Sort sort
  );
 List<Expense>findByProfileIdAndDateBetween(long profileId, LocalDate startDate, LocalDate endDate);

 List<Expense> findByProfileIdAndDate(Long profileId , LocalDate date);

}
