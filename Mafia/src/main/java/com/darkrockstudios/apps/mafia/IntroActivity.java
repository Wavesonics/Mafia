package com.darkrockstudios.apps.mafia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.example.games.basegameutils.BaseGameActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IntroActivity extends BaseGameActivity
{
	@InjectView(R.id.INTRO_logo)
	View m_logo;

	@InjectView(R.id.INTRO_sign_in_container)
	View m_signInContainer;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		// set requested clients (games and cloud save)
		setRequestedClients( BaseGameActivity.CLIENT_GAMES );

		// enable debug log, if applicable
		if( BuildConfig.DEBUG )
		{
			enableDebugLog( true, "MyActivity" );
		}

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_intro );
		ButterKnife.inject( this );
	}

	@Override
	public void onSignInFailed()
	{
		Crouton.makeText( this, R.string.INTRO_sign_in_failed, Style.ALERT ).show();

		m_logo.setVisibility( View.GONE );
		m_signInContainer.setVisibility( View.VISIBLE );
	}

	@Override
	public void onSignInSucceeded()
	{
		Intent intent = new Intent( this, MainActivity.class );
		startActivity( intent );
		finish();
	}

	@OnClick(R.id.INTRO_sign_in_button)
	public void onSignInClicked( final View view )
	{
		beginUserInitiatedSignIn();
	}
}
