package io.github.Lefraudeur.DeACoudre.Utils;

public class Coordinates {
    public double x, y, z;
    public float pitch, yaw;
    public Coordinates(double x, double y, double z, float yaw, float pitch){
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}
