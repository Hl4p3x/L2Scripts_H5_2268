package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.StatsSet;

public class Aggression extends Skill
{
	private final boolean _unaggring;
	private final boolean _silent;
	private final boolean ignorePlayables;
	private final boolean autoAttack;	

	public Aggression(StatsSet set)
	{
		super(set);
		_unaggring = set.getBool("unaggroing", false);
		_silent = set.getBool("silent", false);
		ignorePlayables = set.getBool("ignorePlayables", false);
		autoAttack = set.getBool("autoAttack", false);		
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int effect = _effectPoint;

		if(isSSPossible() && (activeChar.getChargedSoulShot() || activeChar.getChargedSpiritShot() > 0))
			effect *= 2;

		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isAutoAttackable(activeChar))
					continue;
				if(target.isNpc())
					if(_unaggring)
					{
						if(target.isNpc() && activeChar.isPlayable())
							((NpcInstance) target).getAggroList().addDamageHate(activeChar, 0, -effect);
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);
						if(!_silent)
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
					}
				else if(target.isPlayable() && !target.isDebuffImmune() && !ignorePlayables)
				{
					target.setTarget(activeChar);
					if(autoAttack)
						target.getAI().Attack(activeChar, false, false);					
				}
				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}