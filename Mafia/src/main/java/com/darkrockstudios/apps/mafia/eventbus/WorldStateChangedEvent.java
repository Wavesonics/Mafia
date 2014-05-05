package com.darkrockstudios.apps.mafia.eventbus;

import com.darkrockstudios.apps.mafia.game.World;

/**
 * Created by Adam on 4/27/2014.
 */
public class WorldStateChangedEvent
{
	public final World.State m_newState;

	public WorldStateChangedEvent( final World.State newState )
	{
		m_newState = newState;
	}
}
