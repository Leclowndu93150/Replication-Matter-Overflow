package com.leclowndu93150.replicationaddon.component;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.client.screen.addon.StateButtonAddon;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

public final class TankPriorityButtonHelper {

    private TankPriorityButtonHelper() {
    }

    public static void addPriorityButtons(ActiveTile<?> tile, TankPriorityHolder holder, int lockX, int lockY) {
        int increaseX = lockX;
        int increaseY = lockY + 18;
        int decreaseX = lockX;
        int decreaseY = increaseY + 16;
        
        tile.addButton(createButton(holder, increaseX, increaseY, 1));
        tile.addButton(createButton(holder, decreaseX, decreaseY, -1));
    }

    private static ButtonComponent createButton(TankPriorityHolder holder, int x, int y, int delta) {
        ButtonComponent button = new ButtonComponent(x, y, 14, 14) {
            @Override
            @OnlyIn(Dist.CLIENT)
            public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
                String tooltipKey = delta > 0 ? "tooltip.replication_matter_overflow.priority.increase" : "tooltip.replication_matter_overflow.priority.decrease";
                return Collections.singletonList(() -> new StateButtonAddon(this,
                        new StateButtonInfo(0, delta > 0 ? AssetTypes.BUTTON_ARROW_UP : AssetTypes.BUTTON_ARROW_DOWN, tooltipKey)) {
                    @Override
                    public int getState() {
                        return 0;
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        return List.of(
                                Component.translatable(tooltipKey),
                                Component.translatable("tooltip.replication_matter_overflow.priority.current", holder.getTankPriority())
                        );
                    }
                });
            }
        };
        return button.setPredicate((player, compound) -> {
            if (delta > 0 && holder.getTankPriority() >= TankPriorityHolder.MAX_PRIORITY) {
                return;
            }
            if (delta < 0 && holder.getTankPriority() <= TankPriorityHolder.MIN_PRIORITY) {
                return;
            }
            holder.adjustTankPriority(delta);
        });
    }
}
