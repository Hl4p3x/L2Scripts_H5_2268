package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillListPacket extends L2GameServerPacket
{
	public enum EnchantSkillType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE_ROUTE,
	}

	private final List<Skill> _skills;
	private final EnchantSkillType _type;

	class Skill
	{
		public int id;
		public int level;

		Skill(int id, int nextLevel)
		{
			this.id = id;
			level = nextLevel;
		}
	}

	public void addSkill(int id, int level)
	{
		_skills.add(new Skill(id, level));
	}

	public ExEnchantSkillListPacket(EnchantSkillType type)
	{
		_type = type;
		_skills = new ArrayList<Skill>();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type.ordinal());
		writeD(_skills.size());
		for(Skill sk : _skills)
		{
			writeD(sk.id);
			writeD(sk.level);
		}
	}
}