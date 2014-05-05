package com.darkrockstudios.apps.mafia.eventbus;

/**
 * Created by Adam on 5/4/2014.
 */
public class VoteCastEvent
{
	public final String m_voterId;
	public final String m_nomineeId;

	public VoteCastEvent( final String voterId, final String nomineeId )
	{
		super();

		m_voterId = voterId;
		m_nomineeId = nomineeId;
	}
}
