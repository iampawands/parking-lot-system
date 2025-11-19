package parkinglot.strategy.parking;

import parkinglot.model.ParkingFloor;
import parkinglot.model.ParkingSpot;
import parkinglot.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Strategy that finds the first available spot without any preference.
 * Fastest strategy but may not be optimal for space utilization.
 */
public class FirstAvailableStrategy implements ParkingStrategy {
    @Override
    public Optional<ParkingSpot> findParkingSpot(List<ParkingFloor> floorList, Vehicle vehicle) {
        for (ParkingFloor floor : floorList) {
            Optional<ParkingSpot> spot = floor.findAvailableSpot(vehicle);
            if (spot.isPresent()) {
                return spot;
            }
        }
        return Optional.empty();
    }
}

