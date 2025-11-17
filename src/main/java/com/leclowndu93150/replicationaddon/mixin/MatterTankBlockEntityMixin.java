package com.leclowndu93150.replicationaddon.mixin;

import com.buuz135.replication.block.tile.MatterTankBlockEntity;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.leclowndu93150.replicationaddon.component.TankPriorityButtonHelper;
import com.leclowndu93150.replicationaddon.component.TankPriorityHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MatterTankBlockEntity.class)
public abstract class MatterTankBlockEntityMixin implements TankPriorityHolder {

    @Unique
    @Save
    private int replicationaddon$tankPriority;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void replicationaddon$initPriorityButtons(BasicTileBlock<MatterTankBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, CallbackInfo ci) {
        TankPriorityButtonHelper.addPriorityButtons((MatterTankBlockEntity) (Object) this, this, 98, 28);
    }

    @Override
    public int getTankPriority() {
        return this.replicationaddon$tankPriority;
    }

    @Override
    public void setTankPriority(int priority) {
        int clamped = TankPriorityHolder.super.clampPriority(priority);
        if (clamped == this.replicationaddon$tankPriority) {
            return;
        }
        this.replicationaddon$tankPriority = clamped;
        MatterTankBlockEntity self = (MatterTankBlockEntity) (Object) this;
        self.syncObject(this.replicationaddon$tankPriority);
        self.markForUpdate();
    }
}
