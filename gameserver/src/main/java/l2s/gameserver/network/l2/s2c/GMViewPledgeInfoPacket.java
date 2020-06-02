package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;

public class GMViewPledgeInfoPacket extends L2GameServerPacket
{
	private String char_name, clan_name, leader_name, ally_name;
	private int clan_id, clan_crest_id, clan_level, rank, rep, ally_id, ally_crest_id;
	private int hasCastle, hasHideout, hasFortress;
	private boolean atWar, _isDisbanded;
	private List<PledgeMemberInfo> infos = new ArrayList<PledgeMemberInfo>();

	public GMViewPledgeInfoPacket(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		for(UnitMember member : clan)
		{
			if(member == null)
				continue;
			char_name = member.getName();
			clan_level = member.getLevel();
			clan_id = member.getClassId();
			clan_crest_id = member.isOnline() ? member.getObjectId() : 0;
			rep = member.getSponsor() != 0 ? 1 : 0;
			infos.add(new PledgeMemberInfo(char_name, clan_level, clan_id, clan_crest_id, member.getSex(), 1, rep));
		}

		char_name = activeChar.getName();
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		leader_name = clan.getLeaderName();
		clan_crest_id = clan.getCrestId();
		clan_level = clan.getLevel();
		hasCastle = clan.getCastle();
		hasHideout = clan.getHasHideout();
		hasFortress = clan.getHasFortress();
		rank = clan.getRank();
		rep = clan.getReputationScore();
		ally_id = clan.getAllyId();
		if(clan.getAlliance() != null)
		{
			ally_name = clan.getAlliance().getAllyName();
			ally_crest_id = clan.getAlliance().getAllyCrestId();
		}
		else
		{
			ally_name = "";
			ally_crest_id = 0;
		}
		atWar = clan.isAtWar();
		_isDisbanded = clan.isPlacedForDisband();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x00);
		writeS(char_name);
		writeD(clan_id);
		writeD(0x00);
		writeS(clan_name);
		writeS(leader_name);

		writeD(clan_crest_id); // crest id .. is used again
		writeD(clan_level);
		writeD(hasCastle);
		writeD(0);
		writeD(hasHideout);
		writeD(hasFortress);
		writeD(rank);
		writeD(rep);
		writeD(_isDisbanded ? 3 : 0);
		writeD(0x00);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(atWar);
		writeD(0x00);//territory Id

		writeD(infos.size());
		for(PledgeMemberInfo _info : infos)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex);
			writeD(_info.race);
			writeD(_info.online);
			writeD(_info.sponsor);
			writeC(0x00);
		}
		infos.clear();
	}

	@Override
	protected final void writeImplHF()
	{
		writeS(char_name);
		writeD(clan_id);
		writeD(0x00);
		writeS(clan_name);
		writeS(leader_name);

		writeD(clan_crest_id);
		writeD(clan_level);
		writeD(hasCastle);
		writeD(hasHideout);
		writeD(hasFortress);
		writeD(rank);
		writeD(rep);
		writeD(_isDisbanded ? 3 : 0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(atWar);
		writeD(0); // Territory castle ID

		writeD(infos.size());
		for(PledgeMemberInfo _info : infos)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex);
			writeD(_info.race);
			writeD(_info.online);
			writeD(_info.sponsor);
		}
		infos.clear();
	}

	static class PledgeMemberInfo
	{
		public String _name;
		public int level, class_id, online, sex, race, sponsor;

		public PledgeMemberInfo(String __name, int _level, int _class_id, int _online, int _sex, int _race, int _sponsor)
		{
			_name = __name;
			level = _level;
			class_id = _class_id;
			online = _online;
			sex = _sex;
			race = _race;
			sponsor = _sponsor;
		}
	}
}