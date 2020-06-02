package l2s.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Effect;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;

/**
 * @author pchayka
 */

public class EffectDispelEffects extends Effect
{
	private final String _dispelType;
	private final int _cancelRate;
	private final String[] _stackTypes;
	private int _negateCount;

	/*
	 * cancelRate is skill dependant constant:
	 * Cancel - 25
	 * Touch of Death/Insane Crusher - 25
	 * Mage/Warrior Bane - 80
	 * Mass Mage/Warrior Bane - 40
	 * Infinity Spear - 10
	 */

	public EffectDispelEffects(Env env, EffectTemplate template)
	{
		super(env, template);
		_dispelType = template.getParam().getString("dispelType", "");
		_cancelRate = template.getParam().getInteger("cancelRate", 0);
		_negateCount = template.getParam().getInteger("negateCount", 5);
		_stackTypes = template.getParam().getString("negateStackTypes", "").split(";");
	}

	@Override
	public void onStart()
	{	
		List<Effect> _musicList = new ArrayList<Effect>();
		List<Effect> _buffList = new ArrayList<Effect>();

		//H5 - triggerable skills go first
		if(_dispelType.equals("cancellation"))
			_negateCount = Rnd.get(1, _negateCount);
			
		// Getting effect lists
		for(Effect e : _effected.getEffectList().getAllEffects())
		{
			if(_dispelType.equals("cancellation"))
			{
				if(!e.isOffensive() && !e.getSkill().isToggle() && e.isCancelable())
				{
					if(e.getSkill().isMusic())
						_musicList.add(e);
					else
						_buffList.add(e);
				}
			}
			else if(_dispelType.equals("bane"))
			{
				if(!e.isOffensive() && (ArrayUtils.contains(_stackTypes, e.getStackType()) || ArrayUtils.contains(_stackTypes, e.getStackType2())) && e.isCancelable())
					_buffList.add(e);
			}
			else if(_dispelType.equals("cleanse"))
			{
				if(e.isOffensive() && e.isCancelable() && e.getSkill().getId() != 5660)
					_buffList.add(e);
			}
		}

		// Reversing lists and adding to a new list
		List<Effect> _effectList = new ArrayList<Effect>();
		Collections.reverse(_musicList);
		Collections.reverse(_buffList);
		_effectList.addAll(_musicList);
		_effectList.addAll(_buffList);

		if(_effectList.isEmpty())
			return;

		double prelimChance, eml, dml, cancel_res_multiplier = _effected.calcStat(Stats.CANCEL_RESIST, 0, null, null); // constant resistance is applied for whole cycle of cancellation
		int buffTime, negated = 0;
		
		if(getSkill().getId() == 3592)
		{
			if(Rnd.chance(80))
				_negateCount = 2;
			else
				_negateCount = 1;
		}
		if(_effected.calcStat(Stats.CANCEL_RESIST, 0, null, null) >= 100)
			return;

		for(Effect e : _effectList)
		{
			if(negated < _negateCount)
			{
				eml = e.getSkill().getMagicLevel();
				dml = getSkill().getMagicLevel() - (eml == 0 ? _effected.getLevel() : eml); // FIXME: no effect can have have mLevel == 0. Tofix in skilldata
				buffTime = e.getTimeLeft();
				if(cancel_res_multiplier <= 0)
					cancel_res_multiplier = 1;
				cancel_res_multiplier = 1 - (cancel_res_multiplier * .01);
				prelimChance = (2. * dml + _cancelRate + buffTime / 120) * cancel_res_multiplier; // retail formula

				if(Rnd.chance(calcSkillChanceLimits(prelimChance, _effector.isPlayable())))
				{
					negated++;
					_effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
					e.exit();
				}
			}
		}
	}

	private double calcSkillChanceLimits(double prelimChance, boolean isPlayable)
	{
		if(_dispelType.equals("bane"))
		{
			if(prelimChance < 40)
				return 40;
			else if(prelimChance > 90)
				return 90;
		}
		else if(_dispelType.equals("cancellation"))
		{
			if(prelimChance < 25)
				return 25;
			else if(prelimChance > 75)
				return 75;
		}
		else if(_dispelType.equals("cleanse"))
			return _cancelRate;
		return prelimChance;
	}

	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}