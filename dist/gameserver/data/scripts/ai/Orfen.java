package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;
import npc.model.OrfenInstance;

public class Orfen extends Fighter
{
	public static final NpcString[] MsgOnRecall =
	{
		NpcString.S1_,
		NpcString.S1__,
		NpcString.YOURE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME,
		NpcString.S1___
	};

	public final Skill[] _paralyze;

	public Orfen(NpcInstance actor)
	{
		super(actor);
		_paralyze = getActor().getTemplate().getDebuffSkills();
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;
		OrfenInstance actor = getActor();

		if(actor.isTeleported() && actor.getCurrentHpPercents() > 95)
		{
			actor.setTeleported(false);
			return true;
		}

		return false;
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultNewTask();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		super.onEvtAttacked(attacker, damage);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
			return;

		double distance = actor.getDistance(attacker);

		// if(attacker.isMuted() &&)
		if(distance > 300 && distance < 1000 && _damSkills.length > 0 && Rnd.chance(10))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length - 1)], attacker.getName());
			teleToLocation(attacker, Location.findFrontPosition(actor, attacker, 0, 50));
			Skill r_skill = _damSkills[Rnd.get(_damSkills.length)];
			if(canUseSkill(r_skill, attacker, -1))
				addTaskAttack(attacker, r_skill, 1000000);
		}
		else if(_paralyze.length > 0 && Rnd.chance(20))
		{
			Skill r_skill = _paralyze[Rnd.get(_paralyze.length)];
			if(canUseSkill(r_skill, attacker, -1))
				addTaskAttack(attacker, r_skill, 1000000);
		}
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		super.onEvtSeeSpell(skill, caster);
		OrfenInstance actor = getActor();
		if(actor.isCastingNow())
			return;

		double distance = actor.getDistance(caster);
		if(_damSkills.length > 0 && skill.getEffectPoint() > 0 && distance < 1000 && Rnd.chance(20))
		{
			Functions.npcSay(actor, MsgOnRecall[Rnd.get(MsgOnRecall.length)], caster.getName());
			teleToLocation(caster, Location.findFrontPosition(actor, caster, 0, 50));
			Skill r_skill = _damSkills[Rnd.get(_damSkills.length)];
			if(canUseSkill(r_skill, caster, -1))
				addTaskAttack(caster, r_skill, 1000000);
		}
	}

	@Override
	public OrfenInstance getActor()
	{
		return (OrfenInstance) super.getActor();
	}

	private void teleToLocation(Creature attacker, Location loc)
	{
		attacker.teleToLocation(loc);
	}
}