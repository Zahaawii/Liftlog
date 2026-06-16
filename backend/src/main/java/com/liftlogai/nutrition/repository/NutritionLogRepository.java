package com.liftlogai.nutrition.repository;

import com.liftlogai.nutrition.entity.NutritionLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {

    Page<NutritionLog> findByUserId(Long userId, Pageable pageable);

    Optional<NutritionLog> findByIdAndUserId(Long id, Long userId);

    List<NutritionLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    @Query("""
            select coalesce(sum(n.calories), 0),
                   coalesce(sum(n.protein), 0),
                   coalesce(sum(n.carbohydrates), 0),
                   coalesce(sum(n.fat), 0)
            from NutritionLog n
            where n.user.id = :userId
              and n.logDate = :logDate
            """)
    List<Object[]> dailyTotals(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);
}
