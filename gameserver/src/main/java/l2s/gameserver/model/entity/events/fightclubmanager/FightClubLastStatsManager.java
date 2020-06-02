package l2s.gameserver.model.entity.events.fightclubmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.model.Player;

public class FightClubLastStatsManager
{
	private static class SortRanking implements Comparator<FightClubLastPlayerStats>, Serializable
	{
		private static final long serialVersionUID = 7691414259610932752L;

		@Override
		public int compare(FightClubLastPlayerStats o1, FightClubLastPlayerStats o2)
		{
			return Integer.compare(o2.getScore(), o1.getScore());
		}
	}

	public static enum FightClubStatType
	{
		KILL_PLAYER("Kill Player");

		private final String _name;

		private FightClubStatType(String name)
		{
			_name = name;
		}

		public String getName()
		{
			return _name;
		}
	}

	private static FightClubLastStatsManager _instance;

	public static FightClubLastStatsManager getInstance()
	{
		if(_instance == null)
			_instance = new FightClubLastStatsManager();
		return _instance;
	}

	private List<FightClubLastPlayerStats> _allStats;

	public FightClubLastStatsManager()
	{
		_allStats = new CopyOnWriteArrayList<FightClubLastPlayerStats>();
	}

	public void updateStat(Player player, FightClubStatType type, int score)
	{
		FightClubLastPlayerStats myStat = getMyStat(player);

		if(myStat == null)
		{
			myStat = new FightClubLastPlayerStats(player, type.getName(), score);
			_allStats.add(myStat);
		}
		else
			myStat.setScore(score);
	}

	private FightClubLastPlayerStats getMyStat(Player player)
	{
		for(FightClubLastPlayerStats stat : _allStats)
		{
			if(stat.isMyStat(player))
				return stat;
		}
		return null;
	}

	public List<FightClubLastPlayerStats> getStats(boolean sortByScore)
	{
		List<FightClubLastPlayerStats> listToSort = new ArrayList<FightClubLastPlayerStats>();
		listToSort.addAll(_allStats);
		if(sortByScore)
			Collections.sort(listToSort, new SortRanking());

		return listToSort;
	}

	public void clearStats()
	{
		_allStats.clear();
	}
}