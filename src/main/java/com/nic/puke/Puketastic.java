package com.nic.puke;

import com.nic.puke.capability.IPukeCapability;
import com.nic.puke.capability.PukeCapability;
import com.nic.puke.network.MessageSyncPukeCap;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Puketastic.MODID, name = Puketastic.NAME, version = Puketastic.VERSION)
public class Puketastic
{
    public static final String MODID = "puketastic";
    public static final String NAME = "Puketastic";
    public static final String VERSION = "0.1";
    public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(Puketastic.MODID);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        NETWORK_WRAPPER.registerMessage(MessageSyncPukeCap.Handler.class, MessageSyncPukeCap.class, 0, Side.CLIENT);
        CapabilityManager.INSTANCE.register(IPukeCapability.class, new PukeCapability.Storage(), PukeCapability::new);

    }
}
