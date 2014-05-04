package com.darkrockstudios.apps.mafia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.mafia.R;

/**
 * Created by Adam on 4/27/2014.
 */
public class LoadingFragment extends Fragment
{
	public static LoadingFragment newInstance()
	{
		LoadingFragment fragment = new LoadingFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_loading, container, false );
	}
}
