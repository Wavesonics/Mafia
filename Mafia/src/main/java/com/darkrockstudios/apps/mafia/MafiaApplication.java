package com.darkrockstudios.apps.mafia;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.darkrockstudios.apps.mafia.misc.BitmapCache;

/**
 * Created by Adam on 5/4/2014.
 */
public class MafiaApplication extends Application
{
	private static RequestQueue s_requestQueue;
	private static final int MAX_CACHE_SIZE = 5 * 1014 * 1024;
	private static BitmapCache s_bitmapCache;

	@Override
	public void onCreate()
	{
		super.onCreate();

		s_requestQueue = Volley.newRequestQueue( this );
		s_bitmapCache = new BitmapCache( MAX_CACHE_SIZE );
	}

	public static RequestQueue getRequestQueue()
	{
		return s_requestQueue;
	}

	public static BitmapCache getBitmapCache()
	{
		return s_bitmapCache;
	}
}
