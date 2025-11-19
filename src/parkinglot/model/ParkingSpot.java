package parkinglot.model;

import parkinglot.enums.VehicleSize;

import java.util.Optional;
import java.util.UUID;

public class ParkingSpot {
    private final String id;
    private final VehicleSize spotSize;
    private volatile boolean isOccupied;
    private volatile Vehicle parkedVehicle;
    private ParkingFloor parkingFloor;

    public ParkingSpot(VehicleSize spotSize) {
        this.id = UUID.randomUUID().toString();
        this.spotSize = spotSize;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }

    public void setParkingFloor(ParkingFloor parkingFloor) {
        this.parkingFloor = parkingFloor;
    }

    public Optional<ParkingFloor> getParkingFloor() {
        return Optional.ofNullable(parkingFloor);
    }

    public VehicleSize getSpotSize() {
        return spotSize;
    }

    public String getId() {
        return id;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }


    public boolean canFitVehicle(Vehicle vehicle){
        if(isOccupied) return false;

        // Large spots can accommodate all vehicles
        // Medium spots can accommodate small and medium vehicles
        // Small spots can only accommodate small vehicles
        return switch (vehicle.getVehicleSize()){
            case SMALL -> spotSize.ordinal() >= VehicleSize.SMALL.ordinal();
            case MEDIUM -> spotSize.ordinal() >= VehicleSize.MEDIUM.ordinal();
            case LARGE -> spotSize.equals(VehicleSize.LARGE);
            default -> false;
        };
    }

    public synchronized boolean parkVehicle(Vehicle vehicle){
        if(isOccupied) {
            return false;
        }
        this.parkedVehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    public synchronized boolean unparkVehicle(){
        if(!isOccupied) {
            return false;
        }
        this.parkedVehicle = null;
        this.isOccupied = false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "id='" + id + '\'' +
                ", spotSize=" + spotSize +
                ", isOccupied=" + isOccupied +
                ", parkedVehicle=" + (parkedVehicle != null ? parkedVehicle.getLicenseNumber() : "null") +
                '}';
    }
}
