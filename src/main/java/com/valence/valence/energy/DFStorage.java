package com.valence.valence.energy;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.EnergyStorage;

public class DFStorage extends EnergyStorage {
    public DFStorage(int capacity) { super(capacity); }
    public DFStorage(int capacity, int maxTransfer) { super(capacity, maxTransfer, maxTransfer); }
    public DFStorage(int capacity, int maxReceive, int maxExtract) { super(capacity, maxReceive, maxExtract); }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int filled = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && filled > 0) onEnergyChanged();
        return filled;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) onEnergyChanged();
        return extracted;
    }

    protected void onEnergyChanged() {}

    public int getDF() { return energy; }
    public int getMaxDF() { return capacity; }
    public int generateDF(int amount, boolean simulate) { return receiveEnergy(amount, simulate); }
    public int consumeDF(int amount, boolean simulate) { return extractEnergy(amount, simulate); }

    public void serializeNBT(Tag tag) { if (tag instanceof IntTag intTag) energy = intTag.getAsInt(); }
    public Tag serializeNBT() { return IntTag.valueOf(energy); }
}
