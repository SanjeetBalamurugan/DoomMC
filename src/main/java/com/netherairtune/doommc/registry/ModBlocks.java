package com.netherairtune.doommc.registry;

import com.netherairtune.doommc.DoomMC;
import com.netherairtune.doommc.block.DoomBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static Block DOOM_BLOCK;

    public static void init() {
        DOOM_BLOCK = register(
            "doom_block",
            new DoomBlock(AbstractBlock.Settings.create().strength(2.0f))
        );
    }

    private static Block register(String name, Block block) {
        Identifier id = new Identifier(DoomMC.MOD_ID, name);

        Registry.register(Registries.BLOCK, id, block);

        Registry.register(
            Registries.ITEM,
            id,
            new BlockItem(block, new Item.Settings())
        );

        return block;
    }
}
