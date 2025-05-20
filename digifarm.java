// Spring Boot backend example for a basic digital farming platform

package com.example.digitalfarming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class DigitalFarmingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalFarmingApplication.class, args);
    }

}

// --- Data Models ---

class Farm {
    private String farmId;
    private String name;
    private String location;
    private double size;
    private LocalDateTime createdAt;

    public Farm(String name, String location, double size) {
        this.farmId = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getFarmId() {
        return farmId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getSize() {
        return size;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

class Sensor {
    private String sensorId;
    private String farmId;
    private String sensorType;
    private String location;
    private LocalDateTime registeredAt;

    public Sensor(String farmId, String sensorType, String location) {
        this.sensorId = UUID.randomUUID().toString();
        this.farmId = farmId;
        this.sensorType = sensorType;
        this.location = location;
        this.registeredAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSensorId() {
        return sensorId;
    }

    public String getFarmId() {
        return farmId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}

class Reading {
    private String sensorId;
    private OffsetDateTime timestamp;
    private double value;
    private String unit;
    private LocalDateTime receivedAt;

    public Reading(String sensorId, OffsetDateTime timestamp, double value, String unit) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.value = value;
        this.unit = unit;
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSensorId() {
        return sensorId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}

// --- Data Storage (In-Memory - Replace with Database) ---

@RestController
@RequestMapping("/api")
public class DigitalFarmingController {

    private Map<String, Farm> farms = new HashMap<>();
    private Map<String, Sensor> sensors = new HashMap<>();
    private List<Reading> readings = new ArrayList<>();

    // --- Farm Management ---

    @PostMapping("/farms")
    public ResponseEntity<Map<String, String>> createFarm(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String location = (String) payload.get("location");
        Double size = (Double) payload.get("size");

        if (name == null || location == null || size == null) {
            return new ResponseEntity<>(Map.of("error", "Missing required fields"), HttpStatus.BAD_REQUEST);
        }

        Farm newFarm = new Farm(name, location, size);
        farms.put(newFarm.getFarmId(), newFarm);
        return new ResponseEntity<>(Map.of("message", "Farm created successfully", "farmId", newFarm.getFarmId()), HttpStatus.CREATED);
    }

    @GetMapping("/farms/{farmId}")
    public ResponseEntity<?> getFarm(@PathVariable String farmId) {
        if (!farms.containsKey(farmId)) {
            return new ResponseEntity<>(Map.of("error", "Farm not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(farms.get(farmId), HttpStatus.OK);
    }

    @GetMapping("/farms")
    public ResponseEntity<List<Farm>> listFarms() {
        return new ResponseEntity<>(new ArrayList<>(farms.values()), HttpStatus.OK);
    }

    // --- Sensor Management ---

    @PostMapping("/sensors")
    public ResponseEntity<Map<String, String>> registerSensor(@RequestBody Map<String, Object> payload) {
        String farmId = (String) payload.get("farmId");
        String sensorType = (String) payload.get("sensorType");
        String location = (String) payload.get("location");

        if (farmId == null || sensorType == null || location == null) {
            return new ResponseEntity<>(Map.of("error", "Missing required fields"), HttpStatus.BAD_REQUEST);
        }

        if (!farms.containsKey(farmId)) {
            return new ResponseEntity<>(Map.of("error", "Farm not found"), HttpStatus.NOT_FOUND);
        }

        Sensor newSensor = new Sensor(farmId, sensorType, location);
        sensors.put(newSensor.getSensorId(), newSensor);
        return new ResponseEntity<>(Map.of("message", "Sensor registered successfully", "sensorId", newSensor.getSensorId()), HttpStatus.CREATED);
    }

    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<?> getSensor(@PathVariable String sensorId) {
        if (!sensors.containsKey(sensorId)) {
            return new ResponseEntity<>(Map.of("error", "Sensor not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sensors.get(sensorId), HttpStatus.OK);
    }

    @GetMapping("/farms/{farmId}/sensors")
    public ResponseEntity<List<Sensor>> getFarmSensors(@PathVariable String farmId) {
        List<Sensor> farmSensors = sensors.values().stream()
                .filter(sensor -> sensor.getFarmId().equals(farmId))
                .toList();
        return new ResponseEntity<>(farmSensors, HttpStatus.OK);
    }

    // --- Data Readings ---

    @PostMapping("/readings")
    public ResponseEntity<Map<String, String>> addReading(@RequestBody Map<String, Object> payload) {
        String sensorId = (String) payload.get("sensorId");
        String timestampStr = (String) payload.get("timestamp");
        Double value = (Double) payload.get("value");
        String unit = (String) payload.get("unit");

        if (sensorId == null || timestampStr == null || value == null || unit == null) {
            return new ResponseEntity<>(Map.of("error", "Missing required fields"), HttpStatus.BAD_REQUEST);
        }

        if (!sensors.containsKey(sensorId)) {
            return new ResponseEntity<>(Map.of("error", "Sensor not found"), HttpStatus.NOT_FOUND);
        }

        try {
            OffsetDateTime timestamp = OffsetDateTime.parse(timestampStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            Reading newReading = new Reading(sensorId, timestamp, value, unit);
            readings.add(newReading);
            return new ResponseEntity<>(Map.of("message", "Reading added successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Invalid timestamp format. Use ISO 8601 format (e.g., 2023-10-27T10:00:00+00:00)"), HttpStatus.BAD_REQUEST);
        }
    }

    // Add more endpoints for fetching readings, analyzing data, etc.
}