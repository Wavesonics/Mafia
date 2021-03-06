package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;

/**
 * Created by Adam on 5/3/2014.
 */
public class PlayerReadyRPC extends RemoteProcedureCall
{
	public final String  m_participantId;
	public final boolean m_ready;

	public PlayerReadyRPC( final GameController gameController, final boolean ready )
	{
		super();

		m_participantId = gameController.getLocalParticipantId();
		m_ready = ready;
	}

	@Override
	public void makeProcedureCall( final GameController gameController )
	{
		gameController.markReady( m_participantId, m_ready );
	}

	@Override
	public boolean isBroadcast()
	{
		return true;
	}
}
