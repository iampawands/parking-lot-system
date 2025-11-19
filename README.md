# Parking Lot Management System

A production-grade, thread-safe parking lot management system implemented in Java. This system demonstrates advanced object-oriented design principles, design patterns, and concurrent programming best practices.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Key Components](#key-components)
- [Features](#features)
- [Usage](#usage)
- [Strategy Patterns](#strategy-patterns)
- [Thread Safety](#thread-safety)
- [Performance Optimizations](#performance-optimizations)
- [Project Structure](#project-structure)

## Overview

This parking lot system provides a complete solution for managing vehicle parking with support for:
- Multiple floors and parking spots
- Different vehicle sizes (Small, Medium, Large)
- Configurable parking strategies
- Flexible fee calculation strategies
- Thread-safe concurrent operations
- Real-time statistics and monitoring

## Architecture

The system follows a layered architecture:

```
┌─────────────────────────────────────┐
│      ParkingLot (Service Layer)     │
│    - Singleton Pattern              │
│    - Thread-safe Operations         │
└──────────────┬──────────────────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───▼────────┐    ┌──────▼──────────┐
│  Strategy  │    │   Model Layer    │
│   Layer    │    │                  │
│            │    │ - ParkingFloor   │
│ - Parking  │    │ - ParkingSpot    │
│ - Fee      │    │ - Vehicle        │
│            │    │ - Ticket         │
└────────────┘    └──────────────────┘
```

## Design Patterns

### 1. **Singleton Pattern**
- `ParkingLot` uses double-checked locking for thread-safe singleton initialization
- Ensures only one instance of the parking lot exists

### 2. **Strategy Pattern**
- **Parking Strategy**: Different algorithms for finding parking spots
- **Fee Strategy**: Various pricing models for calculating parking fees

### 3. **Inheritance**
- `Vehicle` is an abstract base class
- `Car`, `Bike`, `Truck` extend `Vehicle` with specific sizes

## Key Components

### Model Layer

#### `Vehicle` (Abstract Class)
- Base class for all vehicles
- Properties: `licenseNumber`, `vehicleSize`
- Subclasses: `Car`, `Bike`, `Truck`

#### `ParkingSpot`
- Represents a single parking spot
- Properties: `id`, `spotSize`, `isOccupied`, `parkedVehicle`
- Thread-safe operations with `volatile` fields
- Supports size compatibility (large spots can accommodate smaller vehicles)

#### `ParkingFloor`
- Manages multiple parking spots
- Maintains availability queues for O(1) spot lookup
- Tracks floor number and statistics

#### `Ticket`
- Generated when a vehicle parks
- Contains: `ticketId`, `vehicle`, `parkingSpot`, `entryDateTime`, `exitDateTime`
- Used for fee calculation

### Service Layer

#### `ParkingLot`
- Main service class (Singleton)
- Manages floors, tickets, and strategies
- Provides parking/unparking operations
- Thread-safe with synchronized blocks

### Strategy Layer

#### Parking Strategies
- `BestFitParkingStrategy`: Finds the smallest available spot
- `NearestToEntranceStrategy`: Prioritizes lower floors and nearest spots
- `FirstAvailableStrategy`: Fastest strategy, returns first available spot

#### Fee Strategies
- `FlatRateFeeStrategy`: Simple hourly rate with minimum charge
- `TieredFeeStrategy`: Progressive pricing (first hour, next hours, premium)
- `PeakHoursFeeStrategy`: Time-based pricing with peak hours

## Features

### 1. **Vehicle Size Compatibility**
- Large spots can accommodate all vehicle sizes
- Medium spots can accommodate small and medium vehicles
- Small spots can only accommodate small vehicles

### 2. **Thread Safety**
- All critical operations are thread-safe
- Uses `volatile` for visibility
- `ConcurrentHashMap` and `CopyOnWriteArrayList` for concurrent collections
- Synchronized blocks for atomic operations

### 3. **Statistics & Monitoring**
- Real-time availability tracking
- Utilization rate calculation
- Active tickets count
- Floor-wise and overall statistics

### 4. **Flexible Strategies**
- Easy to add new parking algorithms
- Configurable fee calculation models
- Runtime strategy switching

## Usage

### Basic Setup

```java
// Get singleton instance
ParkingLot parkingLot = ParkingLot.getInstance();

// Create floors
ParkingFloor floor1 = new ParkingFloor(1);
floor1.addSpot(new ParkingSpot(VehicleSize.SMALL));
floor1.addSpot(new ParkingSpot(VehicleSize.MEDIUM));
floor1.addSpot(new ParkingSpot(VehicleSize.LARGE));

ParkingFloor floor2 = new ParkingFloor(2);
floor2.addSpot(new ParkingSpot(VehicleSize.SMALL));
floor2.addSpot(new ParkingSpot(VehicleSize.MEDIUM));

// Add floors to parking lot
parkingLot.addFloor(floor1);
parkingLot.addFloor(floor2);

// Set strategies
parkingLot.setParkingStrategy(new BestFitParkingStrategy());
parkingLot.setFeeStrategy(new FlatRateFeeStrategy());
```

### Parking a Vehicle

```java
// Create vehicles
Vehicle car = new Car("ABC1234");
Vehicle bike = new Bike("XYZ5678");
Vehicle truck = new Truck("DEF9012");

// Park vehicles
Optional<Ticket> ticket1 = parkingLot.parkVehicle(car);
Optional<Ticket> ticket2 = parkingLot.parkVehicle(bike);

if (ticket1.isPresent()) {
    System.out.println("Car parked! Ticket ID: " + ticket1.get().getTicketId());
}
```

### Unparking and Fee Calculation

```java
// Unpark by license number
Optional<Double> fee = parkingLot.unparkVehicle("ABC1234");
if (fee.isPresent()) {
    System.out.println("Parking fee: $" + fee.get());
}

// Or unpark by ticket ID
Optional<Ticket> ticket = parkingLot.getTicketById("ticket-id");
if (ticket.isPresent()) {
    Optional<Double> fee = parkingLot.unparkVehicleByTicketId("ticket-id");
}
```

### Statistics

```java
// Get overall statistics
Map<String, Object> stats = parkingLot.getStatistics();
System.out.println("Total Spots: " + stats.get("totalSpots"));
System.out.println("Occupied: " + stats.get("occupiedSpots"));
System.out.println("Available: " + stats.get("availableSpots"));
System.out.println("Utilization: " + stats.get("utilizationRate") + "%");

// Floor-specific statistics
int occupied = floor1.getOccupiedSpots();
int available = floor1.getAvailableSpots(VehicleSize.MEDIUM);
```

## Strategy Patterns

### Parking Strategies

#### BestFitParkingStrategy
```java
parkingLot.setParkingStrategy(new BestFitParkingStrategy());
// Finds the smallest available spot that can fit the vehicle
// Optimizes space utilization
```

#### NearestToEntranceStrategy
```java
parkingLot.setParkingStrategy(new NearestToEntranceStrategy());
// Prioritizes lower floor numbers
// Useful for customer convenience
```

#### FirstAvailableStrategy
```java
parkingLot.setParkingStrategy(new FirstAvailableStrategy());
// Fastest strategy - returns first available spot
// Best for high-throughput scenarios
```

### Fee Strategies

#### FlatRateFeeStrategy
```java
parkingLot.setFeeStrategy(new FlatRateFeeStrategy());
// Default: $10/hour, minimum $5
// Simple and predictable pricing
```

#### TieredFeeStrategy
```java
// Custom rates: first hour, next hours, premium rate, minimum
parkingLot.setFeeStrategy(new TieredFeeStrategy(10.0, 15.0, 20.0, 5.0));
// Progressive pricing model
```

#### PeakHoursFeeStrategy
```java
parkingLot.setFeeStrategy(new PeakHoursFeeStrategy());
// Default: Peak hours 8-10 AM and 5-7 PM at $20/hour
// Normal hours at $10/hour
// Customizable peak times
```

## Thread Safety

The system is designed for concurrent operations:

### Mechanisms Used

1. **Volatile Fields**: `isOccupied` and `parkedVehicle` in `ParkingSpot` for visibility
2. **Synchronized Methods**: Critical operations like `parkVehicle()` and `unparkVehicle()`
3. **Concurrent Collections**: 
   - `ConcurrentHashMap` for ticket storage
   - `CopyOnWriteArrayList` for floor list
4. **Double-Checked Locking**: Singleton initialization
5. **Atomic Operations**: `parkVehicle()` returns boolean for atomic check-and-set

### Example: Concurrent Parking

```java
// Multiple threads can safely park vehicles concurrently
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        Vehicle vehicle = new Car("CAR" + Thread.currentThread().getId());
        parkingLot.parkVehicle(vehicle);
    });
}
```

## Performance Optimizations

### 1. **O(1) Spot Lookup**
- `ParkingFloor` maintains availability queues by vehicle size
- Fast path for finding available spots
- Fallback to full scan if queue is out of sync

### 2. **Reduced Synchronization**
- Minimal synchronized blocks
- Volatile fields for read-heavy operations
- Concurrent collections for better scalability

### 3. **Efficient Data Structures**
- `ConcurrentHashMap` for O(1) ticket lookups
- `CopyOnWriteArrayList` for thread-safe floor iteration
- Queue-based availability tracking

### 4. **Memory Efficiency**
- Lazy initialization
- Optional types to avoid null checks
- Efficient object creation

## Project Structure

```
src/parkinglot/
├── enums/
│   └── VehicleSize.java          # Vehicle size enumeration
├── model/
│   ├── Vehicle.java              # Abstract vehicle class
│   ├── Car.java                  # Car implementation
│   ├── Bike.java                 # Bike implementation
│   ├── Truck.java                # Truck implementation
│   ├── ParkingSpot.java          # Parking spot model
│   ├── ParkingFloor.java         # Floor management
│   └── Ticket.java               # Parking ticket
├── service/
│   └── ParkingLot.java           # Main service (Singleton)
├── strategy/
│   ├── parking/
│   │   ├── ParkingStrategy.java           # Parking strategy interface
│   │   ├── BestFitParkingStrategy.java    # Best fit algorithm
│   │   ├── NearestToEntranceStrategy.java # Nearest spot algorithm
│   │   └── FirstAvailableStrategy.java    # First available algorithm
│   └── fee/
│       ├── FeeStrategy.java              # Fee strategy interface
│       ├── FlatRateFeeStrategy.java      # Flat rate pricing
│       ├── TieredFeeStrategy.java        # Tiered pricing
│       └── PeakHoursFeeStrategy.java     # Peak hours pricing
└── ParkingLotDemo.java           # Demo/Example usage
```

## Compilation and Execution

### Compile
```bash
javac -d out/production/lld-practice src/parkinglot/**/*.java
```

### Run Demo
```bash
java -cp out/production/lld-practice parkinglot.ParkingLotDemo
```

## Extending the System

### Adding a New Parking Strategy

```java
public class CustomParkingStrategy implements ParkingStrategy {
    @Override
    public Optional<ParkingSpot> findParkingSpot(
            List<ParkingFloor> floorList, Vehicle vehicle) {
        // Your custom logic here
        return Optional.of(spot);
    }
}
```

### Adding a New Fee Strategy

```java
public class CustomFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(Ticket ticket) {
        // Your custom fee calculation
        return fee;
    }
}
```

## Key Design Decisions

1. **Singleton Pattern**: Ensures single parking lot instance across the application
2. **Strategy Pattern**: Allows runtime strategy switching without code changes
3. **Volatile Fields**: Ensures visibility across threads without full synchronization
4. **Concurrent Collections**: Better performance than synchronized collections
5. **Optional Return Types**: Explicit handling of null cases
6. **Abstract Vehicle Class**: Easy to extend for new vehicle types

## Best Practices Demonstrated

- ✅ Thread-safe concurrent operations
- ✅ Design patterns (Singleton, Strategy, Inheritance)
- ✅ SOLID principles
- ✅ Clean code and separation of concerns
- ✅ Performance optimizations
- ✅ Extensibility and maintainability
- ✅ Production-ready error handling

## License

This is a practice project for learning Low-Level Design (LLD) principles.

