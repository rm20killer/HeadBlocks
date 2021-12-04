package fr.aerwyn81.headblocks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class Helper118 implements IVersionCompatibility {

    @Override
    public ItemStack createHeadItemStack() {
        return new ItemStack(Material.PLAYER_HEAD);
    }

    @Override
    public boolean isLeftHand(Object event) {
        return ((PlayerInteractEvent) event).getHand() == EquipmentSlot.OFF_HAND;
    }

    @Override
    public ItemStack getItemStackInHand(Object p) {
        return ((Player) p).getInventory().getItemInMainHand();
    }

    @Override
    public void sendTitle(Object player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ((Player) player).sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }
}
