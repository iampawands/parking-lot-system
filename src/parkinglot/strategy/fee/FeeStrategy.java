package parkinglot.strategy.fee;

import parkinglot.model.Ticket;

public interface FeeStrategy {
    double calculateFee(Ticket ticket);
}
