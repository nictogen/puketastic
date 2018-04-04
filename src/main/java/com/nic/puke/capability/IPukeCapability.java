package com.nic.puke.capability;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

/**
 * Created by Nictogen on 2/13/18
 */
public interface IPukeCapability
{
	NBTTagCompound writeNBT();
	void readNBT(NBTTagCompound nbt);
	void syncToPlayer();
	void addNewFood(Item item);
	ArrayList<Item> getRecentFood();
}
