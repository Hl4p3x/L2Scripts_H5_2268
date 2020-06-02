package l2s.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadUserInfoPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.StatsSet;

public class OlympiadTeam
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManager.class);

	private OlympiadGame _game;
	private IntObjectMap<TeamMember> _members;
	private String _name = "";
	private int _side;
	private double _damage;

	public OlympiadTeam(OlympiadGame game, int side)
	{
		_game = game;
		_side = side;
		_members = new CHashIntObjectMap<TeamMember>();
	}

	public void addMember(int obj_id)
	{
		String player_name = "";
		Player player = GameObjectsStorage.getPlayer(obj_id);
		if(player != null)
			player_name = player.getName();
		else
		{
			StatsSet noble = Olympiad._nobles.get(obj_id);
			if(noble != null)
				player_name = noble.getString(Olympiad.CHAR_NAME, "");
		}

		_members.put(obj_id, new TeamMember(obj_id, player_name, player, _game, _side));

		_name = player_name;
	}

	public void addDamage(Player player, double damage)
	{
		_damage += damage;

		TeamMember member = _members.get(player.getObjectId());
		member.addDamage(damage);
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getName()
	{
		return _name;
	}

	public void portPlayersToArena()
	{
		for(TeamMember member : _members.valueCollection())
			member.portPlayerToArena();
	}

	public void portPlayersBack()
	{
		for(TeamMember member : _members.valueCollection())
			member.portPlayerBack();
	}
	public void restoreAll()
	{
		for(TeamMember member : _members.valueCollection())
			member.restoreAll();	
	}
	public void preparePlayers()
	{
		for(TeamMember member : _members.valueCollection())
			member.preparePlayer();

		if(_members.size() <= 1)
			return;

		List<Player> list = new ArrayList<Player>();
		for(TeamMember member : _members.valueCollection())
		{
			Player player = member.getPlayer();
			if(player != null)
			{
				list.add(player);
				player.leaveParty();
			}
		}

		if(list.size() <= 1)
			return;

		Player leader = list.get(0);
		if(leader == null)
			return;

		Party party = new Party(leader, 0);
		leader.setParty(party);

		for(Player player : list)
			if(player != leader)
				player.joinParty(party);
	}

	public void takePointsForCrash()
	{
		for(TeamMember member : _members.valueCollection())
			member.takePointsForCrash();
	}

	public boolean checkPlayers()
	{
		for(TeamMember member : _members.valueCollection())
			if(member.checkPlayer())
				return true;
		return false;
	}

	public boolean isAllDead()
	{
		for(TeamMember member : _members.valueCollection())
			if(!member.isDead() && member.checkPlayer())
				return false;
		return true;
	}

	public boolean contains(int objId)
	{
		return _members.containsKey(objId);
	}

	public List<Player> getPlayers()
	{
		List<Player> players = new ArrayList<Player>(_members.size());
		for(TeamMember member : _members.valueCollection())
		{
			Player player = member.getPlayer();
			if(player != null)
				players.add(player);
		}
		return players;
	}

	public Collection<TeamMember> getMembers()
	{
		return _members.valueCollection();
	}

	public void broadcast(L2GameServerPacket p)
	{
		for(TeamMember member : _members.valueCollection())
		{
			Player player = member.getPlayer();
			if(player != null)
				player.sendPacket(p);
		}
	}

	public void broadcast(IStaticPacket p)
	{
		for(TeamMember member : _members.valueCollection())
		{
			Player player = member.getPlayer();
			if(player != null)
				player.sendPacket(p);
		}
	}

	public void broadcastInfo()
	{
		for(TeamMember member : _members.valueCollection())
		{
			Player player = member.getPlayer();
			if(player != null)
				player.broadcastPacket(new ExOlympiadUserInfoPacket(player, player.getOlympiadSide()));
		}
	}

	public boolean logout(Player player)
	{
		if(player != null)
			for(TeamMember member : _members.valueCollection())
			{
				Player pl = member.getPlayer();
				if(pl != null && pl == player)
					member.logout();
			}
		return checkPlayers();
	}

	public boolean doDie(Player player)
	{
		if(player != null)
			for(TeamMember member : _members.valueCollection())
			{
				Player pl = member.getPlayer();
				if(pl != null && pl == player)
					member.doDie();
			}
		return isAllDead();
	}

	public void saveNobleData()
	{
		for(TeamMember member : _members.valueCollection())
			member.saveNobleData();
	}
}