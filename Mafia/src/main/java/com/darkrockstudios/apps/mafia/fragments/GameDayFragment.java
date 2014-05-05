package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.rpc.PlayerReadyRPC;
import com.google.android.gms.games.multiplayer.Participant;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameDayFragment extends BaseGameFragment
{
	public static final String ARG_VOTE_WINNER = GameDayFragment.class.getPackage() + ".VOTE_WINNER";

	@InjectView(R.id.DAY_murder)
	TextView m_murderView;

	@InjectView(R.id.DAY_ready_button)
	Button m_readyButton;

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

	@OnClick(R.id.DAY_ready_button)
	public void onReadyClicked( final View view )
	{
		PlayerReadyRPC playerReady = new PlayerReadyRPC( m_gameController, true );
		m_gameController.getNetwork().executeRpc( playerReady );

		m_readyButton.setEnabled( false );
	}
}
