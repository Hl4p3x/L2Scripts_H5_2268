package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

/**
 * AI Emperor's Seal Device.
 * @author pchayka
 */
public class SealDevice extends Fighter
{
	private boolean _firstAttack = false;

	public SealDevice(NpcInstance actor)
	{
		super(actor);
		actor.block();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!_firstAttack)
		{
			actor.broadcastPacket(new MagicSkillUse(actor, actor, 5980, 1, 0, 0));
			_firstAttack = true;
		}
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}
}