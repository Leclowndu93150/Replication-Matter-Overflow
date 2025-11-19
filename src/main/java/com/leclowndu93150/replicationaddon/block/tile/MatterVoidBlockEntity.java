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
import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.client.screen.addon.WidgetScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.util.AssetUtil;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.leclowndu93150.replicationaddon.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BooleanSupplier;

public class MatterVoidBlockEntity extends NetworkBlockEntity<MatterVoidBlockEntity> implements IMatterTanksSupplier, IMatterTanksConsumer {

    @Save
    private LockableMatterTankBundle<MatterVoidBlockEntity> lockableMatterTankBundle;
    @Save
    private int tankPriority;

    private IMatterType cachedType = MatterType.EMPTY;

    public MatterVoidBlockEntity(BasicTileBlock<MatterVoidBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType != null ? blockEntityType : ModRegistry.MATTER_VOID_BE.get(), pos, state);
        VoidingMatterTankComponent<MatterVoidBlockEntity> tank = new VoidingMatterTankComponent<>("tank", ReplicationConfig.MatterTank.CAPACITY, 32, 28, () -> true);
        tank.setTankAction(FluidTankComponent.Action.BOTH).setOnContentChange(this::onTankContentChange);
        this.lockableMatterTankBundle = new LockableMatterTankBundle<>(this, tank, 32 - 16, 30, false);
        this.addBundle(lockableMatterTankBundle);
        this.addMatterTank(this.lockableMatterTankBundle.getTank());
        this.tankPriority = 0;
    }

    private void onTankContentChange() {
        syncObject(this.lockableMatterTankBundle);
        this.getNetwork().onTankValueChanged(cachedType);
        if (!cachedType.equals(this.lockableMatterTankBundle.getTank().getMatter().getMatterType())) {
            this.cachedType = this.lockableMatterTankBundle.getTank().getMatter().getMatterType();
            this.getNetwork().onTankValueChanged(cachedType);
        }
    }

    @Override
    public void handleButtonMessage(int id, Player playerEntity, CompoundTag compound) {
        super.handleButtonMessage(id, playerEntity, compound);
        if (id == 124578) {
            this.tankPriority = compound.getInt("Priority");
            syncObject(this.tankPriority);
        }
        markComponentDirty();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        this.addGuiAddonFactory(() -> new VoidTankPriorityAddon(this, 32 + 20 + 1, 34 + 18));
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

    @Override
    public int getPriority() {
        return this.tankPriority;
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

        public VoidingMatterTankComponent(String name, int amount, int posX, int posY, BooleanSupplier voidExcessSupplier) {
            super(name, amount, posX, posY, voidExcessSupplier, () -> false);
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

    public static class VoidTankPriorityAddon extends WidgetScreenAddon {

        private final MatterVoidBlockEntity blockEntity;
        private final EditBox editBox;
        private String lastValue;

        public VoidTankPriorityAddon(MatterVoidBlockEntity blockEntity, int posX, int posY) {
            super(posX, posY, new EditBox(Minecraft.getInstance().font, 85, 20, 160, 26, Component.translatable("tooltip.replication.tank.insert_priority")));
            this.blockEntity = blockEntity;
            this.lastValue = "";
            this.editBox = (EditBox) getWidget();
            this.editBox.setValue(blockEntity.getPriority() + "");
            this.editBox.setFilter(s -> {
                if (s.isEmpty()) return true;
                if (s.charAt(0) == '-') return true;
                try {
                    Integer.parseInt(s);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            this.editBox.setMaxLength(6);
            this.editBox.setBordered(false);
            this.editBox.setVisible(true);
            this.editBox.setTextColor(0x72e567);
        }

        @Override
        public int getXSize() {
            return 0;
        }

        @Override
        public int getYSize() {
            return 0;
        }

        @Override
        public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
            this.editBox.setResponder(s -> {
                if (s.isEmpty()) return;
                if (!this.lastValue.equals(s) && screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu() instanceof ILocatable locatable) {
                    var compound = new CompoundTag();
                    if (s.charAt(0) == '-' && s.length() == 1) {
                        compound.putInt("Priority", -0);
                    } else {
                        compound.putInt("Priority", Integer.parseInt(s));
                    }
                    Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), 124578, compound));
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            this.editBox.setValue(blockEntity.getPriority() + "");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
                this.lastValue = s;
            });
            super.drawBackgroundLayer(guiGraphics, screen, iAssetProvider, guiX, guiY, mouseX, mouseY, partialTicks);
            var textWidth = Minecraft.getInstance().font.width(Component.translatable("tooltip.replication.tank.priority").getString());
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("tooltip.replication.tank.priority"), guiX + this.getPosX(), guiY + this.getPosY(), 0x72e567, false);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(textWidth, 0, 0);
            this.editBox.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.pose().popPose();
            for (int i = 0; i < 18; i++) {
                AssetUtil.drawHorizontalLine(guiGraphics, guiX + this.getPosX() + textWidth + i * 2, guiX + this.getPosX() + textWidth + i * 2, guiY + this.getPosY() + 8, 0xff72e567);
            }
        }

        @Override
        public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        }
    }
}
