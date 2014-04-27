package com.darkrockstudios.apps.mafia;

import android.app.Activity;
import android.app.Fragment;

/**
 * Created by Adam on 4/27/2014.
 */
public class BaseGameFragment extends Fragment
{
	private GameController m_gameController;

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof GameControllerProvider )
		{
			GameControllerProvider provider = (GameControllerProvider) activity;
			m_gameController = provider.getGameController();
		}
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
