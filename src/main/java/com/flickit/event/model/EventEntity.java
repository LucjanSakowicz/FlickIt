package com.flickit.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
public class EventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String titleAi;

    private String titleVendor;

    @Column(length = 2000)
    private String descriptionAi;

    private String descriptionVendor;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    private Double alt;
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private UUID vendorId;

    public enum Category {FOOD, SERVICE, OTHER}

    public enum Status {ACTIVE, CLAIMED, EXPIRED, REMOVED}

}
