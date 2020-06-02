package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoPacket;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillResult;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SkillUtils;

/**
 * Format chdd
 * c: (id) 0xD0
 * h: (subid) 0x0F
 * d: skill id
 * d: skill lvl
 */
public class RequestExEnchantSkill extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
		if(!getClient().isHFClient())
			_skillLvl = SkillUtils.convertGODSkillLevelToHF(_skillId, _skillLvl);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if(activeChar.getTransformation() != 0 || activeChar.isMounted() || Olympiad.isRegisteredInComp(activeChar) || activeChar.isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_THE_SKILL_ENHANCING));
			return;
		}
		
		if(!activeChar.getClassId().isOfLevel(ClassLevel.THIRD))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_CORRESPONDING_FUNCTION));
			return;		
		}
		
		if(activeChar.getLevel() < 76)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION));
			return;
		}

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
		if(sl == null)
			return;

		Skill oldSkill = activeChar.getKnownSkill(_skillId);
		if(oldSkill == null)
			return;

		if(!SkillAcquireHolder.getInstance().isSkillPossible(activeChar, oldSkill))
			return;

		int slevel = oldSkill.getLevel();
		int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel());

		// already knows the skill with this level
		if(slevel >= enchantLevel)
			return;

		// Можем ли мы перейти с текущего уровня скилла на данную заточку
		if(slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1)
		{
			activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
			//activeChar.sendMessage("Incorrect enchant level.");
			return;
		}

		Skill skill = SkillHolder.getInstance().getSkill(_skillId, enchantLevel);
		if(skill == null)
		{
			activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
			return;
		}

		int[] cost = sl.getCost();
		int requiredSp = cost[1] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int requiredAdena = cost[0] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int rate = sl.getRate(activeChar);

		if(activeChar.getSp() < requiredSp)
		{
			sendPacket(Msg.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(_skillLvl % 100 == 1) // only first lvl requires book (101, 201, 301 ...)
		{
			if(ItemFunctions.getItemCount(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK) == 0)
			{
				activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
				return;
			}
			ItemFunctions.deleteItem(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK, 1);
		}

		ItemFunctions.deleteItem(activeChar, 57, requiredAdena);
		activeChar.addExpAndSp(0, -1 * requiredSp);
		activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(requiredSp));
		if(Rnd.chance(rate))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(1));
			Log.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", "Successfully enchanted", "skill ID:"+_skillId+" skill level: "+_skillLvl+" and rate in % "+rate+"");
		}	
		else
		{
			skill = SkillHolder.getInstance().getSkill(_skillId, sl.getBaseLevel());
			activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0));
			Log.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", "Failed to enchant", "skill ID:"+_skillId+" skill level: "+_skillLvl+" and rate in % "+rate+"");
		}

		oldSkill = activeChar.addSkill(skill, true);
		if(oldSkill != null)
		{
			TimeStamp timeStamp = activeChar.getSkillReuse(oldSkill);
			if(timeStamp != null)
				activeChar.disableSkill(skill, timeStamp.getReuseCurrent());
		}

		updateSkillShortcuts(activeChar, _skillId, _skillLvl);
		activeChar.sendPacket(new ExEnchantSkillInfoPacket(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
		activeChar.sendSkillList();
	}

	protected static void updateSkillShortcuts(Player player, int skillId, int skillLevel)
	{
		for(ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == skillId && sc.getType() == ShortCut.TYPE_SKILL)
			{
				ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				player.sendPacket(new ShortCutRegisterPacket(player, newsc));
				player.registerShortCut(newsc);
			}
	}
}