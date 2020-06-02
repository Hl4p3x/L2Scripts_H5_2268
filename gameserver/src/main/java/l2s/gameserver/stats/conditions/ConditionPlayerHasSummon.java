package l2s.gameserver.stats.conditions;

import l2s.gameserver.stats.Env;

/**
 * @author VISTALL
 * @date 15:08/05.08.2011
 */
public class ConditionPlayerHasSummon extends Condition
{
	private boolean _value;

	public ConditionPlayerHasSummon(boolean value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;

		return env.character.getServitor() != null == _value;
	}
}
