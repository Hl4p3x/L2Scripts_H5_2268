package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;


/**
 * Format:(c) dddddds
 */
public class ListPartyWaitingPacket extends L2GameServerPacket
{
	private Collection<MatchingRoom> _rooms;
	private int _fullSize;

	public ListPartyWaitingPacket(int region, boolean allLevels, int page, Player activeChar)
	{
		int first = (page - 1) * 64;
		int firstNot = page * 64;
		_rooms = new ArrayList<MatchingRoom>();

		int i = 0;
		List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
		_fullSize = temp.size();
		for(MatchingRoom room : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			_rooms.add(room);
			i++;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fullSize);
		writeD(_rooms.size());

		for(MatchingRoom room : _rooms)
		{
			writeD(room.getId()); //room id
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			writeD(room.getLocationId());
			writeD(room.getMinLevel()); //min level
			writeD(room.getMaxLevel()); //max level
			writeD(room.getMaxMembersSize()); //max members coun
			writeS(room.getTopic()); // room name

			Collection<Player> players = room.getPlayers();
			writeD(players.size()); //members count
			for(Player player : players)
			{
				writeD(player.getClassId().getId());
				writeS(player.getName());
			}
		}
		writeD(0x00); // Total amount of parties
		writeD(0x00); // Total amount of party members
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_fullSize);
		writeD(_rooms.size());

		for(MatchingRoom room : _rooms)
		{
			writeD(room.getId()); //room id
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			writeD(room.getLocationId());
			writeD(room.getMinLevel()); //min level
			writeD(room.getMaxLevel()); //max level
			writeD(room.getMaxMembersSize()); //max members coun
			writeS(room.getTopic()); // room name

			Collection<Player> players = room.getPlayers();
			writeD(players.size()); //members count
			for(Player player : players)
			{
				writeD(player.getClassId().getId());
				writeS(player.getName());
			}
		}
	}
}