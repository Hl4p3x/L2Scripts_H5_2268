package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;


public class Disablers extends Skill
{
	private final boolean _skillInterrupt;

	public Disablers(StatsSet set)
	{
		super(set);
		_skillInterrupt = set.getBool("skillInterrupt", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
			if(target != null)
			{
				reflected = target.checkReflectSkill(activeChar, this);
				realTarget = reflected ? activeChar : target;

				if(_skillInterrupt)
				{
					if(realTarget.getCastingSkill() != null && !realTarget.getCastingSkill().isMagic() && !realTarget.isRaid())
						realTarget.abortCast(false, true);
					if(!realTarget.isRaid())
						realTarget.abortAttack(true, true);
				}

				useInstantEffects(activeChar, target, reflected);
				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}