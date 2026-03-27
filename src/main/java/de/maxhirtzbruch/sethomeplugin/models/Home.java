package de.maxhirtzbruch.sethomeplugin.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Represents a single saved home.
 */
public class Home {

    private final UUID ownerUUID;
    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdAt;

    public Home(UUID ownerUUID, String name, Location location) {
        this.ownerUUID  = ownerUUID;
        this.name       = name;
        this.world      = location.getWorld().getName();
        this.x          = location.getX();
        this.y          = location.getY();
        this.z          = location.getZ();
        this.yaw        = location.getYaw();
        this.pitch      = location.getPitch();
        this.createdAt  = System.currentTimeMillis();
    }

    /** Constructor for deserialization (loading from YAML). */
    public Home(UUID ownerUUID, String name, String world,
                double x, double y, double z,
                float yaw, float pitch, long createdAt) {
        this.ownerUUID = ownerUUID;
        this.name      = name;
        this.world     = world;
        this.x         = x;
        this.y         = y;
        this.z         = z;
        this.yaw       = yaw;
        this.pitch     = pitch;
        this.createdAt = createdAt;
    }

    // ── Getters ──────────────────────────────────────────────

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getName()    { return name; }
    public String getWorld()   { return world; }
    public double getX()       { return x; }
    public double getY()       { return y; }
    public double getZ()       { return z; }
    public float  getYaw()     { return yaw; }
    public float  getPitch()   { return pitch; }
    public long   getCreatedAt(){ return createdAt; }

    /**
     * Returns the Bukkit Location object.
     * Returns null if the world is not loaded.
     */
    public Location toLocation() {
        if (Bukkit.getWorld(world) == null) return null;
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    /** Convenience: formatted coordinates for display. */
    public String getFormattedX() { return String.valueOf((int) x); }
    public String getFormattedY() { return String.valueOf((int) y); }
    public String getFormattedZ() { return String.valueOf((int) z); }
}
