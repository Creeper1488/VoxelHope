package org.example;

public class Cube {
    float x, y, z, angle;
    boolean is_placed;
    int type;

    public Cube(float x, float y, float z, float angle, boolean is_placed, int type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.is_placed = is_placed;
        this.type = type;
    }
}
