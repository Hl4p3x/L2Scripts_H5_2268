package l2s.gameserver.model.entity.events.fightclubmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.utils.Location;

public class FightClubTeam implements Serializable
{
	public static enum TEAM_NAMES
	{
		Red(1453793), 
		Blue(11877953), 
		Green(4109633), 
		Yellow(3079679), 
		Gray(8421504), 
		Orange(34809), 
		Black(1447446), 
		White(16777215), 
		Violet(12199813), 
		Cyan(14934326), 
		Pink(14577135);

		public int _nameColor;

		private TEAM_NAMES(int nameColor)
		{
			_nameColor = nameColor;
		}
	}

	private int _index;
	private String _name;
	private List<FightClubPlayer> _players = new ArrayList<FightClubPlayer>();
	private Location _spawnLoc;
	private int _score;

	public FightClubTeam(int index)
	{
		_index = index;
		chooseName();
	}

	public int getIndex()
	{
		return _index;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String chooseName()
	{
		_name = TEAM_NAMES.values()[_index - 1].toString();
		return _name;
	}

	public int getNickColor()
	{
		return TEAM_NAMES.values()[_index - 1]._nameColor;
	}

	public List<FightClubPlayer> getPlayers()
	{
		return _players;
	}

	public void addPlayer(FightClubPlayer player)
	{
		_players.add(player);
	}

	public void removePlayer(FightClubPlayer player)
	{
		_players.remove(player);
	}

	public void setSpawnLoc(Location loc)
	{
		_spawnLoc = loc;
	}

	public Location getSpawnLoc()
	{
		return _spawnLoc;
	}

	public void setScore(int newScore)
	{
		_score = newScore;
	}

	public void incScore(int by)
	{
		_score += by;
	}

	public int getScore()
	{
		return _score;
	}
}