package parkinglot.model;

import parkinglot.enums.VehicleSize;

public class Bike extends Vehicle{

    public Bike(String licenseNumber) {
        super(licenseNumber, VehicleSize.SMALL);
    }
}
