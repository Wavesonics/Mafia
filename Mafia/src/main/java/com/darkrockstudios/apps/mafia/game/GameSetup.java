package com.darkrockstudios.apps.mafia.game;

import java.io.Serializable;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameSetup implements Serializable
{
	private int m_numMobsters = 1;

	public void increaseMobsters( final GameController gameController )
	{
		if( m_numMobsters + 1 < gameController.getNumPlayers() - 1 )
		{
			++m_numMobsters;
		}
	}

	public void decreaseMobsters()
	{
		if( m_numMobsters - 1 > 0 )
		{
			--m_numMobsters;
		}
	}

	public int getNumMobsters()
	{
		return m_numMobsters;
	}
}
