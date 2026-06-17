package com.liftlogai.ai.controller;

import com.liftlogai.ai.dto.AiFeedbackRequest;
import com.liftlogai.ai.dto.AiFeedbackResponse;
import com.liftlogai.ai.service.AiFeedbackService;
import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/feedback")
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService) {
        this.aiFeedbackService = aiFeedbackService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AiFeedbackResponse requestFeedback(
            Authentication authentication,
            @Valid @RequestBody AiFeedbackRequest request
    ) {
        return aiFeedbackService.requestFeedback(currentUser(authentication), request);
    }

    @GetMapping
    PagedResponse<AiFeedbackResponse> listFeedback(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return aiFeedbackService.listFeedback(currentUser(authentication), page, size);
    }

    @GetMapping("/{id}")
    AiFeedbackResponse getFeedback(Authentication authentication, @PathVariable Long id) {
        return aiFeedbackService.getFeedback(currentUser(authentication), id);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
