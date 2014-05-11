package com.darkrockstudios.apps.mafia.game;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;

/**
 * Created by Adam on 4/27/2014.
 */
public class World
{
	public static enum State
	{
		Setup,
		Pregame,
		Night,
		Day,
		End,
		Invalid
	}

	private final GameController m_gameController;
	private State m_state;
	private GameSetup m_gameSetup;

	public World( final GameController gameController )
	{
		m_gameController = gameController;
		m_state = State.Setup;
	}

	public State getState()
	{
		return m_state;
	}

	private Vote m_currentVote;

	public void changeState( final State state )
	{
		m_state = state;
		resetPlayerReadyStates();

		String voteWinnerId = null;

		if( m_state == State.Night )
		{
			setupMobsterVote();
		}
		else if( m_state == State.Day )
		{
			// Kill the player who won the Mobster vote
			Participant voteWinner = m_currentVote.getVoteWinner();
			voteWinnerId = voteWinner.getParticipantId();
			m_gameSetup.killPlayer( voteWinnerId );

			setupLynchingVote();
		}
		else
		{
			m_currentVote = null;
		}

		BusProvider.get().post( new WorldStateChangedEvent( m_state, voteWinnerId ) );
	}

	public Vote getCurrentVote()
	{
		return m_currentVote;
	}

	private void setupLynchingVote()
	{
		m_currentVote = new Vote();

		final Room room = m_gameController.getRoom();
		for( final PlayerSpecification playerSpec : m_gameSetup.getAllPlayers() )
		{
			if( playerSpec.m_alive )
			{
				m_currentVote.addNominee( room.getParticipant( playerSpec.m_participantId ) );
				m_currentVote.addVoter( room.getParticipant( playerSpec.m_participantId ) );
			}
		}
	}

	private void setupMobsterVote()
	{
		m_currentVote = new Vote();

		final Room room = m_gameController.getRoom();
		for( final PlayerSpecification playerSpec : m_gameSetup.getAllPlayers() )
		{
			if( playerSpec.m_role != PlayerRole.Mobster )
			{
				m_currentVote.addNominee( room.getParticipant( playerSpec.m_participantId ) );
			}
			else
			{
				m_currentVote.addVoter( room.getParticipant( playerSpec.m_participantId ) );
			}
		}
	}

	private void resetPlayerReadyStates()
	{
		for( final PlayerSpecification playerSpec : m_gameSetup.getAllPlayers() )
		{
			playerSpec.m_ready = false;
		}
	}

	public void setupGame( final GameSetup gameSetup )
	{
		m_gameSetup = gameSetup;
		changeState( World.State.Pregame );
	}

	public GameSetup getGameSetup()
	{
		return m_gameSetup;
	}
}
