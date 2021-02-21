package io.github.profjb58.territorial.item;

import io.github.profjb58.territorial.Territorial;
import io.github.profjb58.territorial.networking.S2CPackets;
import io.github.profjb58.territorial.util.ActionLogger;
import io.github.profjb58.territorial.util.LockUtils;
import io.github.profjb58.territorial.util.LockUtils.LockType;
import io.github.profjb58.territorial.util.SideUtils;
import io.github.profjb58.territorial.world.data.LocksPersistentState;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class PadlockItem extends Item {

    private final LockType type;

    public PadlockItem(LockType type) {
        super(new Item.Settings().group(Territorial.BASE_GROUP).maxCount(16));
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if(player != null) {
            if(player.isSneaking() && !ctx.getWorld().isClient()) {

                ItemStack lock = player.getStackInHand(player.getActiveHand());
                String lockName = lock.getName().getString();
                if(!lockName.equals("") && lock.hasCustomName()) {

                    BlockEntity be = ctx.getWorld().getBlockEntity(ctx.getBlockPos());
                    if(be != null) {
                        CompoundTag tag = be.toTag(new CompoundTag());
                        if(!tag.contains("lock_id")) { // No lock has been assigned to the block entity

                            tag.putString("lock_id", lockName);
                            tag.putUuid("lock_owner_uuid", player.getUuid());
                            tag.putInt("lock_type", LockUtils.getLockTypeInt(type));

                            if(!player.isCreative()) {
                                player.getStackInHand(player.getActiveHand()).decrement(1);
                            }
                            player.sendMessage(new TranslatableText("message.territorial.lock_successful"), true);
                            if(SideUtils.isDedicatedServer()) {
                                Territorial.actionLogger.write(ActionLogger.LogType.INFO,
                                        ActionLogger.LogModule.LOCKS,
                                        player.getName().getString() + " claimed block entity at: " + be.getPos());
                            }
                            LocksPersistentState lps = LocksPersistentState.get((ServerWorld) ctx.getWorld());
                            lps.addLock(player.getUuid(), ctx.getBlockPos());

                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeBlockPos(be.getPos());
                            ServerPlayNetworking.send((ServerPlayerEntity) player, S2CPackets.CLIENT_ATTACH_LOCK, buf);
                        }
                        else {
                            player.sendMessage(new TranslatableText("message.territorial.lock_failed"), true);
                        }

                        try {
                            be.fromTag(be.getCachedState(), tag);
                        } catch (Exception ignored) {}

                        return ActionResult.SUCCESS;
                    }
                }
                else {
                    player.sendMessage(new TranslatableText("message.territorial.lock_unnamed"), true);
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
    }


}