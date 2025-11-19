package parkinglot.model;

import parkinglot.enums.VehicleSize;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    private final String ticketId;
    private final ParkingSpot parkingSpot;
    private final VehicleSize vehicleSize;
    private final Vehicle parkedVehicle;
    private final LocalDateTime entryDateTime;
    private LocalDateTime exitDateTime;

    public Ticket(VehicleSize vehicleSize, Vehicle parkedVehicle, ParkingSpot parkingSpot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicleSize = vehicleSize;
        this.parkedVehicle = parkedVehicle;
        this.entryDateTime = LocalDateTime.now();
        this.parkingSpot = parkingSpot;
    }

    public void setExitDateTime(LocalDateTime exitDateTime) {
        this.exitDateTime = exitDateTime;
    }

    public String getTicketId() {
        return ticketId;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public LocalDateTime getEntryDateTime() {
        return entryDateTime;
    }

    public LocalDateTime getExitDateTime() {
        return exitDateTime;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return ticketId.equals(ticket.ticketId);
    }

    @Override
    public int hashCode() {
        return ticketId.hashCode();
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", vehicle=" + parkedVehicle.getLicenseNumber() +
                ", vehicleSize=" + vehicleSize +
                ", entryDateTime=" + entryDateTime +
                ", exitDateTime=" + exitDateTime +
                ", spotId=" + parkingSpot.getId() +
                '}';
    }
}
