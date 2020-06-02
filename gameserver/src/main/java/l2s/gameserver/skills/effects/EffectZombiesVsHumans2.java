package l2s.gameserver.skills.effects;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.stats.Env;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;

/**
 * @author PaInKiLlEr
 */
public final class EffectZombiesVsHumans2 extends Effect
{
	public static final double FEAR_RANGE = 900;
	private static ScheduledFuture<?> _zaraghenieTask;

	public EffectZombiesVsHumans2(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isFearImmune())
		{
			getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		// Fear нельзя наложить на осадных саммонов
		Player player = _effected.getPlayer();
		if(player != null)
		{
			SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
			if(_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance) _effected))
			{
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}

		if(_effected.isInZonePeace())
		{
			getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		Player target = (Player) getEffected();
		if(target.getTransformation() == 303)
			return;

		super.onStart();

		if( !_effected.startFear())
		{
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			_effected.stopMove();
		}

		_zaraghenieTask = ThreadPoolManager.getInstance().schedule(new ZaraghenieTask(), 180000);

		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopFear();
		_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		if(_zaraghenieTask != null)
		{
			_zaraghenieTask.cancel(false);
			_zaraghenieTask = null;
		}
	}

	@Override
	public boolean onActionTime()
	{
		final double angle = Math.toRadians(PositionUtils.calculateAngleFrom(getEffector(), getEffected()));
		final int oldX = getEffected().getX();
		final int oldY = getEffected().getY();
		final int x = oldX + (int) (FEAR_RANGE * Math.cos(angle));
		final int y = oldY + (int) (FEAR_RANGE * Math.sin(angle));
		final int z = getEffected().getZ();
		Location loc = GeoEngine.moveCheck(oldX, oldY, z, x, y, getEffected().getGeoIndex());
		if(loc == null)
			loc = new Location(x, y, z);
		getEffected().setRunning();
		getEffected().moveToLocation(loc, 0, false);
		return true;
	}

	public class ZaraghenieTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			Player target = (Player) getEffected();
			if(target.getTransformation() != 21435 && target.getTransformation() != 22512 && target.getTransformation() != 22513)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6611, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
			else if(target.getTransformation() == 21435)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6612, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
			else if(target.getTransformation() == 22512)
			{
				removeAllSkills(target);
				Skill revengeSkill = SkillHolder.getInstance().getSkill(6613, 1);
				revengeSkill.getEffects(target, target, false, false);
			}
		}
	}

	public static void removeAllSkills(Player player)
	{
		player.getEffectList().stopEffect(6600);
		player.getEffectList().stopEffect(6601);
		player.getEffectList().stopEffect(6602);
		player.getEffectList().stopEffect(6603);
		player.getEffectList().stopEffect(6604);
		player.getEffectList().stopEffect(6605);
		player.getEffectList().stopEffect(6606);
		player.getEffectList().stopEffect(6607);
		player.getEffectList().stopEffect(6608);
		player.getEffectList().stopEffect(6609);
		player.getEffectList().stopEffect(6610);
		player.getEffectList().stopEffect(6611);
		player.getEffectList().stopEffect(6612);
		player.getEffectList().stopEffect(6613);
	}
}