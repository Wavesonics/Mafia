package com.darkrockstudios.apps.mafia.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.darkrockstudios.apps.mafia.MafiaApplication;
import com.darkrockstudios.apps.mafia.R;
import com.darkrockstudios.apps.mafia.game.PlayerVoteStatus;
import com.darkrockstudios.apps.mafia.viewholders.VoteStatusViewHolder;

/**
 * Created by Adam on 5/4/2014.
 */
public class VotingAdapter extends ArrayAdapter<PlayerVoteStatus>
{
	private LayoutInflater m_inflater;

	public VotingAdapter( final Context context )
	{
		super( context, 0 );

		m_inflater = LayoutInflater.from( context );
	}

	@Override
	public View getView( final int position, final View convertView, final ViewGroup parent )
	{
		final View view;
		if( convertView == null )
		{
			view = m_inflater.inflate( R.layout.vote_status_list_item, parent, false );
			VoteStatusViewHolder holder = new VoteStatusViewHolder( view );
			view.setTag( holder );
		}
		else
		{
			view = convertView;
		}

		PlayerVoteStatus item = getItem( position );
		VoteStatusViewHolder holder = (VoteStatusViewHolder) view.getTag();

		ImageLoader imageLoader = new ImageLoader( MafiaApplication.getRequestQueue(), MafiaApplication.getBitmapCache() );
		holder.m_avatarImageView.setImageUrl( item.m_participant.getIconImageUrl(), imageLoader );
		holder.m_participantNameView.setText( item.m_participant.getDisplayName() );
		holder.m_numVotesView.setText( item.getVotes() + "" );

		return view;
	}
}
