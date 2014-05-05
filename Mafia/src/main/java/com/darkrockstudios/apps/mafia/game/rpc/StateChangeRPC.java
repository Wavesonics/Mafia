package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;
import com.darkrockstudios.apps.mafia.game.World;

/**
 * Created by Adam on 5/3/2014.
 */
public class StateChangeRPC extends RemoteProcedureCall
{
	public final World.State m_state;

	public StateChangeRPC( final World.State state )
	{
		super();
		m_state = state;
	}

	@Override
	public void makeProcedureCall( final GameController gameController )
	{
		gameController.getWorld().changeState( m_state );
	}

	@Override
	public boolean isBroadcast()
	{
		return true;
	}
}
