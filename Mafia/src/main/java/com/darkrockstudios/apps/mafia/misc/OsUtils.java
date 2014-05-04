package com.darkrockstudios.apps.mafia.misc;

import android.os.Build;

/**
 * Created by Adam on 4/27/2014.
 */
public final class OsUtils
{
	public static boolean hasJellyBeanMr1()
	{
		return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}
}
