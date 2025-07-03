package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParticleManager {

    private final BattleDrones plugin;

    public HashMap<String, Set<double[]>> lib = new HashMap<>();

    public ParticleManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void load() {
        lib.clear();

        FileConfiguration particlesConfig = plugin.getFileUtils().particles;
        ConfigurationSection rootSection = particlesConfig.getConfigurationSection("");
        if (rootSection == null) {
            plugin.getTextUtils().error("Particles section missing!");
            return;
        }

        for (String particleName : rootSection.getKeys(false)) {
            String type = particlesConfig.getString(particleName + ".type", "").toLowerCase();

            Set<double[]> pointsSet;
            switch (type) {
                case "sphere":
                    pointsSet = calculateSpherePoints(
                      particlesConfig.getDouble(particleName + ".radius"),
                      particlesConfig.getInt(particleName + ".distance")
                    );
                    break;

                case "circle":
                    pointsSet = calculateCirclePoints(
                            particlesConfig.getDouble(particleName + ".radius"),
                            particlesConfig.getInt(particleName + ".distance")
                    );
                    break;
                default:
                    continue;
            }

            lib.put(particleName, pointsSet);
        }
    }

    private Set<double[]> calculateSpherePoints(double radius, int distance) {
        Set<double[]> points = new LinkedHashSet<>();
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / distance) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / distance) {
                double x = radius * Math.cos(theta) * Math.sin(phi);
                double y = radius * Math.cos(phi) + 0.3;
                double z = radius * Math.sin(theta) * Math.sin(phi);
                points.add(new double[]{x, y, z});
            }
        }
        return points;
    }

    private Set<double[]> calculateCirclePoints(double radius, int pointsCount) {
        Set<double[]> points = new LinkedHashSet<>();
        for (int i = 0; i < pointsCount; i++) {
            double angle = 2 * Math.PI * i / pointsCount;
            points.add(new double[]{radius * Math.sin(angle), 0.0d, radius * Math.cos(angle)});
        }
        return points;
    }

    public void displayParticle(String particleName, String particleType, Location location, int r, int g, int b, int size, int amount) {
        Set<double[]> points = lib.get(particleName);
        if (points == null) return;

        World world = location.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);

        for (double[] offset : points) {
            location.add(offset[0], offset[1], offset[2]);
            spawnParticle(particleType, world, location, amount, dustOptions);
            location.subtract(offset[0], offset[1], offset[2]);
        }
    }

    private void spawnParticle(String particleType, World world, Location location, int amount, Particle.DustOptions dustOptions) {
        if ("DUST".equalsIgnoreCase(particleType)) {
            world.spawnParticle(Particle.DUST, location, amount, 0, 0, 0, 0F, dustOptions);
        } else {
            try {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                world.spawnParticle(particle, location, amount, 0, 0, 0, 0F);
            } catch (IllegalArgumentException e) {
                plugin.getTextUtils().error("Invalid particle type: " + particleType);
            }
        }
    }


    public void displayLineParticle(String particleType, Location start, Location end, double distance, double space, int r, int g, int b, int amount, int size) {
        World world = start.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
        Vector current = start.toVector();
        Vector direction = end.toVector().subtract(current).normalize().multiply(space);

        double traveled = 0;
        while (traveled < distance) {
            spawnParticle(particleType, world, current.toLocation(world), amount, dustOptions);
            current.add(direction);
            traveled += space;
        }
    }
}