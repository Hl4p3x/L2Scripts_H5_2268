package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;


public class RecoverAll extends Skill
{
	public RecoverAll(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int fullPoints = Config.VITALITY_LEVELS[4];
		double percent = _power;

		for(Creature target : targets)
		{
			if(target.isPlayer())
			{
				target.setCurrentHp(target.getMaxHp(), false);
				target.setCurrentMp(target.getMaxMp());	
				target.setCurrentCp(target.getMaxCp());
			}
			useInstantEffects(activeChar, target, false);
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}