package parkinglot.model;

import parkinglot.enums.VehicleSize;

public class Car extends Vehicle{

    public Car(String licenseNumber) {
        super(licenseNumber, VehicleSize.MEDIUM);
    }
}
