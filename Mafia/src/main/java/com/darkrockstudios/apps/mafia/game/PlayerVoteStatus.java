package com.darkrockstudios.apps.mafia.game;

import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 5/4/2014.
 */
public class PlayerVoteStatus
{
	public final Participant       m_participant;
	private      List<Participant> m_votesFor;

	public PlayerVoteStatus( final Participant participant )
	{
		m_participant = participant;
		m_votesFor = new ArrayList<>();
	}

	public int getVotes()
	{
		return m_votesFor.size();
	}

	public void addVote( final Participant participant )
	{
		if( !hasVotedFor( participant ) )
		{
			m_votesFor.add( participant );
		}
	}

	public void removeVote( final Participant participant )
	{
		m_votesFor.remove( participant );
	}

	public boolean hasVotedFor( final Participant participant )
	{
		return m_votesFor.contains( participant );
	}
}
