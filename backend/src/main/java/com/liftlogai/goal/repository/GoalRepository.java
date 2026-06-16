package com.liftlogai.goal.repository;

import com.liftlogai.goal.entity.Goal;
import com.liftlogai.goal.entity.GoalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    Page<Goal> findByUserId(Long userId, Pageable pageable);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    List<Goal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);
}
