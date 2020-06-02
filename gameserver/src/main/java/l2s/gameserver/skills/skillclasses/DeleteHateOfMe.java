package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.StatsSet;


public class DeleteHateOfMe extends Skill
{
	public DeleteHateOfMe(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(activeChar.isPlayer() && ((Player) activeChar).isGM())
					activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.Formulas.Chance", (Player)activeChar).addString(getName()).addNumber(getActivateRate()));

				if(target.isNpc() && Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().remove(activeChar, true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}
				
				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, true, false);
			}
	}
}