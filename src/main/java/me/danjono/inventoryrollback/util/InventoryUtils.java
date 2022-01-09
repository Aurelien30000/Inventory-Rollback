package me.danjono.inventoryrollback.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static void setSortedItems(Inventory inv, ItemStack[] mainInventory, ItemStack[] armour) {
        int item = 0;
        int position = 27;

        //Add items
        for (int i = 0; i < mainInventory.length - 5; i++) {
            final ItemStack itemStack = mainInventory[item];
            inv.setItem(position, itemStack);
            position++;
            item++;

            if (item == 9) {
                position = 0;
            }
        }

        item = 36;
        position = 39;

        //Add armour
        if (armour != null) {
            for (ItemStack itemStack : armour) {
                inv.setItem(position, itemStack);
                position--;
                item++;
            }
        }

        position = 40;

        for (int i = item; i < mainInventory.length; i++) {
            inv.setItem(position, mainInventory[item]);
            position++;
            item++;
        }

    }

    public static boolean isEmpty(Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack != null) {
                return false;
            }
        }
        return true;
    }

}
