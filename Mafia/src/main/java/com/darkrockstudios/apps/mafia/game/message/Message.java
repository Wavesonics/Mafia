package com.darkrockstudios.apps.mafia.game.message;

import java.io.Serializable;

/**
 * Created by Adam on 4/27/2014.
 */
public class Message implements Serializable
{
	public int m_type;

	public Message( final int type )
	{
		m_type = type;
	}
}
