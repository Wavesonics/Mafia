package com.darkrockstudios.apps.mafia.game.message;

/**
 * Created by Adam on 4/27/2014.
 */
public class DesignatePlayerClassMessage extends Message
{
	public static final transient int TYPE = 2;

	public DesignatePlayerClassMessage()
	{
		super( TYPE );
	}
}
