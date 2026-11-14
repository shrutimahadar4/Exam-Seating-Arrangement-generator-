package com.examseating.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classroom model class
 */
public class Classroom {
    private int id;
    private String roomNumber;
    private int benches;
    private int seatsPerBench;
    private int capacity;
    private List<Bench> benchList;

    public Classroom() {
        this.benchList = new ArrayList<>();
    }

    public Classroom(int id, String roomNumber, int benches, int seatsPerBench) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.benches = benches;
        this.seatsPerBench = seatsPerBench;
        this.capacity = benches * seatsPerBench;
        this.benchList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getBenches() {
        return benches;
    }

    public void setBenches(int benches) {
        this.benches = benches;
        this.capacity = this.benches * this.seatsPerBench;
    }

    public int getSeatsPerBench() {
        return seatsPerBench;
    }

    public void setSeatsPerBench(int seatsPerBench) {
        this.seatsPerBench = seatsPerBench;
        this.capacity = this.benches * this.seatsPerBench;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Bench> getBenchList() {
        return benchList;
    }

    public void setBenchList(List<Bench> benchList) {
        this.benchList = benchList;
    }

    @Override
    public String toString() {
        return "Classroom [roomNumber=" + roomNumber + ", benches=" + benches + ", seatsPerBench=" + seatsPerBench
                + ", capacity=" + capacity + "]";
    }
}
