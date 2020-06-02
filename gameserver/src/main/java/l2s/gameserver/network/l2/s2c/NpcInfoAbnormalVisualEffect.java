package l2s.gameserver.network.l2.s2c;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.skills.AbnormalEffect;

/**
 * @reworked by Bonux
**/
public class NpcInfoAbnormalVisualEffect extends L2GameServerPacket
{
	private final int _objectId;
	private final int _transformId;
	private final Set<AbnormalEffect> _abnormalEffects;

	public NpcInfoAbnormalVisualEffect(Creature npc)
	{
		_objectId = npc.getObjectId();
		_transformId = 0;
		_abnormalEffects = npc.getAbnormalEffects();
	}

	@Override
	protected boolean canWriteHF()
	{
		return false;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_transformId);
		writeH(_abnormalEffects.size());
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getId());
	}
}