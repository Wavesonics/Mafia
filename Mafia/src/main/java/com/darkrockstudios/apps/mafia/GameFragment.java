package com.darkrockstudios.apps.mafia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameFragment extends BaseGameFragment
{
	public static GameFragment newInstance()
	{
		GameFragment fragment = new GameFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}
}
