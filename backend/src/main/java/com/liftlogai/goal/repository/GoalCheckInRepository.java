package com.liftlogai.goal.repository;

import com.liftlogai.goal.entity.GoalCheckIn;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalCheckInRepository extends JpaRepository<GoalCheckIn, Long> {

    List<GoalCheckIn> findByGoalIdOrderByCheckInDateDescCreatedAtDesc(Long goalId);

    Optional<GoalCheckIn> findFirstByGoalIdOrderByCheckInDateDescCreatedAtDesc(Long goalId);
}
