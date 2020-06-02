package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.SkillUtils;

public class GMViewSkillInfoPacket extends L2GameServerPacket
{
	private String _charName;
	private Collection<Skill> _skills;
	private Player _targetChar;

	public GMViewSkillInfoPacket(Player cha)
	{
		_charName = cha.getName();
		_skills = cha.getAllSkills();
		_targetChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_charName);
		writeD(_skills.size());
		for(Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(SkillUtils.convertHFSkillLevelToGODMask(skill.getId(), skill.getDisplayLevel()));
			writeD(skill.getId());
			writeD(skill.getId()); // Reuse ID
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 0x01 : 0x00);
			writeC(SkillHolder.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
		writeD(0x00);
	}

	@Override
	protected final void writeImplHF()
	{
		writeS(_charName);
		writeD(_skills.size());
		for(Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 0x01 : 0x00);
			writeC(SkillHolder.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
	}
}