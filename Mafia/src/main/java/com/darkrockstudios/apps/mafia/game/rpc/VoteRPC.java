package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.ClientType;
import com.darkrockstudios.apps.mafia.game.GameController;
import com.darkrockstudios.apps.mafia.game.Vote;

/**
 * Created by Adam on 5/4/2014.
 */
public class VoteRPC extends RemoteProcedureCall
{
	public final String m_voterId;
	public final String m_nomineeId;

	public VoteRPC( final GameController gameController, final String nomineeId )
	{
		super();

		m_voterId = gameController.getLocalParticipantId();
		m_nomineeId = nomineeId;
	}

	@Override
	public void makeProcedureCall( final GameController gameController )
	{
		Vote vote = gameController.getWorld().getCurrentVote();
		vote.vote( gameController, m_voterId, m_nomineeId );

		// The master should not progress the game
		if( vote.isVoteComplete() && gameController.getClientType() == ClientType.MASTER )
		{

		}
	}

	@Override
	public boolean isBroadcast()
	{
		return true;
	}
}
