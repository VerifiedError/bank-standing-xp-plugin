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
import net.runelite.client.ui.overlay.OverlayManager;
import lombok.Getter;
import java.time.Instant;

@Slf4j
@PluginDescriptor(
	name = "Bank Standscape",
	description = "Gain XP for standing idle at banks. A fun, cosmetic skill to level up.",
	tags = {"bank", "stand", "afk", "idle", "xp", "grand exchange", "ge", "skill", "fun"}
)
public class BankStandscapePlugin extends Plugin
{
	private static final int TICKS_PER_MINUTE = 100;
	private static final int BASE_XP_PER_MINUTE = 10;
	private static final String CONFIG_GROUP = "bankstandscape";

	@Inject
	private Client client;

	@Inject
	private BankStandscapeConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BankStandscapeOverlay overlay;

	@Getter
	private long totalXp;

	@Getter
	private int currentLevel;

	private int idleTicksCounter = 0;
	private WorldPoint lastPosition;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Bank Standscape started!");
		overlayManager.add(overlay);
		loadXp();
		
		// Send enable message to chat
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
				"🏦 Bank Standscape Plugin enabled! Stand at banks to gain XP!", null);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Bank Standscape stopped!");
		overlayManager.remove(overlay);
		saveXp();
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN || !config.enablePlugin())
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
			if (idleTicksCounter >= TICKS_PER_MINUTE)
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

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
				"🏦 Bank Standscape Plugin enabled! Stand at banks to gain XP!", null);
		}
	}

	private boolean isPlayerInBankArea(WorldPoint location)
	{
		if (location == null) return false;
		
		// Grand Exchange area
		if (location.getX() >= 3149 && location.getX() <= 3180 && 
			location.getY() >= 3501 && location.getY() <= 3524 && 
			location.getPlane() == 0)
		{
			return true;
		}
		
		// Other major banks (if enabled)
		if (config.enableMultiBank())
		{
			// Varrock East Bank
			if (location.getX() >= 3250 && location.getX() <= 3255 && 
				location.getY() >= 3418 && location.getY() <= 3423 && 
				location.getPlane() == 0)
			{
				return true;
			}
			
			// Falador Bank
			if (location.getX() >= 2943 && location.getX() <= 2949 && 
				location.getY() >= 3367 && location.getY() <= 3373 && 
				location.getPlane() == 0)
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean isPlayerIdle(Player player, WorldPoint currentPosition)
	{
		if (lastPosition == null || !currentPosition.equals(lastPosition))
		{
			return false;
		}
		
		return player.getAnimation() == -1 && player.getInteracting() == null;
	}

	private void grantXp()
	{
		int xpGain = BASE_XP_PER_MINUTE;
		
		// Apply level scaling if enabled
		if (config.enableXpScaling() && currentLevel >= 30)
		{
			xpGain = Math.min(25, BASE_XP_PER_MINUTE + (currentLevel - 30) / 10);
		}
		
		totalXp += xpGain;
		int newLevel = calculateLevel(totalXp);
		
		if (newLevel > currentLevel)
		{
			currentLevel = newLevel;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
				"🎉 Bank Standscape level up! You are now level " + currentLevel + "!", null);
		}
		
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
			"💰 You gain " + xpGain + " Bank Standscape XP! (Total: " + totalXp + ")", null);
		
		saveXp();
		log.debug("Granted {} Bank Standscape XP. Total: {}", xpGain, totalXp);
	}

	private int calculateLevel(long xp)
	{
		int level = 1;
		long xpForLevel = 0;
		
		while (xpForLevel <= xp)
		{
			level++;
			xpForLevel += (long) Math.floor(level + 300 * Math.pow(2, level / 7.0)) / 4;
		}
		
		return Math.max(1, level - 1);
	}

	private void loadXp()
	{
		String xpString = configManager.getRSProfileConfiguration(CONFIG_GROUP, "totalXp");
		totalXp = xpString == null ? 0 : Long.parseLong(xpString);
		currentLevel = calculateLevel(totalXp);
	}

	private void saveXp()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, "totalXp", String.valueOf(totalXp));
	}

	@Provides
	BankStandscapeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankStandscapeConfig.class);
	}
}