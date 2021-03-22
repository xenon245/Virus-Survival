package com.github.monulo.virus

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File

class VirusPlugin: JavaPlugin(), Listener, Runnable {
    private val vaccine = ItemStack(Material.ENCHANTED_GOLDEN_APPLE).apply {
        itemMeta.setDisplayName("백신")
    }
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.scheduler.runTaskTimer(this, this, 0L, 1L)
        loadInventory()
        registerRecipe()
    }
    private fun registerRecipe() {
        val key = NamespacedKey(this, "cure")
        val recipe = ShapedRecipe(key, vaccine).apply {
            shape(" G ",
                        " W ",
                        "GAG")
            setIngredient('G', ItemStack(Material.GLASS))
            setIngredient('W', ItemStack(Material.WATER_BUCKET))
            setIngredient('A', ItemStack(Material.GOLDEN_APPLE))
        }
        Bukkit.addRecipe(recipe)
    }
    override fun onDisable() {
        save()
    }
    private fun loadInventory() {
        val file = File(dataFolder, "inventory.yml").also { if(!it.exists()) return}
        val yaml = YamlConfiguration.loadConfiguration(file)
        VirusInv.load(yaml)
    }
    private fun save() {
        val yaml = VirusInv.save()
        val dataFolder = dataFolder.also { it.mkdirs() }
        val file = File(dataFolder, "inventory.yml")
        yaml.save(file)
    }
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        VirusInv.patch(event.player)
    }
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        VirusInv.save()
    }
    @EventHandler
    fun onWorldSave(event: WorldSaveEvent) {
        save()
    }
    val survivor = arrayListOf<Player>()
    @EventHandler
    fun onItemSwap(event: PlayerSwapHandItemsEvent) {
        val item = event.mainHandItem ?: return
        if(item.isSimilar(vaccine)) {
            event.isCancelled = true
            survivor += event.player
            item.amount--
        }
    }
    override fun run() {
        for(player in survivor) {
            player.health = 20.0
            player.foodLevel = 20
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 32767, true, true, true))
        }
    }
}