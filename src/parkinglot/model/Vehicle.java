package parkinglot.model;

import parkinglot.enums.VehicleSize;

public abstract class Vehicle {
    private final String licenseNumber;
    private final VehicleSize vehicleSize;

    public Vehicle(String licenseNumber, VehicleSize vehicleSize) {
        this.licenseNumber = licenseNumber;
        this.vehicleSize = vehicleSize;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return licenseNumber.equals(vehicle.licenseNumber);
    }

    @Override
    public int hashCode() {
        return licenseNumber.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "licenseNumber='" + licenseNumber + '\'' +
                ", vehicleSize=" + vehicleSize +
                '}';
    }
}
