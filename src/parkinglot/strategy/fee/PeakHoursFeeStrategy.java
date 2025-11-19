package parkinglot.strategy.fee;

import parkinglot.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Fee strategy with peak hours pricing.
 * Charges higher rates during peak hours (e.g., 8 AM - 10 AM, 5 PM - 7 PM).
 */
public class PeakHoursFeeStrategy implements FeeStrategy {
    private final double normalRate;
    private final double peakRate;
    private final double minimumCharge;
    private final LocalTime peakStartMorning;
    private final LocalTime peakEndMorning;
    private final LocalTime peakStartEvening;
    private final LocalTime peakEndEvening;

    public PeakHoursFeeStrategy() {
        this(10.0, 20.0, 5.0, 
             LocalTime.of(8, 0), LocalTime.of(10, 0),
             LocalTime.of(17, 0), LocalTime.of(19, 0));
    }

    public PeakHoursFeeStrategy(double normalRate, double peakRate, double minimumCharge,
                               LocalTime peakStartMorning, LocalTime peakEndMorning,
                               LocalTime peakStartEvening, LocalTime peakEndEvening) {
        this.normalRate = normalRate;
        this.peakRate = peakRate;
        this.minimumCharge = minimumCharge;
        this.peakStartMorning = peakStartMorning;
        this.peakEndMorning = peakEndMorning;
        this.peakStartEvening = peakStartEvening;
        this.peakEndEvening = peakEndEvening;
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
        
        // Calculate fee based on peak/non-peak hours
        double totalFee = 0.0;
        LocalDateTime current = entryDateTime;
        
        while (current.isBefore(exitDateTime)) {
            LocalTime currentTime = current.toLocalTime();
            boolean isPeakHour = isPeakHour(currentTime);
            double rate = isPeakHour ? peakRate : normalRate;
            
            // Calculate minutes until next hour or exit
            LocalDateTime nextHour = current.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endTime = exitDateTime.isBefore(nextHour) ? exitDateTime : nextHour;
            
            long minutesInThisPeriod = Duration.between(current, endTime).toMinutes();
            long hoursInThisPeriod = (minutesInThisPeriod + 59) / 60; // Round up
            
            totalFee += rate * hoursInThisPeriod;
            current = endTime;
        }
        
        return Math.max(totalFee, minimumCharge);
    }

    private boolean isPeakHour(LocalTime time) {
        return (time.isAfter(peakStartMorning) && time.isBefore(peakEndMorning)) ||
               (time.isAfter(peakStartEvening) && time.isBefore(peakEndEvening)) ||
               time.equals(peakStartMorning) || time.equals(peakStartEvening);
    }
}

