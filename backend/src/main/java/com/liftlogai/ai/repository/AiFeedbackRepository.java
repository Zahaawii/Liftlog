package com.liftlogai.ai.repository;

import com.liftlogai.ai.entity.AiFeedback;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

    Page<AiFeedback> findByUserId(Long userId, Pageable pageable);

    Optional<AiFeedback> findByIdAndUserId(Long id, Long userId);

    Optional<AiFeedback> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
