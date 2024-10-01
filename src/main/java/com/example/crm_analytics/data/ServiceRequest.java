package com.example.crm_analytics.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class ServiceRequest {
    private Long id;
    private Integer type;
    private String description;
    private Integer location;
    private String priority;
    private String status;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private LocalDateTime completedDate;
    private String address;
    private String observations;
    private long createdByUser;

}
