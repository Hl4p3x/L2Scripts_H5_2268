package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class SelfSacrifice extends Skill
{
	private final int _effRadius;

	public SelfSacrifice(StatsSet set)
	{
		super(set);
		_effRadius = set.getInteger("effRadius", 1000);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);		
	}
	
	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>();
		List<Creature> targets = activeChar.getAroundCharacters(_effRadius, 1000);	
		if((targets == null || targets.isEmpty()) && ((Player) activeChar).getParty() == null)
			return result;
			
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			
			if(!target.isPlayer())
				continue;
			
			if(target.isAutoAttackable(activeChar))
				continue;
				
			if(target.isPlayer())
			{
				Player activeCharTarget = (Player) target;
				Player activeCharPlayer = (Player) activeChar;
				if(activeCharTarget.isInDuel())
					continue;
					
				if(activeCharTarget.isCursedWeaponEquipped())
					continue;
			}
				
			result.add(target);
		}
		return result;
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
			if(target != null)
			{
				// Player holding a cursed weapon can't be buffed and can't buff
				if(getSkillType() == Skill.SkillType.BUFF && target != activeChar)
					if(target.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped())
						continue;

				reflected = target.checkReflectSkill(activeChar, this);
				useInstantEffects(activeChar, target, reflected);
				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isSSPossible())
			if(!(Config.SAVING_SPS && _skillType == SkillType.SELF_SACRIFICE))
				activeChar.unChargeShots(isMagic());
	}
}