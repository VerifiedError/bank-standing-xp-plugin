package net.runelite.client.plugins.bankstandscape;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import java.util.Arrays;
import java.util.List;

public class BankLocations
{
        public static final WorldArea GRAND_EXCHANGE = new WorldArea(3149, 3501, 31, 23, 0);
        
        private static final List<WorldArea> OTHER_BANKS = Arrays.asList(
                new WorldArea(3092, 3489, 8, 8, 0), // Falador
                new WorldArea(3250, 3418, 5, 5, 0), // Varrock East
                new WorldArea(3269, 3438, 5, 5, 0), // Varrock West
                new WorldArea(2847, 3538, 5, 5, 0), // Camelot
                new WorldArea(3010, 3354, 5, 5, 0), // Draynor
                new WorldArea(2943, 3367, 6, 6, 0)  // Barbarian Village
        );

        public static boolean isAtOtherBank(WorldPoint location)
        {
                for (WorldArea area : OTHER_BANKS) 
                { 
                        if (area.contains(location)) 
                                return true; 
                }
                return false;
        }
}