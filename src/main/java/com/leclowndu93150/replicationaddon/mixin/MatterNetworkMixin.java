package com.leclowndu93150.replicationaddon.mixin;

import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.network.IMatterTanksConsumer;
import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.network.MatterNetwork;
import com.hrznstudio.titanium.block_network.element.NetworkElement;
import com.leclowndu93150.replicationaddon.component.TankPriorityHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MatterNetwork.class)
public abstract class MatterNetworkMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("ReplicationAddon/MatterNetwork");

    @Shadow
    private List<NetworkElement> matterStacksHolders;
    
    @Shadow
    private List<NetworkElement> matterStacksSuppliers;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getGameTime()J"), cancellable = true)
    private void replicationaddon$sortAndTransfer(Level level, CallbackInfo ci) {
        this.matterStacksHolders.sort((a, b) -> {
            int aPrio = getPriority(a, level);
            int bPrio = getPriority(b, level);
            if (aPrio != bPrio) return Integer.compare(bPrio, aPrio);
            return Long.compare(a.getPos().asLong(), b.getPos().asLong());
        });
        
        if (level.getGameTime() % 5 == 0) {
            for (NetworkElement matterStacksSupplier : this.matterStacksSuppliers) {
                if (matterStacksSupplier.getLevel() != level) continue;
                if (!matterStacksSupplier.getLevel().isLoaded(matterStacksSupplier.getPos())) continue;
                var origin = matterStacksSupplier.getLevel().getBlockEntity(matterStacksSupplier.getPos());
                if (origin instanceof IMatterTanksSupplier supplier) {
                    for (IMatterTank inputTank : supplier.getTanks()) {
                        if (inputTank.getMatter().isEmpty()) continue;
                        
                        for (NetworkElement destinationElement : this.matterStacksHolders) {
                            if (!destinationElement.getLevel().isLoaded(destinationElement.getPos())) continue;
                            var destination = destinationElement.getLevel().getBlockEntity(destinationElement.getPos());
                            if (destination instanceof IMatterTanksConsumer consumerDestination) {
                                for (IMatterTank outputTank : consumerDestination.getTanks()) {
                                    boolean canAccept = (outputTank.getMatter().isEmpty() && outputTank.getCapacity() > 0) ||
                                                       (outputTank.getMatter().isMatterEqual(inputTank.getMatter()) && outputTank.getMatterAmount() < outputTank.getCapacity());
                                    
                                    if (canAccept) {
                                        inputTank.drain(outputTank.fill(inputTank.drain(outputTank.getCapacity(), IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                        if (inputTank.getMatter().isEmpty()) break;
                                    }
                                }
                                if (inputTank.getMatter().isEmpty()) break;
                            }
                        }
                    }
                }
            }
            ci.cancel();
        }
    }

    private static int getPriority(NetworkElement element, Level level) {
        if (element == null || !level.isLoaded(element.getPos())) return 0;
        var be = level.getBlockEntity(element.getPos());
        if (be instanceof TankPriorityHolder holder) {
            return holder.getTankPriority();
        }
        return 0;
    }
}
