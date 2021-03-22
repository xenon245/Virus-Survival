package com.github.monulo.virus

import com.google.common.collect.ImmutableList
import net.minecraft.server.v1_16_R3.ItemStack
import net.minecraft.server.v1_16_R3.NonNullList
import net.minecraft.server.v1_16_R3.PlayerInventory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import kotlin.math.min

object VirusInv {
    private val items: NonNullList<ItemStack>

    private val armor: NonNullList<ItemStack>

    private val extraSlots: NonNullList<ItemStack>

    private val contents: List<NonNullList<ItemStack>>

    init {
        val inv = PlayerInventory(null)

        items = inv.items
        armor = inv.armor
        extraSlots = inv.extraSlots
        contents = ImmutableList.of(items, armor, extraSlots)
    }

    private const val ITEMS = "items"
    private const val ARMOR = "armor"
    private const val EXTRA_SLOTS = "extraSlots"
    fun load(yaml: YamlConfiguration) {
        yaml.loadItemStackList(ITEMS, items)
        yaml.loadItemStackList(ARMOR, armor)
        yaml.loadItemStackList(EXTRA_SLOTS, extraSlots)
    }
    @Suppress("UNCHECKED_CAST")
    private fun ConfigurationSection.loadItemStackList(name: String, list: NonNullList<ItemStack>) {
        val map = getMapList(name)
        val items = map.map { CraftItemStack.asNMSCopy(CraftItemStack.deserialize(it as Map<String, Any>))}

        for(i in 0 until min(list.count(), items.count())) {
            list[i] = items[i]
        }
    }
    fun save(): YamlConfiguration {
        val yaml = YamlConfiguration()
        yaml.setItemStackList(ITEMS, items)
        yaml.setItemStackList(ARMOR, armor)
        yaml.setItemStackList(EXTRA_SLOTS, extraSlots)
        return yaml
    }
    private fun ConfigurationSection.setItemStackList(name: String, list: NonNullList<ItemStack>) {
        set(name, list.map { CraftItemStack.asCraftMirror(it).serialize() })
    }
    fun patch(player: Player) {
        val entityPlayer = (player as CraftPlayer).handle
        val playerInv = entityPlayer.inventory
        playerInv.setField("items", items)
        playerInv.setField("armor", armor)
        playerInv.setField("extraSlots", extraSlots)
        playerInv.setField("f", contents)
    }
    private fun Any.setField(name: String, value: Any) {
        val field = javaClass.getDeclaredField(name).apply {
            isAccessible = true
        }
        field.set(this, value)
    }
}
