package parkinglot;

import parkinglot.enums.VehicleSize;
import parkinglot.model.*;
import parkinglot.service.ParkingLot;
import parkinglot.strategy.fee.FlatRateFeeStrategy;
import parkinglot.strategy.parking.BestFitParkingStrategy;

public class ParkingLotDemo {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();


        ParkingFloor parkingFloor1 = new ParkingFloor();
        parkingFloor1.addSpot(new ParkingSpot(VehicleSize.SMALL));
        parkingFloor1.addSpot(new ParkingSpot(VehicleSize.MEDIUM));
        parkingFloor1.addSpot(new ParkingSpot(VehicleSize.LARGE));
        parkingFloor1.addSpot(new ParkingSpot(VehicleSize.MEDIUM));

        ParkingFloor parkingFloor2 = new ParkingFloor();
        parkingFloor2.addSpot(new ParkingSpot(VehicleSize.SMALL));
        parkingFloor2.addSpot(new ParkingSpot(VehicleSize.SMALL));
        parkingFloor2.addSpot(new ParkingSpot(VehicleSize.LARGE));
        parkingFloor2.addSpot(new ParkingSpot(VehicleSize.LARGE));


        parkingLot.addFloor(parkingFloor1);
        parkingLot.addFloor(parkingFloor2);
        parkingLot.setParkingStrategy(new BestFitParkingStrategy());
        parkingLot.setFeeStrategy(new FlatRateFeeStrategy());


        parkingFloor1.printAvailability();
        parkingFloor2.printAvailability();

        Vehicle vehicle1 = new Car("1234");
        Vehicle vehicle2 = new Bike("q3462");

        parkingLot.parkVehicle(vehicle1);

        parkingFloor1.printAvailability();
        parkingFloor2.printAvailability();

        parkingLot.parkVehicle(vehicle2);

        parkingFloor1.printAvailability();
        parkingFloor2.printAvailability();
    }
}
