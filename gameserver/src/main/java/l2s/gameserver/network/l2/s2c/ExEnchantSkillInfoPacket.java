package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.base.EnchantSkillLearn;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.utils.SkillUtils;

public class ExEnchantSkillInfoPacket extends L2GameServerPacket
{
	private List<Integer> _routes;

	private int _id, _level, _canAdd, canDecrease;

	public ExEnchantSkillInfoPacket(int id, int level)
	{
		_routes = new ArrayList<Integer>();
		_id = id;
		_level = level;

		// skill already enchanted?
		if(SkillUtils.isEnchantedSkill(_level))
		{
			canDecrease = 1;
			// get detail for next level
			EnchantSkillLearn nextSkill = SkillTreeTable.getSkillEnchant(_id, _level + 1);

			// if it exists add it
			if(nextSkill != null)
			{
				_canAdd = 1;
				addEnchantSkillDetail(nextSkill.getLevel());
			}

			for(EnchantSkillLearn el : SkillTreeTable.getEnchantsForChange(_id, _level))
				addEnchantSkillDetail(el.getLevel());
		}
		else
		{
			// not already enchanted
			for(EnchantSkillLearn esd : SkillTreeTable.getFirstEnchantsForSkill(_id))
			{
				_canAdd = 1;
				addEnchantSkillDetail(esd.getLevel());
			}
		}
	}

	public void addEnchantSkillDetail(int level)
	{
		_routes.add(level);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_id);
		writeD(SkillUtils.convertHFSkillLevelToGODMask(_id, _level));
		writeD(_canAdd); // can add enchant
		writeD(canDecrease); // can decrease enchant

		writeD(_routes.size());
		for(Integer route : _routes)
			writeD(SkillUtils.convertHFSkillLevelToGODMask(_id, route));
	}
	
	@Override
	protected void writeImplHF()
	{
		writeD(_id);
		writeD(_level);
		writeD(_canAdd); // can add enchant
		writeD(canDecrease); // can decrease enchant

		if(canDecrease > 0)
			_routes.add(_level);

		writeD(_routes.size());
		for(Integer route : _routes)
			writeD(route);
	}
}