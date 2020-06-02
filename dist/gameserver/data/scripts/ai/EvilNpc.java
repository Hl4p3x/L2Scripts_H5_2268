package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

public class EvilNpc extends DefaultAI
{
	private long _lastAction;
	private static final String[] _txt = {
		"Leave me alone!",
		"Calm down!",
		"I will avenge you, then you will ask for forgiveness!",
		"we'll be in trouble!",
		"I shall complain to you, have you arrested!" };

	public EvilNpc(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null || attacker.getPlayer() == null)
			return;

		// Ругаемся и кастуем скилл не чаще, чем раз в 3 секунды
		if(System.currentTimeMillis() - _lastAction > 3000)
		{
			int chance = Rnd.get(0, 100);
			if(chance < 2)
				attacker.getPlayer().setKarma(attacker.getPlayer().getKarma() + 5);
			else if(chance < 4)
				actor.doCast(SkillHolder.getInstance().getSkill(4578, 1), attacker, true); // Petrification
			else
				actor.doCast(SkillHolder.getInstance().getSkill(4185, 7), attacker, true); // Sleep

			Functions.npcSay(actor, attacker.getName() + ", " + _txt[Rnd.get(_txt.length)]);
			_lastAction = System.currentTimeMillis();
		}
	}
}