package com.darkrockstudios.apps.mafia.eventbus;

/**
 * Created by adam on 5/4/14.
 */
public class SignInStateChangedEvent
{
	public final boolean m_signedIn;

	public SignInStateChangedEvent( final boolean signedIn )
	{
		m_signedIn = signedIn;
	}
}
