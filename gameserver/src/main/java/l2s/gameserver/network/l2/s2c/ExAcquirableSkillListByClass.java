package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.network.l2.ServerPacketOpcodes;
import l2s.gameserver.utils.SkillUtils;


/**
 * Reworked: VISTALL
 */
public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
	private AcquireType _type;
	private final List<Skill> _skills;

	class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int cost;
		public int requirements;
		public int subUnit;

		Skill(int id, int nextLevel, int maxLevel, int cost, int requirements, int subUnit)
		{
			this.id = id;
			this.nextLevel = nextLevel;
			this.maxLevel = maxLevel;
			this.cost = cost;
			this.requirements = requirements;
			this.subUnit = subUnit;
		}
	}

	public ExAcquirableSkillListByClass(AcquireType type, int size)
	{
		_skills = new ArrayList<Skill>(size);
		_type = type;
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int Cost, int requirements, int subUnit)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, subUnit));
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int Cost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, Cost, requirements, 0));
	}

	@Override
	protected final void writeImpl() {
		writeH(_type.ordinal());
		writeH(_skills.size());

		for(Skill temp : _skills)
		{
			writeD(temp.id);
			writeH(SkillUtils.convertHFSkillLevelToGODMask(temp.id, temp.nextLevel));
			writeH(SkillUtils.convertHFSkillLevelToGODMask(temp.id, temp.maxLevel));
			writeC(temp.requirements);
			writeQ(temp.cost);
			writeC(0x01); // UNK
			if(_type == AcquireType.SUB_UNIT)
				writeH(temp.subUnit);
		}
	}

	@Override
	protected void writeImplHF() {
		writeD(_type.ordinal());
		writeD(_skills.size());

		for(Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.cost);
			writeD(temp.requirements);
			if(_type == AcquireType.SUB_UNIT)
				writeD(temp.subUnit);
		}
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		if(isHF())
			return ServerPacketOpcodes.AcquireSkillListPacket;
		return super.getOpcodes();
	}
}