package l2s.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.HtmlUtils;

public class Hero
{
	private static final Logger _log = LoggerFactory.getLogger(Hero.class);

	private static Hero _instance;
	private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
	private static final String GET_ALL_HEROES = "SELECT * FROM heroes";

	private static Map<Integer, StatsSet> _heroes;
	private static Map<Integer, StatsSet> _completeHeroes;

	private static Map<Integer, List<HeroDiary>> _herodiary;
	private static Map<Integer, StatsSet> _heroMessage;

	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	public static final String ACTIVE = "active";
	public static final String MESSAGE = "message";  //TODO [VISTALL]

	public static Hero getInstance()
	{
		if(_instance == null)
			_instance = new Hero();
		return _instance;
	}

	public Hero()
	{
		init();
	}

	private static void HeroSetClanAndAlly(int charId, StatsSet hero)
	{
		Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(charId);
		hero.set(CLAN_CREST, e.getKey() == null ? 0 : e.getKey().getCrestId());
		hero.set(CLAN_NAME, e.getKey() == null ? "" : e.getKey().getName());
		hero.set(ALLY_CREST, e.getValue() == null ? 0 : e.getValue().getAllyCrestId());
		hero.set(ALLY_NAME, e.getValue() == null ? "" : e.getValue().getAllyName());
		e = null;
	}

	private void init()
	{
		_heroes = new ConcurrentHashMap<Integer, StatsSet>();
		_completeHeroes = new ConcurrentHashMap<Integer, StatsSet>();
		_herodiary = new ConcurrentHashMap<Integer, List<HeroDiary>>();
		_heroMessage = new ConcurrentHashMap<Integer, StatsSet>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_HEROES);
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, Olympiad.getNobleName(charId));
				hero.set(Olympiad.CLASS_ID, Olympiad.getNobleClass(charId));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				hero.set(ACTIVE, rset.getInt(ACTIVE));
				HeroSetClanAndAlly(charId, hero);
				loadDiary(charId);
				_heroes.put(charId, hero);
			}
			DbUtils.close(statement, rset);

			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, Olympiad.getNobleName(charId));
				hero.set(Olympiad.CLASS_ID, Olympiad.getNobleClass(charId));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				hero.set(ACTIVE, rset.getInt(ACTIVE));
				HeroSetClanAndAlly(charId, hero);
				_completeHeroes.put(charId, hero);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt load Heroes", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		_log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		_log.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
	}

	public Map<Integer, StatsSet> getHeroes()
	{
		return _heroes;
	}

	public synchronized void clearHeroes()
	{
		mysql.set("UPDATE heroes SET played = 0, active = 0");

		if(!_heroes.isEmpty())
			for(StatsSet hero : _heroes.values())
			{
				if(hero.getInteger(ACTIVE) == 0)
					continue;

				String name = hero.getString(Olympiad.CHAR_NAME);

				Player player = World.getPlayer(name);

				if(player != null)
				{
					PcInventory inventory = player.getInventory();
					inventory.writeLock();
					try
					{
						for(ItemInstance item : player.getInventory().getItems())
							if(item.isHeroWeapon())
								player.getInventory().destroyItem(item);
					}
					finally
					{
						inventory.writeUnlock();
					}

					player.setHero(false);
					removeSkills(player);
					player.sendSkillList();
					player.updatePledgeClass();
					player.broadcastUserInfo(true);
				}
			}

		_heroes.clear();
		_herodiary.clear();
	}

	public synchronized boolean computeNewHeroes(List<StatsSet> newHeroes)
	{
		if(newHeroes.isEmpty())
			return true;

		Map<Integer, StatsSet> heroes = new ConcurrentHashMap<Integer, StatsSet>();
		boolean error = false;

		for(StatsSet hero : newHeroes)
		{
			int charId = hero.getInteger(Olympiad.CHAR_ID);

			if(_completeHeroes != null && _completeHeroes.containsKey(charId))
			{
				StatsSet oldHero = _completeHeroes.get(charId);
				int count = oldHero.getInteger(COUNT);
				oldHero.set(COUNT, count + 1);
				oldHero.set(PLAYED, 1);
				oldHero.set(ACTIVE, 0);

				heroes.put(charId, oldHero);
			}
			else
			{
				StatsSet newHero = new StatsSet();
				newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
				newHero.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
				newHero.set(COUNT, 1);
				newHero.set(PLAYED, 1);
				newHero.set(ACTIVE, 0);

				heroes.put(charId, newHero);
			}

			addHeroDiary(charId, HeroDiary.ACTION_HERO_GAINED, 0);
			loadDiary(charId);
		}

		_heroes.putAll(heroes);
		heroes.clear();

		updateHeroes(0);

		return error;
	}

	public void updateHeroes(int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO heroes (char_id, count, played, active) VALUES (?,?,?,?)");

			for(Integer heroId : _heroes.keySet())
			{
				if(id > 0 && heroId != id)
					continue;
				StatsSet hero = _heroes.get(heroId);
				statement.setInt(1, heroId);
				statement.setInt(2, hero.getInteger(COUNT));
				statement.setInt(3, hero.getInteger(PLAYED));
				statement.setInt(4, hero.getInteger(ACTIVE));
				statement.execute();
				if(_completeHeroes != null && !_completeHeroes.containsKey(heroId))
				{
					HeroSetClanAndAlly(heroId, hero);
					_completeHeroes.put(heroId, hero);
				}
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt update Heroes");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	public static void deleteHero(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO heroes (char_id, count, played, active) VALUES (?,?,?,?)");

			for(Integer heroId : _heroes.keySet())
			{
				int id = player.getObjectId();
				if(id > 0 && heroId != id)
					continue;
				StatsSet hero = _heroes.get(heroId);
				statement.setInt(1, heroId);
				statement.setInt(2, hero.getInteger(COUNT));
				statement.setInt(3, hero.getInteger(PLAYED));
				statement.setInt(4, 0);
				statement.execute();
				if(_completeHeroes != null && !_completeHeroes.containsKey(heroId))
				{
					_completeHeroes.remove(heroId);
				}
			}
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt update Heroes");
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger(ACTIVE) == 1)
			return true;
		return false;
	}

	public boolean isInactiveHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger(ACTIVE) == 0)
			return true;
		return false;
	}

	public void activateHero(Player player)
	{
		StatsSet hero = _heroes.get(player.getObjectId());
		hero.set(ACTIVE, 1);
		_heroes.remove(player.getObjectId());
		_heroes.put(player.getObjectId(), hero);

		if(!player.isSubClassActive())
		{
			addSkills(player);
			player.sendSkillList();
		}

		player.setHero(true);
		player.updatePledgeClass();
		player.broadcastPacket(new SocialActionPacket(player.getObjectId(), SocialActionPacket.GIVE_HERO));
		if(player.getClan() != null && player.getClan().getLevel() >= 5)
		{
			player.getClan().incReputation((int) (1000 * Config.CLAN_REPUTATION_MOD_ON_RECEIVE_HERO), true, "Hero:activateHero:" + player);
			player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addString(player.getName()).addNumber(Math.round(1000 * Config.RATE_CLAN_REP_SCORE)), player);
		}
		player.broadcastUserInfo(true);
		updateHeroes(player.getObjectId());
	}

	public static void addSkills(Player player)
	{
		player.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HEROIC_MIRACLE, 1));
		player.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HEROIC_BERSERKER, 1));
		player.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HEROIC_VALOR, 1));
		player.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HEROIC_GRANDEUR, 1));
		player.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HEROIC_DREAD, 1));
	}

	public static void removeSkills(Player player)
	{
		player.removeSkillById(Skill.SKILL_HEROIC_MIRACLE);
		player.removeSkillById(Skill.SKILL_HEROIC_BERSERKER);
		player.removeSkillById(Skill.SKILL_HEROIC_VALOR);
		player.removeSkillById(Skill.SKILL_HEROIC_GRANDEUR);
		player.removeSkillById(Skill.SKILL_HEROIC_DREAD);
	}

	public void loadDiary(int charId)
	{
		List<HeroDiary> diary = new ArrayList<HeroDiary>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, charId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				long time = rset.getLong("time");
				int action = rset.getInt("action");
				int param = rset.getInt("param");

				HeroDiary d = new HeroDiary(action,  time, param);
				diary.add(d);
			}

			_herodiary.put(charId, diary);

			if(Config.ALT_DEBUG_ENABLED)
				_log.info("Hero System: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
		}
		catch(SQLException e)
		{
			_log.warn("Hero System: Couldnt load Hero Diary for CharId: " + charId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void showHeroDiary(Player activeChar, int heroclass, int charid, int page)
	{
		final int perpage = 10;

		List<HeroDiary> mainlist = _herodiary.get(charid);

		if(mainlist != null)
		{
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(activeChar, null);
			html.setFile("olympiad/monument_hero_info.htm");
			html.replace("%title%", StringsHolder.getInstance().getString(activeChar, "hero.diary"));
			html.replace("%heroname%", Olympiad.getNobleName(charid));
			html.replace("%message%", _heroMessage.get(charid) == null ? "" : _heroMessage.get(charid).getString(MESSAGE));

			List<HeroDiary> list = new ArrayList<HeroDiary>(mainlist);

			Collections.reverse(list);

			boolean color = true;
			final StringBuilder fList = new StringBuilder(500);
			int counter = 0;
			int breakat = 0;
			for(int i = (page - 1) * perpage; i < list.size(); i++)
			{
				breakat = i;
				HeroDiary diary = list.get(i);
				Map.Entry<String, String> entry = diary.toString(activeChar);

				fList.append("<tr><td>");
				if(color)
					fList.append("<table width=270 bgcolor=\"131210\">");
				else
					fList.append("<table width=270>");
				fList.append("<tr><td width=270><font color=\"LEVEL\">" + entry.getKey() + "</font></td></tr>");
				fList.append("<tr><td width=270>" + entry.getValue() + "</td></tr>");
				fList.append("<tr><td>&nbsp;</td></tr></table>");
				fList.append("</td></tr>");
				color = !color;
				counter++;
				if(counter >= perpage)
					break;
			}

			if(breakat < list.size() - 1)
			{
				html.replace("%buttprev%", HtmlUtils.PREV_BUTTON);
				html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
			}
			else
				html.replace("%buttprev%", StringUtils.EMPTY);

			if(page > 1)
			{
				html.replace("%buttnext%", HtmlUtils.NEXT_BUTTON);
				html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
			}
			else
				html.replace("%buttnext%", StringUtils.EMPTY);

			html.replace("%list%", fList.toString());

			activeChar.sendPacket(html);
		}
	}

	public void addHeroDiary(int playerId, int id, int param)
	{
		insertHeroDiary(playerId, id, param);

		List<HeroDiary> list = _herodiary.get(playerId);
		if(list != null)
			list.add(new HeroDiary(id, System.currentTimeMillis(), param)) ;
	}

	private void insertHeroDiary(int charId, int action, int param)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.error("SQL exception while saving DiaryData.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void setHeroMessage(int charId, String message)
	{
		_heroMessage.get(charId).set(MESSAGE, message);
	}

	public void saveHeroMessage(int charId)
	{
		if(_heroMessage.get(charId) == null)
			return;

		Connection con = null;
		PreparedStatement statement = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?;");

			statement.setString(1, _heroMessage.get(charId).getString(MESSAGE));
			statement.setInt(2, charId);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.error("SQL exception while saving HeroMessage.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void shutdown()
	{
		for(int charId : _heroMessage.keySet())
			saveHeroMessage(charId);
	}

	public int getHeroByClass(int classid)
	{
		if(!_heroes.isEmpty())
			for(Integer heroId : _heroes.keySet())
			{
				StatsSet hero = _heroes.get(heroId);
				if(hero.getInteger(Olympiad.CLASS_ID) == classid)
					return heroId;
			}
		return 0;
	}

	public Map.Entry<Integer, StatsSet> getHeroStats(int classId)
	{
		if(!_heroes.isEmpty())
		{
			for(Map.Entry<Integer, StatsSet> entry : _heroes.entrySet())
			{
				if(entry.getValue().getInteger(Olympiad.CLASS_ID) == classId)
					return entry;
			}
		}
		return null;
	}
}