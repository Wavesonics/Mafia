package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.ClientType;
import com.darkrockstudios.apps.mafia.game.PlayerSpecification;
import com.darkrockstudios.apps.mafia.game.World;
import com.darkrockstudios.apps.mafia.game.rpc.PlayerReadyRPC;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Adam on 4/27/2014.
 */
public class PreGameFragment extends BaseGameFragment
{
	private static final String FRAGTAG_GAME_SETUP = PreGameFragment.class.getSimpleName();

	@InjectView(R.id.testTextView1)
	TextView m_testView1;

	@InjectView(R.id.testTextView2)
	TextView m_testView2;

	@InjectView(R.id.ready_button)
	Button m_readyButton;

	private EventHandler m_eventHandler;

	public static PreGameFragment newInstance()
	{
		PreGameFragment fragment = new PreGameFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );

		if( m_gameController.getClientType() == ClientType.MASTER )
		{
			GameSetupFragment gameSetupFragment = GameSetupFragment.newInstance();
			gameSetupFragment.show( getFragmentManager(), FRAGTAG_GAME_SETUP );
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_pre, container, false );
		ButterKnife.inject( this, view );

		updateViews();

		return view;
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

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	private class EventHandler
	{
		@Subscribe
		public void onWorldStateChanged( final WorldStateChangedEvent event )
		{
			if( event.m_newState == World.State.Setup || event.m_newState == World.State.Pregame )
			{
				updateViews();
			}
		}
	}

	private void updateViews()
	{
		final World world = m_gameController.getWorld();

		m_readyButton.setVisibility( View.GONE );

		if( world.getState() == World.State.Setup )
		{
			m_testView1.setText( m_gameController.getClientType().toString() );
			m_testView2.setText( m_gameController.getWorld().getState().toString() );
		}
		else if( world.getState() == World.State.Pregame )
		{
			PlayerSpecification localPlayerSpec = m_gameController.getLocalPlayerSpec();

			m_testView1.setText( localPlayerSpec.m_role.toString() );

			final String description;
			switch( localPlayerSpec.m_role )
			{
				case Citizen:
					description = "You are a citizen. Try not to die. But hey, at lease you get to lynch people!";
					break;
				case Investigator:
					description = "You are an Investigator. Investigate people at night!";
					break;
				case Mobster:
					description = "You are a Mobster. Kill bitches at night!";
					break;
				default:
					description = "ERROR";
					break;
			}

			m_testView2.setText( description );

			m_readyButton.setVisibility( View.VISIBLE );
		}
		else if( world.getState() == World.State.Night )
		{
			m_testView1.setText( "It's night time" );
			m_testView2.setText( "Do night time actions!" );
		}
		else if( world.getState() == World.State.Day )
		{
			m_testView1.setText( "It's day time" );
			m_testView2.setText( "Let's lynch some bitches!" );
		}
		else if( world.getState() == World.State.End )
		{
			m_testView1.setText( "GAME OVER" );
			m_testView2.setText( "Oh well" );
		}
	}

	@OnClick(R.id.ready_button)
	public void onReadyClicked( final View view )
	{
		PlayerReadyRPC playerReady = new PlayerReadyRPC( m_gameController, true );
		m_gameController.getNetwork().executeRpc( playerReady );

		m_readyButton.setEnabled( false );
	}
}
