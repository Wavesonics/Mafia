package com.darkrockstudios.apps.mafia.game.rpc;

import com.darkrockstudios.apps.mafia.game.GameController;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by Adam on 5/3/2014.
 */
public class Network implements RealTimeMessageReceivedListener, RealTimeMultiplayer.ReliableMessageSentCallback
{
	private final GameController m_gameController;

	public Network( final GameController gameController )
	{
		m_gameController = gameController;
	}

	public void executeRpc( final RemoteProcedureCall rpc )
	{
		if( rpc.isBroadcast() )
		{
			broadcastRpc( rpc );
		}
		else
		{
			sendRpc( rpc );
		}
	}

	private void broadcastRpc( final RemoteProcedureCall rpc )
	{
		final GoogleApiClient apiClient = m_gameController.getApiClient();
		final Room room = m_gameController.getRoom();
		final String localParticipantId = m_gameController.getLocalParticipantId();

		try
		{
			byte[] rpcData = rpcToBytes( rpc );

			for( final Participant p : room.getParticipants() )
			{
				if( !p.getParticipantId().equals( localParticipantId ) )
				{
					Games.RealTimeMultiplayer.sendReliableMessage( apiClient,
					                                               this,
					                                               rpcData,
					                                               room.getRoomId(),
					                                               p.getParticipantId() );
				}
			}
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}

		// Lastly, execute locally
		rpc.makeProcedureCall( m_gameController );
	}

	private void sendRpc( final RemoteProcedureCall rpc )
	{
		final GoogleApiClient apiClient = m_gameController.getApiClient();
		final Room room = m_gameController.getRoom();
		final String localParticipantId = m_gameController.getLocalParticipantId();

		try
		{
			byte[] rpcData = rpcToBytes( rpc );


			for( final String participantId : rpc.recipientList() )
			{
				if( !participantId.equals( localParticipantId ) )
				{
					Games.RealTimeMultiplayer.sendReliableMessage( apiClient,
					                                               this,
					                                               rpcData,
					                                               room.getRoomId(),
					                                               participantId );
				}
				// If the local user was in the recipient list, execute locally
				else
				{
					rpc.makeProcedureCall( m_gameController );
				}
			}
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private byte[] rpcToBytes( final RemoteProcedureCall rpc ) throws IOException
	{
		byte[] bytes;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream( bos );
		try
		{
			out.writeObject( rpc );
			bytes = bos.toByteArray();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch( final IOException ignore )
			{

			}
		}

		return bytes;
	}

	private RemoteProcedureCall bytesToRpc( final byte[] rpcData )
	{
		RemoteProcedureCall remoteProcedureCall = null;

		ByteArrayInputStream bis = new ByteArrayInputStream( rpcData );
		ObjectInput in = null;
		try
		{
			in = new ObjectInputStream( bis );
			Object obj = in.readObject();

			if( obj instanceof RemoteProcedureCall )
			{
				remoteProcedureCall = (RemoteProcedureCall) obj;
			}
		}
		catch( ClassNotFoundException | IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch( final IOException ignore )
			{

			}
			try
			{
				if( in != null )
				{
					in.close();
				}
			}
			catch( final IOException ignore )
			{

			}
		}

		return remoteProcedureCall;
	}

	@Override
	public void onRealTimeMessageSent( final int statusCode, final int tokenId, final String recipientParticipantId )
	{

	}

	@Override
	public void onRealTimeMessageReceived( final RealTimeMessage realTimeMessage )
	{
		byte[] messageData = realTimeMessage.getMessageData();
		RemoteProcedureCall rpc = bytesToRpc( messageData );
		rpc.makeProcedureCall( m_gameController );
	}
}
