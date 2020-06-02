package l2s.gameserver.model.entity.events.fightclubmanager;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;

public class FightClubLastPlayerStats
{
	private String _playerNickName;
	private ClassId _classId;
	private String _clanName;
	private String _allyName;
	private String _typeName;
	private int _score;

	public FightClubLastPlayerStats(Player player, String typeName, int score)
	{
		_playerNickName = player.getName();
		_clanName = player.getClan() != null ? player.getClan().getName() : "<br>";
		_allyName = player.getAlliance() != null ? player.getAlliance().getAllyName() : "<br>";
		_classId = player.getClassId();
		_typeName = typeName;
		_score = score;
	}

	public boolean isMyStat(Player player)
	{
		return _playerNickName.equals(player.getName());
	}

	public String getPlayerName()
	{
		return _playerNickName;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public String getAllyName()
	{
		return _allyName;
	}

	public ClassId getClassId()
	{
		return _classId;
	}

	public String getTypeName()
	{
		return _typeName;
	}

	public int getScore()
	{
		return _score;
	}

	public void setScore(int i)
	{
		_score = i;
	}
}