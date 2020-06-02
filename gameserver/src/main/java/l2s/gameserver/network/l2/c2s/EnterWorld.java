package l2s.gameserver.network.l2.c2s;

import java.util.Calendar;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CHeroDao;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.action.PlayerAction;
import l2s.gameserver.instancemanager.BotManager;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.CCPHelpers.CCPSecondaryPassword;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ClientSetTimePacket;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.ExAdenaInvenCount;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExBR_NewIConCashBtnWnd;
import l2s.gameserver.network.l2.s2c.ExBR_PremiumStatePacket;
import l2s.gameserver.network.l2.s2c.ExBasicActionList;
import l2s.gameserver.network.l2.s2c.ExEnterWorldPacket;
import l2s.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;
import l2s.gameserver.network.l2.s2c.ExGoodsInventoryChangedNotiPacket;
import l2s.gameserver.network.l2.s2c.ExLightingCandleEvent;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExNotifyPremiumItem;
import l2s.gameserver.network.l2.s2c.ExOpenMPCCPacket;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfoPacket;
import l2s.gameserver.network.l2.s2c.ExReceiveShowPostFriend;
import l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExUserInfoAbnormalVisualEffect;
import l2s.gameserver.network.l2.s2c.ExUserInfoCubic;
import l2s.gameserver.network.l2.s2c.ExUserInfoEquipSlot;
import l2s.gameserver.network.l2.s2c.ExUserInfoInvenWeight;
import l2s.gameserver.network.l2.s2c.HennaInfoPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MagicAndSkillList;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAllPacket;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.PetInfoPacket;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdatePacket;
import l2s.gameserver.network.l2.s2c.PledgeSkillListPacket;
import l2s.gameserver.network.l2.s2c.QuestListPacket;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.RidePacket;
import l2s.gameserver.network.l2.s2c.SSQInfoPacket;
import l2s.gameserver.network.l2.s2c.ShortCutInitPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.UIPacket;
import l2s.gameserver.security.hwid.HwidEngine;
import l2s.gameserver.security.hwid.HwidGamer;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.tables.OfflineBuffersTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.AdminFunctions;
import l2s.gameserver.utils.GameStats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.TradeHelper;

public class EnterWorld extends L2GameClientPacket
{
	private static final Object _lock = new Object();

	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	protected void readImpl()
	{
		//readS(); - клиент всегда отправляет строку "narcasse"
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}

		int MyObjectId = activeChar.getObjectId();
		Long MyStoreId = activeChar.getStoredId();

		synchronized (_lock)//TODO [G1ta0] че это за хуйня, и почему она тут
		{
			for(Player cha : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(MyStoreId.equals(cha.getStoredId()))
					continue;
				try
				{
					if(cha.getObjectId() == MyObjectId)
					{
						_log.warn("Double EnterWorld for char: " + activeChar.getName());
						cha.kick();
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
			}
		}
		
		if(GameObjectsStorage.getAllPlayers().size() >= Player.TUI)
		{
			activeChar.kick();
			return;
		}
		
		GameStats.incrementPlayerEnterGame();

		if(client != null && Config.ALLOW_HWID_ENGINE)
		{
			String HWID = client.getHWID();
			if(HWID != null)
			{
				String canILog = HwidEngine.getInstance().canILog(HWID);
				if(canILog == null)
					canILog = activeChar.getHwidLock() != null ? "Character is locked by HWID!" : HWID.equals(activeChar.getHwidLock()) ? null : null;
				if(canILog != null)
				{
					HwidEngine.getInstance().logFailedLogin(activeChar);
					activeChar.sendPacket(new ExShowScreenMessage(canILog, 30000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
					client.closeNow(false);
					return;
				}
		  
				HwidGamer gamer = HwidEngine.getInstance().newPlayer(activeChar);
				activeChar.setHwidGamer(gamer);
				//gamer.logToPlayer(activeChar.getObjectId(), "Successfully logged to game!");
			}
		}

		onEnterWorld(activeChar);
	}

	public static void onEnterWorld(Player activeChar)
	{
		boolean first = activeChar.entering;

		activeChar.sendPacket(ExLightingCandleEvent.DISABLED);
		//TODO: activeChar.sendPacket(new ExChannlChatEnterWorld(activeChar));
		//TODO: activeChar.sendPacket(new ExChannlChatPlegeInfo(activeChar));
		//activeChar.sendPacket(ExConnectedTimeAndGettableReward.STATIC);
		//activeChar.sendPacket(new ExPeriodicHenna(activeChar));
		activeChar.sendPacket(new ExEnterWorldPacket());
		activeChar.sendPacket(new HennaInfoPacket(activeChar));

		/*List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
			activeChar.sendPacket(new ExCastleState(c));*/

		activeChar.sendSkillList();
		activeChar.sendPacket(new EtcStatusUpdatePacket(activeChar));

		activeChar.sendPacket(new UIPacket(activeChar));
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
		activeChar.sendPacket(new ExUserInfoCubic(activeChar));
		activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));

		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

		/*double mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.PHYSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.PHYSIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MAGIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MAGIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MUSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MUSIC, mpCostDiff));*/

		activeChar.sendPacket(new QuestListPacket(activeChar));
		activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));

		activeChar.sendPacket(new SSQInfoPacket());

		activeChar.sendItemList(false);
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ShortCutInitPacket(activeChar));
		activeChar.sendPacket(new ExBasicActionList(activeChar));

		activeChar.getMacroses().sendMacroses();

		Announcements.getInstance().showAnnouncements(activeChar);

		if(!activeChar.isHero())
			activeChar.onHero(false);

		if(activeChar.getTitle().equals("*Away*"))
		{
			activeChar.setTitle(null);
			activeChar.setTitleColor(Player.DEFAULT_TITLE_COLOR, true);
		}

		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			{
				activeChar.setGMInvisible(true);
				activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
			}

			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);

			activeChar.spawnMe();
			activeChar.setPendingOlyEnd(false);
			activeChar.setPendingLfcEnd(false);

			if(activeChar.getVar("lfcfirst") == null)
			{
				activeChar.setVar("lfcNotes", "on", -1);
				activeChar.setVar("lfcfirst", "on", -1);
			}
			if(activeChar.isInStoreMode() && !activeChar.isInBuffStore())
				if(!TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType()))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(activeChar.isInBuffStore())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}

			activeChar.setRunning();
			activeChar.standUp();
			activeChar.startTimers();
			Log.LogEvent(activeChar.getName(), "EnterGame", "EnterGame", "char: "+activeChar.getName()+" entered into game");
		}

		activeChar.sendPacket(new ExBR_PremiumStatePacket(activeChar, activeChar.hasPremiumAccount()));

		activeChar.sendPacket(new ExSetCompassZoneCode(activeChar));
		//TODO: Исправить посылаемые данные.
		activeChar.sendPacket(new MagicAndSkillList(activeChar, 3503292, 730502));
		activeChar.sendPacket(new ExStorageMaxCountPacket(activeChar));
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));

		/*if(Config.ALLOW_WORLD_CHAT)
			activeChar.sendPacket(new ExWorldChatCnt(activeChar));*/

		if(!ProductHolder.getInstance().getAllItems().isEmpty())
			activeChar.sendPacket(ExBR_NewIConCashBtnWnd.HAS_UPDATES);	// TODO: Посылать при наличии новинок в Итем-молле 1, если нет, то 0.

		checkNewMail(activeChar);

		if(first)
			activeChar.getListeners().onEnter();

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

		if(first && activeChar.getCreateTime() > 0)
		{
			Calendar create = Calendar.getInstance();
			create.setTimeInMillis(activeChar.getCreateTime());
			Calendar now = Calendar.getInstance();

			int day = create.get(Calendar.DAY_OF_MONTH);
			if(create.get(Calendar.MONTH) == Calendar.FEBRUARY && day == 29)
				day = 28;

			int myBirthdayReceiveYear = activeChar.getVarInt(Player.MY_BIRTHDAY_RECEIVE_YEAR, 0);
			if(create.get(Calendar.MONTH) == now.get(Calendar.MONTH) && create.get(Calendar.DAY_OF_MONTH) == day)
			{
				if((myBirthdayReceiveYear == 0 && create.get(Calendar.YEAR) != now.get(Calendar.YEAR)) || myBirthdayReceiveYear > 0 && myBirthdayReceiveYear != now.get(Calendar.YEAR))
				{
					Mail mail = new Mail();
					mail.setSenderId(1);
					mail.setSenderName(StringsHolder.getInstance().getString(activeChar, "birthday.npc"));
					mail.setReceiverId(activeChar.getObjectId());
					mail.setReceiverName(activeChar.getName());
					mail.setTopic(StringsHolder.getInstance().getString(activeChar, "birthday.title"));
					mail.setBody(StringsHolder.getInstance().getString(activeChar, "birthday.text"));

					ItemInstance item = ItemFunctions.createItem(21169);
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(1L);
					item.save();

					mail.addAttachment(item);
					mail.setUnread(true);
					mail.setType(Mail.SenderType.BIRTHDAY);
					mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					mail.save();

					activeChar.setVar(Player.MY_BIRTHDAY_RECEIVE_YEAR, String.valueOf(now.get(Calendar.YEAR)), -1);
				}
			}
		}

		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);

			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeShowInfoUpdatePacket(activeChar.getClan()), new PledgeSkillListPacket(activeChar.getClan()));
		}

		// engage and notify Partner
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills();
		}

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();
		if(Config.SHOW_HTML_WELCOME)
		{
			String html = HtmCache.getInstance().getHtml("welcome.htm", activeChar);
			NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(5);
			msg.setHtml(HtmlUtils.bbParse(html));
			activeChar.sendPacket(msg);
		}	
		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			if(activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				Skill castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && activeChar.getAnimationEndTime() > 0)
					activeChar.sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}

			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));

			if(activeChar.isMoving || activeChar.isFollow)
				activeChar.sendPacket(activeChar.movePacket());

			if(activeChar.getMountNpcId() != 0)
				activeChar.sendPacket(new RidePacket(activeChar));

			if(activeChar.isFishing())
				activeChar.stopFishing();
		}

		activeChar.entering = false;

		if(activeChar.isHFClient())
			activeChar.sendUserInfo(true);

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitTypePacket(activeChar, ChangeWaitTypePacket.WT_SITTING));
		if(activeChar.isInStoreMode())
			activeChar.sendPacket(activeChar.getPrivateStoreMsgPacket(activeChar));

		if(activeChar.isDead())
			activeChar.sendPacket(new DiePacket(activeChar));

		activeChar.unsetVar("offline");

        if(!activeChar.isJailed())
        {
        	if(activeChar.getNoChannel() ==0)
        	{
        		activeChar.setNoChannel(0);
        	}
        	else
        	{
        		activeChar.getNoChannelRemained();
        	}
        	activeChar.standUp();
        	activeChar.unblock();       	   
        }
        else 
        {
        	//activeChar.sitDown(null);
        	//activeChar.block();
        	activeChar.setNoChannel(-1);
        	activeChar.teleToLocation(Location.findPointToStay(activeChar, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
        }
        
		// Just in case
		activeChar.sendActionFailed();
		PlayerAction.getInstance().useHandler(activeChar, PlayerAction.ActionType.PLAYER_ON_LOGIN);
		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			//silence
			if(activeChar.getVarBoolean("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			//invul
			if(activeChar.getVarBoolean("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.startAbnormalEffect(AbnormalEffect.INVINCIBILITY);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			//undying
			if(activeChar.getVarBoolean("gm_undying"))
			{
				activeChar.setGMUndying(true);
				activeChar.sendMessage("Undying state has been enabled.");
			}
			//gmspeed
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
					activeChar.doCast(SkillHolder.getInstance().getSkill(7029, var_gmspeed), activeChar, true);
			}
			catch(Exception E)
			{}
		}
		// Bot manager punishment
		if(Config.ALT_ENABLE_BOTREPORT)
			BotManager.getInstance().onEnter(activeChar);

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		activeChar.sendPacket(ClientSetTimePacket.STATIC);

		Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
			activeChar.sendPacket(new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));

		if(activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager cursedManagerInstance = CursedWeaponsManager.getInstance();
			cursedManagerInstance.getCursedWeapon(activeChar.getCursedWeaponEquippedId()).giveSkillAndUpdateStats();
			cursedManagerInstance.showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		}
		if(first)
		{
			activeChar.sendUserInfo();
			if (Config.BUFF_STORE_ENABLED)
			{
				OfflineBuffersTable.getInstance().onLogin(activeChar);
			}
		}
	
		if(!first)
		{
			//Персонаж вылетел во время просмотра
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
					activeChar.returnFromObserverMode();
				
				else if(activeChar.getOlympiadObserveGame() != null)
					activeChar.leaveOlympiadObserverMode(true);
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar);

			if(activeChar.getServitor() != null)
				activeChar.sendPacket(new PetInfoPacket(activeChar.getServitor()));

			if(activeChar.isInParty())
			{
				Servitor member_servitor;
				//sends new member party window for all members
				//we do all actions before adding member to a list, this speeds things up a little
				activeChar.sendPacket(new PartySmallWindowAllPacket(activeChar.getParty(), activeChar));

				for(Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
						activeChar.sendPacket(new PartySpelledPacket(member, true));
						if((member_servitor = member.getServitor()) != null)
							activeChar.sendPacket(new PartySpelledPacket(member_servitor, true));

						activeChar.sendPacket(RelationChangedPacket.update(activeChar, member, activeChar));
					}

				// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
				if(activeChar.getParty().isInCommandChannel())
					activeChar.sendPacket(ExOpenMPCCPacket.STATIC);
			}

			for(int shotId : activeChar.getAutoSoulShot())
				activeChar.sendPacket(new ExAutoSoulShot(shotId, true));

			for(Effect e : activeChar.getEffectList().getAllFirstEffects())
				if(e.getSkill().isToggle())
					activeChar.sendPacket(new MagicSkillLaunchedPacket(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar));

			activeChar.broadcastCharInfo();
		}
		else
			activeChar.sendUserInfo(); // Отобразит права в клане

		activeChar.updateEffectIcons();
		activeChar.updateStats();

		if(Config.ALT_PCBANG_POINTS_ENABLED)
			activeChar.sendPacket(new ExPCCafePointInfoPacket(activeChar, 0, 1, 2, 12));

		if(!activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotiPacket.STATIC : ExNotifyPremiumItem.STATIC);

		activeChar.sendVoteSystemInfo();
		activeChar.getNevitSystem().onEnterWorld();

		checkNewMail(activeChar);
		if(!CHeroDao.isExpiredFor(activeChar.getObjectId()) && !activeChar.isHero())
		{
			activeChar.setHero(true);
			activeChar.updatePledgeClass();
			if(!activeChar.isSubClassActive())
			{
				Hero.addSkills(activeChar);
				activeChar.sendSkillList();
			}
			activeChar.broadcastUserInfo(true);
		}
		else if(CHeroDao.isCustomHero(activeChar.getObjectId()))
		{
			activeChar.setHero(false);
			activeChar.updatePledgeClass();
			Hero.removeSkills(activeChar);
			CHeroDao.removeCustomHero(activeChar.getObjectId());	
			activeChar.sendSkillList();
			activeChar.broadcastUserInfo(true);			
		}

		activeChar.checkP2PPeriod();

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_ENTER_GAME))
			hook.onPlayerEnterGame(activeChar);
	}

	private static void notifyClanMembers(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		SubUnit subUnit = activeChar.getSubUnit();
		if(clan == null || subUnit == null)
			return;

		UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
		if(member == null)
			return;

		member.setPlayerInstance(activeChar, false);

		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		L2GameServerPacket msg = new SystemMessagePacket(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
		PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(activeChar);
		for(Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
				clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
			else if(clanMember.getObjectId() == apprentice)
				clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
			else
				clanMember.sendPacket(msg);
		}

		if(!activeChar.isClanLeader())
			return;

		ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
			return;

		if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
			return;

		if(clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < clanHall.getRentalFee())
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
	}

	private static void loadTutorial(Player player)
	{
		Quest q = QuestHolder.getInstance().getQuest(255);
		if(q != null)
		{
			player.processQuestEvent(q.getId(), "UC", null); //mandatory
			if(Config.ALLOW_SECOND_AUTH_CHECK && CCPSecondaryPassword.hasPassword(player))
				player.processQuestEvent(q.getId(), "CheckPass", null);
			else if(player.getLevel() == 1 || (Rnd.get(1, 10) == 1))
				if(Config.ALLOW_SECOND_AUTH_CHECK)
					player.processQuestEvent(q.getId(), "ProposePass", null);
			if(Config.ALLOW_SHOW_CHANGE_LOG)	
				player.processQuestEvent(q.getId(), "ShowChangeLog", null);	
		}
	}

	private static void checkNewMail(Player activeChar)
	{
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
			if(mail.isUnread())
			{
				activeChar.sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
	}
}