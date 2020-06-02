package l2s.gameserver.skills.skillclasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.StatsSet;


public class NegateEffects extends Skill
{
	private Map<EffectType, Integer> _negateEffects = new HashMap<EffectType, Integer>();
	private Map<String, Integer> _negateStackType = new HashMap<String, Integer>();
	private final boolean _onlyPhysical;
	private final boolean _negateDebuffs;
	private final boolean _isForceNegate;

	public NegateEffects(StatsSet set)
	{
		super(set);

		String[] negateEffectsString = set.getString("negateEffects", "").split(";");
		for(int i = 0; i < negateEffectsString.length; i++)
			if(!negateEffectsString[i].isEmpty())
			{
				String[] entry = negateEffectsString[i].split(":");
				_negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}

		String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
		for(int i = 0; i < negateStackTypeString.length; i++)
			if(!negateStackTypeString[i].isEmpty())
			{
				String[] entry = negateStackTypeString[i].split(":");
				_negateStackType.put(entry[0], entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}

		_onlyPhysical = set.getBool("onlyPhysical", false);
		_negateDebuffs = set.getBool("negateDebuffs", true);
		_isForceNegate = set.getBool("force", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					activeChar.sendPacket(new ExMagicAttackInfo(activeChar.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					continue;
				}

				if(!_negateEffects.isEmpty())
					for(Map.Entry<EffectType, Integer> e : _negateEffects.entrySet())
						negateEffectAtPower(target, e.getKey(), e.getValue().intValue());

				if(!_negateStackType.isEmpty())
					for(Map.Entry<String, Integer> e : _negateStackType.entrySet())
						negateEffectAtPower(target, e.getKey(), e.getValue().intValue());

				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private void negateEffectAtPower(Creature target, EffectType type, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			Skill skill = e.getSkill();
			if(skill.getId() == 4515)
				continue;
			if(_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs)
				continue;
			// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
			if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
				continue;
			if(e.getEffectType() == type && e.getStackOrder() <= power)
				e.exit();
		}
	}

	private void negateEffectAtPower(Creature target, String stackType, int power)
	{
		for(Effect e : target.getEffectList().getAllEffects())
		{
			Skill skill = e.getSkill();
			if(!_isForceNegate)
			{
				if (_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs && _isForceUse)
					continue;
				// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
				if (!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
					continue;
			}
			if(e.checkStackType(stackType) && e.getStackOrder() <= power)
				e.exit();
		}
	}
}