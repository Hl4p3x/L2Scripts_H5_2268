package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoPacket;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillResult;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SkillUtils;

/**
 * Format (ch) dd
 */
public final class RequestExEnchantSkillImmortal extends L2GameClientPacket
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
			return;
		}

		Skill skill = SkillHolder.getInstance().getSkill(_skillId, enchantLevel);
		if(skill == null)
			return;

		if(!ItemFunctions.deleteItem(activeChar, SkillTreeTable.IMMORTAL_ENCHANT_BOOK, 1))
		{
			activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			return;
		}

		activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(1));
		Log.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", "Successfully immortal enchanted", "skill ID:"+_skillId+" skill level: "+_skillLvl);

		oldSkill = activeChar.addSkill(skill, true);
		if(oldSkill != null)
		{
			TimeStamp timeStamp = activeChar.getSkillReuse(oldSkill);
			if(timeStamp != null)
				activeChar.disableSkill(skill, timeStamp.getReuseCurrent());
		}

		RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
		activeChar.sendPacket(new ExEnchantSkillInfoPacket(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
		activeChar.sendSkillList();
	}
}