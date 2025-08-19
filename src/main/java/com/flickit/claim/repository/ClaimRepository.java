package com.flickit.claim.repository;

import com.flickit.claim.model.ClaimEntity;
import com.flickit.claim.model.ClaimId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<ClaimEntity, ClaimId> {
    boolean existsById(ClaimId id);
}
