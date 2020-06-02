package l2s.gameserver.instancemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.LfcDAO;
import l2s.gameserver.dao.LfcDAO.Arenas;
import l2s.gameserver.dao.LfcStatisticDAO;
import l2s.gameserver.dao.LfcStatisticDAO.GlobalStatistic;
import l2s.gameserver.dao.LfcStatisticDAO.LocalStatistic;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.utils.Location;


public class LfcManager
{
	private static final Logger _log = LoggerFactory.getLogger(LfcManager.class);
	
	private static Player _player1;
	private static Player _player2;
	
	private Arenas _gameArena;
	
	private Reflection _reflection;

	public LfcManager(Arenas arena, Player player1, Player player2)
	{
		_gameArena = arena;
		_player1 = player1;
		_player2 = player2;

		_reflection = new Reflection();
		InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(Rnd.get(147, 150));
		_reflection.init(instantZone);	
	}
	
	public void initFight(Arenas arena)
	{
		//cases: 1 = player1 fail ; 2 = player2 failed ; -1 = all good
		if(!getValidMatch(arena, true))
			return;
		//_player1.setLfcGame(this);
		//_player2.setLfcGame(this);	
		//_player1.setPendingLfcStart(true);
		//_player2.setPendingLfcStart(true);
		_player1.sendPacket(new ExShowScreenMessage("Porting to arena - "+arena.getArenaNameRu()+" in 10 seconds ", 10000, ScreenMessageAlign.TOP_CENTER, true));
		_player2.sendPacket(new ExShowScreenMessage("Porting to arena - "+arena.getArenaNameRu()+" in 10 seconds ", 10000, ScreenMessageAlign.TOP_CENTER, true));
		_player1.sendMessage("Porting to arena in 10 sec, please remain where you are!");
		_player2.sendMessage("Porting to arena in 10 sec, please remain where you are!");
		ThreadPoolManager.getInstance().schedule(new StartMatch(arena), 10000L);
		
	}
	
	public Arenas getArena()
	{
		return _gameArena;
	}
	
	public class StartMatch extends RunnableImpl
	{
		private Arenas _arena;

		public StartMatch(Arenas arena)
		{
			_arena = arena;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!getValidMatch(_arena, false))
				return;		
			teleToArena(_player1, 1);
			teleToArena(_player2, 2);
			addBuffers();
			ThreadPoolManager.getInstance().schedule(new OpenDoors(), 60000L);
		}
	}	
	
	public class OpenDoors extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(DoorInstance door : _reflection.getDoors())
				door.openMe();	
			if(_player1 != null)
				_player1.sendPacket(new ExShowScreenMessage("The battle has began - GOOD LUCK!", 5000, ScreenMessageAlign.TOP_CENTER, true));
			if(_player2 != null)
				_player2.sendPacket(new ExShowScreenMessage("The battle has began - GOOD LUCK!", 5000, ScreenMessageAlign.TOP_CENTER, true));
			deleteBuffers();	
		}
	}		
	
	private void teleToArena(Player player, int side)
	{
		player.setArenaIdForLogout(0);	
		player.setLfcGame(this);	
		DuelEvent duel = player.getEvent(DuelEvent.class);
		if (duel != null)
			duel.abortDuel(player);
			//maybe that?

		if(player.isDead())
			player.setPendingRevive(true);
		if(player.isSitting())
			player.standUp();

		player.setTarget(null);
			
		player.leaveParty();
		
		Functions.unRide(player);
		Functions.unSummonPet(player, true);
		player.dispelBuffs();
		player.setTransformation(0);
		
		// Сброс кулдауна всех скилов, время отката которых меньше 15 минут
		for(TimeStamp sts : player.getSkillReuses())
		{
			if(sts == null)
				continue;
			Skill skill = SkillHolder.getInstance().getSkill(sts.getId(), sts.getLevel());
			if(skill == null)
				continue;
			player.enableSkill(skill);
		}
		
		InstantZone instantZone = _reflection.getInstancedZone();

		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(side - 1), 50, 50, _reflection.getGeoIndex());
		
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		
		player.teleToLocation(tele, _reflection);		
		player.setPendingLfcStart(false);
		player.sendPacket(new ExShowScreenMessage("The gates will open in 60 seconds, get ready!", 10000, ScreenMessageAlign.TOP_CENTER, true));
	}
	
	public void endMatch(Player looser)
	{
		Player winner = _player1 == looser ? _player2 : _player1;
		endMatch(getArena(), winner, looser, false);
	}
	
	private void endMatch(Arenas arena, Player winner, Player looser, boolean beforeMatch)
	{
		int price = arena.getCouponId();
		long price_count = arena.getCouponCount();	
		boolean winnerOn = winner != null ? true : false;
		boolean looserOn = looser != null ? true : false;
		if(beforeMatch)
		{
			if(winnerOn)
			{
				winner.setLfcGame(null);
				winner.sendPacket(new ExShowScreenMessage("Battle has cancelled!", 10000, ScreenMessageAlign.TOP_CENTER, true));
			}	
			if(looserOn)
			{
				looser.setLfcGame(null);
				looser.sendPacket(new ExShowScreenMessage("Battle has cancelled!", 10000, ScreenMessageAlign.TOP_CENTER, true));	
			}	
			BroadCastToWorld("Battle has cancelled on "+arena.getArenaNameRu()+" arena!");			
		}
		else
		{
			int isPayArena = arena.isMoneyFight() && arena.getCouponId() == 9142 ? 1 : 0; 
			if(looserOn && winnerOn)
			{
				looser.sendPacket(new ExShowScreenMessage("Battle is over, you lost! Player "+winner.getName()+" has won "+arena.getCouponCount()+" "+arena.getNameByCoupon()+" ", 10000, ScreenMessageAlign.TOP_CENTER, true));	
				if(LfcStatisticDAO.getPlayerRecord(looser) == null)
				{
					GlobalStatistic global_info = new GlobalStatistic(looser.getName(), 0, 1, isPayArena, 0);
					LfcStatisticDAO.addGlobalStat(global_info);
				}
				else
				{
					GlobalStatistic stat = LfcStatisticDAO.getPlayerRecord(looser);
					stat.IncreaseLooseCount();
					if(arena.isMoneyFight() && arena.getCouponId() == 9142)
						stat.IncreasePayMatchPlayed();
				}
			}
			if(winnerOn && looserOn)
			{
				if(LfcStatisticDAO.getPlayerRecord(winner) == null)
				{
					long money = 0;
					if(arena.isMoneyFight() && arena.getCouponId() == 9142)
						money = arena.getCouponValue();
					GlobalStatistic global_info = new GlobalStatistic(winner.getName(), 1, 0, isPayArena, money);
					LfcStatisticDAO.addGlobalStat(global_info);
				}
				else
				{
					GlobalStatistic stat = LfcStatisticDAO.getPlayerRecord(winner);
					stat.IncreaseWinCount();
					if(arena.isMoneyFight() && arena.getCouponId() == 9142)
					{
						stat.IncreaseMoneyEarned(arena.getCouponValue());
						stat.IncreasePayMatchPlayed();
					}	
				}			
				if(arena.isMoneyFight() && arena.getCouponId() == 9142)
					LfcStatisticDAO.increaseMoney(arena.getCouponValue());
				if(winnerOn)
				{	
					winner.getInventory().addItem(price, price_count);
					winner.sendPacket(SystemMessagePacket.obtainItems(price, price_count, 0));			
					winner.sendPacket(new ExShowScreenMessage("Battle is over, you won! You, "+winner.getName()+", won "+arena.getCouponCount()+" "+arena.getNameByCoupon()+" ", 10000, ScreenMessageAlign.TOP_CENTER, true));
					if(arena.haveWinnerEffect())
					{
						String effectType = arena.getHeroType();
						int hours = arena.getWinnerEffectTime();
						winner.setVar("arena_reward", ""+effectType+"", hours*600000);
						AbnormalEffect ae = AbnormalEffect.getByName(effectType);
						winner.startAbnormalEffect(ae);
					}	
				}
			}
			
			LfcStatisticDAO.increaseBattles(1);
			
			if(winner != null && winner.isOnline())
				winner.setLfcGame(null);
			if(looser != null && looser.isOnline())	
				looser.setLfcGame(null);
			try
			{
				//in case that one player gone offline
				LocalStatistic local_info = new LocalStatistic(arena.getArenaId(), arena.getArenaNameRu(), arena.getArenaNameEn(), winner.getName(), looser.getName(), arena.getCouponId(), arena.getCouponCount());
				LfcStatisticDAO.addLocalFight(local_info);
				BroadCastToWorld("Player "+winner.getName()+" won this battle and got "+price_count+" "+price+" on "+arena.getArenaId()+" arena!");	
			}
			catch(Exception e)
			{}	
		}
		ThreadPoolManager.getInstance().schedule(new collapse(arena, winner, looser, beforeMatch), 5000L);
	}

	public class collapse extends RunnableImpl
	{
		Arenas _arena;
		Player _winner;
		Player _looser;
		boolean _abort;

		public collapse(Arenas arena, Player winner, Player looser, boolean abort)
		{
			_arena = arena;
			_winner = winner;
			_looser = looser;
			_abort = abort;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!_abort)
			{
				portPlayersBack(_winner);
				portPlayersBack(_looser);
				_reflection.collapse();
				_reflection = null;
			}	
			_arena.clear();
			_gameArena = null;
			_player1 = null;
			_player2 = null;

		}
	}
	
	private void portPlayersBack(Player player)
	{
		if(player == null)
			return; //not online =/
		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new RevivePacket(player));
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);
			
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());			
		player.teleToLocation(-84600, 151048, -3120, ReflectionManager.DEFAULT);
		player.setPendingLfcEnd(false);
	}		
	
	private boolean getValidMatch(Arenas arena, boolean first)
	{
		if(_player1 == null)
		{
			endMatch(arena, _player2, _player1, true);
			return false;
		}
		if(_player2 == null)
		{
			endMatch(arena, _player1, _player2, true);
			return false;
		}
		if(arena.isMoneyFight() && first)
		{
			/*if(_player1.getInventory().getCountOf(arena.getKeyToArena()) < arena.getKeyCount() || _player2.getInventory().getCountOf(arena.getKeyToArena()) < arena.getKeyCount())
			{
				endMatch(arena, _player2, _player1, true);
				return false;				
			}
			if(_player1.getInventory().getCountOf(arena.getKeyToArena()) < arena.getKeyCount() || !_player1.getInventory().destroyItemByItemId(arena.getKeyToArena(), arena.getKeyCount()))
			{
				endMatch(arena, _player2, _player1, true);
				return false;						
			}
			
			if(_player2.getInventory().getCountOf(arena.getKeyToArena()) < arena.getKeyCount() || !_player2.getInventory().destroyItemByItemId(arena.getKeyToArena(), arena.getKeyCount()))
			{
				endMatch(arena, _player1, _player2, true);
				return false;						
			}		
*/			
		}	
		if(_player1.getOlympiadGame() != null || Olympiad.isRegistered(_player1))
		{
			endMatch(arena, _player2, _player1, true);
			return false;		
		}
		if(_player2.getOlympiadGame() != null || Olympiad.isRegistered(_player2))
		{
			endMatch(arena, _player1, _player2, true);
			return false;		
		}
		if(_player1.getTeam() != TeamType.NONE)
		{
			endMatch(arena, _player2, _player1, true);
			return false;	
		}	
		if(_player2.getTeam() != TeamType.NONE)
		{
			endMatch(arena, _player1, _player2, true);
			return false;			
		}			
		if(_player1.isTeleporting())
		{
			endMatch(arena, _player2, _player1, true);
			return false;			
		}
		if(_player2.isTeleporting())
		{
			endMatch(arena, _player1, _player2, true);
			return false;			
		}		
		if(_player1.getParty() != null && _player1.getParty().isInDimensionalRift())
		{
			endMatch(arena, _player2, _player1, true);
			return false;			
		}

		if(_player2.getParty() != null && _player2.getParty().isInDimensionalRift())
		{
			endMatch(arena, _player1, _player2, true);
			return false;			
		}
		
		if(_player1.isCursedWeaponEquipped())
		{
			endMatch(arena, _player2, _player1, true);
			return false;			
		}		

		if(_player2.isCursedWeaponEquipped())
		{
			endMatch(arena, _player1, _player2, true);
			return false;			
		}		
		if(_player1.isInObserverMode())
		{
			endMatch(arena, _player2, _player1, true);
			return false;			
		}		
		if(_player2.isInObserverMode())
		{
			endMatch(arena, _player1, _player2, true);
			return false;			
		}		
		return true;	
	}
	
	private static void BroadCastToWorld(String text)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getVarBoolean("lfcNotes"))
			{
				player.sendPacket(new ExShowScreenMessage(text, 7000, ScreenMessageAlign.TOP_CENTER, true));
				
			}
		}
	}

	private String getBufferSpawnGroup(int instancedZoneId)
	{
		String bufferGroup = null;
		switch(instancedZoneId)
		{
			case 147:
				bufferGroup = "olympiad_147_buffers";
				break;
			case 148:
				bufferGroup = "olympiad_148_buffers";
				break;
			case 149:
				bufferGroup = "olympiad_149_buffers";
				break;
			case 150:
				bufferGroup = "olympiad_150_buffers";
				break;
		}
		return bufferGroup;
	}

	public void addBuffers()
	{
		if(getBufferSpawnGroup(_reflection.getInstancedZoneId()) != null)
			_reflection.spawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
	}

	public void deleteBuffers()
	{
		_reflection.despawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
	}	
	
	public static void returnBid(Arenas arena)
	{
		if(arena == null)
			return; //means 0 if from logout?
		Player player1 = arena.getPlayerOne();
		Player player2 = arena.getPlayerTwo();
		int returnId = arena.getKeyToArena();
		long returnCount = arena.getKeyCount();
		if(player1 != null)
		{
			player1.getInventory().addItem(returnId, returnCount); 
			player1.sendPacket(SystemMessagePacket.obtainItems(returnId, returnCount, 0));			
			player1.sendPacket(new ExShowScreenMessage("Battle Canceled, you got your bids back! ", 10000, ScreenMessageAlign.TOP_CENTER, true));
		}	
		if(player2 != null)
		{
			player2.getInventory().addItem(returnId, returnCount); 
			player2.sendPacket(SystemMessagePacket.obtainItems(returnId, returnCount, 0));			
			player2.sendPacket(new ExShowScreenMessage("Battle Canceled, you got your bids back! ", 10000, ScreenMessageAlign.TOP_CENTER, true));			
		}	
		arena.setPlayerOne(null);
		arena.setPlayerTwo(null);			
	}
	
	public static void cancelArenaLogout(Player initiator, int arenaId)
	{
		Arenas arena = LfcDAO.getArenaByArenaId(arenaId);
		returnBid(arena);	
	}
}