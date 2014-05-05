package com.darkrockstudios.apps.mafia.game;

import android.util.Log;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.VoteCastEvent;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 5/4/2014.
 */
public class Vote
{
	private static final String TAG = Vote.class.getSimpleName();

	private List<Participant>      m_notVoted;
	private List<PlayerVoteStatus> m_nominees;

	public Vote()
	{
		m_nominees = new ArrayList<>();
		m_notVoted = new ArrayList<>();
	}

	public void addVoter( final Participant participant )
	{
		m_notVoted.add( participant );
	}

	public void addNominee( final Participant participant )
	{
		m_nominees.add( new PlayerVoteStatus( participant ) );
	}

	public List<PlayerVoteStatus> getNominees()
	{
		return m_nominees;
	}

	public void vote( final GameController gameController, final String voterId, final String nomineeId )
	{
		final Room room = gameController.getRoom();
		final Participant voter = room.getParticipant( voterId );
		final Participant nominee = room.getParticipant( nomineeId );

		for( final PlayerVoteStatus curNominee : m_nominees )
		{
			if( curNominee.m_participant.getParticipantId().equals( nomineeId ) )
			{
				curNominee.addVote( voter );
				m_notVoted.remove( voter );

				Log.d( TAG, voter.getDisplayName() + " successfully voted for: " + nominee.getDisplayName() );
			}
			// Ensure we haven't double voted by removing us from anyone else
			else
			{
				curNominee.removeVote( voter );
			}
		}

		BusProvider.get().post( new VoteCastEvent( voterId, nomineeId ) );
	}

	public boolean isVoteComplete()
	{
		return m_notVoted.isEmpty();
	}

	public Participant getVoteWinner()
	{
		Participant winner = null;

		int highestVotes = -1;
		for( final PlayerVoteStatus nominee : m_nominees )
		{
			if( nominee.getVotes() > highestVotes )
			{
				winner = nominee.m_participant;
				highestVotes = nominee.getVotes();
			}
		}

		return winner;
	}
}
