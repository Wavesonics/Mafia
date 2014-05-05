package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.eventbus.BusProvider;
import com.darkrockstudios.apps.mafia.eventbus.SignInStateChangedEvent;
import com.darkrockstudios.apps.mafia.game.GameController;
import com.darkrockstudios.apps.mafia.game.Nav;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by adam on 5/4/14.
 */
public class SignInFragment extends BaseGameFragment
{
	private EventHandler m_evenHandler;

	@InjectView(R.id.INTRO_logo)
	View m_logo;

	@InjectView(R.id.INTRO_sign_in_container)
	View m_signInContainer;

	public static SignInFragment newInstance()
	{
		SignInFragment fragment = new SignInFragment();
		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_signin, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		m_evenHandler = new EventHandler();
		BusProvider.get().register( m_evenHandler );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if( m_gameController.getGameHelper().isSignedIn() )
		{
			Nav.gotoInvitationsScreen( getActivity() );
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		BusProvider.get().unregister( m_evenHandler );
		m_evenHandler = null;
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
		public void onSignInStateChanged( final SignInStateChangedEvent event )
		{
			if( !event.m_signedIn )
			{
				Crouton.makeText( getActivity(), R.string.INTRO_sign_in_failed, Style.ALERT ).show();

				m_logo.setVisibility( View.GONE );
				m_signInContainer.setVisibility( View.VISIBLE );
			}
		}
	}

	@OnClick(R.id.INTRO_sign_in_button)
	public void onSignInClicked( final View view )
	{
		GameController gameController = GameController.get( getFragmentManager() );
		gameController.getGameHelper().beginUserInitiatedSignIn();
	}
}
