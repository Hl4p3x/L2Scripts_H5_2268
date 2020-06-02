package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.stats.Env;

public final class EffectBuff extends Effect
{
	public EffectBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}