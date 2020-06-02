package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.utils.Location;

public class PetStatusUpdatePacket extends L2GameServerPacket
{
	private int type, obj_id, level;
	private int maxFed, curFed, maxHp, curHp, maxMp, curMp;
	private long exp, exp_this_lvl, exp_next_lvl;
	private Location _loc;
	private String title;

	public PetStatusUpdatePacket(final Servitor servitor)
	{
		type = servitor.getServitorType();
		obj_id = servitor.getObjectId();
		_loc = servitor.getLoc();
		title = servitor.getTitle();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		curFed = servitor.getCurrentFed();
		maxFed = servitor.getMaxFed();
		level = servitor.getLevel();
		exp = servitor.getExp();
		exp_this_lvl = servitor.getExpForThisLevel();
		exp_next_lvl = servitor.getExpForNextLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(type);
		writeD(obj_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeS(title);
		writeD(curFed);
		writeD(maxFed);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);// 0% absolute value
		writeQ(exp_next_lvl);// 100% absolute value
		writeD(0); // ???
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(type);
		writeD(obj_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeS(title);
		writeD(curFed);
		writeD(maxFed);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);// 0% absolute value
		writeQ(exp_next_lvl);// 100% absolute value
	}
}