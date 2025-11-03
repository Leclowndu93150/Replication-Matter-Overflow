package com.leclowndu93150.replicationaddon.registry;

import com.leclowndu93150.replicationaddon.ReplicationMatterOverflow;
import com.leclowndu93150.replicationaddon.block.MatterVoidBlock;
import com.leclowndu93150.replicationaddon.block.tile.MatterVoidBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ReplicationMatterOverflow.MODID);

    public static final DeferredBlock<MatterVoidBlock> MATTER_VOID = ReplicationMatterOverflow.BLOCKS.register("matter_void", MatterVoidBlock::new);
    
    public static final DeferredItem<BlockItem> MATTER_VOID_ITEM = ReplicationMatterOverflow.ITEMS.registerSimpleBlockItem(MATTER_VOID);
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MatterVoidBlockEntity>> MATTER_VOID_BE = BLOCK_ENTITIES.register("matter_void", 
        () -> {
            var type = BlockEntityType.Builder.of(
                (pos, state) -> new MatterVoidBlockEntity(MATTER_VOID.get(), null, pos, state),
                MATTER_VOID.get()
            ).build(null);
            return type;
        });
}
