package com.darkrockstudios.apps.mafia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends BaseGameActivity implements RoomUpdateListener, RoomStatusUpdateListener, GameSetupHandler, GameControllerProvider
{
	private final static String FRAGTAG_INVITATIONS = MainActivity.class.getPackage() + ".INVITATIONS";
	private final static String FRAGTAG_GAME        = MainActivity.class.getPackage() + ".GAME";

	private GameController m_gameController;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_gameController = new GameController( getGameHelper(), getApiClient() );

		setContentView( R.layout.activity_main );
		ButterKnife.inject( this );

		gotoInvitationsScreen();
	}

	private void gotoInvitationsScreen()
	{
		InvitationsFragment fragment = InvitationsFragment.newInstance();
		getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_INVITATIONS ).commit();
	}

	private void gotoInvitationInbox()
	{
		// launch the intent to show the invitation inbox screen
		Intent intent = Games.Invitations.getInvitationInboxIntent( getApiClient() );
		startActivityForResult( intent, GameController.RC_INVITATION_INBOX );
	}

	private void gotoWaitingRoom( final Room room )
	{
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent( getApiClient(), room, GameController.MIN_PLAYERS );
		startActivityForResult( i, GameController.RC_WAITING_ROOM );
	}

	public void gotoSelectPlayers()
	{
		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent( m_gameController.getApiClient(),
		                                                                    GameController.MIN_PLAYERS - 1,
		                                                                    GameController.MAX_PLAYERS - 1 );
		startActivityForResult( intent, GameController.RC_SELECT_PLAYERS );
	}

	private void gotoGameScreen()
	{
		GameFragment fragment = GameFragment.newInstance();
		getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_GAME ).commit();
	}

	@Override
	public boolean onCreateOptionsMenu( final Menu menu )
	{
		getMenuInflater().inflate( R.menu.main_menu, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		int id = item.getItemId();
		if( id == R.id.action_settings )
		{
			return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onSignInFailed()
	{
		// If we fail sign in here, go back to the IntroActivity
		Intent intent = new Intent( this, IntroActivity.class );
		startActivity( intent );
		finish();
	}

	@Override
	public void onSignInSucceeded()
	{

	}

	@Override
	public void onActivityResult( final int request, final int response, final Intent data )
	{
		super.onActivityResult( request, response, data );

		if( request == GameController.RC_SELECT_PLAYERS )
		{
			if( response != Activity.RESULT_OK )
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
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
		else if( request == GameController.RC_WAITING_ROOM )
		{
			if( response == Activity.RESULT_OK )
			{
				// (start game)
				gotoGameScreen();
			}
			else if( response == Activity.RESULT_CANCELED )
			{
				// Waiting room was dismissed with the back button. The meaning of this
				// action is up to the game. You may choose to leave the room and cancel the
				// match, or do something else like minimize the waiting room and
				// continue to connect in the background.

				// in this example, we take the simple approach and just leave the room:
				Games.RealTimeMultiplayer.leave( getApiClient(), this, m_gameController.getRoom().getRoomId() );
				getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
			}
			else if( response == GamesActivityResultCodes.RESULT_LEFT_ROOM )
			{
				// player wants to leave the room.
				Games.RealTimeMultiplayer.leave( getApiClient(), this, m_gameController.getRoom().getRoomId() );
				getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
			}
		}
		if( request == GameController.RC_INVITATION_INBOX )
		{
			if( response != Activity.RESULT_OK )
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
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// go to game screen
		}
	}

	// create a RoomConfigBuilder that's appropriate for your implementation
	private RoomConfig.Builder makeBasicRoomConfigBuilder()
	{
		return RoomConfig.builder( this )
		                 .setMessageReceivedListener( m_gameController )
		                 .setRoomStatusUpdateListener( this );
	}

	@Override
	public void onRoomCreated( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			displayError( "Game Created" );
		}
		else
		{
			m_gameController.setRoom( room );
			displayConfirm( "Game Created" );
			gotoWaitingRoom( room );
		}
	}

	@Override
	public void onJoinedRoom( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			displayError( "Failed to join Game" );
		}
		else
		{
			m_gameController.setRoom( room );
			displayConfirm( "Game Joined" );
			gotoWaitingRoom( room );
		}
	}

	@Override
	public void onLeftRoom( final int i, final String s )
	{
		m_gameController.setRoom( null );
	}

	@Override
	public void onRoomConnected( final int statusCode, final Room room )
	{
		if( statusCode != GamesStatusCodes.STATUS_OK )
		{
			// let screen go to sleep
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

			// show error message, return to main screen.
			Crouton.makeText( this, "Connected to Game", Style.CONFIRM ).show();
		}
	}

	@Override
	public void onRoomConnecting( final Room room )
	{
		displayInfo( "Connecting..." );
	}

	@Override
	public void onRoomAutoMatching( final Room room )
	{

	}

	@Override
	public void onPeerInvitedToRoom( final Room room, final List<String> peers )
	{
		displayInfo( "Invited player." );
	}

	@Override
	public void onPeerDeclined( final Room room, final List<String> peers )
	{
		displayInfo( "Invited player declined." );
	}

	@Override
	public void onPeerJoined( final Room room, final List<String> peers )
	{
		displayInfo( "Player Joined" );
	}

	@Override
	public void onPeerLeft( final Room room, final List<String> peers )
	{
		displayInfo( "Player Left" );
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

	private void displayError( final int messageResource )
	{
		displayError( getString( messageResource ) );
	}

	private void displayError( final String message )
	{
		displayMessage( message, Style.ALERT );
	}

	private void displayInfo( final int messageResource )
	{
		displayInfo( getString( messageResource ) );
	}

	private void displayInfo( final String message )
	{
		displayMessage( message, Style.INFO );
	}

	private void displayConfirm( final int messageResource )
	{
		displayConfirm( getString( messageResource ) );
	}

	private void displayConfirm( final String message )
	{
		displayMessage( message, Style.CONFIRM );
	}

	private void displayMessage( final String message, final Style style )
	{
		Crouton.makeText( this, message, style ).show();
	}

	@Override
	public void createGame()
	{
		gotoSelectPlayers();
	}

	@Override
	public void joinGame()
	{
		gotoInvitationInbox();
	}

	@Override
	public GameController getGameController()
	{
		return m_gameController;
	}
}
