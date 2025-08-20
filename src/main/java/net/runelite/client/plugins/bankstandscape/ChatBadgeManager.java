package net.runelite.client.plugins.bankstandscape;

import net.runelite.client.util.ColorUtil;
import java.awt.Color;

public class ChatBadgeManager
{
        public String getBadgeForLevel(int level)
        {
                if (level >= 99) return ColorUtil.wrapWithColorTag("[Golden Gnome]", new Color(255, 215, 0));
                if (level >= 90) return ColorUtil.wrapWithColorTag("[Pillar Pundit]", new Color(200, 180, 255));
                if (level >= 75) return ColorUtil.wrapWithColorTag("[Flipping Ghost]", new Color(170, 255, 250));
                if (level >= 60) return ColorUtil.wrapWithColorTag("[GE Gossiper]", new Color(255, 170, 200));
                if (level >= 40) return ColorUtil.wrapWithColorTag("[Professional]", new Color(160, 220, 140));
                if (level >= 20) return ColorUtil.wrapWithColorTag("[Bank Stander]", new Color(220, 220, 220));
                if (level >= 5)  return ColorUtil.wrapWithColorTag("[Wanderer]", new Color(180, 180, 180));
                return "";
        }

        public String getPremiumBadge(CustomTitle title)
        {
                switch (title)
                {
                        case MARKET_WATCHER: return ColorUtil.wrapWithColorTag("~Market Watcher~", Color.CYAN);
                        case FLIPPING_GHOST: return ColorUtil.wrapWithColorTag("<Flipping Ghost>", Color.WHITE);
                        case BRASSICUS_BLESSED: return ColorUtil.wrapWithColorTag("[Cabbage-Kin]", Color.GREEN);
                        case TAX_EVADER: return ColorUtil.wrapWithColorTag("{Tax Evader}", Color.RED);
                        default: return "";
                }
        }
}