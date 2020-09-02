package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.DeathType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.SoundUtil;
import be.isach.ultracosmetics.util.Sounds;
import be.isach.ultracosmetics.util.UtilParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathBomb extends DeathEffect {

    int step = 0;
    float stepY = 0;
    float radius = 1.5f;

    double i = 0;

    public DeathBomb(UltraPlayer owner, UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, owner, DeathType.valueOf("deathbomb"));
    }

    // @Override
    // public void onUpdate() {
    //     for (int i = 0; i < 6; i++) {
    //         Location location = getPlayer().getLocation();
    //         double inc = (2 * Math.PI) / 100;
    //         double angle = step * inc + stepY + i;
    //         Vector v = new Vector();
    //         v.setX(Math.cos(angle) * radius);
    //         v.setZ(Math.sin(angle) * radius);
    //         UtilParticles.display(getType().getEffect(), location.add(v).add(0, stepY, 0));
    //         location.subtract(v).subtract(0, stepY, 0);
    //         if (stepY < 3) {
    //             radius -= 0.022;
    //             stepY += 0.045;
    //         } else {
    //             stepY = 0;
    //             step = 0;
    //             radius = 1.5f;
    //             SoundUtil.playSound(getPlayer(), Sounds.DIG_SNOW, .5f, 1.5f);
    //             UtilParticles.display(getType().getEffect(), location.clone().add(0, 3, 0), 48, 0.3f);
    //         }
    //     }
    // }

    @Override
    public void onUpdate() {
    }

    @EventHandler
        public void onDeath(PlayerDeathEvent e){
            Location location = e.getEntity().getPlayer().getLocation();
            Location location2 = location.clone();
            double radius = 1.1d;
            double radius2 = 1.1d;
            double particles = 100;

            for (int step = 0; step < 100; step += 4) {
                double interval = (2 * Math.PI) / particles;
                double angle = step * interval + i;
                Vector v = new Vector();
                v.setX(Math.cos(angle) * radius);
                v.setZ(Math.sin(angle) * radius);
                UtilParticles.display(getType().getEffect(), location.add(v));
                location.subtract(v);
                location.add(0, 0.12d, 0);
                radius -= 0.044f;
            }
            for (int step = 0; step < 100; step += 4) {
                double interval = (2 * Math.PI) / particles;
                double angle = step * interval + i + 3.5;
                Vector v = new Vector();
                v.setX(Math.cos(angle) * radius2);
                v.setZ(Math.sin(angle) * radius2);
                UtilParticles.display(getType().getEffect(), location2.add(v));
                location2.subtract(v);
                location2.add(0, 0.12d, 0);
                radius2 -= 0.044f;
            }
            i += 0.05;

            SoundUtil.playSound(location, Sounds.EXPLODE, 2.0f, 1.0f);
        }

}