package com.bankstandingxp;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@PluginDescriptor(
    name = "Bank Standing XP",
    description = "Train your favorite skill while standing in your favorite bank spot - Jake",
    tags = {"Bank Standing", "Bank standing XP", "socializing", "adventure", "afk"}
)
public class BankStandingXpPlugin extends Plugin
{
    @Inject
    private Client client;

    // Core variables
    private WorldPoint lastPosition;
    private Instant standingStartTime;
    private Instant lastXpGainTime;
    private boolean isStanding = false;
    private boolean isInBankArea = false;
    private int totalXpGained = 0;
    private int currentTier = 0;

    // Bank locations by tier
    private static final List<WorldPoint> TIER_1_BANKS = Arrays.asList(
        new WorldPoint(3164, 3487, 0)  // Grand Exchange
    );
    
    private static final List<WorldPoint> TIER_2_BANKS = Arrays.asList(
        new WorldPoint(2725, 3493, 0), // Camelot
        new WorldPoint(3208, 3220, 0), // East Varrock
        new WorldPoint(3185, 3441, 0), // West Varrock
        new WorldPoint(2946, 3368, 0), // Falador
        new WorldPoint(3093, 3245, 0)  // Draynor
    );

    @Override
    protected void startUp() throws Exception
    {
        System.out.println("🏦 Bank Standing XP Plugin Started!");
        totalXpGained = 0;
        reset();
        sendChatMessage("🏦 Bank Standing XP Plugin has been enabled! Find a bank and stand still for 60 seconds to gain XP!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        System.out.println("🏦 Bank Standing XP Plugin Stopped! Total XP: " + totalXpGained);
        reset();
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
        
        // Update bank area status
        updateBankArea(currentPosition);
        
        // Update standing status
        updateStandingState(currentPosition);
        
        // Check for XP gain
        checkForXpGain();
    }

    private void updateBankArea(WorldPoint position)
    {
        int newTier = 0;
        
        // Check Tier 1 (Grand Exchange)
        for (WorldPoint bankPoint : TIER_1_BANKS)
        {
            if (position.distanceTo(bankPoint) <= 5) // Within 5 tiles
            {
                newTier = 1;
                break;
            }
        }
        
        // Check Tier 2 (Major banks)
        if (newTier == 0)
        {
            for (WorldPoint bankPoint : TIER_2_BANKS)
            {
                if (position.distanceTo(bankPoint) <= 4) // Within 4 tiles
                {
                    newTier = 2;
                    break;
                }
            }
        }
        
        if (newTier != currentTier)
        {
            currentTier = newTier;
            isInBankArea = currentTier > 0;
            
            if (isInBankArea)
            {
                String tierName = currentTier == 1 ? "Grand Exchange (Tier 1)" : "Major Bank (Tier 2)";
                sendChatMessage("🏦 Entered " + tierName + " - Stand still for 60 seconds to gain XP!");
                playSound();
            }
            else
            {
                sendChatMessage("❌ Left bank area - Find a bank to continue gaining XP!");
                resetStandingTimer();
            }
        }
    }

    private void updateStandingState(WorldPoint currentPosition)
    {
        if (!isInBankArea)
        {
            if (isStanding)
            {
                resetStandingTimer();
            }
            return;
        }

        if (lastPosition == null || !lastPosition.equals(currentPosition))
        {
            // Player moved
            if (isStanding)
            {
                sendChatMessage("🚶 You moved! Standing timer reset - Stand still for 60 seconds.");
            }
            resetStandingTimer();
            lastPosition = currentPosition;
        }
        else
        {
            // Player is standing still
            if (!isStanding)
            {
                startStandingTimer();
            }
        }
    }

    private void startStandingTimer()
    {
        isStanding = true;
        standingStartTime = Instant.now();
        sendChatMessage("⏱️ Standing timer started! Hold still for 60 seconds...");
    }

    private void resetStandingTimer()
    {
        isStanding = false;
        standingStartTime = null;
    }

    private void checkForXpGain()
    {
        if (!isStanding || !isInBankArea || standingStartTime == null)
        {
            return;
        }

        long secondsStanding = java.time.Duration.between(standingStartTime, Instant.now()).getSeconds();
        
        // Show countdown messages
        if (secondsStanding == 30)
        {
            sendChatMessage("⏰ 30 seconds remaining...");
        }
        else if (secondsStanding == 50)
        {
            sendChatMessage("⏰ 10 seconds remaining...");
        }
        else if (secondsStanding == 55)
        {
            sendChatMessage("⏰ 5 seconds remaining...");
        }
        
        // Grant XP after 60 seconds, then every 5 seconds
        if (secondsStanding >= 60)
        {
            if (lastXpGainTime == null || java.time.Duration.between(lastXpGainTime, Instant.now()).getSeconds() >= 5)
            {
                grantXpGain();
                lastXpGainTime = Instant.now();
            }
        }
    }

    private void grantXpGain()
    {
        // Base XP: 50, with tier multipliers
        int baseXp = 50;
        int multiplier = currentTier == 1 ? 100 : 75; // GE = 100%, others = 75%
        int xpGain = (baseXp * multiplier) / 100;
        
        totalXpGained += xpGain;
        
        // Random event chance (10%)
        boolean randomEvent = Math.random() < 0.1;
        if (randomEvent)
        {
            int bonusXp = xpGain * 2;
            totalXpGained += bonusXp;
            sendChatMessage("🎉 RANDOM EVENT! You're a professional bank warmer! Bonus " + bonusXp + " XP!");
            playSound();
            playSound(); // Double beep for bonus
        }
        
        String tierText = currentTier == 1 ? "Tier 1" : "Tier 2";
        sendChatMessage("💰 You gain " + xpGain + " Bank Standing XP! (" + tierText + ") Total: " + totalXpGained);
        
        playSound();
        
        System.out.println("Bank Standing XP gained: " + xpGain + " (Total: " + totalXpGained + ")");
    }

    private void sendChatMessage(String message)
    {
        if (client != null)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }
    }

    private void playSound()
    {
        try
        {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
        catch (Exception e)
        {
            // Sound failed, ignore
        }
    }

    private void reset()
    {
        lastPosition = null;
        standingStartTime = null;
        lastXpGainTime = null;
        isStanding = false;
        isInBankArea = false;
        currentTier = 0;
    }
}