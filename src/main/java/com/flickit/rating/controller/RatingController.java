package com.flickit.rating.controller;

import com.flickit.rating.dto.CreateRatingRequest;
import com.flickit.rating.dto.RatingDto;
import com.flickit.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Event rating endpoints")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Rate an event")
    public ResponseEntity<RatingDto> rate(@RequestBody @Valid CreateRatingRequest request) {
        return ResponseEntity.ok(ratingService.rateEvent(request));
    }
}
