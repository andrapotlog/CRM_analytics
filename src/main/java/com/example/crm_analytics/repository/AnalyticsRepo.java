package com.example.crm_analytics.repository;

import com.example.crm_analytics.entity.AnalyticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepo extends JpaRepository<AnalyticsData, Long> {
}
