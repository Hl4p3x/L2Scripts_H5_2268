package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.stats.Env;

public class EffectInterrupt extends Effect
{
	public EffectInterrupt(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getEffected().isRaid())
			getEffected().abortCast(false, true);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}