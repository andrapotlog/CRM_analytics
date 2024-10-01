package com.example.crm_analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="analytics_data")
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double averageResponseTime;
    private Double averageCompletionTime;
    private Double caseResolutionRate;
/*    private Double publicSatisfactionScore;*/

    @Column(name = "timestamp")
    private LocalDateTime creationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "analyticsData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YearlyMetrics> casesResolved;

    @OneToMany(mappedBy = "analyticsData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryMetrics> categoryMetrics;

    @OneToMany(mappedBy = "analyticsData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CountMetrics> categoryCountMetrics;

    @OneToMany(mappedBy = "analyticsData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CountMetrics> statusCountMetrics;

    @OneToMany(mappedBy = "analyticsData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CountMetrics> priorityCountMetrics;

    public AnalyticsData(Double averageResponseTime,
                         Double averageCompletionTime,
                         Double caseResolutionRate,
                        /* Double publicSatisfactionScore,*/
                         List<YearlyMetrics> casesResolved,
                         List<CategoryMetrics> categoryMetrics,
                         List<CountMetrics> categoryCountMetrics,
                         List<CountMetrics> statusCountMetrics,
                         List<CountMetrics> priorityCountMetrics
    ) {
        this.averageResponseTime = averageResponseTime;
        this.averageCompletionTime = averageCompletionTime;
        this.caseResolutionRate = caseResolutionRate;
        /*this.publicSatisfactionScore = publicSatisfactionScore;*/
        this.casesResolved = casesResolved;
        this.categoryMetrics = categoryMetrics;
        this.categoryCountMetrics = categoryCountMetrics;
        this.statusCountMetrics = statusCountMetrics;
        this.priorityCountMetrics = priorityCountMetrics;
    }

    @PrePersist
    private void setParentReferences() {
        if (casesResolved != null) {
            for (YearlyMetrics yearlyMetrics : casesResolved) {
                yearlyMetrics.setAnalyticsData(this);
                if (yearlyMetrics.getMetrics() != null) {
                    for (MonthlyMetrics monthlyMetrics : yearlyMetrics.getMetrics()) {
                        monthlyMetrics.setYearlyMetrics(yearlyMetrics);
                    }
                }
            }
        }

        if (categoryMetrics != null) {
            for (CategoryMetrics categoryMetricsItem : categoryMetrics) {
                categoryMetricsItem.setAnalyticsData(this);
            }
        }

        if (categoryCountMetrics != null) {
            for (CountMetrics countMetrics : categoryCountMetrics) {
                countMetrics.setAnalyticsData(this);
            }
        }

        if (statusCountMetrics != null) {
            for (CountMetrics countMetrics : statusCountMetrics) {
                countMetrics.setAnalyticsData(this);
            }
        }

        if (priorityCountMetrics != null) {
            for (CountMetrics countMetrics : priorityCountMetrics) {
                countMetrics.setAnalyticsData(this);
            }
        }
    }

    @Entity
    @Table(name="yearly_metrics")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class YearlyMetrics {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String year;

        @OneToMany(mappedBy = "yearlyMetrics", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<MonthlyMetrics> metrics;

        @ManyToOne
        @JoinColumn(name = "analytics_data_id", nullable = false)
        private AnalyticsData analyticsData;

        public YearlyMetrics(String year, List<MonthlyMetrics> metrics/*,AnalyticsData analyticsData*/) {
            this.year = year;
            this.metrics = metrics;
//            this.analyticsData = analyticsData;
        }

        @PrePersist
        private void setParentReferences() {
            if (metrics != null) {
                for (MonthlyMetrics monthlyMetrics : metrics) {
                    monthlyMetrics.setYearlyMetrics(this);
                }
            }
        }
    }

    @Entity
    @Table(name="monthly_metrics")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MonthlyMetrics {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String month;
        private Integer count;

        @ManyToOne
        @JoinColumn(name = "yearly_metrics_id", nullable = false)
        private YearlyMetrics yearlyMetrics;

        public MonthlyMetrics(String month, int count/*,  YearlyMetrics yearlyMetrics*/) {
            this.month = month;
            this.count = count;
//            this.yearlyMetrics = yearlyMetrics;
        }
    }

    @Entity
    @Table(name="category_metrics")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CategoryMetrics {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private Integer category;
        private Double responseTime;
        private Double completionTime;
        private Double rate;

        @ManyToOne
        @JoinColumn(name = "analytics_data_id", nullable = false)
        private AnalyticsData analyticsData;

        public CategoryMetrics(Integer category, Double responseTime, Double completionTime, Double rate/*, AnalyticsData analyticsData*/) {
            this.category = category;
            this.responseTime = responseTime;
            this.completionTime = completionTime;
            this.rate = rate;
//            this.analyticsData = analyticsData;
        }
    }

    @Entity
    @Table(name="count_metrics")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CountMetrics {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String category;
        private Integer count;

        @ManyToOne
        @JoinColumn(name = "analytics_data_id", nullable = false)
        private AnalyticsData analyticsData;

        public CountMetrics(String category, Integer count/*, AnalyticsData analyticsData*/) {
            this.category = category;
            this.count = count;
//            this.analyticsData = analyticsData;
        }
    }
}