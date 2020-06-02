package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Playable;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Env;

public final class EffectSilentMove extends Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayable())
			((Playable) _effected).startSilentMoving();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayable())
			((Playable) _effected).stopSilentMoving();
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		if(!getSkill().isToggle())
			return false;

		double manaDam = calc();
		if(manaDam > _effected.getCurrentMp())
		{
			_effected.sendPacket(Msg.NOT_ENOUGH_MP);
			_effected.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}