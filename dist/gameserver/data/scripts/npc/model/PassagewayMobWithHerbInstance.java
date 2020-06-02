package npc.model;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public final class PassagewayMobWithHerbInstance extends MonsterInstance
{
	public PassagewayMobWithHerbInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	public static final int FieryDemonBloodHerb = 9849;

	@Override
	public void calculateRewards(Creature lastAttacker)
	{
		if(lastAttacker == null)
		{
			return;
		}

		super.calculateRewards(lastAttacker);

		if(lastAttacker.isPlayable())
		{
			dropItem(lastAttacker.getPlayer(), FieryDemonBloodHerb, 1);
		}
	}
}