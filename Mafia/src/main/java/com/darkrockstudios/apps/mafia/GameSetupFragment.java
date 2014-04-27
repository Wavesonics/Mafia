package com.darkrockstudios.apps.mafia;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameSetupFragment extends BaseGameFragment
{
	public static GameSetupFragment newInstance()
	{
		GameSetupFragment fragment = new GameSetupFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_setup, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public Dialog onCreateDialog( final Bundle savedInstanceState )
	{
		Dialog dialog = super.onCreateDialog( savedInstanceState );
		dialog.setTitle( R.string.GAME_SETUP_title );
		setCancelable( false );

		return dialog;
	}

	@OnClick(R.id.testButton)
	public void onSetupCompleteClicked( final View view )
	{
		if( m_gameController != null )
		{
			dismiss();
			m_gameController.completeSetup();
		}
	}
}
