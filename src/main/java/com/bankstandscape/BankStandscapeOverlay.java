package com.bankstandscape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class BankStandscapeOverlay extends OverlayPanel
{
	private final BankStandscapePlugin plugin;
	private final BankStandscapeConfig config;

	@Inject
	private BankStandscapeOverlay(BankStandscapePlugin plugin, BankStandscapeConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay())
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Bank Standscape")
			.color(Color.CYAN)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Level:")
			.right(String.valueOf(plugin.getCurrentLevel()))
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Total XP:")
			.right(String.format("%,d", plugin.getTotalXp()))
			.build());

		return super.render(graphics);
	}
}