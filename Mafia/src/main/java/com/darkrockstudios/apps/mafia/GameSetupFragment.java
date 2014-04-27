package com.darkrockstudios.apps.mafia;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.darkrockstudios.apps.mafia.game.GameSetup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Adam on 4/27/2014.
 */
public class GameSetupFragment extends BaseGameFragment
{
	private GameSetup m_gameSetup;

	@InjectView(R.id.GAME_SETUP_num_mobsters)
	TextView m_numMobstersView;

	public static GameSetupFragment newInstance()
	{
		GameSetupFragment fragment = new GameSetupFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_gameSetup = new GameSetup();
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

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	@OnClick(R.id.testButton)
	public void onSetupCompleteClicked( final View view )
	{
		if( m_gameController != null )
		{
			dismiss();
			m_gameController.completeSetup( m_gameSetup );
		}
	}

	@OnClick(R.id.GAME_SETUP_num_mobsters_decrease_button)
	public void onDecreaseMobstersClicked( final View view )
	{
		m_gameSetup.decreaseMobsters();
	}

	@OnClick(R.id.GAME_SETUP_num_mobsters_increase_button)
	public void onIncreaseMobstersClicked( final View view )
	{
		m_gameSetup.increaseMobsters( m_gameController );
	}

	private void updateView()
	{
		m_numMobstersView.setText( m_gameSetup.getNumMobsters() + "" );
	}
}
