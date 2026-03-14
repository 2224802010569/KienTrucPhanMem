package com.example.fintrack.AnalyticService.usecase;

import com.example.fintrack.AnalyticService.data.AnalyticsRepository;
import com.example.fintrack.AnalyticService.entity.AnalyticsData;
import com.example.fintrack.AnalyticService.service.AnalyticsDomainService;

import java.util.List;

public class GetAnalyticsUseCase {

    private AnalyticsDomainService service;

    // nhận repository từ ngoài
    public GetAnalyticsUseCase(AnalyticsRepository repository) {
        service = new AnalyticsDomainService(repository);
    }

    public List<AnalyticsData> execute(String userId){
        return service.getAnalytics(userId);
    }

    public String getInsight(List<AnalyticsData> list){
        return service.generateInsight(list);
    }
}