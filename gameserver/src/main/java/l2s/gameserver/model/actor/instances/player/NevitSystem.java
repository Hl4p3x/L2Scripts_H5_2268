package l2s.gameserver.model.actor.instances.player;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNavitAdventEffectPacket;
import l2s.gameserver.network.l2.s2c.ExNavitAdventPointInfoPacket;
import l2s.gameserver.network.l2.s2c.ExNavitAdventTimeChangePacket;
import l2s.gameserver.skills.AbnormalEffect;

/**
	@author Bonux;
	@date 10.04.2011
**/
public class NevitSystem
{
	public static final int ADVENT_TIME = 14400; // 240 минут длится период постоянного начисления очков.
	private static final int MAX_POINTS = 7200;
	private static final int BONUS_EFFECT_TIME = Config.NEVIT_BONUS_TIME; // 180 секунд длится эффект бонуса нэвита.

	private Player _player;
	private int _points = 0;
	private int _time;
	private ScheduledFuture<?> _adventTask;
	private ScheduledFuture<?> _nevitEffectTask;
	private int _percent;
	private boolean _active;

	public NevitSystem(Player player)
	{
		_player = player;
	}

	public void setPoints(int points, int time)
	{
		_points = points;
		_active = false;
		_percent = getPercent(_points);

		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.HOUR_OF_DAY, 6);
		temp.set(Calendar.MINUTE, 30);
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		if(_player.getLastAccess() < temp.getTimeInMillis() / 1000L && System.currentTimeMillis() > temp.getTimeInMillis())
			_time = ADVENT_TIME;
		else
			_time = time;
	}

	public void restartSystem()
	{
		_time = ADVENT_TIME;
		_player.sendPacket(new ExNavitAdventTimeChangePacket(_active, _time));
	}

	public void onEnterWorld()
	{
		_player.sendPacket(new ExNavitAdventPointInfoPacket(_points));
		_player.sendPacket(new ExNavitAdventTimeChangePacket(_active, _time));
		startNevitEffect(_player.getVarInt("nevit", 0));
		if(_percent >= 45 && _percent < 50)
			_player.sendPacket(SystemMsg.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_BLESSING);
		else if(_percent >= 50 && _percent < 75)
			_player.sendPacket(SystemMsg.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT_CONTINUE_TO_BATTLE_EVIL_WHEREVER_IT_MAY_LURK);
		else if(_percent >= 75)
			_player.sendPacket(SystemMsg.NEVITS_BLESSING_SHINES_STRONGLY_FROM_ABOVE_YOU_CAN_ALMOST_SEE_HIS_DIVINE_AURA);
	}

	public void startAdventTask()
	{
		if(!_active)
		{
			_active = true;
			if(_time > 0 && _adventTask == null)
				_adventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AdventTask(), 30000L, 30000L);

			_player.sendPacket(new ExNavitAdventTimeChangePacket(_active, _time));
		}
	}

	private void startNevitEffect(int time)
	{
		if(getEffectTime() > 0)
		{
			stopNevitEffectTask(false);
			time += getEffectTime();
		}
		if(time > 0)
		{
			_player.setVar("nevit", time, -1);
			_player.sendPacket(new ExNavitAdventEffectPacket(time));
			_player.sendPacket(SystemMsg.THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE_YOU_ARE_IMBUED_WITH_FULL_VITALITY_AS_WELL_AS_A_VITALITY_REPLENISHING_EFFECT);
			_player.startAbnormalEffect(AbnormalEffect.NAVIT_ADVENT);
			_nevitEffectTask = ThreadPoolManager.getInstance().schedule(new NevitEffectEnd(), time * 1000L);
		}
	}

	public void stopTasksOnLogout()
	{
		stopNevitEffectTask(true);
		stopAdventTask(false);
	}

	public void stopAdventTask(boolean sendPacket)
	{
		if(_adventTask != null)
		{
			_adventTask.cancel(true);
			_adventTask = null;
		}
		_active = false;
		if(sendPacket)
			_player.sendPacket(new ExNavitAdventTimeChangePacket(_active, _time));
	}

	private void stopNevitEffectTask(boolean saveTime)
	{
		if(_nevitEffectTask != null)
		{
			if(saveTime)
			{
				int time = getEffectTime();
				if(time > 0)
					_player.setVar("nevit", time, -1);
				else
					_player.unsetVar("nevit");
			}
			_nevitEffectTask.cancel(true);
			_nevitEffectTask = null;
		}
	}

	public boolean isActive()
	{
		return _active;
	}

	public int getTime()
	{
		return _time;
	}

	public int getPoints()
	{
		return _points;
	}

	public void addPoints(int val)
	{
		_points += val;
		int percent = getPercent(_points);
		if(_percent != percent)
		{
			_percent = percent;
			if(_percent == 45)
				_player.sendPacket(SystemMsg.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_BLESSING);
			else if(_percent == 50)
				_player.sendPacket(SystemMsg.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT_CONTINUE_TO_BATTLE_EVIL_WHEREVER_IT_MAY_LURK);
			else if(_percent == 75)
				_player.sendPacket(SystemMsg.NEVITS_BLESSING_SHINES_STRONGLY_FROM_ABOVE_YOU_CAN_ALMOST_SEE_HIS_DIVINE_AURA);
		}
		if(_points > MAX_POINTS)
		{
			_percent = 0;
			_points = 0;
			startNevitEffect(BONUS_EFFECT_TIME);
		}
		_player.sendPacket(new ExNavitAdventPointInfoPacket(_points));
	}

	public int getPercent(int points)
	{
		return (int)(100.0D / MAX_POINTS * points);
	}

	public void setTime(int time)
	{
		_time = time;
	}

	public boolean isBlessingActive()
	{
		return getEffectTime() > 0;
	}

	private int getEffectTime()
	{
		if(_nevitEffectTask == null)
			return 0;
		return (int) Math.max(0, _nevitEffectTask.getDelay(TimeUnit.SECONDS));
	}

	private class AdventTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			_time -= 30;
			if(_time <= 0)
			{
				_time = 0;
				stopAdventTask(true);
			}
			else
			{
				addPoints(72);
				if((_time % 60) == 0)
					_player.sendPacket(new ExNavitAdventTimeChangePacket(true, _time));
			}
		}
	}

	private class NevitEffectEnd extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			_player.sendPacket(new ExNavitAdventEffectPacket(0));
			_player.sendPacket(new ExNavitAdventPointInfoPacket(_points));
			_player.sendPacket(SystemMsg.NEVITS_BLESSING_HAS_ENDED_CONTINUE_YOUR_JOURNEY_AND_YOU_WILL_SURELY_MEET_HIS_FAVOR_AGAIN_SOMETIME_SOON);
			_player.stopAbnormalEffect(AbnormalEffect.NAVIT_ADVENT);
			_player.unsetVar("nevit");
			stopNevitEffectTask(false);
		}
	}
}