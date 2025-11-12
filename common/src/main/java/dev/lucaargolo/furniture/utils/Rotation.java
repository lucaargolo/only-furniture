package dev.lucaargolo.furniture.utils;

public enum Rotation {

    R0, R90, R180, R270;

    public int getAngle() {
        return switch (this) {
            case R0 -> 0;
            case R90 -> 90;
            case R180 -> 180;
            case R270 -> 270;
        };
    }

    public Rotation getClockWise() {
        return switch (this) {
            case R0 -> R90;
            case R90 -> R180;
            case R180 -> R270;
            case R270 -> R0;
        };
    }

    public Rotation getCounterClockWise() {
        return switch (this) {
            case R0 -> R270;
            case R90 -> R0;
            case R180 -> R90;
            case R270 -> R180;
        };
    }
}
