package com.example.crm_analytics.controller;

import com.example.crm_analytics.entity.AnalyticsData;
import com.example.crm_analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsData> getAnalyticsData() {
        AnalyticsData data = analyticsService.getAnalyticsData();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
