package com.darkrockstudios.apps.mafia;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity implements GameSetupHandler, GameControllerProvider
{
	private GameController m_gameController;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		ButterKnife.inject( this );

		m_gameController = GameController.get( getFragmentManager() );
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

	public void displayError( final int messageResource )
	{
		displayError( getString( messageResource ) );
	}

	public void displayError( final String message )
	{
		displayMessage( message, Style.ALERT );
	}

	public void displayInfo( final int messageResource )
	{
		displayInfo( getString( messageResource ) );
	}

	public void displayInfo( final String message )
	{
		displayMessage( message, Style.INFO );
	}

	public void displayConfirm( final int messageResource )
	{
		displayConfirm( getString( messageResource ) );
	}

	public void displayConfirm( final String message )
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
		m_gameController.gotoSelectPlayers();
	}

	@Override
	public void joinGame()
	{
		m_gameController.gotoInvitationInbox();
	}

	@Override
	public GameController getGameController()
	{
		return m_gameController;
	}
}
