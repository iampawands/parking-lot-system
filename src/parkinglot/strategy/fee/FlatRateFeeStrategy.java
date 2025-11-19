package parkinglot.strategy.fee;

import parkinglot.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;

public class FlatRateFeeStrategy implements FeeStrategy{

    private final double FLAT_RATE_PER_HOUR = 10d;
    private final double MINIMUM_CHARGE = 5d; // Minimum charge even for less than an hour

    @Override
    public double calculateFee(Ticket ticket) {
        LocalDateTime entryDateTime = ticket.getEntryDateTime();
        LocalDateTime exitDateTime = ticket.getExitDateTime();
        
        if(exitDateTime.isBefore(entryDateTime)){
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        
        Duration duration = Duration.between(entryDateTime, exitDateTime);
        long totalMinutes = duration.toMinutes();
        
        // Calculate hours, rounding up for partial hours
        // If parked for 0-60 minutes, charge for 1 hour (minimum charge)
        // If parked for 61-120 minutes, charge for 2 hours, etc.
        long hours = totalMinutes == 0 ? 1 : (totalMinutes + 59) / 60; // Round up
        
        double fee = FLAT_RATE_PER_HOUR * hours;
        
        // Apply minimum charge
        return Math.max(fee, MINIMUM_CHARGE);
    }
}
