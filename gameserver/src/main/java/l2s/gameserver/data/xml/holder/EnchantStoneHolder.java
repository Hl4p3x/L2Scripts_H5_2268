package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.support.EnchantStone;

/**
 * @author Bonux
**/
public final class EnchantStoneHolder extends AbstractHolder
{
	private static final EnchantStoneHolder _instance = new EnchantStoneHolder();

	private TIntObjectMap<EnchantStone> _stones = new TIntObjectHashMap<EnchantStone>();

	public static EnchantStoneHolder getInstance()
	{
		return _instance;
	}

	public void addEnchantStone(EnchantStone stone)
	{
		_stones.put(stone.getItemId(), stone);
	}

	public EnchantStone getEnchantStone(int id)
	{
		return _stones.get(id);
	}

	@Override
	public int size()
	{
		return _stones.size();
	}

	@Override
	public void clear()
	{
		_stones.clear();
	}
}