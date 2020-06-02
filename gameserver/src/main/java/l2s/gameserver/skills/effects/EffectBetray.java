package l2s.gameserver.skills.effects;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.stats.Env;

public class EffectBetray extends Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected != null && _effected.isSummon())
		{
			Servitor servitor = (Servitor) _effected;
			servitor.setDepressed(true);
			servitor.getAI().Attack(servitor.getPlayer(), true, false);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected != null && _effected.isSummon())
		{
			Servitor servitor = (Servitor) _effected;
			servitor.setDepressed(false);
			servitor.getAI().setIntention(AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}