package com.darkrockstudios.apps.mafia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.WorldStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.ClientType;
import com.darkrockstudios.apps.mafia.game.GameController;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameFragment extends BaseGameFragment
{
	@InjectView(R.id.testTextView1)
	TextView m_testView1;

	@InjectView(R.id.testTextView2)
	TextView m_testView2;

	@InjectView(R.id.testButton)
	Button m_testButton;

	public static GameFragment newInstance()
	{
		GameFragment fragment = new GameFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
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

	@OnClick(R.id.testButton)
	public void onSetupCompleteClicked( final View view )
	{
		GameController gameController = getGameController();
		if( gameController != null )
		{
			gameController.completeSetup();
		}
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

		if( m_gameController.geClientType() == ClientType.MASTER )
		{
			m_testButton.setVisibility( View.VISIBLE );
		}
		else
		{
			m_testButton.setVisibility( View.GONE );
		}
	}
}
