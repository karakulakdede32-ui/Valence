package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AdvancedMinerBlock extends BaseEntityBlock {
    public AdvancedMinerBlock(BlockBehaviour.Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedMinerTileEntity(BlockEntityType.BARREL, pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, net.minecraft.world.level.Level lvl, BlockPos pos, 
                                  Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof AdvancedMinerTileEntity miner)) return InteractionResult.FAIL;
        
        // Sneak + right-click: Open GUI
        if (pl.isShiftKeyDown()) {
            pl.openMenu((MenuProvider) miner);
            return InteractionResult.SUCCESS;
        }
        
        // Check for fuel in hand
        ItemStack held = pl.getItemInHand(hnd);
        boolean isCoal = held.is(Items.COAL) || held.is(Items.CHARCOAL);
        
        if (miner.hasFuel() && isCoal) {
            // Extract all ore types
            miner.extractAllOreTypes(lvl);
            held.shrink(1);
            pl.displayClientMessage(Component.literal("Extracted!"), true);
        } else if (!miner.hasFuel()) {
            // Add fuel
            if (isCoal) {
                miner.setFuel(1);
                held.shrink(1);
                pl.displayClientMessage(Component.literal("Fuel added!"), true);
            } else {
                pl.displayClientMessage(Component.literal("Need coal/charcoal for fuel"), true);
            }
        } else {
            pl.displayClientMessage(Component.literal("Need fuel to extract"), true);
        }
        
        return InteractionResult.SUCCESS;
    }
}