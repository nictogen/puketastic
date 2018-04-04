package com.nic.puke.spore;

import com.nic.puke.capability.IPukeCapability;
import com.nic.puke.capability.PukeCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Nictogen on 2/17/18
 */
@Mod.EventBusSubscriber
public class Spore
{

	private static ArrayList<Spore> spores = new ArrayList<>();

	private double x, y, z, mX, mY, mZ;

	private double pX, pY, pZ;

	private Color color;

	private Item food;

	private int ticks;

	private boolean shrinks;

	private Spore(double x, double y, double z, double mX, double mY, double mZ, Color color, @Nullable Item food, boolean shrinks)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.pX = x;
		this.pY = y;
		this.pZ = z;
		this.mX = mX;
		this.mY = mY;
		this.mZ = mZ;
		this.color = color;
		this.food = food;
		this.shrinks = shrinks;
	}

	private int getBrightnessForRender(World world)
	{
		BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
		return world.isBlockLoaded(blockpos) ? world.getCombinedLight(blockpos, 0) : 0;
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event)
	{

		EntityPlayer player = Minecraft.getMinecraft().player;
		GlStateManager.disableCull();
		GlStateManager.pushMatrix();
		Vec3d playerPos = new Vec3d(player.prevPosX + (player.posX - player.prevPosX)*event.getPartialTicks(), player.prevPosY + (player.posY - player.prevPosY)*event.getPartialTicks(), player.prevPosZ + (player.posZ - player.prevPosZ)*event.getPartialTicks());

		GlStateManager.translate(-playerPos.x, -playerPos.y, -playerPos.z);
//		GlStateManager.translate(player.posX, player.posY, -player.posZ);
		ArrayList<Spore> newSpores = new ArrayList<>();
		newSpores.addAll(spores);
		for (Spore spore : newSpores)
		{
			if (spore != null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(spore.pX, spore.pY, spore.pZ);
				Vec3d diff = new Vec3d(spore.pX, spore.pY, spore.pZ).subtract(new Vec3d(spore.x, spore.y, spore.z)).scale(event.getPartialTicks());
				GlStateManager.translate(diff.x, diff.y, diff.z);
				if(spore.shrinks)
				{
					double percentage = 1 - (((double) spore.ticks) / 400.0);

					GlStateManager.scale(0.75, 0.75, 0.75);
					GlStateManager.scale(percentage, percentage, percentage);
				} else {
					double percentage = 1 + (((double) spore.ticks) / 800.0);

					GlStateManager.scale(0.75, 0.75, 0.75);
					GlStateManager.scale(percentage, percentage, percentage);
				}
				drawCube(spore);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.enableCull();
	}

	@SubscribeEvent
	public static void tick(TickEvent.WorldTickEvent event)
	{
		ArrayList<Spore> newSpores = new ArrayList<>(spores);
		newSpores.removeAll(newSpores.stream().filter(spore -> spore != null && (spore.ticks > 400 && spore.shrinks)).collect(Collectors.toList()));
		for (Spore spore : newSpores)
		{
			if (spore != null)
			{
				BlockPos pos = new BlockPos(spore.x, spore.y - 0.2, spore.z);
				if (event.world.getBlockState(pos).isFullCube())
				{
					spore.mY = 0;
					spore.mX *= 0.1;
					spore.mZ *= 0.1;
				}
				else
				{
					if (spore.mY == 0)
						spore.mY = -0.001;
					else
						spore.mY -= 0.002;
				}
				spore.pX = spore.x;
				spore.pY = spore.y;
				spore.pZ = spore.z;
				spore.x += spore.mX;
				spore.y += spore.mY;
				spore.z += spore.mZ;
				spore.ticks++;
			}
		}
		spores.clear();
		spores.addAll(newSpores);
	}

	@SubscribeEvent
	public static void tick(LivingEvent.LivingUpdateEvent event)
	{
		if (event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			if(!(player.isPotionActive(Potion.getPotionById(9)) || player.isPotionActive(Potion.getPotionById(19)))) return;
			Random rand = player.getRNG();
			float f = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F);
			float f1 = -MathHelper.sin(player.rotationPitch * 0.017453292F);
			float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F);

			Item food = null;
			if (event.getEntity().hasCapability(PukeCapability.PUKE_CAP, null))
			{
				IPukeCapability capability = event.getEntity().getCapability(PukeCapability.PUKE_CAP, null);
				ArrayList<Item> recentFood = capability.getRecentFood();
				if (rand.nextGaussian() > 0 && !recentFood.isEmpty())
				{
					food = recentFood.get(rand.nextInt(recentFood.size()));
				}
			}

			for (int i = 0; i < 5; i++)
			{
				spores.add(new Spore(player.posX + f / 5f + rand.nextGaussian() / 20f, player.posY + 1.55f, player.posZ + f2 / 5f + rand.nextGaussian() / 20f,
						f*.01f + rand.nextGaussian()*.005f,
						f1*.005f + rand.nextGaussian()*.005f,
						f2*.01f + rand.nextGaussian()*.005f,
						new Color(128, 108 + rand.nextInt(100), 73), food, rand.nextInt(10) != 0));
			}
		}
	}

	private static void drawCube(Spore spore)
	{
		if (spore.food == null)
			GlStateManager.disableTexture2D();

		drawFace(spore,
				new Vec3d[] { new Vec3d(-0.05, -0.05, -0.05), new Vec3d(0.05, -0.05, -0.05), new Vec3d(0.05, -0.05, 0.05), new Vec3d(-0.05, -0.05, 0.05) });
		drawFace(spore, new Vec3d[] { new Vec3d(-0.05, -0.05, 0.05), new Vec3d(0.05, -0.05, 0.05), new Vec3d(0.05, 0.05, 0.05), new Vec3d(-0.05, 0.05, 0.05) });
		drawFace(spore, new Vec3d[] { new Vec3d(0.05, -0.05, 0.05), new Vec3d(0.05, -0.05, -0.05), new Vec3d(0.05, 0.05, -0.05), new Vec3d(0.05, 0.05, 0.05) });
		drawFace(spore,
				new Vec3d[] { new Vec3d(-0.05, -0.05, -0.05), new Vec3d(0.05, -0.05, -0.05), new Vec3d(0.05, 0.05, -0.05), new Vec3d(-0.05, 0.05, -0.05) });
		drawFace(spore,
				new Vec3d[] { new Vec3d(-0.05, -0.05, -0.05), new Vec3d(-0.05, -0.05, 0.05), new Vec3d(-0.05, 0.05, 0.05), new Vec3d(-0.05, 0.05, -0.05) });
		drawFace(spore, new Vec3d[] { new Vec3d(-0.05, 0.05, -0.05), new Vec3d(0.05, 0.05, -0.05), new Vec3d(0.05, 0.05, 0.05), new Vec3d(-0.05, 0.05, 0.05) });
		GlStateManager.enableTexture2D();
	}

	private static void drawFace(Spore spore, Vec3d[] points)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		int i = spore.getBrightnessForRender(Minecraft.getMinecraft().world);
		int j = i >> 16 & 65535;
		int k = i & 65535;

		if (spore.food != null)
		{

			Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(spore.food);

			double minU = sprite.getInterpolatedU(5), maxU = sprite.getInterpolatedU(9);
			double minV = sprite.getInterpolatedV(5), maxV = sprite.getInterpolatedV(9);

			bufferBuilder.begin(6, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
			bufferBuilder.pos(points[0].x, points[0].y, points[0].z).tex(minU, minV).lightmap(j, k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.pos(points[1].x, points[1].y, points[1].z).tex(minU, maxV).lightmap(j, k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.pos(points[2].x, points[2].y, points[2].z).tex(maxU, maxV).lightmap(j, k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.pos(points[3].x, points[3].y, points[3].z).tex(maxU, minV).lightmap(j, k).color(255, 255, 255, 255).endVertex();

		}
		else
		{

			Color color = spore.color;
			bufferBuilder.begin(6, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
			bufferBuilder.pos(points[0].x, points[0].y, points[0].z).tex(0, 0).lightmap(j, k).color(color.getRed(), color.getGreen(), color.getBlue(), 255)
					.endVertex();
			bufferBuilder.pos(points[1].x, points[1].y, points[1].z).tex(0, 1).lightmap(j, k).color(color.getRed(), color.getGreen(), color.getBlue(), 255)
					.endVertex();
			bufferBuilder.pos(points[2].x, points[2].y, points[2].z).tex(1, 1).lightmap(j, k).color(color.getRed(), color.getGreen(), color.getBlue(), 255)
					.endVertex();
			bufferBuilder.pos(points[3].x, points[3].y, points[3].z).tex(1, 0).lightmap(j, k).color(color.getRed(), color.getGreen(), color.getBlue(), 255)
					.endVertex();

		}
		tessellator.draw();
	}
}
