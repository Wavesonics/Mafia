package com.darkrockstudios.apps.mafia.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameSetup implements Serializable
{
	private int m_numMobsters = 1;

	private Map<String, PlayerSpecification> m_players;

	public GameSetup()
	{
		m_players = new HashMap<>();
	}

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

	public void addPlayer( final PlayerSpecification playerSpec )
	{
		m_players.put( playerSpec.m_participantId, playerSpec );
	}

	public PlayerSpecification getPlayer( final String participantId )
	{
		return m_players.get( participantId );
	}
}
