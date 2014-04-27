package com.darkrockstudios.apps.mafia.game;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.darkrockstudios.apps.mafia.GameFragment;
import com.darkrockstudios.apps.mafia.IntroActivity;
import com.darkrockstudios.apps.mafia.InvitationsFragment;
import com.darkrockstudios.apps.mafia.LoadingFragment;
import com.darkrockstudios.apps.mafia.MainActivity;
import com.darkrockstudios.apps.mafia.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.GameHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 4/26/2014.
 */
public class GameController extends Fragment implements OnInvitationReceivedListener, RoomUpdateListener, RoomStatusUpdateListener, RealTimeMessageReceivedListener, GameHelper.GameHelperListener, RealTimeMultiplayer.ReliableMessageSentCallback
{
	private static final String FRAGTAG             = GameController.class.getName() + ".GAMECONTROLLER";
	private final static String FRAGTAG_INVITATIONS = MainActivity.class.getPackage() + ".INVITATIONS";
	private final static String FRAGTAG_GAME        = MainActivity.class.getPackage() + ".GAME";
	private final static String FRAGTAG_LOADING = MainActivity.class.getPackage() + ".LOADING";

	protected MainActivity m_activity = null;

	// Request code for the "select players" UI
	public final static int RC_SELECT_PLAYERS   = 10000;
	public final static int RC_INVITATION_INBOX = 10001;
	public final static int RC_WAITING_ROOM     = 10002;

	public final static int MIN_PLAYERS = 2;
	public final static int MAX_PLAYERS = 8;

	private GameHelper m_gameHelper;

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
	public void onDestroy()
	{
		Log.d( "mab", this + ": onDestroy()" );
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
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );

		m_gameHelper = new GameHelper( m_activity, GameHelper.CLIENT_GAMES );
		m_gameHelper.setup( this );

		m_world = new World();

		m_gameHelper.onStart( m_activity );

		gotoInvitationsScreen();
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
			gotoLoadingScreen();
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
			gotoLoadingScreen();
		}
		else if( requestCode == GameController.RC_WAITING_ROOM )
		{
			if( responseCode == Activity.RESULT_OK )
			{
				// (start game)
				gotoGameScreen();
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
			}
			else if( responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM )
			{
				// player wants to leave the room.
				Games.RealTimeMultiplayer.leave( getApiClient(), this, getRoom().getRoomId() );
				m_activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
			}
		}
	}

	// create a RoomConfigBuilder that's appropriate for your implementation
	private RoomConfig.Builder makeBasicRoomConfigBuilder()
	{
		return RoomConfig.builder( this )
		                 .setMessageReceivedListener( this )
		                 .setRoomStatusUpdateListener( this );
	}

	public ClientType geClientType()
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

	public void gotoInvitationsScreen()
	{
		InvitationsFragment fragment = InvitationsFragment.newInstance();
		m_activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_INVITATIONS ).commit();
	}

	public void gotoInvitationInbox()
	{
		// launch the intent to show the invitation inbox screen
		Intent intent = Games.Invitations.getInvitationInboxIntent( getApiClient() );
		startActivityForResult( intent, GameController.RC_INVITATION_INBOX );
	}

	public void gotoWaitingRoom( final Room room )
	{
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent( getApiClient(), room, GameController.MIN_PLAYERS );
		startActivityForResult( i, GameController.RC_WAITING_ROOM );
	}

	public void gotoSelectPlayers()
	{
		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent( getApiClient(),
		                                                                    GameController.MIN_PLAYERS - 1,
		                                                                    GameController.MAX_PLAYERS - 1 );
		startActivityForResult( intent, GameController.RC_SELECT_PLAYERS );
	}

	public void gotoLoadingScreen()
	{
		LoadingFragment fragment = LoadingFragment.newInstance();
		m_activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_LOADING ).commit();
	}

	public void gotoGameScreen()
	{
		GameFragment fragment = GameFragment.newInstance();
		m_activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_GAME ).commit();
	}

	public void completeSetup()
	{
		byte[] b = new byte[ 1 ];
		b[ 0 ] = (byte) World.State.Pregame.ordinal();

		broadcastMessage( b );
		m_world.setState( World.State.Pregame );
	}

	private void broadcastMessage( final byte[] message )
	{
		final String localParticipantId = m_room.getParticipantId( m_localPlayerId );
		for( final Participant p : m_room.getParticipants() )
		{
			if( !p.getParticipantId().equals( localParticipantId ) )
			{
				Games.RealTimeMultiplayer.sendReliableMessage( getApiClient(),
				                                               this,
				                                               message,
				                                               m_room.getRoomId(),
				                                               p.getParticipantId() );
			}
		}
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
			gotoWaitingRoom( room );
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
		}
		else
		{
			setRoom( room );
			m_activity.displayConfirm( "Game Joined" );
			gotoWaitingRoom( room );
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

	@Override
	public void onPeerLeft( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Player Left" );
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
		// If we fail sign in here, go back to the IntroActivity
		Intent intent = new Intent( m_activity, IntroActivity.class );
		startActivity( intent );
		m_activity.finish();
	}

	@Override
	public void onSignInSucceeded()
	{

	}

	@Override
	public void onRealTimeMessageReceived( final RealTimeMessage realTimeMessage )
	{
		byte[] message = realTimeMessage.getMessageData();
		m_world.setState( World.State.values()[ message[ 0 ] ] );
	}

	@Override
	public void onRealTimeMessageSent( final int statusCode, final int tokenId, final String recipientParticipantId )
	{

	}

	public void leaveGame()
	{
		if( m_room != null )
		{
			Games.RealTimeMultiplayer.leave( getApiClient(), this, m_room.getRoomId() );
		}
	}
}
