package parkinglot.model;

import parkinglot.enums.VehicleSize;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ParkingFloor {
    private final String id;
    private final Map<String, ParkingSpot> spotMap;
    // Optimized: Maintain queues of available spots by size for O(1) access
    private final Map<VehicleSize, Queue<ParkingSpot>> availableSpotsBySize;
    private final int floorNumber;

    public ParkingFloor() {
        this(0);
    }

    public ParkingFloor(int floorNumber) {
        this.id = UUID.randomUUID().toString();
        this.spotMap = new ConcurrentHashMap<>();
        this.availableSpotsBySize = new ConcurrentHashMap<>();
        this.floorNumber = floorNumber;
        // Initialize queues for each vehicle size
        for (VehicleSize size : VehicleSize.values()) {
            availableSpotsBySize.put(size, new ArrayDeque<>());
        }
    }

    public void addSpot(ParkingSpot parkingSpot){
        parkingSpot.setParkingFloor(this);
        spotMap.put(parkingSpot.getId(), parkingSpot);
        // Add to appropriate availability queues based on what vehicles can fit
        VehicleSize spotSize = parkingSpot.getSpotSize();
        // A LARGE spot can accommodate all vehicles
        if (spotSize == VehicleSize.LARGE) {
            availableSpotsBySize.get(VehicleSize.SMALL).offer(parkingSpot);
            availableSpotsBySize.get(VehicleSize.MEDIUM).offer(parkingSpot);
            availableSpotsBySize.get(VehicleSize.LARGE).offer(parkingSpot);
        } else if (spotSize == VehicleSize.MEDIUM) {
            // A MEDIUM spot can accommodate SMALL and MEDIUM vehicles
            availableSpotsBySize.get(VehicleSize.SMALL).offer(parkingSpot);
            availableSpotsBySize.get(VehicleSize.MEDIUM).offer(parkingSpot);
        } else {
            // A SMALL spot can only accommodate SMALL vehicles
            availableSpotsBySize.get(VehicleSize.SMALL).offer(parkingSpot);
        }
    }

    public Optional<ParkingSpot> findAvailableSpot(Vehicle vehicle) {
        VehicleSize vehicleSize = vehicle.getVehicleSize();
        Queue<ParkingSpot> availableSpots = availableSpotsBySize.get(vehicleSize);
        
        // Fast path: Check queue first
        while (!availableSpots.isEmpty()) {
            ParkingSpot spot = availableSpots.peek();
            if (spot != null && !spot.isOccupied() && spot.canFitVehicle(vehicle)) {
                return Optional.of(spot);
            }
            // Remove invalid spots from queue
            availableSpots.poll();
        }
        
        // Fallback: Search all spots (in case queue is out of sync)
        return spotMap.values().stream()
                .filter(spot -> !spot.isOccupied() && spot.canFitVehicle(vehicle))
                .min(Comparator.comparing(ParkingSpot::getSpotSize));
    }

    public void onSpotOccupied(ParkingSpot spot) {
        // Remove from availability queues
        VehicleSize spotSize = spot.getSpotSize();
        if (spotSize == VehicleSize.LARGE) {
            availableSpotsBySize.get(VehicleSize.SMALL).remove(spot);
            availableSpotsBySize.get(VehicleSize.MEDIUM).remove(spot);
            availableSpotsBySize.get(VehicleSize.LARGE).remove(spot);
        } else if (spotSize == VehicleSize.MEDIUM) {
            availableSpotsBySize.get(VehicleSize.SMALL).remove(spot);
            availableSpotsBySize.get(VehicleSize.MEDIUM).remove(spot);
        } else {
            availableSpotsBySize.get(VehicleSize.SMALL).remove(spot);
        }
    }

    public void onSpotFreed(ParkingSpot spot) {
        // Add back to availability queues
        VehicleSize spotSize = spot.getSpotSize();
        if (spotSize == VehicleSize.LARGE) {
            if (!availableSpotsBySize.get(VehicleSize.SMALL).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.SMALL).offer(spot);
            }
            if (!availableSpotsBySize.get(VehicleSize.MEDIUM).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.MEDIUM).offer(spot);
            }
            if (!availableSpotsBySize.get(VehicleSize.LARGE).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.LARGE).offer(spot);
            }
        } else if (spotSize == VehicleSize.MEDIUM) {
            if (!availableSpotsBySize.get(VehicleSize.SMALL).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.SMALL).offer(spot);
            }
            if (!availableSpotsBySize.get(VehicleSize.MEDIUM).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.MEDIUM).offer(spot);
            }
        } else {
            if (!availableSpotsBySize.get(VehicleSize.SMALL).contains(spot)) {
                availableSpotsBySize.get(VehicleSize.SMALL).offer(spot);
            }
        }
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getTotalSpots() {
        return spotMap.size();
    }

    public int getOccupiedSpots() {
        return (int) spotMap.values().stream().filter(ParkingSpot::isOccupied).count();
    }

    public int getAvailableSpots(VehicleSize vehicleSize) {
        // Count spots that can fit the vehicle size
        return (int) spotMap.values().stream()
                .filter(spot -> !spot.isOccupied() && canFitVehicleSize(spot, vehicleSize))
                .count();
    }

    private boolean canFitVehicleSize(ParkingSpot spot, VehicleSize vehicleSize) {
        VehicleSize spotSize = spot.getSpotSize();
        return switch (vehicleSize) {
            case SMALL -> spotSize.ordinal() >= VehicleSize.SMALL.ordinal();
            case MEDIUM -> spotSize.ordinal() >= VehicleSize.MEDIUM.ordinal();
            case LARGE -> spotSize.equals(VehicleSize.LARGE);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingFloor that = (ParkingFloor) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ParkingFloor{" +
                "id='" + id + '\'' +
                ", floorNumber=" + floorNumber +
                ", totalSpots=" + spotMap.size() +
                ", occupiedSpots=" + getOccupiedSpots() +
                ", availableSpots=" + (spotMap.size() - getOccupiedSpots()) +
                '}';
    }

    public void printAvailability() {
        System.out.printf("\nAvailability for floor %s\n", id);
        Map<VehicleSize, Long> countMap = spotMap.values().stream().filter(parkingSpot -> !parkingSpot.isOccupied())
                .collect(Collectors.groupingBy(ParkingSpot::getSpotSize, Collectors.counting()));

        for (VehicleSize vehicleSize : VehicleSize.values()) {
            System.out.printf(" %s spot: %d\n", vehicleSize, countMap.getOrDefault(vehicleSize,0L));
        }
    }

}
