package org.example.rideshare.controller;

import org.example.rideshare.dto.RideRequest;
import org.example.rideshare.exception.NotFoundException;
import org.example.rideshare.model.Ride;
import org.example.rideshare.model.User;
import org.example.rideshare.repository.UserRepository;
import org.example.rideshare.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RideController {

    private final RideService rideService;
    private final UserRepository userRepository;

    public RideController(RideService rideService, UserRepository userRepository) {
        this.rideService = rideService;
        this.userRepository = userRepository;
    }

    @PostMapping("/rides")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Ride> createRide(@Valid @RequestBody RideRequest request,
                                           Authentication authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        Ride ride = rideService.createRide(request, userId);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/driver/rides/requests")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Ride>> getPendingRides() {
        List<Ride> rides = rideService.getPendingRides();
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/driver/rides/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Ride> acceptRide(@PathVariable String id,
                                           Authentication authentication) {
        String driverId = getUserIdFromAuthentication(authentication);
        Ride ride = rideService.acceptRide(id, driverId);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/rides/{id}/complete")
    public ResponseEntity<Ride> completeRide(@PathVariable String id) {
        Ride ride = rideService.completeRide(id);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/user/rides")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Ride>> getMyRides(Authentication authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        List<Ride> rides = rideService.getMyRides(userId);
        return ResponseEntity.ok(rides);
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        return user.getId();
    }
}
