package l2s.gameserver.model.entity.olympiad;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExOlympiadMatchEndPacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadModePacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.FixEnchantOlympiad;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;

public class TeamMember
{
	private static final Logger _log = LoggerFactory.getLogger(TeamMember.class);

	private String _name = StringUtils.EMPTY;
	private String _clanName = StringUtils.EMPTY;
	private int _classId;
	private int _clanId;
	private double _damage;
	private boolean _isDead;

	private final int _objId;
	private final OlympiadGame _game;
	private final CompType _type;
	private final int _side;

	private Player _player;
	private Location _returnLoc = null;

	public boolean isDead()
	{
		return _isDead;
	}

	public void doDie()
	{
		_isDead = true;
	}

	public TeamMember(int obj_id, String name, Player player, OlympiadGame game, int side)
	{
		_objId = obj_id;
		_name = name;
		_game = game;
		_type = game.getType();
		_side = side;

		_player = player;
		if(_player == null)
			return;

		_clanName = player.getClan() == null ? StringUtils.EMPTY : player.getClan().getName();
		_clanId = player.getClan() == null ? 0 : player.getClan().getClanId();
		_classId = player.getActiveClassId();

		player.setOlympiadSide(side);
		player.setOlympiadGame(game);
	}

	public StatsSet getStat()
	{
		return Olympiad._nobles.get(_objId);
	}

	public void incGameCount()
	{
		StatsSet set = getStat();
		switch(_type)
		{
			case TEAM:
				set.set(Olympiad.GAME_TEAM_COUNT, set.getInteger(Olympiad.GAME_TEAM_COUNT) + 1);
				break;
			case CLASSED:
				set.set(Olympiad.GAME_CLASSES_COUNT, set.getInteger(Olympiad.GAME_CLASSES_COUNT) + 1);
				break;
			case NON_CLASSED:
				set.set(Olympiad.GAME_NOCLASSES_COUNT, set.getInteger(Olympiad.GAME_NOCLASSES_COUNT) + 1);
				break;
		}
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
		{
			StatsSet stat = getStat();
			int points = stat.getInteger(Olympiad.POINTS);
			int diff = Math.min(OlympiadGame.MAX_POINTS_LOOSE, points / _type.getLooseMult());
			stat.set(Olympiad.POINTS, points - diff);
			Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

			// TODO: Снести подробный лог после исправления беспричинного отъёма очков.
			Player player = _player;
			if(player == null)
				Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
			else
			{
				if(player.isLogoutStarted())
					Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
				if (!player.isOnline())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(!player.isConnected())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(player.getOlympiadGame() == null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGame() == null", "olympiad");
				if(player.getOlympiadObserveGame() != null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadObserveGame() != null", "olympiad");
			}
		}
	}

	public boolean checkPlayer()
	{
		Player player = _player;
		if(player == null || player.isLogoutStarted() || !player.isOnline() || player.getOlympiadGame() == null || player.isInObserverMode())
			return false;
		GameClient client = player.getNetConnection();
		if(client == null)
			return false;
		if(!player.isConnected())
			return false;
		return true;
	}

	public void portPlayerToArena()
	{
		Player player = _player;
		if(!checkPlayer() || player.isTeleporting())
		{
			_player = null;
			return;
		}
			
		DuelEvent duel = player.getEvent(DuelEvent.class);
		if (duel != null)
			duel.abortDuel(player);
			//maybe that?
			
		for(Effect e : player.getEffectList().getAllEffects())
			if(e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0)
				e.exit();	
				
		if(!player.isInZonePeace())
			_returnLoc = new Location(-84600, 151048, -3120);
		else	
			_returnLoc = player.getLoc();
		//_returnLoc = player._stablePoint == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player._stablePoint;

		if(player.isDead())
			player.setPendingRevive(true);
		if(player.isSitting())
			player.standUp();

		player.setTarget(null);
		if(player.isMounted())
			player.setMount(0, 0, 0);
		if(player.getServitor() != null && Config.UNSUMMON_SUMMONS_OLY)
			player.getServitor().unSummon();		
		player.setIsInOlympiadMode(true);
			
		player.leaveParty();
		
		Reflection ref = _game.getReflection();
		InstantZone instantZone = ref.getInstancedZone();

		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());
	
		player.setTeam(TeamType.NONE);
		
		if(_type == CompType.TEAM)
			player.setTeam(_side == 1 ? TeamType.BLUE : TeamType.RED);	

		player._stablePoint = _returnLoc;
		player.teleToLocation(tele, ref);	

		player.sendPacket(new ExOlympiadModePacket(_side));
	}
	public void restoreAll()
	{
		Player player = _player;
		if(player == null)
			return;
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
	}
	public void portPlayerBack()
	{
		Player player = _player;
		if(player == null)
			return;

		Olympiad._playersIp.remove(player.getIP());
		Olympiad._playersHWIDs.remove(player.getHWID());

		if(_returnLoc == null) // Player not portnulo the stadium
			return;

		player.setIsInOlympiadMode(false);
		player.setOlympiadSide(-1);
		player.setOlympiadGame(null);

		if(_type == CompType.TEAM)
			player.setTeam(TeamType.NONE);

		EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.

		if(player.getServitor() != null)
			player.getServitor().getEffectList().stopAllEffects();

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new RevivePacket(player));
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);
			
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());			

		// Возвращаем клановые скиллы если репутация положительная.
		if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
			player.getClan().enableSkills(player);

		// Add Hero Skills
		if(player.isHero())
			Hero.addSkills(player);

		// Обновляем скилл лист, после добавления скилов
		player.sendSkillList();
		player.sendPacket(new ExOlympiadModePacket(0));
		player.sendPacket(new ExOlympiadMatchEndPacket());

		player._stablePoint = null;
		player.setPendingOlyEnd(false);
		player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
		// Restore point itemov
		if(Config.OLY_ENCH_LIMIT_ENABLE && player.getVar("EnItemOlyRec") != null)
		{
			FixEnchantOlympiad.restoreEnchantItemsOly(player);
		}
		
	}

	public void preparePlayer()
	{
		Player player = _player;
		if(player == null)
			return;

		if(player.isInObserverMode())
			if(player.getOlympiadObserveGame() != null)
				player.leaveOlympiadObserverMode(true);
			else
				player.leaveObserverMode();

		// Un activate clan skills
		if(player.getClan() != null)
			player.getClan().disableSkills(player);

		// Remove Hero Skills
		if(player.isHero())
			Hero.removeSkills(player);

		// Abort casting if player casting
		if(player.isCastingNow())
			player.abortCast(true, true);

		// Удаляем баффы и чужие кубики
		for(Effect e : player.getEffectList().getAllEffects())
			if(e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0)
				e.exit();

		EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.

		// Remove Summon's Buffs
		if(player.getServitor() != null)
		{
			Servitor servitor = player.getServitor();
			if(servitor.isPet())
				servitor.unSummon();
			else
				servitor.getEffectList().stopAllEffects();
		}

		// unsummon agathion
		if(player.getAgathionId() > 0)
			player.setAgathion(0);

		// Сброс кулдауна всех скилов, время отката которых меньше 15 минут
		for(TimeStamp sts : player.getSkillReuses())
		{
			if(sts == null)
				continue;
			Skill skill = SkillHolder.getInstance().getSkill(sts.getId(), sts.getLevel());
			if(skill == null)
				continue;
			if(skill.getReuseDelay() <= 900000L)
				player.enableSkill(skill);
		}

		// Обновляем скилл лист, после удаления скилов
		player.sendSkillList();
		// Обновляем куллдаун, после сброса
		player.sendPacket(new SkillCoolTimePacket(player));

		// Remove Hero weapons
		ItemInstance wpn = player.getActiveWeaponInstance();
		if(wpn != null && wpn.isHeroWeapon())
		{
			player.getInventory().unEquipItem(wpn);
			player.abortAttack(true, true);
		}

		// remove bsps/sps/ss automation
		Set<Integer> activeSoulShots = player.getAutoSoulShot();
		for(int itemId : activeSoulShots)
		{
			player.removeAutoSoulShot(itemId);
			player.sendPacket(new ExAutoSoulShot(itemId, false));
		}

		// Разряжаем заряженные соул и спирит шоты
		ItemInstance weapon = player.getActiveWeaponInstance();
		if(weapon != null)
		{
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);
		}
		// Check item is sharpening
		if(Config.OLY_ENCH_LIMIT_ENABLE)
		{
			FixEnchantOlympiad.storeEnchantItemsOly(player);
		}

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void saveNobleData()
	{
		OlympiadDatabase.saveNobleData(_objId);
	}

	public void logout()
	{
		if (_player!=null)
		{
			_player.setOlympiadGame(null);
			_player = null;
		}
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getName()
	{
		return _name;
	}

	public void addDamage(double d)
	{
		_damage += d;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getClanName()
	{
		return _clanName;
	}
	public int getClanId()
	{
		return _clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getObjectId()
	{
		return _objId;
	}
}