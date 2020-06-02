package ai.Event.Portals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.NpcUtils;

//By Evil_dnk

public class Portalev extends DefaultAI
{
	private ScheduledFuture<?> _waveMovementTask;
	private List<NpcInstance> _wavemonsters = new ArrayList<NpcInstance>();
	private int MONSETER = 40002;
	private int MONSETERCAP = 40003;
	private int WAVES = 4;
	private int wavecount = 0;

	private static final String[] npcsay = new String[] {
			"Армия тьмы на подходе",
			"Прибыл Генерал Армии вторжения",
	};


	public Portalev (NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return false;
	}

	@Override
	protected void onEvtSpawn()
	{
		_waveMovementTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new _waveMovementTask(), 5000L, 30000L);
		super.onEvtSpawn();
	}

	public void cancelTask()
	{
		if(_waveMovementTask != null)
			_waveMovementTask.cancel(true);
	}

	public class _waveMovementTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(wavecount <= WAVES)
			{
				Functions.npcSay(getActor(), npcsay[0]);
				wavecount++;
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				_wavemonsters.add(NpcUtils.spawnSingle(MONSETER, getActor().getX() + Rnd.get(300), getActor().getY() + Rnd.get(300), getActor().getZ(), 300000));
				if(wavecount >= WAVES)
				{
					Functions.npcSay(getActor(), npcsay[1]);
					_wavemonsters.add(NpcUtils.spawnSingle(MONSETERCAP, getActor().getX() + Rnd.get(200), getActor().getY() + Rnd.get(100), getActor().getZ(), 600000));
					cancelTask();
					getActor().deleteMe();
					wavecount = 0;
				}
			}
		}
	}


	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		return false;
	}

	@Override
	protected boolean teleportHome()
	{
		return false;
	}

	@Override
	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		return false;
	}
}