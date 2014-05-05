package com.darkrockstudios.apps.mafia.viewholders;

import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.darkrockstudios.apps.mafia.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Adam on 5/4/2014.
 */
public class VoteStatusViewHolder
{
	@InjectView(R.id.VOTE_avatar_image)
	public NetworkImageView m_avatarImageView;

	@InjectView(R.id.VOTE_participant_name)
	public TextView m_participantNameView;

	@InjectView(R.id.VOTE_num_votes)
	public TextView m_numVotesView;

	public VoteStatusViewHolder( final View view )
	{
		ButterKnife.inject( this, view );
	}
}
