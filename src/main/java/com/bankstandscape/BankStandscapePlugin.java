package com.bankstandscape;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Bank Standscape",
	description = "Gain XP for standing idle at banks.",
	tags = {"bank", "stand", "afk", "idle", "xp"}
)
public class BankStandscapePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BankStandscapeConfig config;

	private long totalXp = 0;
	private int currentLevel = 1;
	private int idleTicksCounter = 0;
	private WorldPoint lastPosition;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Bank Standscape started!");
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
				"Bank Standscape Plugin enabled!", null);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Bank Standscape stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		WorldPoint currentPosition = localPlayer.getWorldLocation();
		boolean inBank = isPlayerInBankArea(currentPosition);
		boolean isIdle = isPlayerIdle(localPlayer, currentPosition);

		if (inBank && isIdle)
		{
			idleTicksCounter++;
			if (idleTicksCounter >= 100) // 60 seconds
			{
				grantXp();
				idleTicksCounter = 0;
			}
		}
		else
		{
			idleTicksCounter = 0;
		}

		lastPosition = currentPosition;
	}

	private boolean isPlayerInBankArea(WorldPoint location)
	{
		if (location == null) return false;
		
		// Grand Exchange area
		return location.getX() >= 3149 && location.getX() <= 3180 && 
			   location.getY() >= 3501 && location.getY() <= 3524 && 
			   location.getPlane() == 0;
	}

	private boolean isPlayerIdle(Player player, WorldPoint currentPosition)
	{
		if (lastPosition == null || !currentPosition.equals(lastPosition))
		{
			return false;
		}
		
		return player.getAnimation() == -1;
	}

	private void grantXp()
	{
		totalXp += 10;
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
			"You gain 10 Bank Standscape XP! (Total: " + totalXp + ")", null);
	}

	@Provides
	BankStandscapeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankStandscapeConfig.class);
	}
}