package com.darkrockstudios.apps.mafia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.example.games.basegameutils.BaseGameActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends BaseGameActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// set requested clients (games and cloud save)
		setRequestedClients(BaseGameActivity.CLIENT_GAMES);

		// enable debug log, if applicable
		if (BuildConfig.DEBUG) {
			enableDebugLog(true, "MyActivity");
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_intro);
		ButterKnife.inject( this );
	}

	@Override
	public void onSignInFailed()
	{

	}

	@Override
	public void onSignInSucceeded()
	{
		Intent intent = new Intent( this, MainMenuActivity.class );
		startActivity( intent );
	}

	@OnClick( R.id.INTRO_sign_in_button )
	public void onSignInClicked( final View view )
	{
		beginUserInitiatedSignIn();
	}
}
