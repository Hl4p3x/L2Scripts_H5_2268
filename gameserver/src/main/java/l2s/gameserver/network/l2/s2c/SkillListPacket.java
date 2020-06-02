package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.SkillUtils;

/**
 * format   d (dddc)
 */
public class SkillListPacket extends L2GameServerPacket
{
	private Collection<Skill> _skills;
	private boolean canEnchant;
	private Player activeChar;
	private final int _learnedSkillId;

	public SkillListPacket(Player player)
	{
		_skills = player.getAllSkills();
		canEnchant = player.getTransformation() == 0;
		activeChar = player;
		_learnedSkillId = 0;
	}

	public SkillListPacket(Player player, int learnedSkillId)
	{
		_skills = player.getAllSkills();
		canEnchant = player.getTransformation() == 0;
		activeChar = player;
		_learnedSkillId = learnedSkillId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_skills.size());

		for (Skill temp : _skills)
		{
			writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			writeD(SkillUtils.convertHFSkillLevelToGODMask(temp.getDisplayId(), temp.getDisplayLevel()));
			writeD(temp.getDisplayId());
			writeD(temp.getId()); // Reuse Id
			writeC(activeChar.isUnActiveSkill(temp.getId()) ? 0x01 : 0x00); // иконка скилла серая если не 0
			writeC(canEnchant ? SkillTreeTable.isEnchantable(activeChar, temp) : 0); // для заточки: если 1 скилл можно точить
		}
		writeD(_learnedSkillId);
	}
	
	@Override
	protected final void writeImplHF()
	{
		writeD(_skills.size());

		for(Skill temp : _skills)
		{
			writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			writeD(temp.getDisplayLevel());
			writeD(temp.getDisplayId());
			writeC(activeChar.isUnActiveSkill(temp.getId()) ? 0x01 : 0x00); // иконка скилла серая если не 0
			writeC(canEnchant ? SkillTreeTable.isEnchantable(activeChar, temp) : 0); // для заточки: если 1 скилл можно точить
		}
	}
}