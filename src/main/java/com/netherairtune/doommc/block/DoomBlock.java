package com.netherairtune.doommc.block;

import com.netherairtune.doommc.client.DoomScreen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;

public class DoomBlock extends Block {

    public DoomBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(
            BlockState state,
            World world,
            net.minecraft.util.math.BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (world.isClient) {
            MinecraftClient.getInstance().setScreen(new DoomScreen());
        }
        return ActionResult.SUCCESS;
    }
}
