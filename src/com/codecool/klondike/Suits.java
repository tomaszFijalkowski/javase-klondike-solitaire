package com.codecool.klondike;

public enum Suits {
    HEARTS ("hearts"),
    DIAMONDS ("diamonds"),
    SPADES ("spades"),
    CLUBS ("clubs");

    private final String name;

    Suits(String text) {
        this.name = text;
    }

    public String getName() {
        return this.name;
    }
}
