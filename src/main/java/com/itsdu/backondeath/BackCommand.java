package com.itsdu.backondeath;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Set;

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("back")
            .executes(BackCommand::run));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        if (player.isSpectator()) {
            source.sendError(Text.literal(BackOnDeath.replaceColorCodes(BackOnDeath.CONFIG.spectatorMessage)));
            return 0;
        }

        DeathDataManager dataManager = DeathDataManager.getServerState(source.getServer());
        DeathLocation deathLocation = dataManager.getDeathLocation(player.getUuid());

        if (deathLocation == null) {
            source.sendFeedback(() -> Text.literal(BackOnDeath.replaceColorCodes(BackOnDeath.CONFIG.noLocationMessage)), false);
            return 0;
        }

        // Handle teleportation
        Identifier dimId = deathLocation.dimension();
        if (BackOnDeath.CONFIG.disabledDimensions.contains(dimId.toString())) {
            source.sendError(Text.literal(BackOnDeath.replaceColorCodes(BackOnDeath.CONFIG.disabledDimensionMessage)));
            return 0;
        }

        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimId);
        ServerWorld targetWorld = source.getServer().getWorld(worldKey);

        if (targetWorld == null) {
            source.sendError(Text.literal("The dimension you died in no longer exists."));
            return 0;
        }

        player.teleport(
                targetWorld,
                deathLocation.x(),
                deathLocation.y(),
                deathLocation.z(),
                Set.<PositionFlag>of(),
                deathLocation.yaw(),
                deathLocation.pitch(),
                true
        );
        
        // Clear data after successful teleport if one-time use is enabled
        if (BackOnDeath.CONFIG.oneTimeUse) {
            dataManager.clearDeathLocation(player.getUuid());
        }

        source.sendFeedback(() -> Text.literal(BackOnDeath.replaceColorCodes(BackOnDeath.CONFIG.teleportMessage)), false);

        return 1;
    }
}
