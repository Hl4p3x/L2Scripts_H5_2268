package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Env;

/**
 * @author PaInKiLlEr
 */
public class EffectDummy extends Effect
{
	public EffectDummy(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		Player target = (Player) getEffected();
		if(target.getTransformation() == 303)
			return;

		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}