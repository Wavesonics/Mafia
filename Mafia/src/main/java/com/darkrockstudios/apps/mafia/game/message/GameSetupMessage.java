package com.darkrockstudios.apps.mafia.game.message;

import com.darkrockstudios.apps.mafia.game.GameSetup;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameSetupMessage extends Message
{
	public GameSetup m_gameSetup;

	public GameSetupMessage( final GameSetup gameSetup )
	{
		super();
		m_gameSetup = gameSetup;
	}
}
