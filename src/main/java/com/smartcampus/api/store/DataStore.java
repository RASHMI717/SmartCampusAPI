/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.api.store;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    public static final Map<String, Room> rooms = new LinkedHashMap<>();
    public static final Map<String, Sensor> sensors = new LinkedHashMap<>();
    public static final Map<String, List<SensorReading>> sensorReadings = new LinkedHashMap<>();

    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room room2 = new Room("LAB-101", "Computer Lab 101", 30);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
    }

    private DataStore() {
    }
}