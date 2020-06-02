package ai;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

/**
 * @author pchayka
 */
public class DeluLizardmanSpecialCommander extends Fighter
{
	private boolean _shouted = false;

	public DeluLizardmanSpecialCommander(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_shouted = false;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(Rnd.chance(40) && !_shouted)
		{
			_shouted = true;
			Functions.npcSay(actor, "Come on my fellows, assist me here!");

			List<NpcInstance> around = actor.getAroundNpc(1000, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.isMonster())
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
		}
		super.onEvtAttacked(attacker, damage);
	}
}