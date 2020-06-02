package l2s.gameserver.model.actor.recorder;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.collections.CollectionUtils;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExUserInfoAbnormalVisualEffect;
import l2s.gameserver.network.l2.s2c.ExUserInfoEquipSlot;
import l2s.gameserver.network.l2.s2c.ExUserInfoInvenWeight;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;

/**
 * @author G1ta0
 */
public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player>
{
	public static final int BROADCAST_KARMA = 1 << 3;
	public static final int SEND_STORAGE_INFO = 1 << 4;
	public static final int SEND_MAX_LOAD = 1 << 5;
	public static final int SEND_CUR_LOAD = 1 << 6;
	public static final int BROADCAST_CHAR_INFO2 = 1 << 7;

	private int _maxCp;

	private int _maxLoad;
	private int _curLoad;

	private int[] _attackElement = new int[6];
	private int[] _defenceElement = new int[6];

	private long _exp;
	private int _sp;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _fame;

	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _trade;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _partyRoom;

	private String _title = StringUtils.EMPTY;

	private int _cubicsHash;

	private int _weaponEnchant;
	private int _weaponVariation1;
	private int _weaponVariation2;

	public PlayerStatsChangeRecorder(Player activeChar)
	{
		super(activeChar);
	}

	@Override
	protected void refreshStats()
	{
		_maxCp = set(SEND_STATUS_INFO, _maxCp, _activeChar.getMaxCp());

		super.refreshStats();

		_maxLoad = set(SEND_CHAR_INFO | SEND_MAX_LOAD, _maxLoad, _activeChar.getMaxLoad());
		_curLoad = set(SEND_CUR_LOAD, _curLoad, _activeChar.getCurrentLoad());

		for(Element e : Element.VALUES)
		{
			_attackElement[e.getId()] = set(SEND_CHAR_INFO, _attackElement[e.getId()], _activeChar.getAttack(e));
			_defenceElement[e.getId()] = set(SEND_CHAR_INFO, _defenceElement[e.getId()], _activeChar.getDefence(e));
		}

		_exp = set(SEND_CHAR_INFO, _exp, _activeChar.getExp());
		_sp = set(SEND_CHAR_INFO, _sp, _activeChar.getIntSp());
		_pk = set(SEND_CHAR_INFO, _pk, _activeChar.getPkKills());
		_pvp = set(SEND_CHAR_INFO, _pvp, _activeChar.getPvpKills());
		_fame = set(SEND_CHAR_INFO, _fame, _activeChar.getFame());

		_karma = set(BROADCAST_KARMA, _karma, _activeChar.getKarma());

		_inventory = set(SEND_STORAGE_INFO, _inventory, _activeChar.getInventoryLimit());
		_warehouse = set(SEND_STORAGE_INFO, _warehouse, _activeChar.getWarehouseLimit());
		_clan = set(SEND_STORAGE_INFO, _clan, Config.WAREHOUSE_SLOTS_CLAN);
		_trade = set(SEND_STORAGE_INFO, _trade, _activeChar.getTradeLimit());
		_recipeDwarven = set(SEND_STORAGE_INFO, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
		_recipeCommon = set(SEND_STORAGE_INFO, _recipeCommon, _activeChar.getCommonRecipeLimit());
		_cubicsHash = set(BROADCAST_CHAR_INFO, _cubicsHash, CollectionUtils.hashCode(_activeChar.getCubics()));
		_partyRoom = set(BROADCAST_CHAR_INFO, _partyRoom, _activeChar.getMatchingRoom() != null && _activeChar.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && _activeChar.getMatchingRoom().getLeader() == _activeChar ? _activeChar.getMatchingRoom().getId() : 0);
		_team = set(BROADCAST_CHAR_INFO2, _team, _activeChar.getTeam());
		_title = set(BROADCAST_CHAR_INFO, _title, _activeChar.getTitle());

		_weaponEnchant = set(BROADCAST_CHAR_INFO, _weaponEnchant, _activeChar.getEnchantEffect());
		_weaponVariation1 = set(BROADCAST_CHAR_INFO, _weaponVariation1, _activeChar.getVariation1Id());
		_weaponVariation2 = set(BROADCAST_CHAR_INFO, _weaponVariation2, _activeChar.getVariation2Id());
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if((_changes & BROADCAST_CHAR_INFO2) == BROADCAST_CHAR_INFO2)
		{
			_activeChar.broadcastCharInfo();
			if(_activeChar.getServitor() != null)
				_activeChar.getServitor().broadcastCharInfo();
		}
		if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
		{
			_activeChar.broadcastCharInfo();
			//TODOGOD
			_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
			_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
		}
		else if ((_changes & SEND_CHAR_INFO) == SEND_CHAR_INFO)
		{
			_activeChar.sendUserInfo();
			//TODOGOD
			_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
			_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
		}

		if ((_changes & SEND_CUR_LOAD) == SEND_CUR_LOAD || (_changes & SEND_MAX_LOAD) == SEND_MAX_LOAD)
		{
			if(_activeChar.isHFClient())
			{
				if ((_changes & SEND_CUR_LOAD) == SEND_CUR_LOAD)
					_activeChar.sendStatusUpdate(false, false, StatusUpdatePacket.CUR_LOAD);

				if ((_changes & SEND_MAX_LOAD) == SEND_MAX_LOAD)
					_activeChar.sendStatusUpdate(false, false, StatusUpdatePacket.MAX_LOAD);
			}
			else
				_activeChar.sendPacket(new ExUserInfoInvenWeight(_activeChar));
		}

		if ((_changes & BROADCAST_KARMA) == BROADCAST_KARMA)
			_activeChar.sendStatusUpdate(true, false, StatusUpdatePacket.KARMA);

		if ((_changes & SEND_STORAGE_INFO) == SEND_STORAGE_INFO)
			_activeChar.sendPacket(new ExStorageMaxCountPacket(_activeChar));
	}
}