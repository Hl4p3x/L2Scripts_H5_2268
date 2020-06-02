package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

public class Thomas extends Fighter
{
	private long _lastSay;

	private static final String[] _stay = {
		"Ha ... Ha ... You came to save the snowman?",
		"So I just do not give it to you!",
		"In order to save your snowman, you'll have to kill me!",
		"Ha ... Ha ... You think it's that easy?" };

	private static final String[] _attacked = {
		"You should all die!",
		"My Snowman and will not have any New Year!",
		"I'll kill you all!",
		"With so little beat? Little porridge eaten? Ha ... Ha ...",
		"And it's called heroes?",
		"Do not you see the snowman!",
		"Only the ancient weapon capable of beating me!" };

	public Thomas(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		// Ругаемся не чаще, чем раз в 10 секунд
		if(!actor.isInCombat() && System.currentTimeMillis() - _lastSay > 10000)
		{
			Functions.npcSay(actor, _stay[Rnd.get(_stay.length)]);
			_lastSay = System.currentTimeMillis();
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null || attacker.getPlayer() == null)
			return;

		// Ругаемся не чаще, чем раз в 5 секунд
		if(System.currentTimeMillis() - _lastSay > 5000)
		{
			Functions.npcSay(actor, _attacked[Rnd.get(_attacked.length)]);
			_lastSay = System.currentTimeMillis();
		}
		super.onEvtAttacked(attacker, damage);
	}
}