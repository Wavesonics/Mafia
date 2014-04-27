package com.darkrockstudios.apps.mafia.game;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;

/**
 * Created by Adam on 4/27/2014.
 */
public class World
{
	public static enum State
	{
		Setup,
		Pregame,
		Night,
		Day,
		End
	}

	private State m_state;

	public World()
	{
		m_state = State.Setup;
	}

	public State getState()
	{
		return m_state;
	}

	public void setState( final State state )
	{
		m_state = state;
		BusProvider.get().post( new WorldStateChangedEvent() );
	}
}
