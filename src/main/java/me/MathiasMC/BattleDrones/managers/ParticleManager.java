package me.MathiasMC.BattleDrones.managers;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class ParticleManager {

    private final BattleDrones plugin;

    public HashMap<String, Set<double[]>> lib = new HashMap<>();

    public ParticleManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void load() {
        lib.clear();
        for (String particle : Objects.requireNonNull(plugin.particles.get.getConfigurationSection("")).getKeys(false)) {
            if (Objects.requireNonNull(plugin.particles.get.getString(particle + ".type")).equalsIgnoreCase("sphere")) {
                final Set<double[]> set = new LinkedHashSet<>();
                final double r = plugin.particles.get.getDouble(particle + ".radius");
                final int distance = plugin.particles.get.getInt(particle + ".distance");
                for (double phi = 0; phi <= Math.PI; phi += Math.PI / distance) {
                    for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / distance) {
                        double x = r * Math.cos(theta) * Math.sin(phi);
                        double y = r * Math.cos(phi) + 0.3;
                        double z = r * Math.sin(theta) * Math.sin(phi);
                        set.add(new double[] { x, y , z });
                    }
                }
                lib.put(particle, set);
            } else if (Objects.requireNonNull(plugin.particles.get.getString(particle + ".type")).equalsIgnoreCase("circle")) {
                final Set<double[]> set = new LinkedHashSet<>();
                final int points = plugin.particles.get.getInt(particle + ".distance");
                final double radius = plugin.particles.get.getDouble(particle + ".radius");
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    set.add(new double[] { radius * Math.sin(angle), 0.0d , radius * Math.cos(angle) });
                }
                lib.put(particle, set);
            }
        }
    }

    public void displayParticle(final String particleName, final String particleType, final Location location, final int r, final int g, final int b, final int size, final int amount) {
        final World world = location.getWorld();
        final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
        for (double[] set : lib.get(particleName)) {
            location.add(set[0], set[1], set[2]);
            switchParticle(particleType, world, location, amount, dustOptions);
            location.subtract(set[0], set[1], set[2]);
        }
    }

    public void switchParticle(final String particleType, final World world, final Location location, final int amount, final Particle.DustOptions dustOptions) {
        if ("REDSTONE".equals(particleType)) {
            world.spawnParticle(Particle.REDSTONE, location, amount, 0, 0, 0, 0F, dustOptions);
            return;
        }
        world.spawnParticle(Particle.valueOf(particleType), location, amount, 0, 0, 0, 0F);
    }

    public void line(String particleType, Location start, Location end, double distance, double space, int r, int g, int b, int amount, int size) {
        final Vector p1 = start.toVector();
        final Vector vector = end.toVector().clone().subtract(p1).normalize().multiply(space);
        final World world = start.getWorld();
        if (world == null) {
            return;
        }
        final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            switchParticle(particleType, world, p1.toLocation(world), amount, dustOptions);
            length += space;
        }
    }
}
