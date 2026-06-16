package com.liftlogai.nutrition.controller;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import com.liftlogai.nutrition.dto.NutritionLogRequest;
import com.liftlogai.nutrition.dto.NutritionLogResponse;
import com.liftlogai.nutrition.service.NutritionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/logs")
    PagedResponse<NutritionLogResponse> listLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return nutritionService.listLogs(currentUser(authentication), page, size);
    }

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    NutritionLogResponse createLog(
            Authentication authentication,
            @Valid @RequestBody NutritionLogRequest request
    ) {
        return nutritionService.createLog(currentUser(authentication), request);
    }

    @GetMapping("/logs/{id}")
    NutritionLogResponse getLog(Authentication authentication, @PathVariable Long id) {
        return nutritionService.getLog(currentUser(authentication), id);
    }

    @PutMapping("/logs/{id}")
    NutritionLogResponse updateLog(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NutritionLogRequest request
    ) {
        return nutritionService.updateLog(currentUser(authentication), id, request);
    }

    @DeleteMapping("/logs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLog(Authentication authentication, @PathVariable Long id) {
        nutritionService.deleteLog(currentUser(authentication), id);
    }

    @GetMapping("/summary/daily")
    DailyNutritionSummaryResponse dailySummary(
            Authentication authentication,
            @RequestParam LocalDate date
    ) {
        return nutritionService.dailySummary(currentUser(authentication), date);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
