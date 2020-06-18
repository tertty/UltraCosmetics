package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.DeathType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class MenuDeathEffects extends CosmeticMenu<DeathType> {

    public MenuDeathEffects(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.DEATHS);
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer ultraPlayer, int page) {
    }

    @Override
    public List<DeathType> enabled() {
        return DeathType.enabled();
    }

    @Override
    protected void toggleOn(UltraPlayer ultraPlayer, DeathType deathType, UltraCosmetics ultraCosmetics) {
        deathType.equip(ultraPlayer, ultraCosmetics);
    }

    @Override
    protected void toggleOff(UltraPlayer ultraPlayer) {
        ultraPlayer.removeDeathEffect();
    }

    @Override
    protected Cosmetic getCosmetic(UltraPlayer ultraPlayer) {
        return ultraPlayer.getCurrentDeathEffect();
    }
    
}