package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.PlayerVoteStatus;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameNightMobsterFragment extends VotingFragment
{
	public static GameNightMobsterFragment newInstance()
	{
		GameNightMobsterFragment fragment = new GameNightMobsterFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_night_mobster, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	@Override
	public void onItemClick( final AdapterView<?> parent, final View view, final int position, final long id )
	{
		super.onItemClick( parent, view, position, id );

		PlayerVoteStatus playerStats = m_adapter.getItem( position );
		Crouton.makeText( getActivity(), "Voted to kill: " + playerStats.m_participant.getDisplayName(), Style.CONFIRM ).show();
	}
}
