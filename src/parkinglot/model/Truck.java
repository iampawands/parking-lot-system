package parkinglot.model;

import parkinglot.enums.VehicleSize;

public class Truck extends Vehicle{

    public Truck(String licenseNumber) {
        super(licenseNumber, VehicleSize.LARGE);
    }
}
