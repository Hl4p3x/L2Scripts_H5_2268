package l2s.gameserver.network.l2.c2s;

import java.util.Collection;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.VillageMasterInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExAcquirableSkillListByClass;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.MulticlassUtils;

public class RequestAquireSkill extends L2GameClientPacket
{
	private AcquireType _type;
	private int _id, _level, _subUnit;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
		if(_type == AcquireType.SUB_UNIT)
			_subUnit = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || _type == null)
			return;

		NpcInstance trainer = player.getLastNpc();
		if(_type != AcquireType.REBORN && _type != AcquireType.MULTICLASS) {
			if ((trainer == null || !player.checkInteractionDistance(trainer)) && !player.isGM()) {
				if (_type == AcquireType.NORMAL) {
					if(!Config.CAN_LEARN_SKILLS_FROM_INTERFACE)
						return;
				}
				else
					return;
			}
		}

		Skill skill = SkillHolder.getInstance().getSkill(_id, _level);
		if(skill == null)
			return;

		ClassId selectedMultiClassId = player.getSelectedMultiClassId();
		if(_type == AcquireType.MULTICLASS)
		{
			if(selectedMultiClassId == null)
				return;
		}
		else
			selectedMultiClassId = null;

		if(!SkillAcquireHolder.getInstance().isSkillPossible(player, selectedMultiClassId, skill, _type, false))
			return;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, selectedMultiClassId, _id, _level, _type);
		if(skillLearn == null)
			return;

		if(!checkSpellbook(player, _type, skillLearn))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
			return;
		}

		switch (_type)
		{
			case NORMAL:
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetNormal", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					trainer.showSkillList(player);
				break;
			case TRANSFORMATION:
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetTransformation", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					trainer.showTransformationSkillList(player, AcquireType.TRANSFORMATION);
				break;
			case COLLECTION:
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetCollection", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					NpcInstance.showCollectionSkillList(player);
				break;
			case TRANSFER_CARDINAL:
			case TRANSFER_EVA_SAINTS:
			case TRANSFER_SHILLIEN_SAINTS:
				learnSimple(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetTransfer", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					trainer.showTransferSkillList(player);
				break;
			case FISHING:
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetFishing", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					NpcInstance.showFishingSkillList(player);
				break;
			case CLAN:
				learnClanSkill(player, skillLearn, trainer, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetClan", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				break;
			case SUB_UNIT:
				learnSubUnitSkill(player, skillLearn, trainer, skill, _subUnit);
				Log.LogEvent(player.getName(), "Skills", "SkillGetSubUnit", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				break;
			case CERTIFICATION:
				if(!player.isBaseClassActive())
				{
					player.sendPacket(SystemMsg.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE);
					return;
				}
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				Log.LogEvent(player.getName(), "Skills", "SkillGetCertification", "char: "+player.getName()+" learned skill "+skillLearn.getId()+" level: "+skillLearn.getLevel()+" for "+skillLearn.getCost()+"");
				if(trainer != null)
					trainer.showTransformationSkillList(player, AcquireType.CERTIFICATION);
				break;
			case REBORN:
				if(!player.isBaseClassActive())
				{
					player.sendPacket(SystemMsg.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE);
					return;
				}
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.REBORN);

				final ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.REBORN, skills.size());

				for(SkillLearn s : skills)
				{
					asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
					Log.LogEvent(player.getName(), "Skills", "SkillGetReborn", "char: "+player.getName()+" learned skill "+s.getId()+" level: "+s.getLevel()+" for "+s.getCost()+"");
				}
				player.sendPacket(asl);

				player.sendActionFailed();					
				break;		
			case MULTICLASS:
				learnSimpleNextLevel(player, _type, skillLearn, skill);
				MulticlassUtils.showMulticlassAcquireList(player, selectedMultiClassId);
				break;		
		}
	}

	/**
	 * Изучение следующего возможного уровня скилла
	 */
	private static void learnSimpleNextLevel(Player player, AcquireType type, SkillLearn skillLearn, Skill skill)
	{
		final int skillLevel = player.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1)
			return;

		learnSimple(player, type, skillLearn, skill);
	}

	private static void learnSimple(Player player, AcquireType type, SkillLearn skillLearn, Skill skill)
	{
		if(player.getSp() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
			return;
		}

		player.getInventory().writeLock();
		try
		{
			for(ItemData item : skillLearn.getRequiredItemsForLearn(type))
			{
				if(!ItemFunctions.haveItem(player, item.getId(), item.getCount()))
					return;
			}

			for(ItemData item : skillLearn.getRequiredItemsForLearn(type))
				ItemFunctions.deleteItem(player, item.getId(), item.getCount(), true);
		}
		finally
		{
			player.getInventory().writeUnlock();
		}

		player.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(),  skill.getLevel()));

		player.setSp(player.getSp() - skillLearn.getCost());
		player.addSkill(skill, true);
		player.sendUserInfo();
		player.updateStats();

		player.sendSkillList(skill.getId());

		RequestExEnchantSkill.updateSkillShortcuts(player, skill.getId(), skill.getLevel());
	}

	private static void learnClanSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill)
	{
		if(!(trainer instanceof VillageMasterInstance))
			return;

		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return;
		}

		Clan clan = player.getClan();
		final int skillLevel = clan.getSkillLevel(skillLearn.getId(), 0);
		if(skillLevel != skillLearn.getLevel() - 1) // можно выучить только следующий уровень
			return;
		if(clan.getReputationScore() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return;
		}

		player.getInventory().writeLock();
		try
		{
			for(ItemData item : skillLearn.getRequiredItemsForLearn(AcquireType.CLAN))
			{
				if(!ItemFunctions.haveItem(player, item.getId(), item.getCount()))
					return;
			}

			for(ItemData item : skillLearn.getRequiredItemsForLearn(AcquireType.CLAN))
				ItemFunctions.deleteItem(player, item.getId(), item.getCount(), true);
		}
		finally
		{
			player.getInventory().writeUnlock();
		}

		clan.incReputation(-skillLearn.getCost(), false, "AquireSkill: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
		clan.addSkill(skill, true);
		clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));

		NpcInstance.showClanSkillList(player);
	}

	private static void learnSubUnitSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill, int id)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;
		SubUnit sub = clan.getSubUnit(id);
		if(sub == null)
			return;

		if((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) != Clan.CP_CL_TROOPS_FAME)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		int lvl = sub.getSkillLevel(skillLearn.getId(), 0);
		if(lvl >= skillLearn.getLevel())
		{
			player.sendPacket(SystemMsg.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_ACQUIRED);
			return;
		}

		if(lvl != (skillLearn.getLevel() - 1))
		{
			player.sendPacket(SystemMsg.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
			return;
		}

		if(clan.getReputationScore() < skillLearn.getCost())
		{
			player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return;
		}

		player.getInventory().writeLock();
		try
		{
			for(ItemData item : skillLearn.getRequiredItemsForLearn(AcquireType.SUB_UNIT))
			{
				if(!ItemFunctions.haveItem(player, item.getId(), item.getCount()))
					return;
			}

			for(ItemData item : skillLearn.getRequiredItemsForLearn(AcquireType.SUB_UNIT))
				ItemFunctions.deleteItem(player, item.getId(), item.getCount(), true);
		}
		finally
		{
			player.getInventory().writeUnlock();
		}

		clan.incReputation(-skillLearn.getCost(), false, "AquireSkill2: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
		sub.addSkill(skill, true);
		player.sendPacket(new SystemMessagePacket(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));

		if(trainer != null)
			NpcInstance.showSubUnitSkillList(player);
	}

	private static boolean checkSpellbook(Player player, AcquireType type, SkillLearn skillLearn)
	{
		// скилы по клику учатся другим способом
		if(skillLearn.isClicked())
			return false;

		for(ItemData item : skillLearn.getRequiredItemsForLearn(type))
		{
			if(!ItemFunctions.haveItem(player, item.getId(), item.getCount()))
				return false;
		}
		return true;
	}
}