package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.VoteCastEvent;
import com.darkrockstudios.apps.mafia.game.PlayerVoteStatus;
import com.darkrockstudios.apps.mafia.game.Vote;
import com.darkrockstudios.apps.mafia.game.rpc.VoteRPC;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameNightMobsterFragment extends BaseGameFragment implements AdapterView.OnItemClickListener
{
	@InjectView(R.id.NIGHT_mobster_vote_list)
	ListView m_votingList;

	private VotingAdapter m_adapter;
	private EventHandler  m_eventHolder;

	public static GameNightMobsterFragment newInstance()
	{
		GameNightMobsterFragment fragment = new GameNightMobsterFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_adapter = new VotingAdapter( getActivity() );
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_night_mobster, container, false );
		ButterKnife.inject( this, view );

		m_votingList.setAdapter( m_adapter );
		m_votingList.setOnItemClickListener( this );

		return view;
	}

	@Override
	public void onViewCreated( final View view, final Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		updateVotingList();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_eventHolder = new EventHandler();
		BusProvider.get().register( m_eventHolder );
	}

	@Override
	public void onPause()
	{
		super.onPause();

		BusProvider.get().unregister( m_eventHolder );
		m_eventHolder = null;
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
		PlayerVoteStatus playerStats = m_adapter.getItem( position );

		Crouton.makeText( getActivity(), "Voted to kill: " + playerStats.m_participant.getDisplayName(), Style.CONFIRM ).show();

		VoteRPC voteRPC = new VoteRPC( m_gameController, playerStats.m_participant.getParticipantId() );
		m_gameController.getNetwork().executeRpc( voteRPC );
	}

	private void updateVotingList()
	{
		m_adapter.clear();

		final Vote vote = m_gameController.getWorld().getCurrentVote();
		for( final PlayerVoteStatus playerStatus : vote.getNominees() )
		{
			m_adapter.add( playerStatus );
		}

		m_adapter.notifyDataSetChanged();
	}

	private class EventHandler
	{
		@Subscribe
		public void onVoteCast( final VoteCastEvent event )
		{
			final String localParticipantId = m_gameController.getLocalParticipantId();

			if( isAdded() )
			{
				updateVotingList();
			}

			if( event.m_voterId.equals( localParticipantId ) )
			{
				m_gameController.markReady( localParticipantId, true );
			}
		}
	}
}
