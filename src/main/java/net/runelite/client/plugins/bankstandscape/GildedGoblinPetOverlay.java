package net.runelite.client.plugins.bankstandscape;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class GildedGoblinPetOverlay extends Overlay
{
        private final Client client;
        private final BankStandscapePlugin plugin;
        private final BankStandscapeConfig config;

        @Inject
        private GildedGoblinPetOverlay(Client client, BankStandscapePlugin plugin, BankStandscapeConfig config)
        {
                this.client = client;
                this.plugin = plugin;
                this.config = config;
                setPosition(OverlayPosition.DYNAMIC);
                setLayer(OverlayLayer.ABOVE_SCENE);
        }

        @Override
        public Dimension render(Graphics2D graphics)
        {
                // Only show if unlocked, enabled, and the player is idle in the GE
                if (!config.hasGildedGoblinPet() || !config.showGildedGoblinPet() || !plugin.isIdleInGe())
                {
                        return null;
                }

                final Player player = client.getLocalPlayer();
                if (player == null)
                {
                        return null;
                }

                // Main pet body "G" for Goblin
                String petText = "G";
                Point textLocation = Perspective.getCanvasTextLocation(client, graphics, player.getLocalPoint(), petText, player.getLogicalHeight() / 2);

                if (textLocation != null)
                {
                        // Render the pet itself to the side of the player
                        Point petPoint = new Point(textLocation.getX() - 30, textLocation.getY());
                        OverlayUtil.renderTextLocation(graphics, petPoint, petText, new Color(255, 215, 0)); // Gold color

                        // Create a blinking "sparkle" effect
                        if ((System.currentTimeMillis() / 500) % 2 == 0)
                        {
                                Point sparklePoint = new Point(petPoint.getX() + 8, petPoint.getY() - 8);
                                OverlayUtil.renderTextLocation(graphics, sparklePoint, "*", Color.WHITE);
                        }
                }
                return null;
        }
}