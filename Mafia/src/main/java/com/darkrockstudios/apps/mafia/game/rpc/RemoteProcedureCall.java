package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 5/3/2014.
 */
public abstract class RemoteProcedureCall implements Serializable
{
	private transient final List<String> m_recipients;

	public RemoteProcedureCall()
	{
		m_recipients = new ArrayList<>();
	}

	public abstract void makeProcedureCall( final GameController gameController );

	public abstract boolean isBroadcast();

	public final List<String> recipientList()
	{
		return m_recipients;
	}

	public final void addRecipient( final String recipient )
	{
		m_recipients.add( recipient );
	}
}
