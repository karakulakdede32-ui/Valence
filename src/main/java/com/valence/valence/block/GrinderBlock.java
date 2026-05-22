package com.valence.valence.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;

public class GrinderBlock extends Block {
    public GrinderBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK));
    }
}
