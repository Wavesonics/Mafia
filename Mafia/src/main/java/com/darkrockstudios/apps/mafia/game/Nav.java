package com.darkrockstudios.apps.mafia.game;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.darkrockstudios.apps.mafia.MainActivity;
import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.fragments.BaseGameFragment;
import com.darkrockstudios.apps.mafia.fragments.GameDayFragment;
import com.darkrockstudios.apps.mafia.fragments.GameDeadFragment;
import com.darkrockstudios.apps.mafia.fragments.GameEndFragment;
import com.darkrockstudios.apps.mafia.fragments.GameNightCitizenFragment;
import com.darkrockstudios.apps.mafia.fragments.GameNightInvestigatorFragment;
import com.darkrockstudios.apps.mafia.fragments.GameNightMobsterFragment;
import com.darkrockstudios.apps.mafia.fragments.InvitationsFragment;
import com.darkrockstudios.apps.mafia.fragments.LoadingFragment;
import com.darkrockstudios.apps.mafia.fragments.PreGameFragment;
import com.darkrockstudios.apps.mafia.fragments.SignInFragment;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.Room;

/**
 * Created by Adam on 5/4/2014.
 */
public final class Nav
{
	public final static String FRAGTAG_SIGNIN      = MainActivity.class.getPackage() + ".SIGNIN";
	public final static String FRAGTAG_INVITATIONS = MainActivity.class.getPackage() + ".INVITATIONS";
	public final static String FRAGTAG_GAME        = MainActivity.class.getPackage() + ".GAME";
	public final static String FRAGTAG_LOADING     = MainActivity.class.getPackage() + ".LOADING";

	public static void gotoSignInScreen( final Activity activity )
	{
		SignInFragment fragment = SignInFragment.newInstance();
		activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_SIGNIN ).commit();
	}

	public static void gotoInvitationsScreen( final Activity activity )
	{
		InvitationsFragment fragment = InvitationsFragment.newInstance();
		activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_INVITATIONS )
		        .commit();
	}

	public static void gotoInvitationInbox( final GameController gameController )
	{
		// launch the intent to show the invitation inbox screen
		Intent intent = Games.Invitations.getInvitationInboxIntent( gameController.getApiClient() );
		gameController.startActivityForResult( intent, GameController.RC_INVITATION_INBOX );
	}

	public static void gotoWaitingRoom( final Room room, final GameController gameController )
	{
		Intent i = Games.RealTimeMultiplayer
				           .getWaitingRoomIntent( gameController.getApiClient(), room, room.getParticipantIds().size() );
		gameController.startActivityForResult( i, GameController.RC_WAITING_ROOM );
	}

	public static void gotoSelectPlayers( final GameController gameController )
	{
		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent( gameController.getApiClient(),
		                                                                    GameController.MIN_PLAYERS - 1,
		                                                                    GameController.MAX_PLAYERS - 1 );
		gameController.startActivityForResult( intent, GameController.RC_SELECT_PLAYERS );
	}

	public static void gotoLoadingScreen( final Activity activity )
	{
		LoadingFragment fragment = LoadingFragment.newInstance();
		activity.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_LOADING ).commit();
	}

	public static void gotoPreGameScreen( final GameController gameController )
	{
		PreGameFragment fragment = PreGameFragment.newInstance();
		gotoGameFragment( gameController, fragment );
	}

	public static void gotoNightScreen( final GameController gameController )
	{
		final PlayerSpecification localPlayer = gameController.getLocalPlayerSpec();

		BaseGameFragment fragment;
		switch( localPlayer.m_role )
		{
			case Citizen:
				fragment = GameNightCitizenFragment.newInstance();
				break;
			case Mobster:
				fragment = GameNightMobsterFragment.newInstance();
				break;
			case Investigator:
				fragment = GameNightInvestigatorFragment.newInstance();
				break;
			default:
				throw new IllegalStateException( "Tried to create Night fragment, but local player role was invalid!" );
		}

		gotoGameFragment( gameController, fragment );
	}

	public static void gotoDayScreen( final GameController gameController, final String voteWinnerId )
	{
		final GameDayFragment fragment = GameDayFragment.newInstance( voteWinnerId );
		gotoGameFragment( gameController, fragment );
	}

	public static void gotoEndScreen( final GameController gameController )
	{
		final GameEndFragment fragment = GameEndFragment.newInstance();
		gotoGameFragment( gameController, fragment );
	}

	public static void gotoDeadScreen( final GameController gameController )
	{
		final GameDeadFragment fragment = GameDeadFragment.newInstance();
		gotoGameFragment( gameController, fragment );
	}

	private static void gotoGameFragment( final GameController gameController, final Fragment fragment )
	{
		gameController.getFragmentManager().beginTransaction().replace( R.id.container, fragment, FRAGTAG_GAME ).commit();
	}
}