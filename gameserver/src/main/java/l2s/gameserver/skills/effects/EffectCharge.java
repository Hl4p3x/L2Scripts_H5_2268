package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Env;

public final class EffectCharge extends Effect
{
	// Максимальное количество зарядов находится в поле val="xx"

	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (getEffected().isPlayer())
		{
			final Player player = (Player)getEffected();

			if (player.getIncreasedForce() >= calc())
				player.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			else
				player.setIncreasedForce(player.getIncreasedForce() + 1);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
