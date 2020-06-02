package l2s.gameserver.stats.conditions;

import l2s.gameserver.stats.Env;

public class ConditionPlayerEvent extends Condition
{
	private final boolean _value;

	public ConditionPlayerEvent(boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.isInCtF() == _value || env.character.isInTvT() == _value || env.character.isInLastHero() == _value || env.character.isInZombieVsHumans() == _value || env.character.isInEventModelEvent();
	}
}