package handler.bbs;

import java.util.List;

import handler.bbs.custom.BBSConfig;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.CommunityBoardManager;
import l2s.gameserver.handler.bbs.ICommunityBoardHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
**/
public abstract class ScriptsCommunityHandler implements ScriptFile, ICommunityBoardHandler
{
	@Override
	public void onLoad()
	{
		if(Config.BBS_ENABLED)
			CommunityBoardManager.getInstance().registerHandler(this);
	}

	@Override
	public void onReload()
	{
		if(Config.BBS_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{
		//
	}

	protected boolean isOnPvPEvent(Player player) {
		if(player.isInPvPEvent() || player.getLfcGame() != null || player.getPendingLfcEnd())
			return true;

		List<SingleMatchEvent> events = player.getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
		{
			if(!event.canUseCommunityFunctions(player))
				return true;
		}
		return false;
	}

	protected boolean checkUseCondition(Player player)
	{
		if(player.getVar("jailed") != null)	// Если в тюрьме
			return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_DEAD)	// Если мертв, или притворяется мертвым
			if(player.isAlikeDead())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_EVENTS)	// На ивентах
			if(isOnPvPEvent(player))
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_A_BATTLE)	// В состоянии битвы
			if(player.isCastingNow() || player.isInCombat() || player.isAttackingNow())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_PVP)	// В PvP
			if(player.getPvpFlag() > 0)
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_OLLYMPIAD)	// На олимпиаде
			if(player.isInOlympiadMode() || player.getOlympiadObserveGame() != null)
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_FLIGHT)	// В состоянии полета
			if(player.isFlying() || player.isInFlyingTransform())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_IN_VEHICLE)	// На корабле
			if(player.isInBoat())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_MOUNTED)	// На ездовом животном
			if(player.isMounted())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_CANNOT_MOVE)	// В состоянии обизвдижения
			if(player.isMovementDisabled())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IN_TRADE)	// В состоянии торговли
			if(player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_FISHING)	// На рыбалке
			if(player.isFishing())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_TELEPORTING)	// Во время телепортации
			if(player.isLogoutStarted() || player.isTeleporting())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_DUEL)	// На дуели
			if(player.isInDuel())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WITH_CURSED_WEAPON)	// Имея проклятое оружие
			if(player.isCursedWeaponEquipped())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IS_PK)	// Когда PK
			if(player.isPK())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY)	// Если клан лидер
			if(!player.isClanLeader())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_NOBLESSES_ONLY)	// Если дворянин
			if(!player.isNoble())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_SIEGE)	// На осаждаемой территории
			if(player.isOnSiegeField())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY)	// В мирной зоне
			if(!player.isInZonePeace())
				return false;

		return true;
	}

	protected void onWrongCondition(Player player)
	{
		player.sendMessage(player.isLangRus() ? "Не соблюдены условия для использование данной функции." : "You are not allowed to use this action in you current stance.");
		player.sendPacket(ShowBoardPacket.CLOSE);
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
		{
			onWrongCondition(player);
			return;
		}

		doBypassCommand(player, bypass);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		if(BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
		{
			onWrongCondition(player);
			return;
		}

		doWriteCommand(player, bypass, arg1, arg2, arg3, arg4, arg5);
	}

	protected void doBypassCommand(Player player, String bypass)
	{
		//
	}

	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

}
