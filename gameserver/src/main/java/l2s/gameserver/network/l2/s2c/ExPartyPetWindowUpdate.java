package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
	private int owner_obj_id, npc_id, _type, curHp, maxHp, curMp, maxMp, level;
	private int obj_id = 0;
	private String _name;

	public ExPartyPetWindowUpdate(Servitor servitor)
	{
		obj_id = servitor.getObjectId();
		owner_obj_id = servitor.getPlayer().getObjectId();
		npc_id = servitor.getTemplate().npcId + 1000000;
		_type = servitor.getServitorType();
		_name = servitor.getName();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		level = servitor.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
		writeD(npc_id);
		writeD(_type);
		writeD(owner_obj_id);
		writeS(_name);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
	}
}