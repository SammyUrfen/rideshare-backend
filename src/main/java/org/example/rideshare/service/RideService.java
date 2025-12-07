package org.example.rideshare.service;

import org.example.rideshare.dto.RideRequest;
import org.example.rideshare.exception.BadRequestException;
import org.example.rideshare.exception.NotFoundException;
import org.example.rideshare.model.Ride;
import org.example.rideshare.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public Ride createRide(RideRequest request, String userId) {
        // Create new Ride model
        Ride ride = new Ride();

        // Set fields from request
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDropLocation(request.getDropLocation());

        // Set userId from security context
        ride.setUserId(userId);

        // Set status to REQUESTED
        ride.setStatus("REQUESTED");

        // Set createdAt to current date
        ride.setCreatedAt(new Date());

        // Save and return
        return rideRepository.save(ride);
    }

    public List<Ride> getPendingRides() {
        // Find and return all rides with status REQUESTED
        return rideRepository.findByStatus("REQUESTED");
    }

    public Ride acceptRide(String rideId, String driverId) {
        // Find ride by ID, throw NotFoundException if not found
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Ride not found with id: " + rideId));

        // Check if current status is REQUESTED
        if (!"REQUESTED".equals(ride.getStatus())) {
            throw new BadRequestException("Ride cannot be accepted. Current status: " + ride.getStatus());
        }

        // Set driverId from security context
        ride.setDriverId(driverId);

        // Set status to ACCEPTED
        ride.setStatus("ACCEPTED");

        // Save and return updated ride
        return rideRepository.save(ride);
    }

    public Ride completeRide(String rideId) {
        // Find ride by ID, throw NotFoundException if not found
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Ride not found with id: " + rideId));

        // Check if current status is ACCEPTED
        if (!"ACCEPTED".equals(ride.getStatus())) {
            throw new BadRequestException("Ride cannot be completed. Current status: " + ride.getStatus());
        }

        // Set status to COMPLETED
        ride.setStatus("COMPLETED");

        // Save and return updated ride
        return rideRepository.save(ride);
    }

    public List<Ride> getMyRides(String userId) {
        // Find and return all rides for the given userId
        return rideRepository.findByUserId(userId);
    }
}
