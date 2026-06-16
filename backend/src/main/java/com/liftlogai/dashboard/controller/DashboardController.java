package com.liftlogai.dashboard.controller;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.dashboard.dto.DashboardSummaryResponse;
import com.liftlogai.dashboard.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    DashboardSummaryResponse summary(Authentication authentication) {
        return dashboardService.summary((AuthenticatedUser) authentication.getPrincipal());
    }
}
