package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.PlayerVoteStatus;
import com.google.android.gms.games.multiplayer.Participant;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameDayFragment extends VotingFragment
{
	public static final String ARG_VOTE_WINNER = GameDayFragment.class.getPackage() + ".VOTE_WINNER";

	@InjectView(R.id.DAY_murder)
	TextView m_murderView;

	private String m_voteWinnerId;

	public static GameDayFragment newInstance( final String voteWinnerId )
	{
		GameDayFragment fragment = new GameDayFragment();

		Bundle args = new Bundle();
		args.putString( ARG_VOTE_WINNER, voteWinnerId );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_voteWinnerId = getArguments().getString( ARG_VOTE_WINNER );
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_day, container, false );
		ButterKnife.inject( this, view );

		Participant victim = m_gameController.getRoom().getParticipant( m_voteWinnerId );
		m_murderView.setText( victim.getDisplayName() + " was killed in the night" );

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
		Crouton.makeText( getActivity(), "Voted to lynch: " + playerStats.m_participant.getDisplayName(), Style.CONFIRM )
		       .show();
	}
}
