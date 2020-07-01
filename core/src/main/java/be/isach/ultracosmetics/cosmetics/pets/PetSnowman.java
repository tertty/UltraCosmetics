package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.UCMaterial;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.Overridden;
import org.bukkit.entity.Snowman;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.block.*;

/**
 * Represents an instance of a snowman pet summoned by a player.
 *
 * @author RadBuilder
 * @since 07-02-2017
 */
public class PetSnowman extends Pet {
    public PetSnowman(UltraPlayer owner, UltraCosmetics ultraCosmetics) {
        super(owner, ultraCosmetics, PetType.getByName("snowman"), ItemFactory.create(UCMaterial.SNOWBALL, UltraCosmeticsData.get().getItemNoPickupString()));
        Bukkit.getScheduler().runTaskLater(getUltraCosmetics(), () -> {
            if (getOwner() != null && getEntity() != null) {
                Snowman snowman = (Snowman) getEntity();
                snowman.setDerp(false);
            }
        }, 30);
    }

    @EventHandler(priority = EventPriority.LOWEST)
        public void snow(EntityBlockFormEvent e) {
            if((e.getEntity() instanceof Snowman) && (e.getEntity().getCustomName() != null)){
                if(e.getNewState().getType() == UCMaterial.SNOW.parseMaterial()){
                    e.setCancelled(true);
                    e.getBlock().setType(UCMaterial.AIR.parseMaterial());
                }
            }
        }
}
