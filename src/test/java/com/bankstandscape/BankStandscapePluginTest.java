package com.bankstandscape;

import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankStandscapePluginTest
{
    @Mock
    private Client client;

    @Mock
    private BankStandscapeConfig config;

    @InjectMocks
    private BankStandscapePlugin plugin;

    @Test
    public void testChatMessageOnLogin()
    {
        String greeting = "Hello";
        when(config.greeting()).thenReturn(greeting);

        GameStateChanged event = new GameStateChanged();
        event.setGameState(GameState.LOGGED_IN);

        plugin.onGameStateChanged(event);

        verify(client).addChatMessage(ChatMessageType.GAMEMESSAGE, "", greeting, null);
    }
}

