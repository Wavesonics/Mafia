package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.mafia.R;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameDeadFragment extends BaseGameFragment
{
	public static GameDeadFragment newInstance()
	{
		GameDeadFragment fragment = new GameDeadFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_dead, container, false );

		return view;
	}
}
