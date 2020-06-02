package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

public class ChainHeal extends Skill
{
	private final int[] _healPercents;
	private final int _healRadius;
	private final int _maxTargets;

	public ChainHeal(StatsSet set)
	{
		super(set);
		_healRadius = set.getInteger("healRadius", 350);
		String[] params = set.getString("healPercents", "").split(";");
		_maxTargets = params.length;
		_healPercents = new int[params.length];
		for(int i = 0; i < params.length; i++)
			_healPercents[i] = Integer.parseInt(params[i]);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int curTarget = 0;
		for(Creature target : targets)
		{
			if(target == null)
				continue;

			getEffects(activeChar, target, getActivateRate() > 0, false);

			double hp = _healPercents[curTarget] * target.getMaxHp() / 100.;
			double addToHp = Math.max(0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

			if(addToHp > 0)
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);

			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage(SystemMessage.XS2S_HP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));

			curTarget++;

			useInstantEffects(activeChar, target, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private boolean checkMainTarget(Creature activeChar, Creature target)
	{
		if(activeChar == target)
			return true;
			
		if(target == null || target.isHealBlocked() || target.isInvul() /*|| activeChar.getObjectId() != aimingTarget.getObjectId() && target.getObjectId() == activeChar.getObjectId()*/) //can take yourself
			return false;
		
		//	if(target.isPet() && target.getPlayer().getParty() == activeChar.getParty())
		//	{
			
		//	}
			
			if(target.isDoor())
				return false;
			if(target.isNpc())
				return false;
			//if(!target.isPlayer())
				//continue;
			if(target.isSummon() || target.isPet())
			{
				Player owner = target.getPlayer();
				if(owner == null)
					return false;
				if(owner != activeChar)
				{	
					if(owner.getParty() == null)
						return false;
					
					if(!owner.getParty().containsMember(activeChar.getPlayer()))
						return false;
					if(owner.getClan() == null)
						return false;
					if(!owner.getClan().isAnyMember(activeChar.getObjectId()))
						return false;
				}	
			}			
		if(target.isAutoAttackable(activeChar))
			return false;
		if(target.isPlayer())
		{
			Player activeCharTarget = (Player) target;
			Player activeCharPlayer = (Player) activeChar;
			if(activeCharTarget.isInDuel())
				return false;
				
			if(activeCharTarget.isCursedWeaponEquipped())
				return false;
			
			if(activeCharTarget.isHealBlocked())
				return false;
				
			boolean _party = activeCharTarget.isInParty();
			boolean _partySelf = activeCharPlayer.isInParty();
			boolean _isInSameParty = false;
			
			boolean _clan = activeCharTarget.getClan() == null ? false : true;
			boolean _clanSelf = activeCharPlayer.getClan() == null ? false : true;
			boolean _isInSameClan = false;
				
			boolean _ally = activeCharTarget.getAlliance() == null ? false : true;
			boolean _allySelf = activeCharPlayer.getAlliance() == null ? false : true;
			boolean isInSameAlly = false;
				
			if(_party && _partySelf)
				for(Player member : activeCharPlayer.getParty().getPartyMembers())
					if(member == activeCharTarget)
						_isInSameParty = true;
							
			if(_clan && _clanSelf)
				for (Player clanMember : activeCharPlayer.getClan().getOnlineMembers(0))
					if(clanMember == activeCharTarget)
						_isInSameClan = true;
				
			if(_ally && _allySelf)
				if(activeCharPlayer.getClan().getAllyId() == activeCharTarget.getClan().getAllyId() && activeCharPlayer.getClan().getAllyId() != 0)
					isInSameAlly = true;
						
			if(!isInSameAlly && !_isInSameClan && !_isInSameParty)
				return false;
				
			return true;
		}
		return false;	
	}
	

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>();
		List<Creature> targets = aimingTarget.getAroundCharacters(_healRadius, 128);	
		if(targets == null || targets.isEmpty())
		{
			result.add(activeChar);
			return result;
		}
			
		List<HealTarget> healTargets = new ArrayList<HealTarget>();	

		if(checkMainTarget(activeChar,aimingTarget))
			healTargets.add(new HealTarget(-100.0D, aimingTarget));
			
		for(Creature target : targets)
		{
			if(target == null || target.isHealBlocked() || target.isInvul() /*|| activeChar.getObjectId() != aimingTarget.getObjectId() && target.getObjectId() == activeChar.getObjectId()*/) //can take yourself
				continue;
			
		//	if(target.isPet() && target.getPlayer().getParty() == activeChar.getParty())
		//	{
			
		//	}
			
			if(target.isDoor())
				continue;
			if(target.isNpc())
				continue;
			//if(!target.isPlayer())
				//continue;
			if(target.isSummon() || target.isPet())
			{
				Player owner = target.getPlayer();
				if(owner == null)
					continue;
				if(owner != activeChar)
				{	
					if(owner.getParty() == null)
						continue;
					
					if(!owner.getParty().containsMember(activeChar.getPlayer()))
						continue;
					if(owner.getClan() == null)
						continue;
					if(!owner.getClan().isAnyMember(activeChar.getObjectId()))
						continue;
				}	
			}
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
				if(activeCharTarget.isHealBlocked())
					continue;
					
				boolean _party = activeCharTarget.isInParty();
				boolean _partySelf = activeCharPlayer.isInParty();
				boolean _isInSameParty = false;
				
				boolean _clan = activeCharTarget.getClan() == null ? false : true;
				boolean _clanSelf = activeCharPlayer.getClan() == null ? false : true;
				boolean _isInSameClan = false;
				
				boolean _ally = activeCharTarget.getAlliance() == null ? false : true;
				boolean _allySelf = activeCharPlayer.getAlliance() == null ? false : true;
				boolean isInSameAlly = false;
				
				if(_party && _partySelf)
					for(Player member : activeCharPlayer.getParty().getPartyMembers())
						if(member == activeCharTarget)
							_isInSameParty = true;
							
				if(_clan && _clanSelf)
					for (Player clanMember : activeCharPlayer.getClan().getOnlineMembers(0))
						if(clanMember == activeCharTarget)
							_isInSameClan = true;
				
				if(_ally && _allySelf)
					if(activeCharPlayer.getClan().getAllyId() == activeCharTarget.getClan().getAllyId() && activeCharPlayer.getClan().getAllyId() != 0)
						isInSameAlly = true;
						
				if(!isInSameAlly && !_isInSameClan && !_isInSameParty)
					continue;
			}
			double hpPercent = target.getCurrentHp() / target.getMaxHp();
				
			healTargets.add(new HealTarget(hpPercent, target));
		}

		HealTarget[] healTargetsArr = new HealTarget[healTargets.size()];
		healTargets.toArray(healTargetsArr);
		Arrays.sort(healTargetsArr, new Comparator<HealTarget>()
		{
			@Override
			public int compare(HealTarget o1, HealTarget o2)
			{
				if(o1 == null || o2 == null)
					return 0;
				if(o1.getHpPercent() < o2.getHpPercent())
					return -1;
				if(o1.getHpPercent() > o2.getHpPercent())
					return 1;
				return 0;
			}
		});

		int targetsCount = 0;
		for(HealTarget ht : healTargetsArr)
		{
			result.add(ht.getTarget());
			targetsCount++;
			if(targetsCount >= _maxTargets)
				break;
		}
		return result;
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayable() && activeChar.getPlayer().isInFightClub() && !activeChar.getPlayer().getFightClubEvent().canUsePositiveMagic(activeChar, target))
			return false;

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	private static class HealTarget
	{
		private final double hpPercent;
		private final Creature target;
		
		public HealTarget(double hpPercent, Creature target)
		{
			this.hpPercent = hpPercent;
			this.target = target;
		}

		public double getHpPercent()
		{
			return hpPercent;
		}

		public Creature getTarget()
		{
			return target;
		}
	}
}