package l2s.gameserver.network.l2.s2c;

public class ExChangeClientEffectInfo extends L2GameServerPacket
{
	public static final ExChangeClientEffectInfo LIGHTNING = new ExChangeClientEffectInfo(2, 1, 0);
	public static final ExChangeClientEffectInfo NONE = new ExChangeClientEffectInfo(2, 2, 0);
	public static final ExChangeClientEffectInfo HEAVY_BLUR = new ExChangeClientEffectInfo(2, 4, 0);
	public static final ExChangeClientEffectInfo MODERATE_BLUR = new ExChangeClientEffectInfo(2, 5, 0);
	public static final ExChangeClientEffectInfo WEAK_BLUR = new ExChangeClientEffectInfo(2, 6, 0);
	public static final ExChangeClientEffectInfo WEAKER_BLUR = new ExChangeClientEffectInfo(2, 7, 0);
	public static final ExChangeClientEffectInfo WEAKEST_BLUR = new ExChangeClientEffectInfo(2, 8, 0);
	
	public static final ExChangeClientEffectInfo STATIC_FREYA_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo STATIC_FREYA_DESTROYED = new ExChangeClientEffectInfo(0, 0, 2);
	
	private final int _type, _key, _value;
	
	/**
	 * @param type <ul>
	 *            <li>0 - ChangeZoneState</li>
	 *            <li>1 - SetL2Fog</li>
	 *            <li>2 - postEffectData</li>
	 *            </ul>
	 * @param key
	 * @param value
	 */
	public ExChangeClientEffectInfo(int type, int key, int value)
	{
		_type = type;
		_key = key;
		_value = value;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeD(_key);
		writeD(_value);
	}

	@Override
	protected boolean canWriteHF()
	{
		return _type == 0;
	}

	@Override
	protected void writeImplHF()
	{
		writeD(_key);
		writeD(_value);
	}
}
