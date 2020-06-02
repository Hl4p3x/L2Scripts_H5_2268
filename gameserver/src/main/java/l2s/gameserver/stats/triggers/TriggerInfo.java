package l2s.gameserver.stats.triggers;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.conditions.Condition;

/**
 * @author VISTALL
 * @date 15:03/22.01.2011
 */
public class TriggerInfo extends Skill.AddedSkill
{
	private final TriggerType _type;
	private final double _chance;
	private Condition[] _conditions = Condition.EMPTY_ARRAY;

	public TriggerInfo(int id, int level, TriggerType type, double chance)
	{
		super(id, level);
		_type = type;
		_chance = chance;
	}

	public final void addCondition(Condition c)
	{
		_conditions = ArrayUtils.add(_conditions, c);
	}

	public boolean checkCondition(Creature actor, Creature target, Creature aimTarget, Skill owner, double damage)
	{
		// Скилл проверяется и кастуется на aimTarget
		if(getSkill().checkTarget(actor, aimTarget, aimTarget, false, false) != null)
			return false;

		Env env = new Env();
		env.character = actor;
		env.skill = owner;
		
		if(owner != null && owner.getId() == 1557)
			env.target = (Creature)actor.getPlayer().getServitor();
		else	
			env.target = target;
			
		//env.target = target; // В условии проверяется реальная цель.
		env.value = damage;

		for(Condition c : _conditions)
			if(!c.test(env))
				return false;
		return true;
	}

	public TriggerType getType()
	{
		return _type;
	}

	public double getChance()
	{
		return _chance;
	}
}
