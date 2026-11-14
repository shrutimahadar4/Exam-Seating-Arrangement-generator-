package com.examseating.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Bench model class representing a bench in a classroom
 */
public class Bench {
    private int benchNumber;
    private List<Seat> seats;

    public Bench() {
        this.seats = new ArrayList<>();
    }

    public Bench(int benchNumber) {
        this.benchNumber = benchNumber;
        this.seats = new ArrayList<>();
    }

    public Bench(int benchNumber, int seatsCount) {
        this.benchNumber = benchNumber;
        this.seats = new ArrayList<>();
        for (int i = 1; i <= seatsCount; i++) {
            this.seats.add(new Seat(i, benchNumber));
        }
    }

    public int getBenchNumber() {
        return benchNumber;
    }

    public void setBenchNumber(int benchNumber) {
        this.benchNumber = benchNumber;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public void addSeat(Seat seat) {
        this.seats.add(seat);
    }
}
