package com.example.crm_analytics.service;

import com.example.crm_analytics.data.ServiceRequest;
import com.example.crm_analytics.entity.AnalyticsData;
import com.example.crm_analytics.repository.AnalyticsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AnalyticsRepo analyticsRepository;

    @Value("${service.requests.api.url}")
    private String serviceRequestsApiUrl;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public AnalyticsData/*void*/ getAnalyticsData() {
        List<ServiceRequest> serviceRequests = fetchServiceRequests();

        serviceRequests.forEach(System.out::println);
        // Process the service requests data to calculate analytics
        Double averageResponseTime = calculateAverageResponseTime(serviceRequests);
        Double averageCompletionTime = calculateAverageCompletionTime(serviceRequests);
        Double caseResolutionRate = calculateCaseResolutionRate(serviceRequests);
       /* Double publicSatisfactionScore = calculatePublicSatisfactionScore();*/
        List<AnalyticsData.YearlyMetrics> casesResolved = calculateCasesResolved(serviceRequests);
        List<AnalyticsData.CategoryMetrics> categoryMetrics = calculateCategoryMetrics(serviceRequests);
        List<AnalyticsData.CountMetrics> categoryCountMetrics = calculateTypeMetrics(serviceRequests);
        List<AnalyticsData.CountMetrics> priorityCountMetrics = calculatePriorityMetrics(serviceRequests);
        List<AnalyticsData.CountMetrics> statusCountMetrics = calculateStatusMetrics(serviceRequests);

        AnalyticsData analyticsData = new AnalyticsData(
                Double.parseDouble(df.format(averageResponseTime)),
                Double.parseDouble(df.format(averageCompletionTime)),
                caseResolutionRate,
                casesResolved,
                categoryMetrics,
                categoryCountMetrics,
                statusCountMetrics,
                priorityCountMetrics);

        //analyticsRepository.save(analyticsData);  // Save analytics data to the analytics database
        return analyticsData;
    }

    private List<ServiceRequest> fetchServiceRequests() {
        return restTemplate.exchange(serviceRequestsApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ServiceRequest>>() {}).getBody();
    }

    // Methods to calculate analytics (averageResponseTime, caseResolutionRate, etc.)

    private double calculateAverageResponseTime(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .filter(sr -> sr.getUpdateDate() != null)
                .mapToDouble(sr -> Duration.between(sr.getCreationDate(), sr.getUpdateDate()).toHours() )
                .average().orElse(0);
    }

    private double calculateAverageCompletionTime(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .filter(sr -> sr.getUpdateDate() != null && sr.getCompletedDate() != null)
                .mapToDouble(sr -> Duration.between(sr.getUpdateDate(), sr.getCompletedDate()).toHours())
                .average().orElse(0);
    }

    private double calculateCaseResolutionRate(List<ServiceRequest> serviceRequests) {
        return (double) serviceRequests.stream()
                .filter(sr -> "COMPLETED".equals(sr.getStatus()))
                .count() / serviceRequests.size();
    }

    /*private double calculatePublicSatisfactionScore() {
        return 4.2; // Dummy value, replace with actual logic
    }*/

    private List<AnalyticsData.YearlyMetrics> calculateCasesResolved(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .filter(sr -> "COMPLETED".equals(sr.getStatus()))
                .collect(Collectors.groupingBy(sr -> sr.getCreationDate().getYear()))
                .entrySet().stream()
                .map(entry -> new AnalyticsData.YearlyMetrics(
                        String.valueOf(entry.getKey()),
                        entry.getValue().stream()
                                .collect(Collectors.groupingBy(sr -> sr.getCreationDate().getMonth()))
                                .entrySet().stream()
                                .map(monthEntry -> new AnalyticsData.MonthlyMetrics(
                                        monthEntry.getKey().toString(),
                                        monthEntry.getValue().size()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private List<AnalyticsData.CategoryMetrics> calculateCategoryMetrics(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .collect(Collectors.groupingBy(ServiceRequest::getType))
                .entrySet().stream()
                .map(entry -> new AnalyticsData.CategoryMetrics(
                        entry.getKey(),
                        Double.parseDouble(df.format(entry.getValue().stream()
                                .filter(sr -> sr.getUpdateDate() != null)
                                .mapToDouble(sr -> Duration.between(sr.getCreationDate(), sr.getUpdateDate()).toMinutes() / 60.0)
                                .average().orElse(0))),
                        Double.parseDouble(df.format(entry.getValue().stream()
                                .filter(sr -> sr.getUpdateDate() != null && sr.getCompletedDate() != null)
                                .mapToDouble(sr -> Duration.between(sr.getUpdateDate(), sr.getCompletedDate()).toMinutes() / 60.0)
                                .average().orElse(0))),
                        (double) entry.getValue().stream()
                                .filter(sr -> "COMPLETED".equals(sr.getStatus()))
                                .count() / entry.getValue().size()))
                .collect(Collectors.toList());
    }

    private List<AnalyticsData.CountMetrics> calculateTypeMetrics(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .collect(Collectors.groupingBy(ServiceRequest::getType))
                .entrySet().stream()
                .map(entry -> new AnalyticsData.CountMetrics(
                        entry.getKey().toString(),
                        entry.getValue().size()))
                .collect(Collectors.toList());
    }

    private List<AnalyticsData.CountMetrics> calculatePriorityMetrics(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .collect(Collectors.groupingBy(ServiceRequest::getPriority))
                .entrySet().stream()
                .map(entry -> {
                    System.out.println(entry);
                    return new AnalyticsData.CountMetrics(
                            entry.getKey(),
                            entry.getValue().size());
                })
                .collect(Collectors.toList());
    }

    private List<AnalyticsData.CountMetrics> calculateStatusMetrics(List<ServiceRequest> serviceRequests) {
        return serviceRequests.stream()
                .collect(Collectors.groupingBy(ServiceRequest::getStatus))
                .entrySet().stream()
                .map(entry -> {
                    return new AnalyticsData.CountMetrics(
                            entry.getKey(),
                            entry.getValue().size());
                })
                .collect(Collectors.toList());
    }
}
