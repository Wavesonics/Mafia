package com.darkrockstudios.apps.mafia.misc;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * Created by Adam on 8/1/13.
 * Dark Rock Studios
 * darkrockstudios.com
 */
public class BitmapCache extends LruCache implements ImageCache
{
	public BitmapCache( final int maxSize )
	{
		super( maxSize );
	}

	@Override
	public Bitmap getBitmap( final String url )
	{
		return (Bitmap) get( url );
	}

	@Override
	public void putBitmap( final String url, final Bitmap bitmap )
	{
		put( url, bitmap );
	}
}