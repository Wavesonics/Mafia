package com.darkrockstudios.apps.mafia.game.message;

import com.darkrockstudios.apps.mafia.game.World;

/**
 * Created by Adam on 4/27/2014.
 */
public class StateChangeMessage extends Message
{
	public final World.State m_state;

	public StateChangeMessage( final World.State state )
	{
		m_state = state;
	}
}
