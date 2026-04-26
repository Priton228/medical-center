package com.medcenter.controller;

import com.medcenter.dto.RecommendationDtos;
import com.medcenter.security.AuthenticatedUser;
import com.medcenter.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@PreAuthorize("hasRole('PATIENT')")
public class RecommendationController {
    public RecommendationController(RecommendationService recommendationService, AuthenticatedUser currentUser) {
        this.recommendationService = recommendationService;
        this.currentUser = currentUser;
    }


    private final RecommendationService recommendationService;
    private final AuthenticatedUser currentUser;

    @PostMapping("/analyze")
    public RecommendationDtos.RecommendationResponse analyze(@Valid @RequestBody RecommendationDtos.AnalyzeRequest req) {
        return recommendationService.analyze(currentUser.current().getUsername(), req.symptomIds());
    }

    @GetMapping("/me")
    public List<RecommendationDtos.RecommendationResponse> history() {
        return recommendationService.historyForPatient(currentUser.current().getUsername());
    }
}
