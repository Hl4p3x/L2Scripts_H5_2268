package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Env;

/**
 * @author PaInKiLlEr
 */
public final class EffectDummy3 extends Effect
{
	public EffectDummy3(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		Player target = (Player) getEffected();
		if(target.getTransformation() == 303)
			return;

		super.onStart();

		_effected.startParalyzed();
		_effected.abortAttack(true, true);
		_effected.abortCast(true, true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopParalyzed();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}