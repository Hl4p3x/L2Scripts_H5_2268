package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExEnchantSkillInfoPacket;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.SkillUtils;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
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

		if(_skillLvl > 100)
		{
			EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
			if(sl == null)
			{
				activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
				return;
			}

			Skill skill = SkillHolder.getInstance().getSkill(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

			if(skill == null || skill.getId() != _skillId)
			{
				activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
				return;
			}

			if(activeChar.getSkillLevel(_skillId) != skill.getLevel())
			{
				activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
				return;
			}
		}
		else if(activeChar.getSkillLevel(_skillId) != _skillLvl)
		{
			activeChar.sendMessage(new CustomMessage("common.Error", activeChar));
			return;
		}

		sendPacket(new ExEnchantSkillInfoPacket(_skillId, _skillLvl));
	}
}