package com.bankstandscape;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankstandscape")
public interface BankStandscapeConfig extends Config
{
	@ConfigItem(
		keyName = "enablePlugin",
		name = "Enable Plugin",
		description = "Enables or disables the Bank Standscape plugin"
	)
	default boolean enablePlugin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableXpScaling",
		name = "XP Scaling",
		description = "Increases XP per minute as your level increases"
	)
	default boolean enableXpScaling()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableMultiBank",
		name = "Multi-Bank XP",
		description = "Allows gaining XP at other major banks (Grand Exchange is always enabled)"
	)
	default boolean enableMultiBank()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Shows the Bank Standscape overlay with level and XP information"
	)
	default boolean showOverlay()
	{
		return true;
	}
}