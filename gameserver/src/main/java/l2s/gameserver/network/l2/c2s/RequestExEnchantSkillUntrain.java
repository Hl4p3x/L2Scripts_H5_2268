package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoPacket;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillResult;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SkillUtils;

public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
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

		Skill oldSkill = activeChar.getKnownSkill(_skillId);
		if(oldSkill == null)
			return;

		if(!SkillAcquireHolder.getInstance().isSkillPossible(activeChar, oldSkill))
			return;

		int slevel = oldSkill.getDisplayLevel();

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, slevel);
		if(sl == null)
			return;

		if(SkillUtils.getSkillEnchantLevel(_skillLvl) == 0)
			_skillLvl = sl.getBaseLevel();

		if(SkillUtils.getSkillEnchantLevel(slevel) == 1)
		{
			if(_skillLvl != sl.getBaseLevel())
				return;
		}
		else
		{
			if(_skillLvl != (slevel - 1))
				return;

			if(SkillUtils.getSkillEnchantType(_skillLvl) != SkillUtils.getSkillEnchantType(slevel))
				return;
		}

		Skill newSkill;
		if(SkillUtils.getSkillEnchantLevel(_skillLvl) == 0)
			newSkill = SkillHolder.getInstance().getSkill(_skillId, _skillLvl);
		else
			newSkill = SkillHolder.getInstance().getSkill(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

		if(newSkill == null)
			return;

		if(ItemFunctions.getItemCount(activeChar, SkillTreeTable.UNTRAIN_ENCHANT_BOOK) == 0)
		{
			activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			return;
		}

		ItemFunctions.deleteItem(activeChar, SkillTreeTable.UNTRAIN_ENCHANT_BOOK, 1);

		activeChar.addExpAndSp(0, sl.getCost()[1] * sl.getCostMult());
		oldSkill = activeChar.addSkill(newSkill, true);
		if(oldSkill != null)
		{
			TimeStamp timeStamp = activeChar.getSkillReuse(oldSkill);
			if(timeStamp != null)
				activeChar.disableSkill(newSkill, timeStamp.getReuseCurrent());
		}

		if(_skillLvl > 100)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_has_been_decreased_by_1);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_became_0_and_enchant_skill_will_be_initialized);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}
		
		Log.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", "Successfully untrained", "skill ID:"+_skillId+" skill level: "+_skillLvl+" ");			
		

		activeChar.sendPacket(new ExEnchantSkillInfoPacket(_skillId, newSkill.getDisplayLevel()), ExEnchantSkillResult.SUCCESS);
		activeChar.sendSkillList();
		RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
	}
}