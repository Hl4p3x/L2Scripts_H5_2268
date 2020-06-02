package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.stats.Env;

public class ConditionTargetMonsterCorpse extends Condition
{
	private final boolean _flag;

	public ConditionTargetMonsterCorpse(boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		return target != null && target.isMonster() && target.isDead() == _flag;
	}
}
