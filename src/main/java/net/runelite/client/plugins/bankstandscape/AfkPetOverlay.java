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

public class AfkPetOverlay extends Overlay
{
        private final Client client;
        private final BankStandscapePlugin plugin;
        private final BankStandscapeConfig config;

        @Inject
        private AfkPetOverlay(Client client, BankStandscapePlugin plugin, BankStandscapeConfig config)
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
                if (!config.isPremium() || !config.enableAfkPet() || !plugin.isIdleInGe()) 
                        return null;
                
                final Player player = client.getLocalPlayer();
                if (player == null) 
                        return null;
                
                String petText = "gp";
                Point textLocation = Perspective.getCanvasTextLocation(client, graphics, player.getLocalPoint(), petText, player.getLogicalHeight() / 2);
                if (textLocation != null) 
                        OverlayUtil.renderTextLocation(graphics, new Point(textLocation.getX() + 20, textLocation.getY()), petText, Color.YELLOW);
                
                return null;
        }
}