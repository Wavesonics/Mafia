package com.darkrockstudios.apps.mafia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.darkrockstudios.apps.mafia.game.GameController;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity implements GameSetupHandler, DialogInterface.OnClickListener
{
	private GameController m_gameController;

	private List<Dialog> m_dialogs = new ArrayList<>();

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		ButterKnife.inject( this );

		m_gameController = GameController.get( getFragmentManager() );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		for( final Dialog dialog : m_dialogs )
		{
			dialog.dismiss();
		}
		m_dialogs.clear();
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
		final boolean selected;

		final int id = item.getItemId();
		if( id == R.id.action_settings )
		{
			selected = true;
		}
		else
		{
			selected = super.onOptionsItemSelected( item );
		}

		return selected;
	}

	@Override
	public void onBackPressed()
	{
		if( m_gameController != null && m_gameController.getRoom() != null )
		{
			AlertDialog.Builder builder = new AlertDialog.Builder( this );

			builder.setTitle( "Leave Game?" );
			builder.setMessage( "You will not be able to rejoin this game!" );
			builder.setPositiveButton( "Leave", this );
			builder.setNegativeButton( "Stay", this );
			builder.setCancelable( false );

			showDialog( builder.create() );
		}
		else
		{
			super.onBackPressed();
		}
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

	public void showDialog( final Dialog dialog )
	{
		m_dialogs.add( dialog );
		dialog.show();
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
	public void onClick( final DialogInterface dialog, final int which )
	{
		if( which == Dialog.BUTTON_POSITIVE )
		{
			m_gameController.leaveGame();

			Toast.makeText( this, "Leaving game...", Toast.LENGTH_LONG ).show();
			finish();
		}
	}
}
