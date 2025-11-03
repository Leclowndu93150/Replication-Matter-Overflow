package com.leclowndu93150.replicationaddon.client;

import com.buuz135.replication.ReplicationAttachments;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.leclowndu93150.replicationaddon.registry.ModRegistry;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.DecimalFormat;

public class ClientEvents {

    public static void init() {
        EventManager.forge(ItemTooltipEvent.class).process(pre -> {
            if (ItemStack.isSameItem(pre.getItemStack(), new ItemStack(ModRegistry.MATTER_VOID.get())) && pre.getItemStack().has(ReplicationAttachments.TILE)) {
                var tag = pre.getItemStack().get(ReplicationAttachments.TILE);
                var matterStack = MatterStack.loadMatterStackFromNBT(tag.contains("tank") ? tag.getCompound("tank") : tag.getCompound("lockableMatterTankBundle").getCompound("Tank"));
                pre.getToolTip().add(1, Component.translatable("tooltip.titanium.tank.amount").withStyle(ChatFormatting.GOLD).append(Component.literal(ChatFormatting.WHITE + new DecimalFormat().format(matterStack.getAmount()) + ChatFormatting.GOLD + "/" + ChatFormatting.WHITE + new DecimalFormat().format(256000))).append(Component.translatable("tooltip.replication.tank.unit").withStyle(ChatFormatting.DARK_AQUA)));
                pre.getToolTip().add(1, Component.literal(ChatFormatting.GOLD + Component.translatable("tooltip.replication.tank.matter").getString()).append(matterStack.isEmpty() ? Component.translatable("tooltip.titanium.tank.empty").withStyle(ChatFormatting.WHITE) : Component.translatable(matterStack.getTranslationKey())).withStyle(ChatFormatting.WHITE));
            }
        }).subscribe();
    }
}
