package parkinglot.strategy.parking;

import parkinglot.enums.VehicleSize;
import parkinglot.model.ParkingFloor;
import parkinglot.model.ParkingSpot;
import parkinglot.model.Vehicle;

import java.util.List;
import java.util.Optional;

public class BestFitParkingStrategy implements ParkingStrategy {
    @Override
    public Optional<ParkingSpot> findParkingSpot(List<ParkingFloor> floorList, Vehicle vehicle) {
        Optional<ParkingSpot> bestSpot = Optional.empty();
        
        for(ParkingFloor parkingFloor: floorList){
            Optional<ParkingSpot> parkingSpot = parkingFloor.findAvailableSpot(vehicle);

            if(parkingSpot.isPresent()){
                ParkingSpot spot = parkingSpot.get();
                if(bestSpot.isEmpty()){
                    bestSpot = Optional.of(spot);
                } else {
                    // Find the smallest spot that can fit the vehicle
                    // Compare by ordinal - smaller ordinal means smaller size
                    VehicleSize bestSpotSize = bestSpot.get().getSpotSize();
                    VehicleSize currentSpotSize = spot.getSpotSize();
                    
                    // Prefer smaller spots (lower ordinal value)
                    if(currentSpotSize.ordinal() < bestSpotSize.ordinal()){
                        bestSpot = Optional.of(spot);
                    }
                }
            }
        }

        return bestSpot;
    }
}
