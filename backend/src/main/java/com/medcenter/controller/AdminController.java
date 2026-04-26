package com.medcenter.controller;

import com.medcenter.dto.StatsDtos;
import com.medcenter.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final StatsService statsService;

    @GetMapping("/stats/overview")
    public StatsDtos.OverviewStats overview() {
        return statsService.overview();
    }
}
