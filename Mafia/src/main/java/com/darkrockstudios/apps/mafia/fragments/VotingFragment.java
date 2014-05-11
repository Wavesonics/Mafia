package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.VoteCastEvent;
import com.darkrockstudios.apps.mafia.game.PlayerVoteStatus;
import com.darkrockstudios.apps.mafia.game.Vote;
import com.darkrockstudios.apps.mafia.game.rpc.PlayerReadyRPC;
import com.darkrockstudios.apps.mafia.game.rpc.VoteRPC;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;

/**
 * Created by adam on 5/11/14.
 */
public class VotingFragment extends BaseGameFragment implements AdapterView.OnItemClickListener
{
	private static final String TAG = VotingFragment.class.getSimpleName();

	protected VotingAdapter m_adapter;
	private   EventHandler  m_eventHandler;

	@InjectView(R.id.COMMON_vote_list)
	ListView m_votingList;

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_adapter = new VotingAdapter( getActivity() );
	}

	@Override
	public void onViewCreated( View view, Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		m_votingList.setAdapter( m_adapter );
		m_votingList.setOnItemClickListener( this );

		updateVotingList();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_eventHandler = new EventHandler();
		BusProvider.get().register( m_eventHandler );
	}

	@Override
	public void onPause()
	{
		super.onPause();

		BusProvider.get().unregister( m_eventHandler );
		m_eventHandler = null;
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

	@Override
	public void onItemClick( final AdapterView<?> parent, final View view, final int position, final long id )
	{
		PlayerVoteStatus playerStats = m_adapter.getItem( position );

		VoteRPC voteRPC = new VoteRPC( m_gameController, playerStats.m_participant.getParticipantId() );
		m_gameController.getNetwork().executeRpc( voteRPC );
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
				Log.d( TAG, "Vote cast, marking local player as ready." );

				PlayerReadyRPC playerReady = new PlayerReadyRPC( m_gameController, true );
				m_gameController.getNetwork().executeRpc( playerReady );
			}
		}
	}
}
