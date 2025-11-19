package parkinglot.strategy.parking;

import parkinglot.model.ParkingFloor;
import parkinglot.model.ParkingSpot;
import parkinglot.model.Vehicle;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Strategy that finds the nearest available spot to the entrance.
 * Prioritizes lower floor numbers and smaller spot IDs (assuming spots are numbered sequentially).
 */
public class NearestToEntranceStrategy implements ParkingStrategy {
    @Override
    public Optional<ParkingSpot> findParkingSpot(List<ParkingFloor> floorList, Vehicle vehicle) {
        return floorList.stream()
                .sorted(Comparator.comparing(ParkingFloor::getFloorNumber))
                .map(floor -> floor.findAvailableSpot(vehicle))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.comparing((ParkingSpot spot) -> spot.getParkingFloor()
                        .map(ParkingFloor::getFloorNumber).orElse(Integer.MAX_VALUE))
                        .thenComparing(ParkingSpot::getId));
    }
}

