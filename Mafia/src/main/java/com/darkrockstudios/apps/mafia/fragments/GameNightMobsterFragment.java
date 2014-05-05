package com.darkrockstudios.apps.mafia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.rpc.PlayerReadyRPC;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Adam on 5/4/2014.
 */
public class GameNightMobsterFragment extends BaseGameFragment
{
	@InjectView(R.id.NIGHT_mobster_ready_button)
	Button m_readyButton;

	public static GameNightMobsterFragment newInstance()
	{
		GameNightMobsterFragment fragment = new GameNightMobsterFragment();

		Bundle args = new Bundle();
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view = inflater.inflate( R.layout.fragment_game_night_mobster, container, false );
		ButterKnife.inject( this, view );

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ButterKnife.reset( this );
	}

	@OnClick(R.id.NIGHT_mobster_ready_button)
	public void onReadyClicked( final View view )
	{
		PlayerReadyRPC playerReady = new PlayerReadyRPC( m_gameController, true );
		m_gameController.getNetwork().executeRpc( playerReady );

		m_readyButton.setEnabled( false );
	}
}
