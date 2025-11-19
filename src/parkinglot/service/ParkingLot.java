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
        synchronized (lock) {
            // Check if vehicle is already parked
            if(activeTicketsByLicense.containsKey(vehicle.getLicenseNumber())){
                throw new IllegalStateException("Vehicle " + vehicle.getLicenseNumber() + " is already parked");
            }

            if(parkingStrategy == null){
                throw new IllegalStateException("Parking strategy not set");
            }

            Optional<ParkingSpot> parkingSpot = this.parkingStrategy.findParkingSpot(
                    new ArrayList<>(parkingFloors), vehicle);

            if(parkingSpot.isPresent()){
                ParkingSpot spot = parkingSpot.get();
                // Try to park - returns false if spot was taken by another thread
                if(!spot.parkVehicle(vehicle)){
                    // Spot was taken, find another spot
                    Optional<ParkingSpot> newSpot = this.parkingStrategy.findParkingSpot(
                            new ArrayList<>(parkingFloors), vehicle);
                    if(newSpot.isPresent() && newSpot.get().parkVehicle(vehicle)){
                        spot = newSpot.get();
                    } else {
                        System.out.println("No Spot Found for this vehicle: "+vehicle.getLicenseNumber());
                        return Optional.empty();
                    }
                }
                
                // Notify floor that spot is occupied (use final reference)
                final ParkingSpot finalSpot = spot;
                finalSpot.getParkingFloor().ifPresent(floor -> floor.onSpotOccupied(finalSpot));
                
                Ticket ticket = new Ticket(vehicle.getVehicleSize(), vehicle, spot);
                activeTicketsByLicense.put(vehicle.getLicenseNumber(), ticket);
                activeTicketsById.put(ticket.getTicketId(), ticket);
                System.out.printf("\n%s spot successfully booked for vehicle license: %s\n", spot.getId(), vehicle.getLicenseNumber());
                return Optional.of(ticket);
            }

            System.out.println("No Spot Found for this vehicle: "+vehicle.getLicenseNumber());
            return Optional.empty();
        }
    }

    public Optional<Double> unparkVehicle(String licenseNumber){
        synchronized (lock) {
            Ticket ticket = activeTicketsByLicense.remove(licenseNumber);
            if(ticket == null){
                throw new IllegalArgumentException("Ticket not found for license: " + licenseNumber);
            }
            activeTicketsById.remove(ticket.getTicketId());

            ticket.setExitDateTime(LocalDateTime.now());
            ParkingSpot spot = ticket.getParkingSpot();
            spot.unparkVehicle();
            
            // Notify floor that spot is freed (use final reference)
            final ParkingSpot finalSpot = spot;
            finalSpot.getParkingFloor().ifPresent(floor -> floor.onSpotFreed(finalSpot));

            if(feeStrategy == null){
                throw new IllegalStateException("Fee strategy not set");
            }

            Double parkingFee = feeStrategy.calculateFee(ticket);
            return Optional.of(parkingFee);
        }
    }

    public Optional<Double> unparkVehicleByTicketId(String ticketId){
        synchronized (lock) {
            Ticket ticket = activeTicketsById.remove(ticketId);
            if(ticket == null){
                throw new IllegalArgumentException("Ticket not found for ticket ID: " + ticketId);
            }
            activeTicketsByLicense.remove(ticket.getParkedVehicle().getLicenseNumber());

            ticket.setExitDateTime(LocalDateTime.now());
            ParkingSpot spot = ticket.getParkingSpot();
            spot.unparkVehicle();
            
            // Notify floor that spot is freed (use final reference)
            final ParkingSpot finalSpot = spot;
            finalSpot.getParkingFloor().ifPresent(floor -> floor.onSpotFreed(finalSpot));

            if(feeStrategy == null){
                throw new IllegalStateException("Fee strategy not set");
            }

            Double parkingFee = feeStrategy.calculateFee(ticket);
            return Optional.of(parkingFee);
        }
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
