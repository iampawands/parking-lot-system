package parkinglot.strategy.fee;

import parkinglot.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Tiered fee strategy with progressive pricing:
 * - First hour: Base rate
 * - Next 2 hours: Higher rate
 * - After 3 hours: Premium rate
 */
public class TieredFeeStrategy implements FeeStrategy {
    private final double firstHourRate;
    private final double nextHoursRate;
    private final double premiumRate;
    private final double minimumCharge;

    public TieredFeeStrategy() {
        this(10.0, 15.0, 20.0, 5.0);
    }

    public TieredFeeStrategy(double firstHourRate, double nextHoursRate, 
                            double premiumRate, double minimumCharge) {
        this.firstHourRate = firstHourRate;
        this.nextHoursRate = nextHoursRate;
        this.premiumRate = premiumRate;
        this.minimumCharge = minimumCharge;
    }

    @Override
    public double calculateFee(Ticket ticket) {
        LocalDateTime entryDateTime = ticket.getEntryDateTime();
        LocalDateTime exitDateTime = ticket.getExitDateTime();
        
        if(exitDateTime.isBefore(entryDateTime)){
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        
        Duration duration = Duration.between(entryDateTime, exitDateTime);
        long totalMinutes = duration.toMinutes();
        
        if (totalMinutes == 0) {
            return minimumCharge;
        }
        
        long hours = (totalMinutes + 59) / 60; // Round up
        
        double fee = 0.0;
        
        if (hours <= 1) {
            fee = firstHourRate;
        } else if (hours <= 3) {
            fee = firstHourRate + (hours - 1) * nextHoursRate;
        } else {
            fee = firstHourRate + 2 * nextHoursRate + (hours - 3) * premiumRate;
        }
        
        return Math.max(fee, minimumCharge);
    }
}

