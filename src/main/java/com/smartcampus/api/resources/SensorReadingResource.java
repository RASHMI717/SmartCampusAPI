/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.SensorUnavailableException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import com.smartcampus.api.store.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found.")
                    .build();
        }

        List<SensorReading> readings = DataStore.sensorReadings.get(sensorId);

        if (readings == null) {
            readings = new ArrayList<>();
        }

        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found.")
                    .build();
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Reading data is required.")
                    .build();
        }

        if (sensor.getStatus() != null && sensor.getStatus().equalsIgnoreCase("MAINTENANCE")) {
            throw new SensorUnavailableException("Cannot add reading. Sensor is under maintenance.");
        }

        reading.setId(UUID.randomUUID().toString());
        reading.setSensorId(sensorId);
        reading.setTimestamp(System.currentTimeMillis());

        List<SensorReading> readings = DataStore.sensorReadings.get(sensorId);
        if (readings == null) {
            readings = new ArrayList<>();
            DataStore.sensorReadings.put(sensorId, readings);
        }

        readings.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}