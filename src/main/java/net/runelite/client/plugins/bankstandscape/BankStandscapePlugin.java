/*
 * Copyright (c) 2024, Bank Standscape
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.bankstandscape;

import com.google.inject.Provides;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import java.awt.Color;

@Slf4j
@PluginDescriptor(
        name = "Bank Standscape",
        description = "Gain XP for standing idle at banks. A fun, cosmetic skill to level up.",
        tags = {"bank", "stand", "afk", "idle", "xp", "grand exchange", "ge", "skill", "fun"}
)
public class BankStandscapePlugin extends Plugin
{
        // --- Constants ---
        private static final int TICKS_PER_MINUTE = 100;
        private static final int BASE_XP_PER_MINUTE = 10;
        private static final int MAX_IDLE_TICKS_PER_DAY = 36000;
        private static final int CROWD_BONUS_PLAYER_COUNT = 5;
        private static final double CROWD_BONUS_MULTIPLIER = 1.20;
        private static final double PREMIUM_MULTIPLIER = 1.5;
        private static final double OTHER_BANK_XP_MODIFIER = 0.5; // 50% XP at other banks
        private static final int PET_DROP_CHANCE = 10_000_000; // 1 in 10 million
        private static final String CONFIG_GROUP = "bankstandscape";

        // --- Injected RuneLite Services ---
        @Inject private Client client;
        @Inject private BankStandscapeConfig config;
        @Inject private OverlayManager overlayManager;
        @Inject private ConfigManager configManager;
        @Inject private Notifier notifier;
        @Inject private BankStandscapeOverlay overlay;
        @Inject private ChatBadgeManager chatBadgeManager;
        @Inject private AfkPetOverlay afkPetOverlay;
        @Inject private GildedGoblinPetOverlay gildedGoblinPetOverlay;


        // --- Plugin State Variables ---
        @Getter private long totalXp;
        @Getter private int currentLevel;
        @Getter private boolean isIdleInGe; // Used by pet overlays

        private int idleTicksCounter = 0;
        private int dailyIdleTicks = 0;
        private int totalIdleTicks = 0;
        private WorldPoint lastPosition;

        @Override
        protected void startUp() throws Exception
        {
                log.info("Bank Standscape started!");
                overlayManager.add(overlay);
                overlayManager.add(afkPetOverlay);
                overlayManager.add(gildedGoblinPetOverlay);
                loadXp();
        }

        @Override
        protected void shutDown() throws Exception
        {
                log.info("Bank Standscape stopped!");
                overlayManager.remove(overlay);
                overlayManager.remove(afkPetOverlay);
                overlayManager.remove(gildedGoblinPetOverlay);
                saveXp();
        }

        @Subscribe
        public void onGameTick(GameTick gameTick)
        {
                isIdleInGe = false; // Reset each tick
                if (client.getGameState() != GameState.LOGGED_IN || !config.enablePlugin())
                {
                        return;
                }
                Player localPlayer = client.getLocalPlayer();
                if (localPlayer == null)
                {
                        return;
                }

                boolean inBank = isPlayerInBankArea(localPlayer.getWorldLocation());
                boolean isCurrentlyIdle = isPlayerIdle(localPlayer);
                isIdleInGe = inBank && isCurrentlyIdle && BankLocations.GRAND_EXCHANGE.contains(localPlayer.getWorldLocation());

                if (inBank && isCurrentlyIdle && dailyIdleTicks < MAX_IDLE_TICKS_PER_DAY)
                {
                        idleTicksCounter++;
                        dailyIdleTicks++;
                        totalIdleTicks++;
                        if (idleTicksCounter >= TICKS_PER_MINUTE)
                        {
                                grantXp(localPlayer.getWorldLocation());
                                idleTicksCounter = 0;
                        }
                }
                else
                {
                        idleTicksCounter = 0;
                        totalIdleTicks = 0; // Reset total idle ticks when not idle
                }
                lastPosition = localPlayer.getWorldLocation();
        }

        @Subscribe
        public void onChatMessage(ChatMessage chatMessage)
        {
       Player localPlayer = client.getLocalPlayer();
       if (localPlayer == null || !isPlayerInBankArea(localPlayer.getWorldLocation())) return;
                
                handleChatBadge(chatMessage);
                handleIdleBot(chatMessage);
        }

        private void handleChatBadge(ChatMessage chatMessage)
        {
                Player localPlayer = client.getLocalPlayer();
                if (localPlayer == null || !config.enableChatBadges() || chatMessage.getType() != ChatMessageType.PUBLICCHAT ||
                        !chatMessage.getName().equals(localPlayer.getName()))
                {
                        return;
                }

                String badge = config.isPremium() && config.customTitle() != CustomTitle.NONE
                        ? chatBadgeManager.getPremiumBadge(config.customTitle())
                        : chatBadgeManager.getBadgeForLevel(currentLevel);

                if (!badge.isEmpty())
                {
                        MessageNode messageNode = chatMessage.getMessageNode();
                        String cleanName = Text.removeTags(messageNode.getName());
                        messageNode.setName(badge + " " + cleanName);
                }
        }
        
        private void handleIdleBot(ChatMessage chatMessage)
        {
                Player localPlayer = client.getLocalPlayer();
                if (localPlayer == null || !config.isPremium() || !config.enableIdleBot() ||
                        chatMessage.getType() != ChatMessageType.PUBLICCHAT ||
                        chatMessage.getName().equals(localPlayer.getName()))
                {
                        return;
                }
                
                String message = chatMessage.getMessage().toLowerCase();
                String playerName = localPlayer.getName().toLowerCase();
                if (message.contains(playerName) && totalIdleTicks > TICKS_PER_MINUTE)
                {
                        client.invokeMenuAction("Public chat", "", 0, 0, 0, 0);
                        client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, config.idleBotResponse());
                }
        }


        private boolean isPlayerIdle(Player player)
        {
                if (lastPosition == null || !player.getWorldLocation().equals(lastPosition))
                {
                        return false;
                }
                return player.getAnimation() == -1 && player.getInteracting() == null;
        }

        @Subscribe
        public void onAnimationChanged(AnimationChanged event)
        {
                if (event.getActor() == client.getLocalPlayer())
                {
                        idleTicksCounter = 0;
                        totalIdleTicks = 0;
                }
        }

        @Subscribe
        public void onInteractingChanged(InteractingChanged event)
        {
                if (event.getSource() == client.getLocalPlayer())
                {
                        idleTicksCounter = 0;
                        totalIdleTicks = 0;
                }
        }

        @Subscribe
        public void onGameStateChanged(GameStateChanged gameStateChanged)
        {
                if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
                {
                        dailyIdleTicks = 0;
                }
        }

        private boolean isPlayerInBankArea(WorldPoint location)
        {
                if (location == null) return false;
                if (BankLocations.GRAND_EXCHANGE.contains(location)) return true;
                return config.isPremium() && config.enableMultiBank() && BankLocations.isAtOtherBank(location);
        }

        private void grantXp(WorldPoint location)
        {
                double xpGained = getBaseXpForLevel(currentLevel);
                
                if (!BankLocations.GRAND_EXCHANGE.contains(location))
                {
                        xpGained *= OTHER_BANK_XP_MODIFIER;
                }

                if (config.isPremium()) xpGained *= PREMIUM_MULTIPLIER;
                if (config.crowdBonus() && isCrowded()) xpGained *= CROWD_BONUS_MULTIPLIER;
                
                int finalXpGained = (int) xpGained;
                totalXp += finalXpGained;

                // Roll for the rare pet
                rollForGildedGoblin(finalXpGained);

                int newLevel = XpUtil.getLevelForXp(totalXp);
                if (newLevel > currentLevel)
                {
                        currentLevel = newLevel;
                        sendLevelUpNotification();
                }
                saveXp();
                log.debug("Granted {} Bank Standscape XP. Total XP: {}", finalXpGained, totalXp);
        }

        private void rollForGildedGoblin(int xpGained)
        {
                if (config.hasGildedGoblinPet())
                {
                        return; // Already have the pet
                }

                for (int i = 0; i < xpGained; i++)
                {
                        if (ThreadLocalRandom.current().nextInt(PET_DROP_CHANCE) == 0)
                        {
                                // Success!
                                configManager.setConfiguration(CONFIG_GROUP, "hasGildedGoblinPet", true);
                                sendPetUnlockNotification();
                                break; // Stop rolling once unlocked
                        }
                }
        }

        private int getBaseXpForLevel(int level)
        {
                if (!config.enableXpScaling()) return BASE_XP_PER_MINUTE;
                if (level >= 99) return 25;
                if (level >= 85) return 20;
                if (level >= 70) return 17;
                if (level >= 50) return 14;
                if (level >= 30) return 12;
                return BASE_XP_PER_MINUTE;
        }

        private boolean isCrowded()
        {
                Player localPlayer = client.getLocalPlayer();
                if (localPlayer == null || !BankLocations.GRAND_EXCHANGE.contains(localPlayer.getWorldLocation()))
                {
                        return false;
                }
                int nearbyPlayers = 0;
                for (Player player : client.getPlayers())
                {
                        if (!player.equals(localPlayer) && BankLocations.GRAND_EXCHANGE.contains(player.getWorldLocation()))
                        {
                                nearbyPlayers++;
                        }
                }
                return nearbyPlayers >= CROWD_BONUS_PLAYER_COUNT;
        }

        private void sendLevelUpNotification()
        {
                String message = "Congratulations, you just advanced your Bank Standing level to " + currentLevel + "!";
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You've unlocked a new way to loiter productively!", "Bank Standscape");
                if (config.showLevelUpNotifications()) notifier.notify(message);
        }

        private void sendPetUnlockNotification()
        {
                String petMessage = "A flash of gold catches your eye! You have found a Gilded Goblin pet!";
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag(petMessage, Color.RED), "Bank Standscape");
                notifier.notify(petMessage);
        }

        private void loadXp()
        {
                String xpString = configManager.getRSProfileConfiguration(CONFIG_GROUP, "totalXp");
                totalXp = xpString == null ? 0 : Long.parseLong(xpString);
                currentLevel = XpUtil.getLevelForXp(totalXp);
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