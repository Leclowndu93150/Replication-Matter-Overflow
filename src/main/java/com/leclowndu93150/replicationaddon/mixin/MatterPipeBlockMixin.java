package com.leclowndu93150.replicationaddon.mixin;

import com.buuz135.replication.block.MatterPipeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(MatterPipeBlock.class)
public class MatterPipeBlockMixin {
    
    @Shadow
    public static List<Predicate<Block>> ALLOWED_CONNECTION_BLOCKS;
    
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void addCustomModBlocks(CallbackInfo ci) {
        ALLOWED_CONNECTION_BLOCKS.add(block ->
            BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("replication_matter_overflow")
        );
    }
}
