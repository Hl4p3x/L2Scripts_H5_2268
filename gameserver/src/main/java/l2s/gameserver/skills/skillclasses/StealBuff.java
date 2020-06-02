package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.EffectsComparator;

/**
 * @author pchayka
 */

public class StealBuff extends Skill
{
	private final int _stealCount;

	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("stealCount", 1);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || (!target.isPlayer()))
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				//if(calcStealChance(target, activeChar))
				//{
				int stealCount = Rnd.get(1, _stealCount); // ToCheck
				if(!target.isPlayer())
					continue;
				List<Effect> effectsList = target.getEffectList().getAllEffects();
				Collections.sort(effectsList, EffectsComparator.getInstance()); // ToFix: Comparator to HF
				Collections.reverse(effectsList);

				// Крутой изжоп для корявой системы эффектов, которую давно отправить в топку.
				IntObjectMap<List<Effect>> effectSkills = new TreeIntObjectMap<List<Effect>>();
				for(Effect e : effectsList)
				{
					List<Effect> effects = effectSkills.get(e.getDisplayId());
					if(effects == null)
					{
						effects = new ArrayList<Effect>();
						effectSkills.put(e.getDisplayId(), effects);
					}
					effects.add(e);
				}

				int counter = 0;
				loop: for(List<Effect> effects : effectSkills.valueCollection())
				{
					if(counter >= stealCount) 
						break;

					if(effects.isEmpty())
						continue;

					for(Effect e : effects)
					{
						if(!canSteal(e))
							continue loop;
					}

					for(Effect e : effects)
					{
						Effect stolenEffect = cloneEffect(activeChar, e);
						if(stolenEffect != null)
							activeChar.getEffectList().addEffect(stolenEffect);
						e.exit();
					}
					counter++;
				}
		
				//}
				//else
				//{
				//	activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
				//	activeChar.sendPacket(new ExMagicAttackInfo(activeChar.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					//continue;
				//}
				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private boolean canSteal(Effect e)
	{
		if(e.getSkill().isHeroic())
			return false;
		return e != null && e.isInUse() && e.isCancelable() && !e.getSkill().isToggle() && !e.getSkill().isPassive() && !e.getSkill().isOffensive() && e.getEffectType() != EffectType.Vitality && e.getEffectType() != EffectType.VitalityMaintenance && !e.getTemplate()._applyOnCaster;
	}

	private boolean calcStealChance(Creature effected, Creature effector)
	{
		double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_RESIST, 1, null, null);
		int dml = effector.getLevel() - effected.getLevel();   // to check: magicLevel or player level? Since it's magic skill setting player level as default
		double prelimChance = (dml + 50) * (1 - cancel_res_multiplier * .01);   // 50 is random reasonable constant which gives ~50% chance of steal success while else is equal
		return Rnd.chance(prelimChance);
	}

	private Effect cloneEffect(Creature cha, Effect eff)
	{
		Skill skill = eff.getSkill();

		for(EffectTemplate et : skill.getEffectTemplates())
		{
			Effect effect = et.getEffect(new Env(cha, cha, skill));
			if(effect != null)
			{
				effect.setCount(eff.getCount());
				effect.setPeriod(eff.getCount() == 1 ? eff.getPeriod() - eff.getTime() : eff.getPeriod());
				return effect;
			}
		}
		return null;
	}
}