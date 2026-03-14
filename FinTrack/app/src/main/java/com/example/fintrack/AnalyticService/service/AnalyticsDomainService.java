package com.example.fintrack.AnalyticService.service;

import com.example.fintrack.AnalyticService.data.AnalyticsRepository;
import com.example.fintrack.AnalyticService.entity.AnalyticsData;

import java.util.List;

public class AnalyticsDomainService {

    private AnalyticsRepository repository;

    public AnalyticsDomainService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public List<AnalyticsData> getAnalytics(String userId) {
        return repository.getAnalyticsData(userId);
    }

    public String generateInsight(List<AnalyticsData> data){

        for(AnalyticsData d : data){

            if(d.getCategory().equalsIgnoreCase("Ăn uống") && d.getPercent() > 30){
                return "Chi tiêu ăn uống quá cao. Hãy cân nhắc nấu ăn tại nhà.";
            }

            if(d.getCategory().equalsIgnoreCase("Shopping") && d.getPercent() > 40){
                return "Bạn đang chi tiêu nhiều cho mua sắm.";
            }

        }

        return "Chi tiêu của bạn đang ổn định.";
    }
}