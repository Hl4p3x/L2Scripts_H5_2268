package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Effect;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.stats.Env;

public final class EffectGrow extends Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCurrentCollisionHeight(npc.getTemplate().getCollisionHeight() * 1.24);
			npc.setCurrentCollisionRadius(npc.getTemplate().getCollisionRadius() * 1.19);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCurrentCollisionHeight(npc.getTemplate().getCollisionHeight());
			npc.setCurrentCollisionRadius(npc.getTemplate().getCollisionRadius());
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}