package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.model.Effect;
import l2s.gameserver.stats.Env;

public class EffectAggression extends Effect
{
	public EffectAggression(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayer() && _effected != _effector)
			((PlayerAI) _effected.getAI()).lockTarget(_effector);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayer() && _effected != _effector)
			((PlayerAI) _effected.getAI()).lockTarget(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}