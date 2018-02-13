package com.nic.puke.capability;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Nictogen on 2/13/18
 */
public interface IPukeCapability
{
	NBTTagCompound writeNBT();
	void readNBT(NBTTagCompound nbt);
	void syncToPlayer();
}
