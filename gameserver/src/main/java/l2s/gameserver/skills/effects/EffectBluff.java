package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;
import l2s.gameserver.stats.Env;

public final class EffectBluff extends Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getEffected().isNpc() && !getEffected().isMonster())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		getEffected().broadcastPacket(new StartRotatingPacket(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new FinishRotatingPacket(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}