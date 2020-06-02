package data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import templates.TvTArena;

public final class TvTArenaHolder extends AbstractHolder
{
	private static final TvTArenaHolder _instance = new TvTArenaHolder();

	private final TIntObjectMap<TvTArena> _arenas = new TIntObjectHashMap<TvTArena>();

	public static TvTArenaHolder getInstance()
	{
		return _instance;
	}

	public void addArena(TvTArena arena)
	{
		_arenas.put(arena.getId(), arena);
	}

	public TvTArena getArena(int id)
	{
		return _arenas.get(id);
	}

	public TvTArena[] getArenas()
	{
		return _arenas.values(new TvTArena[_arenas.size()]);
	}

	@Override
	public int size()
	{
		return _arenas.size();
	}

	@Override
	public void clear()
	{
		_arenas.clear();
	}
}
