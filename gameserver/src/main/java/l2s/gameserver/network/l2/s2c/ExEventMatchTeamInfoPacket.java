package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;

public class ExEventMatchTeamInfoPacket extends L2GameServerPacket
{
	@SuppressWarnings("unused")
	private int leader_id, loot;
	private List<EventMatchTeamInfo> members = new ArrayList<EventMatchTeamInfo>();

	public ExEventMatchTeamInfoPacket(List<Player> party, Player exclude)
	{
		leader_id = party.get(0).getObjectId();
		loot = party.get(0).getParty().getLootDistribution();

		for(Player member : party)
			if(!member.equals(exclude))
				members.add(new EventMatchTeamInfo(member));
	}

	@Override
	protected void writeImpl()
	{
		// TODO dcd[dSdddddddddd]
	}

	public static class EventMatchTeamInfo
	{
		public String _name, pet_Name;
		public int _id, curCp, maxCp, curHp, maxHp, curMp, maxMp, level, class_id, race_id;
		public int pet_id, pet_NpcId, pet_curHp, pet_maxHp, pet_curMp, pet_maxMp, pet_level;

		public EventMatchTeamInfo(Player member)
		{
			_name = member.getName();
			_id = member.getObjectId();
			curCp = (int) member.getCurrentCp();
			maxCp = member.getMaxCp();
			curHp = (int) member.getCurrentHp();
			maxHp = member.getMaxHp();
			curMp = (int) member.getCurrentMp();
			maxMp = member.getMaxMp();
			level = member.getLevel();
			class_id = member.getClassId().getId();
			race_id = member.getRace().ordinal();

			Servitor servitor = member.getServitor();
			if(servitor != null)
			{
				pet_id = servitor.getObjectId();
				pet_NpcId = servitor.getNpcId() + 1000000;
				pet_Name = servitor.getName();
				pet_curHp = (int) servitor.getCurrentHp();
				pet_maxHp = servitor.getMaxHp();
				pet_curMp = (int) servitor.getCurrentMp();
				pet_maxMp = servitor.getMaxMp();
				pet_level = servitor.getLevel();
			}
			else
				pet_id = 0;
		}
	}
}