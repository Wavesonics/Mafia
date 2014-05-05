package com.darkrockstudios.apps.mafia.eventbus;

import com.darkrockstudios.apps.mafia.game.World;

/**
 * Created by Adam on 4/27/2014.
 */
public class WorldStateChangedEvent
{
	public final World.State m_newState;
	public final String      m_voteWinnerId;

	public WorldStateChangedEvent( final World.State newState )
	{
		m_newState = newState;
		m_voteWinnerId = null;
	}

	public WorldStateChangedEvent( final World.State newState, final String voteWinnerId )
	{
		m_newState = newState;
		m_voteWinnerId = voteWinnerId;
	}
}
