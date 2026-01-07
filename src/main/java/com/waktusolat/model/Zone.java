package com.waktusolat.model;

public class Zone {
    private String code;
    private String name;
    private String state;

    public Zone(String code, String name, String state) {
        this.code = code;
        this.name = name;
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return name;
    }
}
