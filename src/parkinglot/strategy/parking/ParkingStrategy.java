package parkinglot.strategy.parking;

import parkinglot.model.ParkingFloor;
import parkinglot.model.ParkingSpot;
import parkinglot.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface ParkingStrategy {
    Optional<ParkingSpot> findParkingSpot(List<ParkingFloor> floorList, Vehicle vehicle);
}
