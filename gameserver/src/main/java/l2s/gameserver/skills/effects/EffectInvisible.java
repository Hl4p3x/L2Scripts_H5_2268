package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.stats.Env;

public final class EffectInvisible extends Effect
{
	public EffectInvisible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getEffected().isPlayer())
		{
			Player player = getEffected().getPlayer();
			if(player.getActiveWeaponFlagAttachment() != null)
				return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startInvisible(this, true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(getEffected().isPlayer())
		{
			getEffected().stopInvisible(this, true);

			Servitor servitor = getEffected().getServitor();
			if(servitor != null)
				servitor.getEffectList().stopEffect(getSkill());
		}
		else if(getEffected().isServitor())
		{
			getEffected().getPlayer().getEffectList().stopEffect(getSkill());
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}