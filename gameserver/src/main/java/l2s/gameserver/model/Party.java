package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.voicecommands.impl.RefferalSystem;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.s2c.ExAskModifyPartyLooting;
import l2s.gameserver.network.l2.s2c.ExCloseMPCCPacket;
import l2s.gameserver.network.l2.s2c.ExOpenMPCCPacket;
import l2s.gameserver.network.l2.s2c.ExPartyPetWindowAdd;
import l2s.gameserver.network.l2.s2c.ExPartyPetWindowDelete;
import l2s.gameserver.network.l2.s2c.ExSetPartyLooting;
import l2s.gameserver.network.l2.s2c.GetItemPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PartyMemberPositionPacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAddPacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAllPacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowDeleteAllPacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowDeletePacket;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;


public class Party implements PlayerGroup
{
	public static final int MAX_SIZE = Config.MAX_PARTY_SIZE;

	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;

	private final List<Player> _members = new CopyOnWriteArrayList<Player>();

	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemOrder = 0;
	private int _dimentionalRift;

	private Reflection _reflection;
	private CommandChannel _commandChannel;

	public double _rateExp;
	public double _rateSp;
	public double _rateDrop;
	public double _rateAdena;
	public double _rateSpoil;

	public double _dropChanceMod;
	public double _spoilChanceMod;

	private ScheduledFuture<?> positionTask;

	private int _requestChangeLoot = -1;
	private long _requestChangeLootTimer = 0;
	private Set<Integer> _changeLootAnswers = null;
	private static final int[] LOOT_SYSSTRINGS = {487, 488, 798, 799, 800};
	private Future<?> _checkTask = null;
	private Long _partyId;

	/**
	 * constructor ensures party has always one member - leader
	 * @param leader создатель парти
	 * @param itemDistribution режим распределения лута
	 */
	public Party(Player leader, int itemDistribution)
	{
		_partyId = leader.getStoredId();
		_itemDistribution = itemDistribution;
		_members.add(leader);
		_partyLvl = leader.getLevel();
		_rateExp = leader.getPremiumAccount().getRates().getExp();
		_rateSp = leader.getPremiumAccount().getRates().getSp();
		_rateAdena = leader.getPremiumAccount().getRates().getAdena();
		_rateDrop = leader.getPremiumAccount().getRates().getDrop();
		_rateSpoil = leader.getPremiumAccount().getRates().getSpoil();

		_dropChanceMod = leader.getPremiumAccount().getModifiers().getDropChance();
		_spoilChanceMod = leader.getPremiumAccount().getModifiers().getSpoilChance();

		World.Parties.add(this);
	}

	public Long getPartyId()
	{
		return _partyId;
	}

	/**
	 * @return number of party members
	 */
	public int getMemberCount()
	{
		return _members.size();
	}

	public int getMemberCountInRange(Player player, int range)
	{
		int count = 0;
		for(Player member : _members)
			if(member == player || member.isInRangeZ(player, range))
				count++;
		return count;
	}

	/**
	 * @return all party members
	 */
	public List<Player> getPartyMembers()
	{
		return _members;
	}

	public List<Integer> getPartyMembersObjIds()
	{
		List<Integer> result = new ArrayList<Integer>(_members.size());
		for(Player member : _members)
			result.add(member.getObjectId());
		return result;
	}

	public List<Playable> getPartyMembersWithPets()
	{
		List<Playable> result = new ArrayList<Playable>();
		for(Player member : _members)
		{
			result.add(member);
			if(member.getServitor() != null)
				result.add(member.getServitor());
		}
		return result;
	}

	/**
	 * @return next item looter
	 */
	private Player getNextLooterInRange(Player player, ItemInstance item, int range)
	{
		synchronized (_members)
		{
			int antiloop = _members.size();
			while(--antiloop > 0)
			{
				int looter = _itemOrder;
				_itemOrder++;
				if(_itemOrder > _members.size() - 1)
					_itemOrder = 0;

				Player ret = looter < _members.size() ? _members.get(looter) : player;

				if(ret != null && !ret.isDead() && ret.isInRangeZ(player, range) && ret.getInventory().validateCapacity(item) && ret.getInventory().validateWeight(item))
					return ret;
			}
		}
		return player;
	}

	/**
	 * true if player is party leader
	 */
	public boolean isLeader(Player player)
	{
		return getPartyLeader() == player;
	}

	/**
	 * Возвращает лидера партии
	 * @return L2Player Лидер партии
	 */
	public Player getPartyLeader()
	{
		synchronized (_members)
		{
			if(_members.size() == 0)
				return null;
			return _members.get(0);
		}
	}

	/**
	 * Broadcasts packet to every party member
	 * @param msg packet to broadcast
	 */
	@Override
	public void broadCast(IStaticPacket... msg)
	{
		for(Player member : _members)
			member.sendPacket(msg);
	}

	/**
	 * Рассылает текстовое сообщение всем членам группы
	 * @param msg сообщение
	 */
	public void broadcastMessageToPartyMembers(String msg)
	{
		this.broadCast(new SystemMessage(msg));
	}

	/**
	 * Рассылает пакет всем членам группы исключая указанного персонажа<BR><BR>
	 */
	public void broadcastToPartyMembers(Player exclude, L2GameServerPacket msg)
	{
		for(Player member : _members)
			if(exclude != member)
				member.sendPacket(msg);
	}

	public void broadcastToPartyMembersInRange(Player player, L2GameServerPacket msg, int range)
	{
		for(Player member : _members)
			if(player.isInRangeZ(member, range))
				member.sendPacket(msg);
	}

	public boolean containsMember(Player player)
	{
		return _members.contains(player);
	}

	/**
	 * adds new member to party
	 * @param player L2Player to add
	 */
	public boolean addPartyMember(Player player)
	{
		Player leader = getPartyLeader();
		if(leader == null)
			return false;

		synchronized (_members)
		{
			if(_members.isEmpty())
				return false;
			if(_members.contains(player))
				return false;
			if(_members.size() == MAX_SIZE)
				return false;
			_members.add(player);
		}

		if(_requestChangeLoot != -1)
			finishLootRequest(false); // cancel on invite

		player.setParty(this);
		player.getListeners().onPartyInvite();

		Servitor pet;
		List<L2GameServerPacket> addInfo = new ArrayList<L2GameServerPacket>(4 + _members.size() * 4);
		List<L2GameServerPacket> pplayer = new ArrayList<L2GameServerPacket>(20);

		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		pplayer.add(new PartySmallWindowAllPacket(this, player));
		pplayer.add(new SystemMessage(SystemMessage.YOU_HAVE_JOINED_S1S_PARTY).addName(leader));

		addInfo.add(new SystemMessage(SystemMessage.S1_HAS_JOINED_THE_PARTY).addName(player));
		addInfo.add(new PartySpelledPacket(player, true));
		if((pet = player.getServitor()) != null)
		{
			addInfo.add(new ExPartyPetWindowAdd(pet));
			addInfo.add(new PartySpelledPacket(pet, true));
		}

		PartyMemberPositionPacket pmp = new PartyMemberPositionPacket();
		List<L2GameServerPacket> pmember;
		for(Player member : _members)
			if(member != player)
			{
				pmember = new ArrayList<L2GameServerPacket>(addInfo.size() + 4);
				pmember.addAll(addInfo);
				pmember.add(new PartySmallWindowAddPacket(member, player));
				pmember.add(new PartyMemberPositionPacket().add(player));
				pmember.add(RelationChangedPacket.update(member, player, member));
				member.sendPacket(pmember);

				pplayer.add(new PartySpelledPacket(member, true));
				if((pet = member.getServitor()) != null)
					pplayer.add(new PartySpelledPacket(pet, true));
				pplayer.add(RelationChangedPacket.update(player, member, player)); //FIXME
				pmp.add(member);
			}

		pplayer.add(pmp);
		// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
		if(isInCommandChannel())
			pplayer.add(ExOpenMPCCPacket.STATIC);

		player.sendPacket(pplayer);

		startUpdatePositionTask();
		recalculatePartyData();

		if(isInReflection() && getReflection() instanceof DimensionalRift)
			((DimensionalRift) getReflection()).partyMemberInvited();

		return true;
	}

	/**
	 * Удаляет все связи
	 */
	public void dissolveParty()
	{
		for(Player p : _members)
		{
			p.sendPacket(PartySmallWindowDeleteAllPacket.STATIC);
			p.setParty(null);
		}

		synchronized (_members)
		{
			_members.clear();
		}

		setDimensionalRift(null);
		setCommandChannel(null);
		stopUpdatePositionTask();
	}

	/**
	 * removes player from party
	 * @param player L2Player to remove
	 */
	public boolean removePartyMember(Player player, boolean kick)
	{
		boolean isLeader = isLeader(player);
		boolean dissolve = false;

		synchronized (_members)
		{
			if(!_members.remove(player))
				return false;
			
			if(_members.size() == 1)
				dissolve = true;
			
			else if(isLeader && kick)
				dissolve = true;
		}

		player.getListeners().onPartyLeave();

		player.setParty(null);
		recalculatePartyData();

		List<L2GameServerPacket> pplayer = new ArrayList<L2GameServerPacket>(4 + _members.size() * 2);

		// Отсылаемы вышедшему пакет закрытия СС
		if(isInCommandChannel())
			pplayer.add(ExCloseMPCCPacket.STATIC);
		if (kick)
			pplayer.add(Msg.YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY);
		else
			pplayer.add(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
		pplayer.add(PartySmallWindowDeleteAllPacket.STATIC);

		Servitor pet;
		List<L2GameServerPacket> outsInfo = new ArrayList<L2GameServerPacket>(3);
		if((pet = player.getServitor()) != null)
			outsInfo.add(new ExPartyPetWindowDelete(pet));
		outsInfo.add(new PartySmallWindowDeletePacket(player));
		if (kick)
			outsInfo.add(new SystemMessage(SystemMessage.S1_WAS_EXPELLED_FROM_THE_PARTY).addName(player));
		else
			outsInfo.add(new SystemMessage(SystemMessage.S1_HAS_LEFT_THE_PARTY).addName(player));

		List<L2GameServerPacket> pmember;
		for(Player member : _members)
		{
			pmember = new ArrayList<L2GameServerPacket>(2 + outsInfo.size());
			pmember.addAll(outsInfo);
			pmember.add(RelationChangedPacket.update(member, player, member));
			member.sendPacket(pmember);
			pplayer.add(RelationChangedPacket.update(player, member, player));
		}

		player.sendPacket(pplayer);

		Reflection reflection = getReflection();

		if(reflection instanceof DarknessFestival)
			((DarknessFestival) reflection).partyMemberExited();
		else if(isInReflection() && getReflection() instanceof DimensionalRift)
			((DimensionalRift) getReflection()).partyMemberExited(player);
		if(reflection != null && player.getReflection() == reflection && reflection.getReturnLoc() != null)
			player.teleToLocation(reflection.getReturnLoc(), ReflectionManager.DEFAULT);

		Player leader = getPartyLeader();

		if(dissolve)
		{
			// Если в партии остался 1 человек, то удаляем ее из СС
			if(isInCommandChannel())
				_commandChannel.removeParty(this);
			else if(reflection != null)
			{
				//lastMember.teleToLocation(getReflection().getReturnLoc(), 0);
				//getReflection().stopCollapseTimer();
				//getReflection().collapse();
				if(reflection.getInstancedZone() != null && reflection.getInstancedZone().isCollapseOnPartyDismiss())
				{
					if(reflection.getParty() == this) // TODO: убрать затычку
						reflection.startCollapseTimer(reflection.getInstancedZone().getTimerOnCollapse() * 1000);
					if(leader != null && leader.getReflection() == reflection)
						leader.broadcastPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(1));
				}
			}

			dissolveParty();
		}
		else
		{
			if(isInCommandChannel() && _commandChannel.getChannelLeader() == player)
				_commandChannel.setChannelLeader(leader);

			if(isLeader)
				updateLeaderInfo();
		}

		if(_checkTask != null)
		{
			_checkTask.cancel(true);
			_checkTask = null;
		}

		return true;
	}

	public boolean changePartyLeader(Player player)
	{
		Player leader = getPartyLeader();

		// Меняем местами нового и текущего лидера
		synchronized (_members)
		{
			int index = _members.indexOf(player);
			if(index == -1)
				return false;
			_members.set(0, player);
			_members.set(index, leader);
		}

		updateLeaderInfo();

		if(isInCommandChannel() && _commandChannel.getChannelLeader() == leader)
			_commandChannel.setChannelLeader(player);

		return true;
	}

	private void updateLeaderInfo()
	{
		Player leader = getPartyLeader();
		if (leader == null) // некрасиво, но иначе NPE.
			return;

		SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addName(leader);

		for(Player member : _members)
		{
			// индивидуальные пакеты - удаления и инициализация пати
			member.sendPacket(PartySmallWindowDeleteAllPacket.STATIC, // Удаляем все окошки
			new PartySmallWindowAllPacket(this, member), // Показываем окошки
			msg); // Сообщаем о смене лидера
		}

		// броадкасты состояний
		for(Player member : _members)
		{
			broadcastToPartyMembers(member, new PartySpelledPacket(member, true)); // Показываем иконки
			if(member.getServitor() != null)
				this.broadCast(new ExPartyPetWindowAdd(member.getServitor())); // Показываем окошки петов
			// broadcastToPartyMembers(member, new PartyMemberPosition(member)); // Обновляем позицию на карте
		}
	}

	/**
	 * finds a player in the party by name
	 * @param name имя для поиска
	 * @return найденый L2Player или null если не найдено
	 */
	public Player getPlayerByName(String name)
	{
		for(Player member : _members)
			if(name.equalsIgnoreCase(member.getName()))
				return member;
		return null;
	}

	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		switch(item.getItemId())
		{
			case ItemTemplate.ITEM_ID_ADENA:
				distributeAdena(player, item, fromNpc);
				break;
			default:
				distributeItem0(player, item, fromNpc);
				break;
		}

	}

	private void distributeItem0(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		Player target = null;

		List<Player> ret = null;
		switch(_itemDistribution)
		{
			case ITEM_RANDOM:
			case ITEM_RANDOM_SPOIL:
				ret = new ArrayList<Player>(_members.size());
				for(Player member : _members)
					if(member.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !member.isDead() && member.getInventory().validateCapacity(item) && member.getInventory().validateWeight(item))
						ret.add(member);

				target = ret.isEmpty() ? null : ret.get(Rnd.get(ret.size()));
				break;
			case ITEM_ORDER:
			case ITEM_ORDER_SPOIL:
				synchronized (_members)
				{
					ret = new CopyOnWriteArrayList<Player>(_members);
					while(target == null && !ret.isEmpty())
					{
						int looter = _itemOrder;
						_itemOrder++;
						if(_itemOrder > ret.size() - 1)
							_itemOrder = 0;

						Player looterPlayer = looter < ret.size() ? ret.get(looter) : null;

						if(looterPlayer != null)
						{
							if( !looterPlayer.isDead() && looterPlayer.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && ItemFunctions.canAddItem(looterPlayer, item))
								target = looterPlayer;
							else
								ret.remove(looterPlayer);
						}
					}
				}

				if(target == null)
					return;
				break;
			case ITEM_LOOTER:
			default:
				target = player;
				break;
		}

		if(target == null)
			target = player;

		if(target.pickupItem(item, "PartyPickUp"))
		{
			if(Config.ALLOW_REFFERAL_SYSTEM)
				if(fromNpc != null && Config.ITEM_LIST.contains(item.getItemId()))
					RefferalSystem.applyBonus(target, item.getItemId(), (int)item.getCount());		
				
			if(fromNpc == null)
				player.broadcastPacket(new GetItemPacket(item, player.getObjectId()));

			player.broadcastPickUpMsg(item);
			item.pickupMe();

			broadcastToPartyMembers(target, SystemMessagePacket.obtainItemsBy(item, target));
		}
		else
			item.dropToTheGround(player, fromNpc);
	}

	private void distributeAdena(Player player, ItemInstance item, NpcInstance fromNpc)
	{
		if(player == null)
			return;

		List<Player> membersInRange = new ArrayList<Player>();

		if(item.getCount() < _members.size())
			membersInRange.add(player);
		else
		{
			for(Player member : _members)
				if(!member.isDead() && (member == player || player.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && ItemFunctions.canAddItem(player, item))
					membersInRange.add(member);
		}

		if(membersInRange.isEmpty())
			membersInRange.add(player);

		long totalAdena = item.getCount();
		long amount = totalAdena / membersInRange.size();
		long ost = totalAdena % membersInRange.size();

		for(Player member : membersInRange)
		{			
			long count = member.equals(player) ? amount + ost : amount;
			if(Config.ALLOW_REFFERAL_SYSTEM)
				if(Config.ITEM_LIST.contains(57) && fromNpc != null)
					RefferalSystem.applyBonus(member, 57, (int)count);	
				
			member.getInventory().addAdena(count);
			member.sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, count, 0));
		}

		if(fromNpc == null)
			player.broadcastPacket(new GetItemPacket(item, player.getObjectId()));

		item.pickupMe();
	}

	public void distributeXpAndSp(double xpReward, double spReward, List<Player> rewardedMembers, Creature lastAttacker, MonsterInstance monster)
	{
		recalculatePartyData();

		List<Player> mtr = new ArrayList<Player>();
		int partyLevel = lastAttacker.getLevel();
		int partyLvlSum = 0;

		// считаем минимальный/максимальный уровень
		for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			partyLevel = Math.max(partyLevel, member.getLevel());
		}

		// составляем список игроков, удовлетворяющих требованиям
		for(Player member : rewardedMembers)
		{
			if(!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			if(member.getLevel() <= partyLevel - Config.MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY)
				continue;
			partyLvlSum += member.getLevel();
			mtr.add(member);
		}

		if(mtr.isEmpty())
			return;

		// бонус за пати
		double bonus = Config.ALT_PARTY_BONUS[mtr.size() - 1];

		// количество эксп и сп для раздачи на всех
		double XP = xpReward * bonus;
		double SP = spReward * bonus;

		for(Player member : mtr)
		{
			double lvlPenalty = 1.;
			int lvlDiff = partyLevel - member.getLevel();
			if(lvlDiff >= 10 && lvlDiff <= 14) // >= 10 <= 14
				lvlPenalty *= 0.3D;

			// отдаем его часть с учетом пенальти
			double memberXp = XP * lvlPenalty * member.getLevel() / partyLvlSum;
			double memberSp = SP * lvlPenalty * member.getLevel() / partyLvlSum;

			// больше чем соло не дадут
			memberXp = Math.min(memberXp, xpReward);
			memberSp = Math.min(memberSp, spReward);

			member.addExpAndCheckBonus(monster, (long) memberXp, (long) memberSp, memberXp / xpReward);
		}

		recalculatePartyData();
	}

	public void recalculatePartyData()
	{
		_partyLvl = 0;
		double rateExp = 0.;
		double rateSp = 0.;
		double rateDrop = 0.;
		double rateAdena = 0.;
		double rateSpoil = 0.;
		double dropChanceMod = 0.;
		double spoilChanceMod = 0.;
		double minRateExp = Double.MAX_VALUE;
		double minRateSp = Double.MAX_VALUE;
		double minRateDrop = Double.MAX_VALUE;
		double minRateAdena = Double.MAX_VALUE;
		double minRateSpoil = Double.MAX_VALUE;
		double minDropChanceMod = Double.MAX_VALUE;
		double minSpoilChanceMod = Double.MAX_VALUE;
		int count = 0;

		for(Player member : _members)
		{
			int level = member.getLevel();
			_partyLvl = Math.max(_partyLvl, level);
			count++;

			rateExp += member.getPremiumAccount().getRates().getExp();
			rateSp += member.getPremiumAccount().getRates().getSp();
			rateDrop += member.getPremiumAccount().getRates().getDrop();
			rateAdena += member.getPremiumAccount().getRates().getAdena();
			rateSpoil += member.getPremiumAccount().getRates().getSpoil();
			dropChanceMod += member.getPremiumAccount().getModifiers().getDropChance();
			spoilChanceMod += member.getPremiumAccount().getModifiers().getSpoilChance();

			minRateExp = Math.min(minRateExp, member.getPremiumAccount().getRates().getExp());
			minRateSp = Math.min(minRateSp, member.getPremiumAccount().getRates().getSp());
			minRateDrop = Math.min(minRateDrop, member.getPremiumAccount().getRates().getDrop());
			minRateAdena = Math.min(minRateAdena, member.getPremiumAccount().getRates().getAdena());
			minRateSpoil = Math.min(minRateSpoil, member.getPremiumAccount().getRates().getSpoil());
			minDropChanceMod = Math.min(minDropChanceMod, member.getPremiumAccount().getModifiers().getDropChance());
			minSpoilChanceMod = Math.min(minSpoilChanceMod, member.getPremiumAccount().getModifiers().getSpoilChance());
		}

		_rateExp = Config.RATE_PARTY_MIN ? minRateExp : rateExp / count;
		_rateSp = Config.RATE_PARTY_MIN ? minRateSp : rateSp / count;
		_rateDrop = Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count;
		_rateAdena = Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count;
		_rateSpoil = Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count;
		_dropChanceMod = Config.RATE_PARTY_MIN ? minDropChanceMod : dropChanceMod / count;
		_spoilChanceMod = Config.RATE_PARTY_MIN ? minSpoilChanceMod : spoilChanceMod / count;
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;

		if(_itemDistribution == ITEM_RANDOM_SPOIL || _itemDistribution == ITEM_ORDER_SPOIL)
			rv = true;

		return rv;
	}

	public boolean isInDimensionalRift()
	{
		return _dimentionalRift > 0 && getDimensionalRift() != null;
	}

	public void setDimensionalRift(DimensionalRift dr)
	{
		_dimentionalRift = dr == null ? 0 : dr.getId();
	}

	public DimensionalRift getDimensionalRift()
	{
		return _dimentionalRift == 0 ? null : (DimensionalRift) ReflectionManager.getInstance().get(_dimentionalRift);
	}

	public boolean isInReflection()
	{
		if(_reflection != null)
			return true;
		if(_commandChannel != null)
			return _commandChannel.isInReflection();
		return false;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}

	public Reflection getReflection()
	{
		if(_reflection != null)
			return _reflection;
		if(_commandChannel != null)
			return _commandChannel.getReflection();
		return null;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(CommandChannel channel)
	{
		_commandChannel = channel;
	}

	/**
	 * Телепорт всей пати в одну точку (x,y,z)
	 */
	public void Teleport(int x, int y, int z)
	{
		TeleportParty(getPartyMembers(), new Location(x, y, z));
	}

	/**
	 * Телепорт всей пати в одну точку dest
	 */
	public void Teleport(Location dest)
	{
		TeleportParty(getPartyMembers(), dest);
	}

	/**
	 * Телепорт всей пати на территорию, игроки расставляются рандомно по территории
	 */
	public void Teleport(Territory territory)
	{
		RandomTeleportParty(getPartyMembers(), territory);
	}

	/**
	 * Телепорт всей пати на территорию, лидер попадает в точку dest, а все остальные относительно лидера
	 */
	public void Teleport(Territory territory, Location dest)
	{
		TeleportParty(getPartyMembers(), territory, dest);
	}

	public static void TeleportParty(List<Player> members, Location dest)
	{
		for(Player _member : members)
		{
			if(_member == null)
				continue;
			_member.teleToLocation(dest);
		}
	}

	public static void TeleportParty(List<Player> members, Territory territory, Location dest)
	{
		if(!territory.isInside(dest.x, dest.y))
		{
			Log.add("TeleportParty: dest is out of territory", "errors");
			Thread.dumpStack();
			return;
		}
		int base_x = members.get(0).getX();
		int base_y = members.get(0).getY();

		for(Player _member : members)
		{
			if(_member == null)
				continue;
			int diff_x = _member.getX() - base_x;
			int diff_y = _member.getY() - base_y;
			Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
			while(!territory.isInside(loc.x, loc.y))
			{
				diff_x = loc.x - dest.x;
				diff_y = loc.y - dest.y;
				if(diff_x != 0)
					loc.x -= diff_x / Math.abs(diff_x);
				if(diff_y != 0)
					loc.y -= diff_y / Math.abs(diff_y);
			}
			_member.teleToLocation(loc);
		}
	}

	public static void RandomTeleportParty(List<Player> members, Territory territory)
	{
		for(Player member : members)
			member.teleToLocation(Territory.getRandomLoc(territory, member.getGeoIndex(), member.isFlying()));
	}

	private void startUpdatePositionTask()
	{
		if(positionTask == null)
			positionTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new UpdatePositionTask(), 1000, 1000);
	}

	private void stopUpdatePositionTask()
	{
		if(positionTask != null)
			positionTask.cancel(false);
	}

	private class UpdatePositionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			LazyArrayList<Player> update = LazyArrayList.newInstance();

			for(Player member : _members)
			{
				Location loc = member.getLastPartyPosition();
				if(loc == null || member.getDistance(loc) > 256) //TODO подкорректировать
				{
					member.setLastPartyPosition(member.getLoc());
					update.add(member);
				}
			}

			if(!update.isEmpty())
				for(Player member : _members)
				{
					PartyMemberPositionPacket pmp = new PartyMemberPositionPacket();
					for(Player m : update)
						if(m != member)
							pmp.add(m);
					if(pmp.size() > 0)
						member.sendPacket(pmp);
				}

			LazyArrayList.recycle(update);
		}
	}

	public void requestLootChange(byte type)
	{
		if(_requestChangeLoot != -1)
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
				finishLootRequest(false);
			else
				return;
		}
		_requestChangeLoot = type;
		int additionalTime = 45000; // timeout 45sec, guess
		_requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
		_changeLootAnswers = new CopyOnWriteArraySet<Integer>();
		_checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000);
		broadcastToPartyMembers(getPartyLeader(), new ExAskModifyPartyLooting(getPartyLeader().getName(), type));
		SystemMessage sm = new SystemMessage(SystemMessage.REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1);
		sm.addSystemString(LOOT_SYSSTRINGS[type]);
		getPartyLeader().sendPacket(sm);
	}

	public synchronized void answerLootChangeRequest(Player member, boolean answer)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_changeLootAnswers.contains(member.getObjectId()))
			return;
		if(!answer)
		{
			finishLootRequest(false);
			return;
		}
		_changeLootAnswers.add(member.getObjectId());
		if(_changeLootAnswers.size() >= getMemberCount() - 1)
		{
			finishLootRequest(true);
		}
	}

	private synchronized void finishLootRequest(boolean success)
	{
		if(_requestChangeLoot == -1)
			return;
		if(_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if(success)
		{
			this.broadCast(new ExSetPartyLooting(1, _requestChangeLoot));
			_itemDistribution = _requestChangeLoot;
			SystemMessage sm = new SystemMessage(SystemMessage.PARTY_LOOT_CHANGED_S1);
			sm.addSystemString(LOOT_SYSSTRINGS[_requestChangeLoot]);
			this.broadCast(sm);
		}
		else
		{
			this.broadCast(new ExSetPartyLooting(0, (byte) 0));
			this.broadCast(new SystemMessage(SystemMessage.PARTY_LOOT_CHANGE_CANCELLED));
		}
		_changeLootAnswers = null;
		_requestChangeLoot = -1;
		_requestChangeLootTimer = 0;
	}

	private class ChangeLootCheck extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(System.currentTimeMillis() > Party.this._requestChangeLootTimer)
			{
				Party.this.finishLootRequest(false);
			}
		}
	}

	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}
}