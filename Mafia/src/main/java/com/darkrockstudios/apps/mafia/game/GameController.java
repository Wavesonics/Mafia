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

import com.darkrockstudios.apps.mafia.GameFragment;
import com.darkrockstudios.apps.mafia.IntroActivity;
import com.darkrockstudios.apps.mafia.InvitationsFragment;
import com.darkrockstudios.apps.mafia.LoadingFragment;
import com.darkrockstudios.apps.mafia.MainActivity;
import com.darkrockstudios.apps.mafia.OsUtils;
import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.message.GameSetupMessage;
import com.darkrockstudios.apps.mafia.game.message.Message;
import com.darkrockstudios.apps.mafia.game.message.PlayerReadyMessage;
import com.darkrockstudios.apps.mafia.game.message.StateChangeMessage;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Adam on 4/26/2014.
 */
public class GameController extends Fragment implements OnInvitationReceivedListener, RoomUpdateListener, RoomStatusUpdateListener, RealTimeMessageReceivedListener, GameHelper.GameHelperListener, RealTimeMultiplayer.ReliableMessageSentCallback
{
	private static final String TAG = GameController.class.getSimpleName();

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

	public int getNumPlayers()
	{
		int numPlayers = -1;

		if( m_room != null )
		{
			numPlayers = m_room.getParticipantIds().size();
		}

		return numPlayers;
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
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent( getApiClient(), room, room.getParticipantIds().size() );
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

	public void completeSetup( final GameSetup gameSetup )
	{
		assignRoles( gameSetup );

		GameSetupMessage setupMessage = new GameSetupMessage( gameSetup );
		broadcastMessage( setupMessage );

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

	private void setupGame( final GameSetup gameSetup )
	{
		m_world.setupGame( gameSetup );
	}

	public void notifyReady()
	{
		PlayerReadyMessage readyMessage = new PlayerReadyMessage( getLocalParticipantId(), true );
		broadcastMessage( readyMessage );

		// Server must mark him self locally
		if( m_clientType == ClientType.MASTER )
		{
			markReady( getLocalParticipantId(), true );
		}
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
				if( !playerSpec.m_ready )
				{
					allReady = false;
					break;
				}
			}

			// All ready, start the game!
			if( allReady )
			{
				StateChangeMessage stateChangeMessage = new StateChangeMessage( World.State.Night );
				broadcastMessage( stateChangeMessage );

				m_world.setState( World.State.Night );
			}
		}
	}

	public String getLocalParticipantId()
	{
		return m_room.getParticipantId( m_localPlayerId );
	}

	public PlayerSpecification getLocalPlayerSpec()
	{
		return m_world.getGameSetup().getPlayer( getLocalParticipantId() );
	}

	private void broadcastMessage( final Message message )
	{
		try
		{
			byte[] messageData = messageToBytes( message );

			final String localParticipantId = m_room.getParticipantId( m_localPlayerId );
			for( final Participant p : m_room.getParticipants() )
			{
				if( !p.getParticipantId().equals( localParticipantId ) )
				{
					Games.RealTimeMultiplayer.sendReliableMessage( getApiClient(),
					                                               this,
					                                               messageData,
					                                               m_room.getRoomId(),
					                                               p.getParticipantId() );
				}
			}
		}
		catch( final IOException e )
		{
			e.printStackTrace();
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onPeerLeft( final Room room, final List<String> peers )
	{
		m_activity.displayInfo( "Player Left" );

		// We are the last person in the game, end it
		if( peers.size() <= 1 )
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
		byte[] messageData = realTimeMessage.getMessageData();
		Message message = bytesToMessage( messageData );

		if( message instanceof GameSetupMessage )
		{
			Log.d( TAG, "Game setup received" );

			GameSetupMessage gameSetupMessage = (GameSetupMessage) message;
			setupGame( gameSetupMessage.m_gameSetup );
		}
		else if( message instanceof PlayerReadyMessage )
		{
			PlayerReadyMessage readyMessage = (PlayerReadyMessage) message;
			markReady( realTimeMessage.getSenderParticipantId(), readyMessage.m_ready );
		}
		else if( message instanceof StateChangeMessage )
		{
			StateChangeMessage stateChangeMessage = (StateChangeMessage) message;
			m_world.setState( stateChangeMessage.m_state );
		}
	}

	@Override
	public void onRealTimeMessageSent( final int statusCode, final int tokenId, final String recipientParticipantId )
	{

	}

	private byte[] messageToBytes( final Message message ) throws IOException
	{
		byte[] bytes;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream( bos );
		try
		{
			out.writeObject( message );
			bytes = bos.toByteArray();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch( final IOException ignore )
			{

			}
		}

		return bytes;
	}

	private Message bytesToMessage( final byte[] messageData )
	{
		Message message = null;

		ByteArrayInputStream bis = new ByteArrayInputStream( messageData );
		ObjectInput in = null;
		try
		{
			in = new ObjectInputStream( bis );
			Object obj = in.readObject();

			if( obj instanceof Message )
			{
				message = (Message) obj;
			}
		}
		catch( ClassNotFoundException | IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch( final IOException ignore )
			{

			}
			try
			{
				if( in != null )
				{
					in.close();
				}
			}
			catch( final IOException ignore )
			{

			}
		}

		return message;
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
