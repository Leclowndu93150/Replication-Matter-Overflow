package com.leclowndu93150.replicationaddon.block.tile;

import com.buuz135.replication.ReplicationConfig;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.matter_fluid.component.MatterTankComponent;
import com.buuz135.replication.api.network.IMatterTanksConsumer;
import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.block.tile.NetworkBlockEntity;
import com.buuz135.replication.client.gui.ReplicationAddonProvider;
import com.buuz135.replication.container.component.LockableMatterTankBundle;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.leclowndu93150.replicationaddon.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MatterVoidBlockEntity extends NetworkBlockEntity<MatterVoidBlockEntity> implements IMatterTanksSupplier, IMatterTanksConsumer {

    @Save
    private LockableMatterTankBundle<MatterVoidBlockEntity> lockableMatterTankBundle;
    private IMatterType cachedType = MatterType.EMPTY;

    public MatterVoidBlockEntity(BasicTileBlock<MatterVoidBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType != null ? blockEntityType : ModRegistry.MATTER_VOID_BE.get(), pos, state);
        VoidingMatterTankComponent<MatterVoidBlockEntity> tank = new VoidingMatterTankComponent<>("tank", ReplicationConfig.MatterTank.CAPACITY, 78, 28);
        tank.setTankAction(FluidTankComponent.Action.BOTH).setOnContentChange(this::onTankContentChange);
        this.lockableMatterTankBundle = new LockableMatterTankBundle<>(this, tank, 78 + 20, 28, false);
        this.addBundle(lockableMatterTankBundle);
        this.addMatterTank(this.lockableMatterTankBundle.getTank());
    }

    private void onTankContentChange(){
        syncObject(this.lockableMatterTankBundle);
        this.getNetwork().onTankValueChanged(cachedType);
        if (!cachedType.equals(this.lockableMatterTankBundle.getTank().getMatter().getMatterType())) {
            this.cachedType = this.lockableMatterTankBundle.getTank().getMatter().getMatterType();
            this.getNetwork().onTankValueChanged(cachedType);
        }
    }

    @Override
    public ItemInteractionResult onActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == ItemInteractionResult.SUCCESS) {
            return ItemInteractionResult.SUCCESS;
        }
        openGui(playerIn);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public List<? extends IMatterTank> getTanks() {
        return this.getMatterTankComponents();
    }

    @NotNull
    @Override
    public MatterVoidBlockEntity getSelf() {
        return this;
    }

    @Override
    public IAssetProvider getAssetProvider() {
        return ReplicationAddonProvider.INSTANCE;
    }

    @Override
    public int getTitleColor() {
        return 0xe56767;
    }

    @Override
    public float getTitleYPos(float titleWidth, float screenWidth, float screenHeight, float guiWidth, float guiHeight) {
        return super.getTitleYPos(titleWidth, screenWidth, screenHeight, guiWidth, guiHeight) - 16;
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        if (compound.contains("tank")) {
            this.lockableMatterTankBundle.getTank().deserializeNBT(provider, compound.getCompound("tank"));
        }
    }

    public static class VoidingMatterTankComponent<T extends IComponentHarness> extends MatterTankComponent<T> {
        
        private static final double MAX_DISPLAY_AMOUNT = 255999;

        public VoidingMatterTankComponent(String name, int amount, int posX, int posY) {
            super(name, amount, posX, posY);
        }

        @Override
        public double getMatterAmount() {
            return Math.min(super.getMatterAmount(), MAX_DISPLAY_AMOUNT);
        }

        @Override
        public double fill(MatterStack resource, IFluidHandler.FluidAction action) {
            if (!getInsertPredicate().test(resource) || resource.isEmpty()) {
                return 0;
            }
            if (!isMatterValid(resource)) {
                return 0;
            }
            if (action.simulate()) {
                return resource.getAmount();
            }
            if (getMatter().isEmpty()) {
                double toStore = Math.min(MAX_DISPLAY_AMOUNT, resource.getAmount());
                setMatter(new MatterStack(resource, toStore));
                onContentsChanged();
                return resource.getAmount();
            }
            if (!getMatter().isMatterEqual(resource)) {
                return 0;
            }
            double actualAmount = super.getMatterAmount();
            double spaceAvailable = MAX_DISPLAY_AMOUNT - actualAmount;
            if (spaceAvailable > 0) {
                double toAdd = Math.min(spaceAvailable, resource.getAmount());
                getMatter().grow(toAdd);
                onContentsChanged();
            }
            return resource.getAmount();
        }
    }
}
