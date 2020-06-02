package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PartySmallWindowAddPacket extends L2GameServerPacket
{
	private int _leaderObjectId, objectId, _loot;
	private final PartySmallWindowAllPacket.PartySmallWindowMemberInfo member;

	public PartySmallWindowAddPacket(Player player, Player member)
	{
		_leaderObjectId = member.getParty().getPartyLeader().getObjectId();
		objectId = player.getObjectId();
		_loot = member.getParty().getLootDistribution();
		this.member = new PartySmallWindowAllPacket.PartySmallWindowMemberInfo(member);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_leaderObjectId);
		writeD(_loot);
		writeD(member._id);
		writeS(member._name);
		writeD(member.curCp);
		writeD(member.maxCp);
		writeD(member.curHp);
		writeD(member.maxHp);
		writeD(member.curMp);
		writeD(member.maxMp);
		writeD(member.vitality * 7);
		writeC(member.level);
		writeH(member.class_id);
		writeC(member.sex);
		writeH(member.race_id);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(objectId); // c3
		writeD(_loot);
		writeD(member._id);
		writeS(member._name);
		writeD(member.curCp);
		writeD(member.maxCp);
		writeD(member.curHp);
		writeD(member.maxHp);
		writeD(member.curMp);
		writeD(member.maxMp);
		writeD(member.level);
		writeD(member.class_id);
		writeC(member.sex);
		writeD(member.race_id);
		writeD(0);
		writeD(0);
	}
}