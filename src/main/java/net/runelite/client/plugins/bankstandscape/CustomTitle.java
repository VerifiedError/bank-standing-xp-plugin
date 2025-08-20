package net.runelite.client.plugins.bankstandscape;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomTitle
{
        NONE("None"), 
        MARKET_WATCHER("Market Watcher"), 
        FLIPPING_GHOST("Flipping Ghost"), 
        BRASSICUS_BLESSED("Brassy's Blessed"), 
        TAX_EVADER("Tax Evader");

        private final String name;

        @Override
        public String toString() 
        { 
                return name; 
        }
}