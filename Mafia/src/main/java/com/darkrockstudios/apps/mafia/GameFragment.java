package com.darkrockstudios.apps.mafia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.ClientType;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameFragment extends BaseGameFragment
{
	private static final String FRAGTAG_GAME_SETUP = GameFragment.class.getSimpleName();

	@InjectView(R.id.testTextView1)
	TextView m_testView1;

	@InjectView(R.id.testTextView2)
	TextView m_testView2;

	public static GameFragment newInstance()
	{
		GameFragment fragment = new GameFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );

		if( m_gameController.geClientType() == ClientType.MASTER )
		{
			GameSetupFragment gameSetupFragment = GameSetupFragment.newInstance();
			gameSetupFragment.show( getFragmentManager(), FRAGTAG_GAME_SETUP );
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game, container, false );
		ButterKnife.inject( this, view );

		updateViews();

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		BusProvider.get().register( this );
	}

	@Override
	public void onPause()
	{
		super.onPause();
		BusProvider.get().unregister( this );
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	@Subscribe
	public void onWorldStateChanged( final WorldStateChangedEvent event )
	{
		updateViews();
	}

	private void updateViews()
	{
		m_testView1.setText( m_gameController.geClientType().toString() );
		m_testView2.setText( m_gameController.getWorld().getState().toString() );
	}
}
