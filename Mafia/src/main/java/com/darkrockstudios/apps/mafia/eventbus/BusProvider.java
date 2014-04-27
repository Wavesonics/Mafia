package com.darkrockstudios.apps.mafia.eventbus;

import com.squareup.otto.Bus;

/**
 * Created by Adam on 4/27/2014.
 */
public final class BusProvider
{
	private static Bus m_bus;

	private BusProvider()
	{

	}

	public static Bus get()
	{
		if( m_bus == null )
		{
			m_bus = new Bus();
		}

		return m_bus;
	}
}
