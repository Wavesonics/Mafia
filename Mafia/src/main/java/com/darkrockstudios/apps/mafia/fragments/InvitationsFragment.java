package com.darkrockstudios.apps.mafia.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.mafia.GameSetupHandler;
import com.darkrockstudios.apps.mafia.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Adam on 4/26/2014.
 */
public class InvitationsFragment extends BaseGameFragment
{
	private GameSetupHandler m_gameSetupHandler;

	public static InvitationsFragment newInstance()
	{
		InvitationsFragment fragment = new InvitationsFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof GameSetupHandler )
		{
			m_gameSetupHandler = (GameSetupHandler) activity;
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_invitations, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		m_gameSetupHandler = null;
	}

	@OnClick(R.id.INVITATIONS_create_game_button)
	public void onCreateGameClicked( final View view )
	{
		if( m_gameSetupHandler != null )
		{
			m_gameSetupHandler.createGame();
		}
	}

	@OnClick(R.id.INVITATIONS_join_game_button)
	public void onJoinGameClicked( final View view )
	{
		if( m_gameSetupHandler != null )
		{
			m_gameSetupHandler.joinGame();
		}
	}
}
