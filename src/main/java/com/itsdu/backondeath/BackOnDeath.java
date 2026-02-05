package com.itsdu.backondeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackOnDeath implements ModInitializer {
    public static final String MOD_ID = "backondeath";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Back On Death...");
        CONFIG = ModConfig.load();

        // Register /back command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BackCommand.register(dispatcher);
        });

        // Register death listener
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();

                // Check if dimension is disabled
                String dimId = world.getRegistryKey().getValue().toString();
                if (CONFIG.disabledDimensions.contains(dimId)) {
                    return;
                }

                DeathDataManager dataManager = DeathDataManager.getServerState(world.getServer());
                
                DeathLocation location = new DeathLocation(
                    world.getRegistryKey().getValue(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYaw(),
                    player.getPitch()
                );

                dataManager.setDeathLocation(player.getUuid(), location);

                player.sendMessage(Text.literal(replaceColorCodes(CONFIG.deathMessage)), false);
            }
        });
    }

    public static String replaceColorCodes(String text) {
        return text.replace("§", "\u00A7");
    }
}
