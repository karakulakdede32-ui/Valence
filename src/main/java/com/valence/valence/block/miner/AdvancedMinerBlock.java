package com.valence.valence.block.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AdvancedMinerBlock extends BaseEntityBlock {
    public AdvancedMinerBlock(Properties p) {
        super(p);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedMinerTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState st, ServerLevel lvl, BlockPos pos,
                                   Player pl, InteractionHand hnd, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity te = lvl.getBlockEntity(pos);
        if (!(te instanceof AdvancedMinerTileEntity miner)) return InteractionResult.FAIL;
        
        ItemStack held = pl.getItemInHand(hnd);
        
        if (pl.isShiftDown()) {
            pl.openMenu((MenuProvider) miner);
            return InteractionResult.SUCCESS;
        }
        
        if (held.getItem() == Items.COAL || held.getItem() == Items.CHARCOAL) {
            if (!pl.getAbilities().instabuild) {
                held.shrink(1);
            }
            miner.processChunk(lvl, held);

            String results = miner.getOutputItems().stream()
                .map(s -> s.getItem().getDescriptionId())
                .reduce((a, b) -> a + ", " + b)
                .orElse("None");
            pl.displayClientMessage(Component.literal("Extracted: " + results), true);

            for (var item : miner.getOutputItems()) {
                miner.dropItem(pl, item);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        pl.displayClientMessage(Component.literal("Need COAL or CHARCOAL!"), true);
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState st, net.minecraft.world.level.Level lvl,
                        BlockPos pos, BlockState newSt, boolean moving) {
        if (st.hasBlockEntity() && !st.is(newSt.getBlock())) {
            lvl.removeBlockEntity(pos);
        }
    }
}
