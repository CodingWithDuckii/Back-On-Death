package com.itsdu.backondeath;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public record DeathLocation(Identifier dimension, double x, double y, double z, float yaw, float pitch) {
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("dimension", dimension.toString());
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        nbt.putDouble("z", z);
        nbt.putFloat("yaw", yaw);
        nbt.putFloat("pitch", pitch);
        return nbt;
    }

    public static DeathLocation fromNbt(NbtCompound nbt) {
        if (!nbt.contains("dimension")) return null;
        String dimensionStr = nbt.getString("dimension", "");
        Identifier dimension = Identifier.tryParse(dimensionStr);
        if (dimension == null) {
            return null;
        }
        return new DeathLocation(
            dimension,
            nbt.getDouble("x", 0.0D),
            nbt.getDouble("y", 0.0D),
            nbt.getDouble("z", 0.0D),
            nbt.getFloat("yaw", 0.0F),
            nbt.getFloat("pitch", 0.0F)
        );
    }
}
