package com.darkrockstudios.apps.mafia.game;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.darkrockstudios.apps.mafia.BuildConfig;
import com.darkrockstudios.apps.mafia.MainActivity;
import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.SignInStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.rpc.GameSetupRPC;
import com.darkrockstudios.apps.mafia.game.rpc.Network;
import com.darkrockstudios.apps.mafia.game.rpc.StateChangeRPC;
import com.darkrockstudios.apps.mafia.misc.OsUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.GameHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Adam on 4/26/2014.
 */
public class GameController extends Fragment implements OnInvitationReceivedListener, RoomUpdateListener, RoomStatusUpdateListener, GameHelper.GameHelperListener
{
	private static final String TAG = GameController.class.getSimpleName();

	private static final String FRAGTAG = GameController.class.getName() + ".GAMECONTROLLER";

	protected MainActivity m_activity = null;

	// Request code for the "select players" UI
	public final static int RC_SELECT_PLAYERS   = 10000;
	public final static int RC_INVITATION_INBOX = 10001;
	public final static int RC_WAITING_ROOM     = 10002;

	public final static int MIN_PLAYERS = 2;
	public final static int MAX_PLAYERS = 8;

	private GameHelper m_gameHelper;
	private Network m_network;

	private Room m_room;

	private ClientType m_clientType;
	private World      m_world;
	private String     m_localPlayerId;

	public static GameController get( final FragmentManager fragmentManager )
	{
		final GameController gameController;

		Fragment gameControllerFragment = fragmentManager.findFragmentByTag( FRAGTAG );
		if( gameControllerFragment == null )
		{
			gameController = new GameController();
			fragmentManager.beginTransaction().add( gameController, FRAGTAG ).commit();
		}
		else
		{
			gameController = (GameController) gameControllerFragment;
		}

		return gameController;
	}

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof MainActivity )
		{
			m_activity = (MainActivity) activity;
		}
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );

		m_network = new Network( this );

		m_gameHelper = new GameHelper( m_activity, GameHelper.CLIENT_GAMES );
		m_gameHelper.enableDebugLog( BuildConfig.DEBUG );
		m_gameHelper.setup( this );

		m_gameHelper.onStart( m_activity );

		m_world = new World( this );
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		m_gameHelper.onStop();
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		m_activity = null;
	}

	@Override
	public void onActivityResult( final int requestCode, final int responseCode, final Intent data )
	{
		super.onActivityResult( requestCode, responseCode, data );
		m_gameHelper.onActivityResult( requestCode, responseCode, data );

		if( requestCode == GameController.RC_SELECT_PLAYERS )
		{
			if( responseCode != Activity.RESULT_OK )
			{
				// user canceled
				leaveGame();
				return;
			}

			// Get the invitee list
			Bundle extras = data.getExtras();
			final ArrayList<String> invitees = data.getStringArrayListExtra( Games.EXTRA_PLAYER_IDS );

			// create the room and specify a variant if appropriate
			RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
			roomConfigBuilder.addPlayersToInvite( invitees );
			RoomConfig roomConfig = roomConfigBuilder.build();
			Games.RealTimeMultiplayer.create( getApiClient(), roomConfig );

			// prevent screen from sleeping during handshake
			m_activity.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			setClientType( ClientType.MASTER );

			// go to loading screen while we wait for the proper callback
			Nav.gotoLoadingScreen( m_activity );
		}
		if( requestCode == GameController.RC_INVITATION_INBOX )
		{
			if( responseCode != Activity.RESULT_OK )
			{
				// canceled
				return;
			}

			// get the selected invitation
			Bundle extras = data.getExtras();
			Invitation invitation = extras.getParcelable( Multiplayer.EXTRA_INVITATION );

			// accept it!
			RoomConfig roomConfig = makeBasicRoomConfigBuilder()
					                        .setInvitationIdToAccept( invitation.getInvitationId() )
					                        .build();
			Games.RealTimeMultiplayer.join( getApiClient(), roomConfig );

			// prevent screen from sleeping during handshake
			m_activity.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			setClientType( ClientType.SLAVE );

			// go to loading screen while we wait for the proper callback
			Nav.gotoLoadingScreen( m_activity );
		}
		else if( requestCode == GameController.RC_WAITING_ROOM )
		{
			if( responseCode == Activity.RESULT_OK )
			{
				// (start game)
				Nav.gotoPreGameScreen( this );
			}
			else if( responseCode == Activity.RESULT_CANCELED )
			{
				// Waiting room was dismissed with the back button. The meaning of this
				// action is up to the game. You may choose to leave the room and cancel the
				// match, or do something else like minimize the waiting room and
				// continue to connect in the background.

				// in this example, we take the simple approach and just leave the room:
				Games.RealTimeMultiplayer.leave( getApiClient(), this, getRoom().getRoomId() );
				m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

				Nav.gotoInvitationsScreen( m_activity );
			}
			else if( responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM )
			{
				// player wants to leave the room.
				Games.RealTimeMultiplayer.leave( getApiClient(), this, getRoom().getRoomId() );
				m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

				Nav.gotoInvitationsScreen( m_activity );
			}
		}
	}

	public Network getNetwork()
	{
		return m_network;
	}

	// create a RoomConfigBuilder that's appropriate for your implementation
	private RoomConfig.Builder makeBasicRoomConfigBuilder()
	{
		return RoomConfig.builder( this )
		                 .setMessageReceivedListener( m_network )
		                 .setRoomStatusUpdateListener( this );
	}

	public int getNumPlayers()
	{
		int numPlayers = -1;

		if( m_room != null )
		{
			numPlayers = m_room.getParticipantIds().size();
		}

		return numPlayers;
	}

	public ClientType getClientType()
	{
		return m_clientType;
	}

	public void setClientType( final ClientType clientType )
	{
		m_clientType = clientType;
	}

	public World getWorld()
	{
		return m_world;
	}

	public void setRoom( final Room room )
	{
		m_room = room;
		m_localPlayerId = Games.Players.getCurrentPlayerId( getApiClient() );
	}

	public Room getRoom()
	{
		return m_room;
	}

	public GameHelper getGameHelper()
	{
		return m_gameHelper;
	}

	public GoogleApiClient getApiClient()
	{
		return m_gameHelper.getApiClient();
	}

	public void completeSetup( final GameSetup gameSetup )
	{
		assignRoles( gameSetup );

		GameSetupRPC setupMessage = new GameSetupRPC( gameSetup );
		m_network.executeRpc( setupMessage );

		setupGame( gameSetup );
	}

	private void assignRoles( final GameSetup gameSetup )
	{
		Random rand = new Random();
		List<Participant> unassignedPlayers = new ArrayList<>( m_room.getParticipants() );
		// First assign all mobsters
		for( int xx = 0; xx < gameSetup.getNumMobsters(); ++xx )
		{
			int ii = rand.nextInt( unassignedPlayers.size() - 1 );
			Participant participant = unassignedPlayers.get( ii );
			unassignedPlayers.remove( ii );

			PlayerSpecification playerSpec = new PlayerSpecification();
			playerSpec.m_participantId = participant.getParticipantId();
			playerSpec.m_role = PlayerRole.Mobster;

			gameSetup.addPlayer( playerSpec );
		}

		final int investigators = 1;
		// Next pick investigators
		if( unassignedPlayers.size() > 1 )
		{
			for( int xx = 0; xx < investigators; ++xx )
			{
				int ii = rand.nextInt( unassignedPlayers.size() - 1 );
				Participant participant = unassignedPlayers.get( ii );
				unassignedPlayers.remove( ii );

				PlayerSpecification playerSpec = new PlayerSpecification();
				playerSpec.m_participantId = participant.getParticipantId();
				playerSpec.m_role = PlayerRole.Investigator;
				gameSetup.addPlayer( playerSpec );
			}
		}
		// Only one player left, he must be an investigator
		else
		{
			Participant participant = unassignedPlayers.get( 0 );
			unassignedPlayers.remove( 0 );

			PlayerSpecification playerSpec = new PlayerSpecification();
			playerSpec.m_participantId = participant.getParticipantId();
			playerSpec.m_role = PlayerRole.Investigator;
			gameSetup.addPlayer( playerSpec );
		}

		// Make everyone who is left a citizen
		for( final Participant participant : unassignedPlayers )
		{
			PlayerSpecification playerSpec = new PlayerSpecification();
			playerSpec.m_participantId = participant.getParticipantId();
			playerSpec.m_role = PlayerRole.Citizen;
			gameSetup.addPlayer( playerSpec );
		}
	}

	public void setupGame( final GameSetup gameSetup )
	{
		m_world.setupGame( gameSetup );
	}

	public void markReady( final String participantId, final boolean ready )
	{
		PlayerSpecification playerSpec = m_world.getGameSetup().getPlayer( participantId );
		playerSpec.m_ready = ready;

		checkAllReady();
	}

	private void checkAllReady()
	{
		// Only the host should do something here
		if( m_clientType == ClientType.MASTER )
		{
			boolean allReady = true;
			for( final PlayerSpecification playerSpec : m_world.getGameSetup().getAllPlayers() )
			{
				if( !playerSpec.m_ready && playerSpec.m_alive )
				{
					allReady = false;
					break;
				}
			}

			// If everyone is marked as ready, then make sure we are done with the vote if there is one
			final Vote vote = getWorld().getCurrentVote();
			if( vote != null && allReady )
			{
				allReady = vote.isVoteComplete();
			}

			// All ready, start the game!
			if( allReady )
			{
				final World.State nextState = getNextState();

				StateChangeRPC stateChange = new StateChangeRPC( nextState );
				m_network.executeRpc( stateChange );
			}
		}
	}

	private World.State getNextState()
	{
		final World.State nextState;

		if( !m_world.isGameOver() )
		{
			switch( m_world.getState() )
			{
				case Setup:
					nextState = World.State.Pregame;
					break;
				case Pregame:
					nextState = World.State.Night;
					break;
				case Night:
					nextState = World.State.Day;
					break;
				case Day:
					nextState = World.State.Night;
					break;
				case End:
				default:
					nextState = World.State.Invalid;
					break;
			}
		}
		else
		{
			nextState = World.State.End;
		}

		return nextState;
	}

	public String getLocalParticipantId()
	{
		return m_room.getParticipantId( m_localPlayerId );
	}

	public PlayerSpecification getLocalPlayerSpec()
	{
		return m_world.getGameSetup().getPlayer( getLocalParticipantId() );
	}

	@Override
	public void onRoomCreated( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			m_activity.displayError( "Game Created" );
		}
		else
		{
			setRoom( room );
			m_activity.displayConfirm( "Game Created" );
			Nav.gotoWaitingRoom( room, this );
		}
	}

	@Override
	public void onJoinedRoom( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			m_activity.displayError( "Failed to join Game" );
			Nav.gotoInvitationsScreen( m_activity );
		}
		else
		{
			setRoom( room );
			m_activity.displayConfirm( "Game Joined" );
			Nav.gotoWaitingRoom( room, this );
		}
	}

	@Override
	public void onLeftRoom( final int i, final String s )
	{
		setRoom( null );
	}

	@Override
	public void onRoomConnected( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			m_activity.displayError( "Failed to connect to game" );
			Nav.gotoInvitationsScreen( m_activity );
		}
		else
		{
			m_activity.displayConfirm( "Connected to Game" );
		}
	}

	@Override
	public void onRoomConnecting( final Room room )
	{
		m_activity.displayInfo( "Connecting..." );
	}

	@Override
	public void onRoomAutoMatching( final Room room )
	{

	}

	@Override
	public void onPeerInvitedToRoom( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Invited player." );
	}

	@Override
	public void onPeerDeclined( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Invited player declined." );
	}

	@Override
	public void onPeerJoined( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Player Joined" );
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onPeerLeft( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Player Left" );

		// We are the last person in the game, end it
		if( numConnectedParticipants() <= 1 )
		{
			leaveGame();

			AlertDialog.Builder builder = new AlertDialog.Builder( m_activity );

			EndGameDialogListener dialogListener = new EndGameDialogListener();

			builder.setTitle( "Game Over" );
			builder.setMessage( "Everyone has left." );
			builder.setPositiveButton( "OK", dialogListener );
			if( OsUtils.hasJellyBeanMr1() )
			{
				builder.setOnDismissListener( dialogListener );
			}
			builder.setOnCancelListener( dialogListener );
			builder.setCancelable( false );

			m_activity.showDialog( builder.create() );
		}
	}

	private int numConnectedParticipants()
	{
		int numConnected = 0;

		for( final String participantIds : m_room.getParticipantIds() )
		{
			Participant participant = m_room.getParticipant( participantIds );

			if( participant.isConnectedToRoom() )
			{
				++numConnected;
			}
		}

		return numConnected;
	}

	@Override
	public void onConnectedToRoom( final Room room )
	{

	}

	@Override
	public void onDisconnectedFromRoom( final Room room )
	{

	}

	@Override
	public void onPeersConnected( final Room room, final List<String> strings )
	{

	}

	@Override
	public void onPeersDisconnected( final Room room, final List<String> strings )
	{

	}

	@Override
	public void onP2PConnected( final String s )
	{

	}

	@Override
	public void onP2PDisconnected( final String s )
	{

	}

	@Override
	public void onInvitationReceived( final Invitation invitation )
	{

	}

	@Override
	public void onInvitationRemoved( final String s )
	{

	}

	@Override
	public void onSignInFailed()
	{
		Log.d( TAG, "onSignInFailed" );
		BusProvider.get().post( new SignInStateChangedEvent( false ) );
	}

	@Override
	public void onSignInSucceeded()
	{
		Log.d( TAG, "onSignInSucceeded" );
		BusProvider.get().post( new SignInStateChangedEvent( true ) );
	}

	public boolean acceptInvitation()
	{
		final boolean accepted;
		if( m_gameHelper.getInvitationId() != null )
		{
			RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
			roomConfigBuilder.setInvitationIdToAccept( m_gameHelper.getInvitationId() );
			Games.RealTimeMultiplayer.join( getApiClient(), roomConfigBuilder.build() );

			m_gameHelper.clearInvitation();

			setClientType( ClientType.SLAVE );

			// prevent screen from sleeping during handshake
			m_activity.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// go to loading screen, a future callback will dump us into the waiting room
			Nav.gotoLoadingScreen( m_activity );

			accepted = true;
		}
		else
		{
			accepted = false;
		}

		return accepted;
	}

	public void leaveGame()
	{
		if( m_room != null )
		{
			Games.RealTimeMultiplayer.leave( getApiClient(), this, m_room.getRoomId() );
		}
	}

	private class EndGameDialogListener implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener
	{
		@Override
		public void onClick( final DialogInterface dialog, final int which )
		{
			resetGame();
		}

		@Override
		public void onDismiss( final DialogInterface dialog )
		{
			resetGame();
		}

		@Override
		public void onCancel( final DialogInterface dialog )
		{
			resetGame();
		}

		private void resetGame()
		{
			Intent intent = new Intent( m_activity, MainActivity.class );
			m_activity.startActivity( intent );
			m_activity.finish();
		}
	}
}
