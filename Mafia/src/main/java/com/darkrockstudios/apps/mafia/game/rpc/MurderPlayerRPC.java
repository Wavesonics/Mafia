package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;

/**
 * Created by Adam on 5/4/2014.
 */
public class MurderPlayerRPC extends RemoteProcedureCall
{
	@Override
	public void makeProcedureCall( final GameController gameController )
	{

	}

	@Override
	public boolean isBroadcast()
	{
		return false;
	}
}
