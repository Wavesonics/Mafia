package com.darkrockstudios.apps.mafia.fragments;

import android.app.Activity;
import android.app.DialogFragment;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.GameController;
import com.darkrockstudios.apps.mafia.game.Nav;
import com.squareup.otto.Subscribe;

/**
 * Created by Adam on 4/27/2014.
 */
public class BaseGameFragment extends DialogFragment
{
	private   EventHandler   m_eventHandler;
	protected GameController m_gameController;

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		m_gameController = GameController.get( getFragmentManager() );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_eventHandler = new EventHandler();
		BusProvider.get().register( m_eventHandler );
	}

	@Override
	public void onPause()
	{
		super.onPause();

		BusProvider.get().unregister( m_eventHandler );
		m_eventHandler = null;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		m_gameController = null;
	}

	public GameController getGameController()
	{
		return m_gameController;
	}

	private class EventHandler
	{
		@Subscribe
		public void onWorldStateChanged( final WorldStateChangedEvent event )
		{
			switch( event.m_newState )
			{
				case Setup:
					// Do nothing
					break;
				case Pregame:
					break;
				case Night:
					Nav.gotoNightScreen( m_gameController );
					break;
				case Day:
					Nav.gotoDayScreen( m_gameController, event.m_voteWinnerId );
					break;
				case End:
					Nav.gotoEndScreen( m_gameController );
					break;
				case Invalid:
					break;
			}
		}
	}
}
