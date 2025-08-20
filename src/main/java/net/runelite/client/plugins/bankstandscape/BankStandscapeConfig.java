package net.runelite.client.plugins.bankstandscape;

import net.runelite.client.config.*;

@ConfigGroup("bankstandscape")
public interface BankStandscapeConfig extends Config
{
        // General Settings
        @ConfigSection(name = "General", description = "General settings", position = 0)
        String generalSettings = "generalSettings";
        
        @ConfigItem(keyName = "enablePlugin", name = "Enable Plugin", description = "Globally enables or disables the plugin.", position = 1, section = generalSettings)
        default boolean enablePlugin() { return true; }

        @ConfigItem(keyName = "showLevelUpNotifications", name = "Level-Up Notifications", description = "Shows a desktop notification when you level up.", position = 2, section = generalSettings)
        default boolean showLevelUpNotifications() { return true; }

        @ConfigItem(keyName = "enableXpScaling", name = "Enable XP Scaling", description = "Increases XP per minute as your level increases.", position = 3, section = generalSettings)
        default boolean enableXpScaling() { return true; }

        // Cosmetic Settings
        @ConfigSection(name = "Cosmetics", description = "Cosmetic features and unlocks", position = 1)
        String cosmeticSettings = "cosmeticSettings";

        @ConfigItem(keyName = "enableChatBadges", name = "Enable Chat Badges", description = "Shows a title next to your name in chat.", position = 4, section = cosmeticSettings)
        default boolean enableChatBadges() { return true; }

        @ConfigItem(keyName = "showGildedGoblinPet", name = "Show Gilded Goblin Pet", description = "Show your rare pet when idle. (If unlocked)", position = 5, section = cosmeticSettings)
        default boolean showGildedGoblinPet() { return true; }

        @ConfigItem(keyName = "hasGildedGoblinPet", name = "Gilded Goblin Unlocked", description = "You have unlocked the super rare Gilded Goblin pet!", position = 6, section = cosmeticSettings, hidden = true)
        default boolean hasGildedGoblinPet() { return false; }


        // Premium Features
        @ConfigSection(name = "Premium Features", description = "Settings for premium users. Requires donation.", position = 2, closedByDefault = true)
        String premiumSettings = "premiumSettings";

        @ConfigItem(keyName = "isPremium", name = "Activate Premium", description = "Enables all premium features.", position = 7, section = premiumSettings)
        default boolean isPremium() { return false; }
        
        @ConfigItem(keyName = "customTitle", name = "Custom Chat Title", description = "Choose a custom chat title to display. Overrides level badge.", position = 8, section = premiumSettings)
        default CustomTitle customTitle() { return CustomTitle.NONE; }

        @ConfigItem(keyName = "enableMultiBank", name = "Multi-Bank XP", description = "Gain XP at other major banks (at a reduced rate).", position = 9, section = premiumSettings)
        default boolean enableMultiBank() { return false; }

        @ConfigItem(keyName = "enableAfkPet", name = "Enable AFK Pet", description = "Shows a 'Mini Merchant' pet when you're idle at the GE.", position = 10, section = premiumSettings)
        default boolean enableAfkPet() { return false; }

        @ConfigItem(keyName = "crowdBonus", name = "Enable Crowd Bonus", description = "Enables the +20% XP bonus for being near 5+ players at the GE.", position = 11, section = premiumSettings)
        default boolean crowdBonus() { return true; }
        
        @ConfigItem(keyName = "enableIdleBot", name = "Enable Idle Bot", description = "Automatically reply when someone mentions your name while idle.", position = 12, section = premiumSettings)
        default boolean enableIdleBot() { return false; }

        @ConfigItem(keyName = "idleBotResponse", name = "Idle Bot Message", description = "The message your idle bot will use.", position = 13, section = premiumSettings)
        default String idleBotResponse() { return "Sorry, busy watching the graphs!"; }

        // Leaderboard Section
        @ConfigSection(name = "Leaderboards", description = "Leaderboard settings", position = 3)
        String leaderboardSettings = "leaderboardSettings";

        @ConfigItem(keyName = "optInLeaderboard", name = "Opt-in to Leaderboard", description = "Allow your XP to be posted to the public leaderboard.", position = 14, section = leaderboardSettings)
        default boolean optInLeaderboard() { return false; }
}