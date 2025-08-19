package com.flickit.claim.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimEntity {

    @EmbeddedId
    private ClaimId id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Integer rating;

    @Column
    private String comment;

    @Column
    private Instant ratedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }
}
