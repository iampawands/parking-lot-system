package parkinglot.service;

import parkinglot.model.ParkingFloor;
import parkinglot.model.ParkingSpot;
import parkinglot.model.Ticket;
import parkinglot.model.Vehicle;
import parkinglot.strategy.fee.FeeStrategy;
import parkinglot.strategy.parking.ParkingStrategy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParkingLot {
    private static volatile ParkingLot instance;
    private final List<ParkingFloor> parkingFloors;
    private final Map<String, Ticket> activeTicketsByLicense;
    private final Map<String, Ticket> activeTicketsById;
    private volatile FeeStrategy feeStrategy;
    private volatile ParkingStrategy parkingStrategy;
    private final Object lock = new Object();

    private ParkingLot() {
        parkingFloors = new CopyOnWriteArrayList<>();
        activeTicketsByLicense = new ConcurrentHashMap<>();
        activeTicketsById = new ConcurrentHashMap<>();
    }

    public static ParkingLot getInstance(){
        if (instance == null){
            synchronized (ParkingLot.class) {
                if (instance == null){
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }

    public void addFloor(ParkingFloor parkingFloor){
        parkingFloors.add(parkingFloor);
    }

    public void setFeeStrategy(FeeStrategy feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    public void setParkingStrategy(ParkingStrategy parkingStrategy) {
        this.parkingStrategy = parkingStrategy;
    }

    public Optional<Ticket> parkVehicle(Vehicle vehicle){
        // Fast path: Check if already parked (lock-free read)
        if(activeTicketsByLicense.containsKey(vehicle.getLicenseNumber())){
            throw new IllegalStateException("Vehicle " + vehicle.getLicenseNumber() + " is already parked");
        }

        if(parkingStrategy == null){
            throw new IllegalStateException("Parking strategy not set");
        }

        // Fine-grained locking: Find and reserve spot WITHOUT holding the main lock
        // This allows multiple threads to find spots concurrently
        final int MAX_RETRIES = 10;
        ParkingSpot reservedSpot = null;
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            // Find available spot (lock-free operation)
            Optional<ParkingSpot> parkingSpot = this.parkingStrategy.findParkingSpot(
                    new ArrayList<>(parkingFloors), vehicle);
            
            if (!parkingSpot.isPresent()) {
                // No spots available at all
                break;
            }
            
            ParkingSpot spot = parkingSpot.get();
            // Atomic reservation: Each spot has its own lock, so we only lock that specific spot
            // This allows other threads to work on different spots concurrently
            if (spot.parkVehicle(vehicle)) {
                // Successfully reserved atomically at the spot level
                reservedSpot = spot;
                break;
            }
            // Spot was taken by another thread, retry with next attempt
            // No lock held, so other threads can proceed
        }
        
        if (reservedSpot == null) {
            System.out.println("No Spot Found for this vehicle: "+vehicle.getLicenseNumber());
            return Optional.empty();
        }
        
        // Only lock when updating shared state (ticket maps)
        // This is the minimal critical section
        final ParkingSpot finalReservedSpot = reservedSpot;
        synchronized (lock) {
            // Double-check: Vehicle might have been parked by another thread
            if(activeTicketsByLicense.containsKey(vehicle.getLicenseNumber())){
                // Rollback: Free the spot we just reserved
                finalReservedSpot.unparkVehicle();
                finalReservedSpot.getParkingFloor().ifPresent(floor -> floor.onSpotFreed(finalReservedSpot));
                throw new IllegalStateException("Vehicle " + vehicle.getLicenseNumber() + " is already parked");
            }
            
            // Notify floor that spot is occupied
            finalReservedSpot.getParkingFloor().ifPresent(floor -> floor.onSpotOccupied(finalReservedSpot));
            
            Ticket ticket = new Ticket(vehicle.getVehicleSize(), vehicle, finalReservedSpot);
            activeTicketsByLicense.put(vehicle.getLicenseNumber(), ticket);
            activeTicketsById.put(ticket.getTicketId(), ticket);
            System.out.printf("\n%s spot successfully booked for vehicle license: %s\n", 
                    finalReservedSpot.getId(), vehicle.getLicenseNumber());
            return Optional.of(ticket);
        }
    }

    public Optional<Double> unparkVehicle(String licenseNumber){
        Ticket ticket;
        // Fine-grained: Only lock when accessing ticket map
        synchronized (lock) {
            ticket = activeTicketsByLicense.remove(licenseNumber);
            if(ticket == null){
                throw new IllegalArgumentException("Ticket not found for license: " + licenseNumber);
            }
            activeTicketsById.remove(ticket.getTicketId());
        }
        
        // Release lock before unparking spot (allows concurrent unparking)
        ticket.setExitDateTime(LocalDateTime.now());
        ParkingSpot spot = ticket.getParkingSpot();
        spot.unparkVehicle(); // Spot has its own lock
        
        // Notify floor that spot is freed
        final ParkingSpot finalSpot = spot;
        finalSpot.getParkingFloor().ifPresent(floor -> floor.onSpotFreed(finalSpot));

        if(feeStrategy == null){
            throw new IllegalStateException("Fee strategy not set");
        }

        Double parkingFee = feeStrategy.calculateFee(ticket);
        return Optional.of(parkingFee);
    }

    public Optional<Double> unparkVehicleByTicketId(String ticketId){
        Ticket ticket;
        // Fine-grained: Only lock when accessing ticket map
        synchronized (lock) {
            ticket = activeTicketsById.remove(ticketId);
            if(ticket == null){
                throw new IllegalArgumentException("Ticket not found for ticket ID: " + ticketId);
            }
            activeTicketsByLicense.remove(ticket.getParkedVehicle().getLicenseNumber());
        }
        
        // Release lock before unparking spot
        ticket.setExitDateTime(LocalDateTime.now());
        ParkingSpot spot = ticket.getParkingSpot();
        spot.unparkVehicle(); // Spot has its own lock
        
        // Notify floor that spot is freed
        final ParkingSpot finalSpot = spot;
        finalSpot.getParkingFloor().ifPresent(floor -> floor.onSpotFreed(finalSpot));

        if(feeStrategy == null){
            throw new IllegalStateException("Fee strategy not set");
        }

        Double parkingFee = feeStrategy.calculateFee(ticket);
        return Optional.of(parkingFee);
    }

    public Optional<Ticket> getTicketByLicense(String licenseNumber){
        return Optional.ofNullable(activeTicketsByLicense.get(licenseNumber));
    }

    public Optional<Ticket> getTicketById(String ticketId){
        return Optional.ofNullable(activeTicketsById.get(ticketId));
    }

    // Utility methods for statistics and monitoring
    public int getTotalFloors() {
        return parkingFloors.size();
    }

    public int getTotalSpots() {
        return parkingFloors.stream().mapToInt(ParkingFloor::getTotalSpots).sum();
    }

    public int getOccupiedSpots() {
        return parkingFloors.stream().mapToInt(ParkingFloor::getOccupiedSpots).sum();
    }

    public int getAvailableSpots() {
        return getTotalSpots() - getOccupiedSpots();
    }

    public int getActiveTicketsCount() {
        return activeTicketsByLicense.size();
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFloors", getTotalFloors());
        stats.put("totalSpots", getTotalSpots());
        stats.put("occupiedSpots", getOccupiedSpots());
        stats.put("availableSpots", getAvailableSpots());
        stats.put("activeTickets", getActiveTicketsCount());
        stats.put("utilizationRate", getTotalSpots() > 0 ? 
            (double) getOccupiedSpots() / getTotalSpots() * 100 : 0.0);
        return stats;
    }
}
