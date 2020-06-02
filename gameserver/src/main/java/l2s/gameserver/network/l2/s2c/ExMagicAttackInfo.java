package l2s.gameserver.network.l2.s2c;

/**
 *
 * @author monithly
 */
public class ExMagicAttackInfo extends L2GameServerPacket
{
	public final static int CRITICAL = 1;
	public final static int CRITICAL_HEAL = 2;
	public final static int OVERHIT = 3;
	public final static int EVADED = 4;
	public final static int BLOCKED = 5;
	public final static int RESISTED = 6;
	public final static int IMMUNE = 7;

	private final int _attackerId, _targetId, _info;

	public ExMagicAttackInfo(int attackerId, int targetId, int info)
	{
		_attackerId = attackerId;
		_targetId = targetId;
		_info = info;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_attackerId);
		writeD(_targetId);
		writeD(_info);
	}
}
