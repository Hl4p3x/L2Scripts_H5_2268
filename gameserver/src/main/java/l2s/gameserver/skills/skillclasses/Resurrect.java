package l2s.gameserver.skills.skillclasses;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;

public class Resurrect extends Skill
{
	private final boolean _canPet;

	public Resurrect(StatsSet set)
	{
		super(set);
		_canPet = set.getBool("canPet", false);
	}

	@Override
	public boolean checkCondition(final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		if(target == null || target != activeChar && !target.isDead())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		Player player = (Player) activeChar;
		Player pcTarget = target.getPlayer();

		if(pcTarget == null)
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		if(pcTarget.getTeam() != TeamType.NONE && player.getTeam() == TeamType.NONE) // Запрет на res участником эвента незарегистрированного игрока
			return false;
		if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() == TeamType.NONE) // Запрет на resучастника эвента незарегистрированным игроком
			return false;
		if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() == pcTarget.getTeam()) // Свою команду res нельзя except incld lh
			return false; 

		if(player.isInOlympiadMode() || pcTarget.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		for(Event e : player.getEvents())
		{
			if (!e.canRessurect(player, target, forceUse))
			{
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
				return false;
			}
		}

		if((player.isInZone(Zone.ZoneType.SIEGE) && player.getEvents().isEmpty()) || (target.isInZone(Zone.ZoneType.SIEGE) && target.getEvents().isEmpty()))
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(oneTarget())
		{
			if(target.isPet())
			{
				Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
					else
						activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING);
					return false;
				}
				if(!(_canPet || _targetType == SkillTargetType.TARGET_SERVITOR))
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
			}
			else if(target.isPlayer())
			{
				Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)ask.getValue() : null;

				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(SystemMsg.WHILE_A_PET_IS_BEING_RESURRECTED_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
					else
						activeChar.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
					return false;
				}
				if(_targetType == SkillTargetType.TARGET_SERVITOR)
				{
					player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					return false;
				}
				// Check to see if the player is in a festival.
				if(pcTarget.isFestivalParticipant())
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Resurrect", player));
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double percent = _power;

		if(percent < 100 && !isHandler())
		{
			double wit_bonus = _power * (BaseStats.WIT.calcBonus(activeChar) - 1);
			percent += wit_bonus > 20 ? 20 : wit_bonus;
			if(percent > 90)
				percent = 90;
		}

		for(Creature target : targets)
			Loop:if(target != null)
			{
				if(target.getPlayer() == null)
					continue;

				for(Event e : target.getEvents())
					if(!e.canRessurect((Player)activeChar, target, true))
						break Loop;

				if(target.isPet() && _canPet)
				{
					if(target.getPlayer() == activeChar)
						((PetInstance) target).doRevive(percent);
					else
						target.getPlayer().reviveRequest((Player) activeChar, percent, true);
				}
				else if(target.isPlayer())
				{
					if(_targetType == SkillTargetType.TARGET_SERVITOR)
						continue;

					Player targetPlayer = (Player) target;

					Pair<Integer, OnAnswerListener> ask = targetPlayer.getAskListener(false);
					ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)ask.getValue() : null;
					if(reviveAsk != null)
						continue;

					if(targetPlayer.isFestivalParticipant())
						continue;

					targetPlayer.reviveRequest((Player) activeChar, percent, false);
				}
				else
					continue;

				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}