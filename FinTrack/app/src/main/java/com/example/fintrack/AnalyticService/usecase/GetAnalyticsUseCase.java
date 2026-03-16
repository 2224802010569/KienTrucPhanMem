package com.example.fintrack.AnalyticService.usecase;

import com.example.fintrack.AnalyticService.entity.AnalyticsData;
import com.example.fintrack.AnalyticService.service.AnalyticsDomainService;

import java.util.List;

public class GetAnalyticsUseCase {

    private AnalyticsDomainService service;

    public GetAnalyticsUseCase(AnalyticsDomainService service){
        this.service = service;
    }

    public List<AnalyticsData> execute(
            String userId,
            String type,
            String time
    ){

        return service.getAnalytics(userId, type, time);

    }
}