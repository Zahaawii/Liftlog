package com.liftlogai.goal.controller;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.goal.dto.GoalCheckInRequest;
import com.liftlogai.goal.dto.GoalCheckInResponse;
import com.liftlogai.goal.dto.GoalRequest;
import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.goal.service.GoalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    PagedResponse<GoalResponse> listGoals(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return goalService.listGoals(currentUser(authentication), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    GoalResponse createGoal(
            Authentication authentication,
            @Valid @RequestBody GoalRequest request
    ) {
        return goalService.createGoal(currentUser(authentication), request);
    }

    @GetMapping("/{id}")
    GoalResponse getGoal(Authentication authentication, @PathVariable Long id) {
        return goalService.getGoal(currentUser(authentication), id);
    }

    @PutMapping("/{id}")
    GoalResponse updateGoal(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request
    ) {
        return goalService.updateGoal(currentUser(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteGoal(Authentication authentication, @PathVariable Long id) {
        goalService.deleteGoal(currentUser(authentication), id);
    }

    @PostMapping("/{id}/check-ins")
    @ResponseStatus(HttpStatus.CREATED)
    GoalCheckInResponse createCheckIn(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody GoalCheckInRequest request
    ) {
        return goalService.createCheckIn(currentUser(authentication), id, request);
    }

    @GetMapping("/{id}/check-ins")
    List<GoalCheckInResponse> listCheckIns(Authentication authentication, @PathVariable Long id) {
        return goalService.listCheckIns(currentUser(authentication), id);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
