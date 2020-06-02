package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoDetailPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.SkillUtils;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private static final int TYPE_NORMAL_ENCHANT = 0;
	private static final int TYPE_SAFE_ENCHANT = 1;
	private static final int TYPE_UNTRAIN_ENCHANT = 2;
	private static final int TYPE_CHANGE_ENCHANT = 3;
	private static final int TYPE_IMMORTAL_ENCHANT = 4;

	private int _type;
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_type = readD();
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

		int bookId = 0;
		int sp = 0;
		int adenaCount = 0;
		double spMult = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;

		EnchantSkillLearn esd = null;

		switch(_type)
		{
			case TYPE_NORMAL_ENCHANT:
				if(_skillLvl % 100 == 1)
					bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				break;
			case TYPE_SAFE_ENCHANT:
				bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				spMult = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
			case TYPE_UNTRAIN_ENCHANT:
				bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl + 1);
				break;
			case TYPE_CHANGE_ENCHANT:
				bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
				esd = SkillTreeTable.getEnchantsForChange(_skillId, _skillLvl).get(0);
				spMult = 1f / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
			case TYPE_IMMORTAL_ENCHANT:
				bookId = SkillTreeTable.IMMORTAL_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				spMult = 0.;
				break;
		}

		if(esd == null)
			return;

		spMult *= esd.getCostMult();
		int[] cost = esd.getCost();

		sp = (int) (cost[1] * spMult);

		if(_type != TYPE_UNTRAIN_ENCHANT)
			adenaCount = (int) (cost[0] * spMult);

		// send skill enchantment detail
		activeChar.sendPacket(new ExEnchantSkillInfoDetailPacket(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, adenaCount));
	}
}