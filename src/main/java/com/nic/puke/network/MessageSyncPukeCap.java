package com.nic.puke.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

import static com.nic.puke.capability.PukeCapability.PUKE_CAP;

/**
 * Created by Nictogen on 2/13/18
 */
public class MessageSyncPukeCap implements IMessage
{
	public UUID playerUUID;
	public NBTTagCompound nbt;

	public MessageSyncPukeCap()
	{
	}

	public MessageSyncPukeCap(EntityPlayer player)
	{
		this.playerUUID = player.getPersistentID();
		nbt = (NBTTagCompound) PUKE_CAP.getStorage().writeNBT(PUKE_CAP, player.getCapability(PUKE_CAP, null), null);
	}

	@Override public void fromBytes(ByteBuf buf)
	{
		this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
		ByteBufUtils.writeTag(buf, this.nbt);
	}

	public static class Handler implements IMessageHandler<MessageSyncPukeCap, IMessage>
	{

		@Override public IMessage onMessage(MessageSyncPukeCap message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> PUKE_CAP.getStorage()
					.readNBT(PUKE_CAP, Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.playerUUID).getCapability(PUKE_CAP, null), null,
							message.nbt));
			return null;
		}
	}
}