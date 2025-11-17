package com.leclowndu93150.replicationaddon.util;

import com.hrznstudio.titanium.block_network.element.NetworkElement;
import com.leclowndu93150.replicationaddon.component.TankPriorityHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Comparator;
import java.util.List;

public final class TankPriorityUtil {

    private static final Comparator<NetworkElement> PRIORITY_COMPARATOR = (first, second) -> {
        int firstPriority = getPriority(first);
        int secondPriority = getPriority(second);
        if (firstPriority != secondPriority) {
            return Integer.compare(secondPriority, firstPriority);
        }
        long firstPos = first != null ? first.getPos().asLong() : 0;
        long secondPos = second != null ? second.getPos().asLong() : 0;
        return Long.compare(firstPos, secondPos);
    };

    private TankPriorityUtil() {
    }

    public static void sortNetworkElements(List<NetworkElement> elements) {
        if (elements == null || elements.size() <= 1) {
            return;
        }
        elements.sort(PRIORITY_COMPARATOR);
    }

    public static int getPriority(BlockEntity blockEntity) {
        if (blockEntity instanceof TankPriorityHolder holder) {
            return holder.getTankPriority();
        }
        return 0;
    }

    private static int getPriority(NetworkElement element) {
        if (element == null) {
            return 0;
        }
        var level = element.getLevel();
        if (level == null || !level.isLoaded(element.getPos())) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(element.getPos());
        return getPriority(blockEntity);
    }
}
