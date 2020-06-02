package l2s.gameserver.network.l2.s2c;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdatePacket extends L2GameServerPacket
{
	private int clan_id, clan_level, clan_rank, clan_rep, crest_id, ally_id, ally_crest, _territorySide;
	private boolean atwar;
	private String ally_name = StringUtils.EMPTY;
	private int HasCastle, HasHideout, HasFortress;
	private boolean _isDisbanded;

	public PledgeShowInfoUpdatePacket(final Clan clan)
	{
		clan_id = clan.getClanId();
		clan_level = clan.getLevel();
		HasCastle = clan.getCastle();
		HasHideout = clan.getHasHideout();
		HasFortress = clan.getHasFortress();
		clan_rank = clan.getRank();
		clan_rep = clan.getReputationScore();
		crest_id = clan.getCrestId();
		ally_id = clan.getAllyId();
		atwar = clan.isAtWar();
		_territorySide = clan.getWarDominion();
		_isDisbanded = clan.isPlacedForDisband();
		Alliance ally = clan.getAlliance();
		if(ally != null)
		{
			ally_name = ally.getAllyName();
			ally_crest = ally.getAllyCrestId();
		}
	}

	@Override
	protected final void writeImpl()
	{
		//sending empty data so client will ask all the info in response ;)
		writeD(clan_id);
		writeD(Config.REQUEST_ID);
		writeD(crest_id);
		writeD(clan_level);
		writeD(HasCastle);
		writeD(0);
		writeD(HasHideout);
		writeD(HasFortress);
		writeD(clan_rank);// displayed in the "tree" view (with the clan skills)
		writeD(clan_rep);
		writeD(_isDisbanded ? 3 : 0);
		writeD(0);
		writeD(ally_id); //c5
		writeS(ally_name); //c5
		writeD(ally_crest); //c5
		writeD(atwar); //c5

		writeD(0x00);
		writeD(_territorySide);
	}

	@Override
	protected final void writeImplHF()
	{
		//sending empty data so client will ask all the info in response ;)
		writeD(clan_id);
		writeD(crest_id);
		writeD(clan_level);
		writeD(HasCastle);
		writeD(HasHideout);
		writeD(HasFortress);
		writeD(clan_rank);// displayed in the "tree" view (with the clan skills)
		writeD(clan_rep);
		writeD(_isDisbanded ? 3 : 0);
		writeD(0);
		writeD(ally_id); //c5
		writeS(ally_name); //c5
		writeD(ally_crest); //c5
		writeD(atwar); //c5

		writeD(0x00);
		writeD(_territorySide);
	}
}