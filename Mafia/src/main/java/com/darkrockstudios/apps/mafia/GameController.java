package com.darkrockstudios.apps.mafia;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.GameHelper;

/**
 * Created by Adam on 4/26/2014.
 */
public class GameController implements RealTimeMessageReceivedListener
{
	private static final String FRAGTAG = GameController.class.getName() + ".GAMECONTROLLER";

	// Request code for the "select players" UI
	public final static int RC_SELECT_PLAYERS   = 10000;
	public final static int RC_INVITATION_INBOX = 10001;
	public final static int RC_WAITING_ROOM     = 10002;

	public final static int MIN_PLAYERS = 2;
	public final static int MAX_PLAYERS = 8;

	private final GameHelper      m_gameHelper;
	private       GoogleApiClient m_apiClient;

	private Room m_room;

	public GameController( final GameHelper gameHelper, final GoogleApiClient apiClient )
	{
		m_gameHelper = gameHelper;
		m_apiClient = apiClient;
	}
	/*
	public static GameController get( final FragmentManager fragmentManager )
	{
		final GameController gameController;

		Fragment gameControllerFragment = fragmentManager.findFragmentByTag( FRAGTAG );
		if( gameControllerFragment == null )
		{
			gameController = new GameController();
			fragmentManager.beginTransaction().add( gameController, FRAGTAG ).commit();
		}
		else
		{
			gameController = (GameController) gameControllerFragment;
		}

		return gameController;
	}

	@Override
	public void onAttach( Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof MainActivity )
		{
			MainActivity mainActivity = (MainActivity) activity;

			m_gameHelper = mainActivity.getGameHelper();
			m_apiClient = mainActivity.getGoogleApiClient();
		}
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );
	}
	*/

	public void setRoom( final Room room )
	{
		m_room = room;
	}

	public Room getRoom()
	{
		return m_room;
	}

	public GameHelper getGameHelper()
	{
		return m_gameHelper;
	}

	public GoogleApiClient getApiClient()
	{
		return m_apiClient;
	}

	@Override
	public void onRealTimeMessageReceived( RealTimeMessage realTimeMessage )
	{

	}
}
