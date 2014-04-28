package com.darkrockstudios.apps.mafia.game.message;

/**
 * Created by Adam on 4/27/2014.
 */
public class PlayerReadyMessage extends Message
{
	public String  m_participantId;
	public boolean m_ready;

	public PlayerReadyMessage( final String participantId, final boolean ready )
	{
		m_participantId = participantId;
		m_ready = ready;
	}

	public PlayerReadyMessage()
	{

	}
}
