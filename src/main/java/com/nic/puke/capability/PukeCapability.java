package com.nic.puke.capability;

import com.nic.puke.Puketastic;
import com.nic.puke.network.MessageSyncPukeCap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by Nictogen on 2/13/18
 */
public class PukeCapability implements IPukeCapability
{
	private ArrayList<Item> latestFoods = new ArrayList<>();

	@CapabilityInject(IPukeCapability.class)
	public static final Capability<IPukeCapability> PUKE_CAP = null;

	public EntityPlayer player;

	public PukeCapability()
	{

	}

	public PukeCapability(EntityPlayer player)
	{
		this.player = player;
	}

	@Override public NBTTagCompound writeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		for (int i = 0; i < latestFoods.size(); i++)
			compound.setInteger("food" + i, Item.getIdFromItem(latestFoods.get(i)));

		return compound;
	}

	@Override public void readNBT(NBTTagCompound nbt)
	{
		latestFoods.clear();
		for (int i = 0; i < 5; i++)
			if (nbt.hasKey("food" + i))
				latestFoods.add(Item.getItemById(nbt.getInteger("food" + i)));
	}

	@Override public void syncToPlayer()
	{
		Puketastic.NETWORK_WRAPPER.sendToAll(new MessageSyncPukeCap(this.player));
	}

	@Override public void addNewFood(Item item)
	{
			if(latestFoods.size() >= 5)
				latestFoods.remove(0);
			latestFoods.add(item);
	}

	@Override public ArrayList<Item> getRecentFood()
	{
		return this.latestFoods;
	}

	public static class Provider implements ICapabilitySerializable<NBTTagCompound>
	{
		private IPukeCapability capability;

		public Provider(IPukeCapability capability)
		{
			this.capability = capability;
		}

		@Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return PUKE_CAP != null && capability == PUKE_CAP;
		}

		@Nullable @Override public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == PUKE_CAP ? PUKE_CAP.cast(this.capability) : null;
		}

		@Override public NBTTagCompound serializeNBT()
		{
			return (NBTTagCompound) PUKE_CAP.getStorage().writeNBT(PUKE_CAP, this.capability, null);
		}

		@Override public void deserializeNBT(NBTTagCompound nbt)
		{
			PUKE_CAP.getStorage().readNBT(PUKE_CAP, this.capability, null, nbt);
		}
	}

	@Mod.EventBusSubscriber
	public static class EventHandler
	{
		@SubscribeEvent
		public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event)
		{
			if (!(event.getObject() instanceof EntityPlayer) || event.getObject().hasCapability(PUKE_CAP, null))
				return;
			event.addCapability(new ResourceLocation(Puketastic.MODID, "timelord"),
					new Provider(new PukeCapability((EntityPlayer) event.getObject())));
		}

		@SubscribeEvent
		public static void onPlayerStartTracking(PlayerEvent.StartTracking event)
		{
			if (event.getTarget().hasCapability(PUKE_CAP, null))
			{
				event.getTarget().getCapability(PUKE_CAP, null).syncToPlayer();
			}
		}

		@SubscribeEvent
		public static void onPlayerClone(PlayerEvent.Clone event)
		{
			NBTTagCompound nbt = (NBTTagCompound) PUKE_CAP.getStorage().writeNBT(PUKE_CAP, event.getOriginal().getCapability(PUKE_CAP, null), null);
			PUKE_CAP.getStorage().readNBT(PUKE_CAP, event.getEntityPlayer().getCapability(PUKE_CAP, null), null, nbt);
		}

		@SubscribeEvent
		public static void onPlayerUseItem(LivingEntityUseItemEvent.Finish entityUseItemEvent){
			if(entityUseItemEvent.getEntity().hasCapability(PUKE_CAP, null))
			{
				if (entityUseItemEvent.getItem().getItem() instanceof ItemFood)
				{
					entityUseItemEvent.getEntity().getCapability(PUKE_CAP, null).addNewFood(entityUseItemEvent.getItem().getItem());
					entityUseItemEvent.getEntity().getCapability(PUKE_CAP, null).syncToPlayer();
				}
			}
		}
	}

	public static class Storage implements Capability.IStorage<IPukeCapability>
	{

		@Nullable @Override public NBTBase writeNBT(Capability<IPukeCapability> capability, IPukeCapability instance, EnumFacing side)
		{
			return instance.writeNBT();
		}

		@Override public void readNBT(Capability<IPukeCapability> capability, IPukeCapability instance, EnumFacing side, NBTBase nbt)
		{
			instance.readNBT((NBTTagCompound) nbt);
		}
	}

}