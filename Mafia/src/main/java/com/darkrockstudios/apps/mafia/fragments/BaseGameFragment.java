package com.darkrockstudios.apps.mafia.fragments;

import android.app.Activity;
import android.app.DialogFragment;

import com.darkrockstudios.apps.mafia.game.GameController;

/**
 * Created by Adam on 4/27/2014.
 */
public class BaseGameFragment extends DialogFragment
{
	protected GameController m_gameController;

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		m_gameController = GameController.get( getFragmentManager() );
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
}
