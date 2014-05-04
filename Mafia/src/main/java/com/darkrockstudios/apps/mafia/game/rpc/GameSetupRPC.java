package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;
import com.darkrockstudios.apps.mafia.game.GameSetup;

/**
 * Created by Adam on 5/3/2014.
 */
public class GameSetupRPC extends RemoteProcedureCall
{
	public final GameSetup m_gameSetup;

	public GameSetupRPC( final GameSetup gameSetup )
	{
		super();
		m_gameSetup = gameSetup;
	}

	@Override
	public void makeProcedureCall( final GameController gameController )
	{
		gameController.setupGame( m_gameSetup );
	}

	@Override
	public boolean isBroadcast()
	{
		return true;
	}
}
