package com.itsdu.backondeath;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathDataManager extends PersistentState {
    private static final Codec<DeathDataManager> CODEC = NbtCompound.CODEC.xmap(DeathDataManager::fromNbt, DeathDataManager::toNbt);
    private static final PersistentStateType<DeathDataManager> TYPE = new PersistentStateType<>(
            "backondeath_data",
            DeathDataManager::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, DeathLocation> deathLocations = new HashMap<>();

    public void setDeathLocation(UUID playerUuid, DeathLocation location) {
        deathLocations.put(playerUuid, location);
        this.markDirty();
    }

    public DeathLocation getDeathLocation(UUID playerUuid) {
        return deathLocations.get(playerUuid);
    }

    public void clearDeathLocation(UUID playerUuid) {
        if (deathLocations.remove(playerUuid) != null) {
            this.markDirty();
        }
    }

    private NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtCompound playersNbt = new NbtCompound();
        deathLocations.forEach((uuid, location) -> {
            playersNbt.put(uuid.toString(), location.toNbt());
        });
        nbt.put("deathLocations", playersNbt);
        return nbt;
    }

    public static DeathDataManager fromNbt(NbtCompound nbt) {
        DeathDataManager state = new DeathDataManager();
        NbtCompound playersNbt = nbt.getCompoundOrEmpty("deathLocations");
        for (String key : playersNbt.getKeys()) {
            UUID uuid = UUID.fromString(key);
            NbtCompound locationNbt = playersNbt.getCompoundOrEmpty(key);
            DeathLocation location = DeathLocation.fromNbt(locationNbt);
            if (location != null) {
                state.deathLocations.put(uuid, location);
            }
        }
        return state;
    }

    public static DeathDataManager getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE);
    }
}
