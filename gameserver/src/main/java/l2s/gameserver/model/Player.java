package l2s.gameserver.model;

import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_ALTERED_FLAG;
import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_PEACE_FLAG;
import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_PVP_FLAG;
import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_SIEGE_FLAG;
import static l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode.ZONE_SSQ_FLAG;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.ai.PlayableAI.nextAction;
import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.dao.CHeroDao;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.CharacterGroupReuseDAO;
import l2s.gameserver.dao.CharacterPostFriendDAO;
import l2s.gameserver.dao.CharacterSubclassDAO;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.dao.LfcDAO.Arenas;
import l2s.gameserver.dao.PremiumAccountDAO;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.FakePlayersHolder;
import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.PlayerTemplateHolder;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.voicecommands.impl.RefferalSystem;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.AwayManager;
import l2s.gameserver.instancemanager.BotCheckManager;
import l2s.gameserver.instancemanager.BotCheckManager.BotCheckQuestion;
import l2s.gameserver.instancemanager.BypassManager;
import l2s.gameserver.instancemanager.BypassManager.BypassType;
import l2s.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.instancemanager.LfcManager;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.instancemanager.OfflineBufferManager;
import l2s.gameserver.instancemanager.PvPRewardManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2s.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.OnPlayerChatMessageReceive;
import l2s.gameserver.listener.actor.player.impl.BotCheckAnswerListner;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import l2s.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.GameObjectTasks.ChargeCountDownTask;
import l2s.gameserver.model.GameObjectTasks.EndSitDownTask;
import l2s.gameserver.model.GameObjectTasks.EndStandUpTask;
import l2s.gameserver.model.GameObjectTasks.FlagKickTask;
import l2s.gameserver.model.GameObjectTasks.HourlyTask;
import l2s.gameserver.model.GameObjectTasks.KickTask;
import l2s.gameserver.model.GameObjectTasks.PvPFlagTask;
import l2s.gameserver.model.GameObjectTasks.RecomBonusTask;
import l2s.gameserver.model.GameObjectTasks.UnJailTask;
import l2s.gameserver.model.GameObjectTasks.WaterTask;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.Skill.AddedSkill;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.flags.PlayerFlags;
import l2s.gameserver.model.actor.instances.player.AntiFlood;
import l2s.gameserver.model.actor.instances.player.BlockList;
import l2s.gameserver.model.actor.instances.player.BookMarkList;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.model.actor.instances.player.FriendList;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.MacroList;
import l2s.gameserver.model.actor.instances.player.NevitSystem;
import l2s.gameserver.model.actor.instances.player.RecomBonus;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.actor.instances.player.ShortCutList;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.actor.instances.player.SubClassList;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.model.entity.boat.ClanAirShip;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubGameRoom;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.CompType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.FestivalMonsterInstance;
import l2s.gameserver.model.instances.GuardInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetBabyInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.instances.TamedBeastInstance;
import l2s.gameserver.model.instances.TrapInstance;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemContainer;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.LockType;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.model.items.PcFreight;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.PcRefund;
import l2s.gameserver.model.items.PcWarehouse;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.items.Warehouse.WarehouseType;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.model.pledge.RankPrivs;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.BonusRequest;
import l2s.gameserver.network.authcomm.gs2as.ReduceAccountPoints;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.components.SceneMovie;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.AbnormalStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.AcquireSkillListPacket;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.CIPacket;
import l2s.gameserver.network.l2.s2c.CameraModePacket;
import l2s.gameserver.network.l2.s2c.ChairSitPacket;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.ExBR_AgathionEnergyInfoPacket;
import l2s.gameserver.network.l2.s2c.ExBR_PremiumStatePacket;
import l2s.gameserver.network.l2.s2c.ExBasicActionList;
import l2s.gameserver.network.l2.s2c.ExBrExtraUserInfo;
import l2s.gameserver.network.l2.s2c.ExDominionWarStart;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExNewSkillToLearnByLevelUp;
import l2s.gameserver.network.l2.s2c.ExOlympiadMatchEndPacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadModePacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadSpelledInfoPacket;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfoPacket;
import l2s.gameserver.network.l2.s2c.ExPrivateStoreWholeMsg;
import l2s.gameserver.network.l2.s2c.ExQuestItemListPacket;
import l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.ExSpawnEmitterPacket;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExTeleportToLocationActivate;
import l2s.gameserver.network.l2.s2c.ExUserInfoCubic;
import l2s.gameserver.network.l2.s2c.ExVitalityPointInfo;
import l2s.gameserver.network.l2.s2c.ExVoteSystemInfoPacket;
import l2s.gameserver.network.l2.s2c.GetItemPacket;
import l2s.gameserver.network.l2.s2c.HennaInfoPacket;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.ItemListPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.LogOutOkPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MyTargetSelectedPacket;
import l2s.gameserver.network.l2.s2c.NpcInfoPoly;
import l2s.gameserver.network.l2.s2c.ObserverEndPacket;
import l2s.gameserver.network.l2.s2c.ObserverStartPacket;
import l2s.gameserver.network.l2.s2c.PartySmallWindowUpdatePacket;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAllPacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeletePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdatePacket;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyList;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyMsg;
import l2s.gameserver.network.l2.s2c.PrivateStoreList;
import l2s.gameserver.network.l2.s2c.PrivateStoreMsg;
import l2s.gameserver.network.l2.s2c.QuestListPacket;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.network.l2.s2c.RecipeShopMsgPacket;
import l2s.gameserver.network.l2.s2c.RecipeShopSellListPacket;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.RidePacket;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.network.l2.s2c.SetupGaugePacket;
import l2s.gameserver.network.l2.s2c.ShortBuffStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.ShortCutInitPacket;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.network.l2.s2c.SkillListPacket;
import l2s.gameserver.network.l2.s2c.SnoopPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SpecialCameraPacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.TargetSelectedPacket;
import l2s.gameserver.network.l2.s2c.TargetUnselectedPacket;
import l2s.gameserver.network.l2.s2c.TeleportToLocationPacket;
import l2s.gameserver.network.l2.s2c.TradeDonePacket;
import l2s.gameserver.network.l2.s2c.UIPacket;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.security.hwid.HwidGamer;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.skillclasses.Charge;
import l2s.gameserver.skills.skillclasses.Transformation;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.OfflineBuffersTable;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.SkillTreeTable;
import l2s.gameserver.taskmanager.AutoSaveManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.FishTemplate;
import l2s.gameserver.templates.Henna;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.fakeplayer.FakePlayerAITemplate;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.templates.premiumaccount.PremiumAccountTemplate;
import l2s.gameserver.utils.AdminFunctions;
import l2s.gameserver.utils.BotPunish;
import l2s.gameserver.utils.BotPunish.Punish;
import l2s.gameserver.utils.EffectsComparator;
import l2s.gameserver.utils.FixEnchantOlympiad;
import l2s.gameserver.utils.GameStats;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.SiegeUtils;
import l2s.gameserver.utils.SkillUtils;
import l2s.gameserver.utils.SqlBatch;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.TeleportUtils;
import l2s.gameserver.utils.TimeUtils;

public final class Player extends Playable implements PlayerGroup
{
	public static final int DEFAULT_NAME_COLOR = 0xFFFFFF;
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	public static final int MAX_POST_FRIEND_SIZE = 100;

	private static final Logger _log = LoggerFactory.getLogger(Player.class);

	public static final String NO_TRADERS_VAR = "notraders";
	public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	private static final String NOT_CONNECTED = "<not connected>";
	private static final String PA_ITEMS_RECIEVED = "pa_items_recieved";
	private static final String FREE_PA_RECIEVED = "free_pa_recieved";

	public final static int OBSERVER_NONE = 0;
	public final static int OBSERVER_STARTING = 1;
	public final static int OBSERVER_STARTED = 3;
	public final static int OBSERVER_LEAVING = 2;

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
	public static final int STORE_PRIVATE_BUFF = 20;

	public static final int RANK_VAGABOND = 0;
	public static final int RANK_VASSAL = 1;
	public static final int RANK_HEIR = 2;
	public static final int RANK_KNIGHT = 3;
	public static final int RANK_WISEMAN = 4;
	public static final int RANK_BARON = 5;
	public static final int RANK_VISCOUNT = 6;
	public static final int RANK_COUNT = 7;
	public static final int RANK_MARQUIS = 8;
	public static final int RANK_DUKE = 9;
	public static final int RANK_GRAND_DUKE = 10;
	public static final int RANK_DISTINGUISHED_KING = 11;
	public static final int RANK_EMPEROR = 12; // unused
	public static final int TUI = 3000; //tyt bydet ogranichenie

	public static final int[] EXPERTISE_LEVELS =
	{
			0,
			20,
			40,
			52,
			61,
			76,
			80,
			84,
			Integer.MAX_VALUE
	};

	private GameClient _connection;
	private String _login;
	
	private HwidGamer _gamer;
	private String _hwidLock;
	
	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _recomHave, _recomLeftToday, _fame;
	private int _recomLeft = 20;
	private int _recomBonusTime = 3600;
	private boolean _isHourglassEffected, _isRecomTimerActive;
	private int _deleteTimer;
	private boolean _isVoting = false;
	private boolean _hasFlagCTF = false;

	private long _createTime, _onlineTime, _onlineBeginTime, _leaveClanTime, _deleteClanTime, _NoChannel, _NoChannelBegin;
	private int _ping = -1;
	private long _uptime;
	
	private LfcManager lfcGame = null;
	/**
	 * Time on login in game
	 */

	private Player _friends_event;
	private int _stavka;
	private double _coeficient;	 
	private long _lastAccess;
	
	private Player _playerA = null;
	private Player _playerB = null;
	private boolean _isInHunterClub = false;
	private boolean _isInMassPvP = false;
	private boolean _isInRaceEvent = false;
	/**
	 * The Color of players name / title (white is 0xFFFFFF)
	 */
	private int _nameColor = DEFAULT_NAME_COLOR;
	private int _titlecolor = DEFAULT_TITLE_COLOR;

	private int _vitalityLevel = -1;
	private double _vitality = Config.VITALITY_LEVELS[4];
	private boolean _overloaded;

	boolean sittingTaskLaunched;

	/**
	 * Time counter when L2Player is sitting
	 */
	private int _waitTimeWhenSit;

	private boolean _autoLoot = Config.AUTO_LOOT, AutoLootHerbs = Config.AUTO_LOOT_HERBS;
	private boolean _certainDropEnabled = Config.ENABLE_CERTAIN_DROP_INVIDUAL ? true : false;

	private final PcInventory _inventory = new PcInventory(this);
	private final Warehouse _warehouse = new PcWarehouse(this);
	private final ItemContainer _refund = new PcRefund(this);
	private final PcFreight _freight = new PcFreight(this);

	public final BookMarkList bookmarks = new BookMarkList(this, 0);
	public Location bookmarkLocation = null;

	private final AntiFlood _antiFlood = new AntiFlood(this);

	/**
	 * The table containing all L2RecipeList of the L2Player
	 */
	private final Map<Integer, RecipeTemplate> _recipebook = new TreeMap<Integer, RecipeTemplate>();
	private final Map<Integer, RecipeTemplate> _commonrecipebook = new TreeMap<Integer, RecipeTemplate>();
	
	private final Map<String, Object> quickVars = new ConcurrentHashMap<String, Object>();

	/**
	 * Premium Items
	 */
	private Map<Integer, PremiumItem> _premiumItems = new TreeMap<Integer, PremiumItem>();

	/**
	 * The table containing all Quests began by the L2Player
	 */
	private final TIntObjectMap<QuestState> _quests = new TIntObjectHashMap<QuestState>();

	/**
	 * The list containing all shortCuts of this L2Player
	 */
	private final ShortCutList _shortCuts = new ShortCutList(this);

	/**
	 * The list containing all macroses of this L2Player
	 */
	private final MacroList _macroses = new MacroList(this);

	/**
	 * The list containing all subclasses of this L2Player
	 */
	private final SubClassList _subClassList = new SubClassList(this);

	/**
	 * The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5)
	 */
	private int _privatestore;
	/**
	 * @author Sir & Jkk
	 */
	private Skill _macroSkill = null;

	private String _manufactureName;
	private List<ManufactureItem> _createList = Collections.emptyList();
	/**
	 * Данные для магазина продажи
	 */
	private String _sellStoreName;
	private List<TradeItem> _sellList = Collections.emptyList();
	private List<TradeItem> _packageSellList = Collections.emptyList();
	/**
	 * Данные для магазина покупки
	 */
	private String _buyStoreName;
	private List<TradeItem> _buyList = Collections.emptyList();
	/**
	 * Данные для обмена
	 */
	private List<TradeItem> _tradeList = Collections.emptyList();

	/**
	 * hennas
	 */
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR, _hennaINT, _hennaDEX, _hennaMEN, _hennaWIT, _hennaCON;

	private Party _party;
	private Location _lastPartyPosition;

	private Clan _clan;
	private int _pledgeClass = 0, _pledgeType = Clan.SUBUNIT_NONE, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;

	/**
	 * GM Stuff
	 */
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();

	private boolean _messageRefusal = false, _tradeRefusal = false, _blockAll = false, _isPendingOlyEnd = false, _pending_lfc = false, _pending_lfc_start = false;

	private boolean _InTvT = false;
	private boolean _inZombieVsHumans = false;
	private boolean _inCtF = false;
	private boolean _inLastHero = false;	
	private boolean _isInGameThrones = false;	
	
	/**
	 * The L2Summon of the L2Player
	 */
	private Servitor _servitor = null;
	private boolean _riding;
	
	private int _botRating;
	
	private DecoyInstance _decoy = null;

	private Map<Integer, EffectCubic> _cubics = null;
	private int _agathionId = 0;

	private Request _request;

	private ItemInstance _arrowItem;

	/**
	 * The fists L2Weapon of the L2Player (used when no weapon is equipped)
	 */
	private WeaponTemplate _fistsWeaponItem;

	private Map<Integer, String> _chars = new HashMap<Integer, String>(8);

	/**
	 * The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
	 */
	public int expertiseIndex = 0;

	private ItemInstance _enchantScroll = null;
	private ItemInstance _autoAttributeItem = null;

	private WarehouseType _usingWHType;

	private boolean _isOnline = false;

	private AtomicBoolean _isLogout = new AtomicBoolean();

	/**
	 * The L2NpcInstance corresponding to the last Folk which one the player talked.
	 */
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	/**
	 * тут храним мультиселл с которым работаем
	 */
	private MultiSellListContainer _multisell = null;

	private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<Integer>();

	private WorldRegion _observerRegion;
	private AtomicInteger _observerMode = new AtomicInteger(0);

	public int _telemode = 0;

	private int _handysBlockCheckerEventArena = -1;

	public boolean entering = true;

	/**
	 * Эта точка проверяется при нештатном выходе чара, и если не равна null чар возвращается в нее
	 * Используется например для возвращения при падении с виверны
	 * Поле heading используется для хранения денег возвращаемых при сбое
	 */
	public Location _stablePoint = null;

	/**
	 * new loto ticket *
	 */
	public int _loto[] = new int[5];
	/**
	 * new race ticket *
	 */
	public int _race[] = new int[2];

	private final BlockList _blockList = new BlockList(this);
	private final FriendList _friendList = new FriendList(this);

	private boolean _hero = false;

	/**
	 * True if the L2Player is in a boat
	 */
	private Boat _boat;
	private Location _inBoatPosition;

	private PremiumAccountTemplate _premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(0);
	private Future<?> _premiumAccountExpirationTask;

	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;

	private boolean _noble = false;

	private boolean _inOlympiadMode;
	private OlympiadGame _olympiadGame;
	private OlympiadGame _olympiadObserveGame;

	private int _olympiadSide = -1;

	/**
	 * ally with ketra or varka related wars
	 */
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

	private int _cursedWeaponEquippedId = 0;

	private final Fishing _fishing = new Fishing(this);
	private boolean _isFishing;

	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;
	

	private Future<?> _vitalityTask;
	private Future<?> _pcCafePointsTask;
	private Future<?> _unjailTask;
	private Future<?> _flagKickTask;

	private final Lock _storeLock = new ReentrantLock();

	private int _zoneMask;

	private boolean _offline = false;
	private boolean _awaying = false;

	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;

	private int _pcBangPoints;

	Map<Integer, Skill> _transformationSkills = new HashMap<Integer, Skill>();

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;
	private int _battlefieldChatId;
	private int _lectureMark;

	private AtomicState _gmInvisible = new AtomicState();
	private AtomicState _gmUndying = new AtomicState();

	private List<String> bypasses = null, bypasses_bbs = null;
	private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

	private List<String> _blockedActions = new ArrayList<String>();

	private boolean _notShowBuffAnim = false;
	private boolean _notShowTraders = false;
	private boolean _debug = false;

	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;

	private IntObjectMap<TimeStamp> _sharedGroupReuses = new CHashIntObjectMap<TimeStamp>();
	private Pair<Integer, OnAnswerListener> _askDialog = null;

	// High Five: Navit's Bonus System
	private NevitSystem _nevitSystem = new NevitSystem(this);

	private MatchingRoom _matchingRoom;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<Integer, Long>();

	private Language _language = Config.DEFAULT_LANG;

	private final Map<String, CharacterVariable> _variables = new ConcurrentHashMap<String, CharacterVariable>();

	private long _lastEnchantItemTime = 0L;

	private FightClubGameRoom _fightClubGameRoom = null;

	private long _lastNotAfkTime = 0L;

	private long _resurrectionBuffBlockedTime = 0L;

	private final IntObjectMap<OptionDataTemplate> _options = new CTreeIntObjectMap<OptionDataTemplate>();

	private ClassId _selectedMultiClassId = null;

	// during fall validations will be disabled for 10 ms.
	private static final int FALLING_VALIDATION_DELAY = 10000;
	private volatile long _fallingTimestamp = 0;

	/**
	 * Конструктор для L2Player. Напрямую не вызывается, для создания игрока используется PlayerManager.create
	 */
	public Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_login = accountName;
	}

	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to _inventory </li>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2Player</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PlayerTemplate to apply to the L2Player
	 */
	private Player(final FakePlayerAITemplate fakeAiTemplate, final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_ai = new FakeAI(this, fakeAiTemplate);
	}

	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		if(GameObjectsStorage.getAllPlayers().size() >= Player.TUI)
		{
			kick();
			return;
		}		

		_ai = new PlayerAI(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _login;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}

	public String getHWID()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getHWID();
	}

	/**
	 * Возвращает список персонажей на аккаунте, за исключением текущего
	 *
	 * @return Список персонажей
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) _template;
	}
	
	public boolean isSameRace(Player initiator)
	{
		if(!initiator.isPlayer() || !this.isPlayer())
			return false;
		//if not same race and not KAMAEL and not DWARF
		if(initiator.getRace() != this.getRace() && this.getRace() != Race.KAMAEL && initiator.getRace() != Race.KAMAEL && initiator.getRace() != Race.DWARF && this.getRace() != Race.DWARF)
			return false;
			
		return true;	
	}		
	public void changeSex()
	{
		_template = PlayerTemplateHolder.getInstance().getPlayerTemplate(getClassId(), Sex.VALUES[getSex()].revert());
	}

	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		super.doCast(skill, target, forceUse);

		//if(getUseSeed() != 0 && skill.getSkillType() == SkillType.SOWING)
		//	sendPacket(new ExUseSharedGroupItem(getUseSeed(), getUseSeed(), 5000, 5000));
	}

	public void attackOnReuse(Skill skill, GameObject target)
	{
		switch(skill.getSkillType())
		{
			case PDAM:
			case CPDAM:
			case LETHAL_SHOT:
			case SPOIL:
			case SOWING:
			case STUN:
			case DRAIN_SOUL:			
				target.onForcedAttack(this, false);
				break;
		}
	}
	
	@Override
	public void sendReuseMessage(Skill skill)
	{
		/*Не актуально, клиент посылает сам эти сообщения.
		if(isCastingNow())
			return;
		TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;
		long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500)
			return;
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);
		if(hours > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));*/
	}

	@Override
	public final int getLevel()
	{
		return getActiveSubClass() == null ? 1 : getActiveSubClass().getLevel();
	}

	public int getSex()
	{
		return getTemplate().getSex().ordinal();
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public void offline()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}

		setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR, false);
		startAbnormalEffect(Config.SERVICES_OFFLINE_ABNORMAL_EFFECT);
		setOfflineMode(true);

		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1);

		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			leaveParty();
		}

		if(getServitor() != null)
			getServitor().unSummon();

		CursedWeaponsManager.getInstance().doLogout(this);

		Olympiad.logoutPlayer(this);

		if(getPendingLfcStart() && getLfcGame() == null) //--waiting for an oponent
			LfcManager.cancelArenaLogout(this, _arenaIdForLogout);
			
		if(getLfcGame() != null)
		{
			getLfcGame().endMatch(this);
			checkAndCancelLfcArena(getLfcGame().getArena()); //insurance
		}	
		
        if(isInObserverMode())
        {
            if(getOlympiadObserveGame() == null)
                leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(OBSERVER_NONE);
        }

		broadcastCharInfo();
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * Соединение закрывается, клиент закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void kick()
	{
		if(_connection != null)
		{
			_connection.close(LogOutOkPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	/**
	 * Соединение не закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void restart()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}

	/**
	 * Соединение закрывается, клиент не закрывается, персонаж сохраняется и удаляется из игры
	 */
	public void logout()
	{
		if(_connection != null)
		{
			_connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}
	
	public int _arenaIdForLogout = 0;
	
	public void setArenaIdForLogout(int arenaId)
	{
		_arenaIdForLogout = arenaId;
	}	
	
	public int getArenaIdForLogout()
	{
		return _arenaIdForLogout;
	}
	
	private void prepareToLogout()
	{
		if(_isLogout.getAndSet(true))
			return;

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_QUIT_GAME))
			hook.onPlayerQuitGame(this);

		if(getHwidGamer() != null)
			getHwidGamer().removePlayer(this);
		setNetConnection(null);
		setIsOnline(false);
		if(getPendingLfcStart() && getLfcGame() == null) //--waiting for an oponent
			LfcManager.cancelArenaLogout(this, _arenaIdForLogout);
		
		if(getLfcGame() != null)
		{
			getLfcGame().endMatch(this);
			checkAndCancelLfcArena(getLfcGame().getArena()); //insurance
		}	
		
		getListeners().onExit();

		if(isFlying() && !checkLandingState())
			_stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);

		if(isCastingNow())
			abortCast(true, true);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival."); //TODO [G1ta0] custom message
			leaveParty();
		}

		CursedWeaponsManager.getInstance().doLogout(this);
		if(_olympiadObserveGame != null)
			_olympiadObserveGame.removeSpectator(this);

		Olympiad.logoutPlayer(this);

		stopFishing();

		if(isInObserverMode()) 
		{
			if(getOlympiadObserveGame() == null)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(OBSERVER_NONE);
		}
		

		if(_stablePoint != null)
			teleToLocation(_stablePoint);

		Servitor pet = getServitor();
		if(pet != null)
		{
			pet.saveEffects();
			pet.unSummon();
		}

		_friendList.notifyFriends(false);

		if(isProcessingRequest())
			getRequest().cancel();

		stopAllTimers();

		if(isInBoat())
			getBoat().removePlayer(this);

		SubUnit unit = getSubUnit();
		UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(this);
			for(Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT).addString(_name));
				else if(clanMember.getObjectId() == apprentice)
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT).addString(_name));
			}
			member.setPlayerInstance(this, true);
		}

		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onLogout(this);

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);

		MatchingRoom room = getMatchingRoom();
		if(room != null)
		{
			if(room.getLeader() == this)
				room.disband();
			else
				room.removeMember(this, false);
		}
		setMatchingRoom(null);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		destroyAllTraps();

		if(_decoy != null)
		{
			_decoy.unSummon();
			_decoy = null;
		}

		stopPvPFlag();

		Reflection ref = getReflection();

		if(ref != ReflectionManager.DEFAULT)
		{
			if(ref.getReturnLoc() != null)
				_stablePoint = ref.getReturnLoc();

			ref.removeObject(this);				
		}

		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR><BR>
	 */
	public Collection<RecipeTemplate> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<RecipeTemplate> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final RecipeTemplate id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void registerRecipe(final RecipeTemplate recipe, boolean saveDB)
	{
		if(recipe == null)
			return;
		if(!recipe.isCommon())
			_recipebook.put(recipe.getId(), recipe);
		else
			_commonrecipebook.put(recipe.getId(), recipe);
		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
	}

	// ------------------- Quest Engine ----------------------


	public QuestState getQuestState(int id)
	{
		questRead.lock();
		try
		{
			return _quests.get(id);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Quest quest)
	{
		return getQuestState(quest.getId());
	}

	public boolean isQuestCompleted(int id)
	{
		QuestState qs = getQuestState(id);
		return qs != null && qs.isCompleted();
	}

	public boolean isQuestCompleted(Quest quest)
	{
		return isQuestCompleted(quest.getId());
	}

	public void setQuestState(QuestState qs)
	{
		questWrite.lock();
		try
		{
			_quests.put(qs.getQuest().getId(), qs);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(int id)
	{
		questWrite.lock();
		try
		{
			_quests.remove(id);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(Quest quest)
	{
		removeQuestState(quest.getId());
	}

	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<Quest>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.valueCollection())
			{
				if(qs.isStarted())
					quests.add(qs.getQuest());
			}
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
	{
		List<QuestState> states = new ArrayList<QuestState>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		QuestState qs;
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				qs = getQuestState(quest);
				if(qs != null && !qs.isCompleted())
					states.add(getQuestState(quest));
			}
		}
		return states;
	}

	public void processQuestEvent(int questId, String event, NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(questId);
		if(qs == null)
		{
			Quest q = QuestHolder.getInstance().getQuest(questId);
			if(q == null)
			{
				_log.warn("Quest ID[" + questId + "] not found!");
				return;
			}
			qs = q.newQuestState(this);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestListPacket(this)); // TODO: Зачем? о.0
	}

	public boolean isInventoryFull()
	{
		if(getWeightPenalty() >= 3 || getInventoryLimit() * 0.8 < getInventory().getSize())
			return true;
		return false;
	}

	/**
	 * Проверка на переполнение инвентаря и перебор в весе для квестов и эвентов
	 *
	 * @return true если ве проверки прошли успешно
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(isInventoryFull() || Config.QUEST_INVENTORY_MAXIMUM * 0.9 < getInventory().getQuestSize())
		{
			if(msg)
				sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	/**
	 * Останавливаем и запоминаем все квестовые таймеры
	 */
	public void stopQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
				qs.pauseQuestTimers();
			else
				qs.stopQuestTimers();
	}

	/**
	 * Восстанавливаем все квестовые таймеры
	 */
	public void resumeQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			qs.resumeQuestTimers();
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	/**
	 * Проверяет является ли этот персонаж владельцем крепости
	 *
	 * @param fortressId
	 * @return true если владелец
	 */
	public boolean isFortressLord(int fortressId)
	{
		return _clan != null && isClanLeader() && _clan.getHasFortress() == fortressId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(int value)
	{
		if(value > 255)
			_recomHave = 255;
		else if(value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}

	public int getRecomBonusTime()
	{
		if(_recomBonusTask != null)
			return (int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS));
		return _recomBonusTime;
	}

	public void setRecomBonusTime(int val)
	{
		_recomBonusTime = val;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public boolean isHourglassEffected()
	{
		return _isHourglassEffected;
	}

	public void setHourlassEffected(boolean val)
	{
		_isHourglassEffected = val;
	}

	public void startHourglassEffect()
	{
		setHourlassEffected(true);
		stopRecomBonusTask(true);
		sendVoteSystemInfo();
	}

	public void stopHourglassEffect()
	{
		setHourlassEffected(false);
		startRecomBonusTask();
		sendVoteSystemInfo();
	}

	public int addRecomLeft()
	{
		int recoms = 0;
		if(getRecomLeftToday() < 20)
			recoms = 10;
		else
			recoms = 1;
		setRecomLeft(getRecomLeft() + recoms);
		setRecomLeftToday(getRecomLeftToday() + recoms);
		sendUserInfo(true);
		return recoms;
	}

	public int getRecomLeftToday()
	{
		return _recomLeftToday;
	}

	public void setRecomLeftToday(final int value)
	{
		_recomLeftToday = value;
		setVar("recLeftToday", String.valueOf(_recomLeftToday), -1);
	}

	public void giveRecom(final Player target)
	{
		int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
			target.addRecomHave(1);
		if(getRecomLeft() > 0)
			setRecomLeft(getRecomLeft() - 1);

		sendUserInfo(true);
	}

	public void addRecomHave(final int val)
	{
		setRecomHave(getRecomHave() + val);
		broadcastUserInfo(true);
		sendVoteSystemInfo();
	}

	public int getRecomBonus()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoBonus(this);
		return 0;
	}

	public double getRecomBonusMul()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoMultiplier(this);
		return 1;
	}

	public void sendVoteSystemInfo()
	{
		sendPacket(new ExVoteSystemInfoPacket(this));
	}

	public boolean isRecomTimerActive()
	{
		return _isRecomTimerActive;
	}

	public void setRecomTimerActive(boolean val)
	{
		if(_isRecomTimerActive == val)
			return;

		_isRecomTimerActive = val;

		if(val)
			startRecomBonusTask();
		else
			stopRecomBonusTask(true);

		sendVoteSystemInfo();
	}

	private ScheduledFuture<?> _recomBonusTask;
	private ScheduledFuture<?> _chargeCountDownTask;

	public void stopChargeForceCountDown()
	{
		if(_chargeCountDownTask != null)
		{
			_chargeCountDownTask.cancel(false);
			_chargeCountDownTask = null;		
		}
	}
	
	public void startChargeForceCountDown()
	{
		if(_chargeCountDownTask == null)
			_chargeCountDownTask = ThreadPoolManager.getInstance().schedule(new ChargeCountDownTask(this), 10 * 60000); //10 min
	}
	
	public void startRecomBonusTask()
	{
		if(_recomBonusTask == null && getRecomBonusTime() > 0 && isRecomTimerActive() && !isHourglassEffected())
			_recomBonusTask = ThreadPoolManager.getInstance().schedule(new RecomBonusTask(this), getRecomBonusTime() * 1000);
	}

	public void stopRecomBonusTask(boolean saveTime)
	{
		if(_recomBonusTask != null)
		{
			if(saveTime)
				setRecomBonusTime((int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS)));
			_recomBonusTask.cancel(false);
			_recomBonusTask = null;
		}
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if(karma < 0)
			karma = 0;

		if(_karma == karma)
			return;
		
		_karma = karma;

		sendChanges();

		if(getServitor() != null)
			getServitor().broadcastCharInfo();
	}

	@Override
	public int getMaxLoad()
	{
		return (int) calcStat(Stats.MAX_LOAD, 69000, this, null);
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	private void updateEffectIconsImpl()
	{
		Effect[] effects = getEffectList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());

		PartySpelledPacket ps = new PartySpelledPacket(this, false);
		AbnormalStatusUpdatePacket mi = new AbnormalStatusUpdatePacket();

		for(Effect effect : effects)
			if(effect.isInUse())
			{
				if(effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
					sendPacket(new ShortBuffStatusUpdatePacket(effect));
				else
					effect.addIcon(mi);
				if(_party != null)
					effect.addPartySpelledIcon(ps);
			}

		sendPacket(mi);
		if(_party != null)
			_party.broadCast(ps);

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame olymp_game = _olympiadGame;
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfoPacket olympiadSpelledInfo = new ExOlympiadSpelledInfoPacket();

				for(Effect effect : effects)
					if(effect != null && effect.isInUse())
						effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);

				if(olymp_game.getType() == CompType.CLASSED || olymp_game.getType() == CompType.NON_CLASSED)
					for(Player member : olymp_game.getTeamMembers(this))
						member.sendPacket(olympiadSpelledInfo);

				for(Player member : olymp_game.getSpectators())
					member.sendPacket(olympiadSpelledInfo);
			}
		}

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
			event.onEffectIconsUpdate(this, effects);
	}

	public int getWeightPenalty()
	{
		return getSkillLevel(4270, 0);
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted() || getMaxLoad() <= 0)
			return;

		setOverloaded(getCurrentLoad() > getMaxLoad());
		double weightproc = 100. * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0, this, null)) / getMaxLoad();
		int newWeightPenalty = 0;

		if(weightproc < 50)
			newWeightPenalty = 0;
		else if(weightproc < 66.6)
			newWeightPenalty = 1;
		else if(weightproc < 80)
			newWeightPenalty = 2;
		else if(weightproc < 100)
			newWeightPenalty = 3;
		else
			newWeightPenalty = 4;

		int current = getWeightPenalty();
		if(current == newWeightPenalty)
			return;

		if(newWeightPenalty > 0)
			super.addSkill(SkillHolder.getInstance().getSkill(4270, newWeightPenalty));
		else
			super.removeSkill(getKnownSkill(4270));

		sendSkillList();
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getArmorsExpertisePenalty()
	{
		return getSkillLevel(6213, 0);
	}

	public int getWeaponsExpertisePenalty()
	{
		return getSkillLevel(6209, 0);
	}

	public int getExpertisePenalty(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			return getWeaponsExpertisePenalty();
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public void refreshExpertisePenalty()
	{
		if(!Config.EXPERTISE_PENALTY)
			return;
			
		if(isLogoutStarted())
			return;

		// Calculate the current higher Expertise of the L2Player
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for(i = 0; i < EXPERTISE_LEVELS.length; i++)
			if(level < EXPERTISE_LEVELS[i + 1])
				break;

		boolean skillUpdate = false; // Для того, чтобы лишний раз не посылать пакеты
		// Add the Expertise skill corresponding to its Expertise level
		if(expertiseIndex != i)
		{
			expertiseIndex = i;
			if(expertiseIndex > 0)
			{
				addSkill(SkillHolder.getInstance().getSkill(239, expertiseIndex), false);
				skillUpdate = true;
			}
		}

		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		ItemInstance[] items = getInventory().getPaperdollItems();
		for(ItemInstance item : items)
			if(item != null)
			{
				int crystaltype = item.getTemplate().getCrystalType().ordinal();
				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
				{
					if(crystaltype > newWeaponPenalty)
						newWeaponPenalty = crystaltype;
				}
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					if(crystaltype > newArmorPenalty)
						newArmorPenalty = crystaltype;
			}

		newWeaponPenalty = newWeaponPenalty - expertiseIndex;
		if(newWeaponPenalty <= 0)
			newWeaponPenalty = 0;
		else if(newWeaponPenalty >= 4)
			newWeaponPenalty = 4;

		newArmorPenalty = newArmorPenalty - expertiseIndex;
		if(newArmorPenalty <= 0)
			newArmorPenalty = 0;
		else if(newArmorPenalty >= 4)
			newArmorPenalty = 4;

		int weaponExpertise = getWeaponsExpertisePenalty();
		int armorExpertise = getArmorsExpertisePenalty();

		if(weaponExpertise != newWeaponPenalty)
		{
			weaponExpertise = newWeaponPenalty;
			if(newWeaponPenalty > 0)
				super.addSkill(SkillHolder.getInstance().getSkill(6209, weaponExpertise));
			else
				super.removeSkill(getKnownSkill(6209));
			skillUpdate = true;
		}
		if(armorExpertise != newArmorPenalty)
		{
			armorExpertise = newArmorPenalty;
			if(newArmorPenalty > 0)
				super.addSkill(SkillHolder.getInstance().getSkill(6213, armorExpertise));
			else
				super.removeSkill(getKnownSkill(6213));
			skillUpdate = true;
		}

		if(skillUpdate)
		{
			getInventory().validateItemsSkills();

			sendSkillList();
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public int getClassLevel()
	{
		return getClassId().getClassLevel().ordinal();
	}

	public boolean isPendingOlyEnd()
	{
		return _isPendingOlyEnd;
	}
	
	public void setPendingOlyEnd(boolean val)
	{
		_isPendingOlyEnd = val;
	}
	
	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].isOfLevel(ClassLevel.FIRST))
			_clan.incReputation((int) (100 * Config.CLAN_REPUTATION_MOD_ON_FINISH_ACADEMY), true, "Academy");
		else if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].isOfLevel(ClassLevel.SECOND))
		{
			int earnedPoints = 0;
			if(getLvlJoinedAcademy() <= 16)
				earnedPoints = Config.MAX_ACADEM_POINT;
			else if(getLvlJoinedAcademy() >= 39)
				earnedPoints = Config.MIN_ACADEM_POINT;
			else
				earnedPoints = Config.MAX_ACADEM_POINT - (getLvlJoinedAcademy() - 16) * 20;

			earnedPoints = (int) (earnedPoints * Config.CLAN_REPUTATION_MOD_ON_FINISH_ACADEMY);

			_clan.removeClanMember(getObjectId());

			long autoacademyReward = getVarLong("autoacademy_reward", 0);
			if(autoacademyReward > 0)
				addAdena(autoacademyReward, true);

			unsetVar("autoacademy_reward");

			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS);
			sm.addString(getName());
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDeletePacket(getName()), this);

			setClan(null);
			setTitle("");
			sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
			setLeaveClanTime(0);

			broadcastCharInfo();

			sendPacket(PledgeShowMemberListDeleteAllPacket.STATIC);

			ItemFunctions.addItem(this, 8181, 1, true, "Academy finish reward");
		}
	}

	/**
	 * Set the template of the L2Player.
	 *
	 * @param id The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	public synchronized boolean setClassId(final int id, boolean noban, boolean fromQuest)
	{
		ClassId classId = ClassId.VALUES[id];
		if(!noban && !(classId.equalsOrChildOf(ClassId.VALUES[getActiveClassId()]) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS))
		{
			Thread.dumpStack();
			return false;
		}

		PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classId, Sex.VALUES[getSex()]);
		if(template == null)
		{
			_log.error("Missing template for classId: " + id);
			return false;
		}

		_template = template;

		//Если новый ID не принадлежит имеющимся классам значит это новая профа
		if(!getSubClassList().containsClassId(id))
		{
			final SubClass cclass = getActiveSubClass();
			final ClassId oldClass = ClassId.VALUES[cclass.getClassId()];

			getSubClassList().changeSubClassId(oldClass.getId(), id);
			changeClassInDb(oldClass.getId(), id);

			if(cclass.isBase())
			{
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if(classId.isOfLevel(ClassLevel.FIRST))
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8869);
					unsetVar("newbieweapon");
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
					unsetVar("ng1");
					unsetVar("ng2");
					unsetVar("ng3");
					unsetVar("ng4");
				}
				else if(classId.isOfLevel(ClassLevel.SECOND))
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemFunctions.createItem(8870);
					unsetVar("newbiearmor");
					unsetVar("dd1"); // удаляем отметки о выдаче дименшен даймондов
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}

				if(coupons != null)
				{
					coupons.setCount(15);
					sendPacket(SystemMessagePacket.obtainItems(coupons));
					getInventory().addItem(coupons);
				}
			}

			onReceiveNewClassId(oldClass, classId);

			storeCharSubClasses();

			if(fromQuest)
			{
				// Социалка при получении профы
				broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
				sendPacket(new PlaySoundPacket("ItemSound.quest_fanfare_2"));
			}
		}

		broadcastUserInfo(true);

		// Update class icon in party and clan
		if(isInParty())
			getParty().broadCast(new PartySmallWindowUpdatePacket(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));
		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if(checkActiveToggleEffects())
					_log.warn("Removed toggle skills from cheater " + getName());
					
			}
		}, 800);
		return true;
	}

	private void onReceiveNewClassId(ClassId oldClass, ClassId newClass)
	{
		if(oldClass != null)
		{
			if(!isSubClassActive())
			{
				if(isNoble())
				{
					StatsSet noble = Olympiad._nobles.get(getObjectId());
					if(noble != null)
					{
						noble.set(Olympiad.CLASS_ID, newClass.getId());
						Olympiad._nobles.put(getObjectId(), noble);
					}
				}
			}

			if(!newClass.equalsOrChildOf(oldClass) || Config.ALLOWED_REBORN_COUNT > 0)
			{
				removeAllSkills();
				restoreSkills();
				rewardSkills(false);

				checkSkills();

				refreshExpertisePenalty();

				getInventory().refreshEquip();
				getInventory().validateItems();

				recalcHennaStats();

				sendSkillList();

				updateStats();
			}
			else
				rewardSkills(true);

			// Забираем Holy Pomander
			switch(oldClass)
			{
				case CARDINAL:
					ItemFunctions.deleteItem(this, 15307, ItemFunctions.getItemCount(this, 15307), false);
					break;
				case EVAS_SAINT:
					ItemFunctions.deleteItem(this, 15308, ItemFunctions.getItemCount(this, 15308), false);
					break;
				case SHILLIEN_SAINT:
					ItemFunctions.deleteItem(this, 15309, ItemFunctions.getItemCount(this, 15309), false);
					break;
			}
		}

		// Выдача Holy Pomander
		switch(newClass)
		{
			case CARDINAL:
				ItemFunctions.addItem(this, 15307, 1, true, "Give Holy Pomander on take new class");
				break;
			case EVAS_SAINT:
				ItemFunctions.addItem(this, 15308, 1, true, "Give Holy Pomander on take new class");
				break;
			case SHILLIEN_SAINT:
				ItemFunctions.addItem(this, 15309, 4, true, "Give Holy Pomander on take new class");
				break;
		}
	}

	public long getExp()
	{
		return getActiveSubClass() == null ? 0 : getActiveSubClass().getExp();
	}

	public long getMaxExp()
	{
		return getActiveSubClass() == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : getActiveSubClass().getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setAutoAttributeItem(final ItemInstance item)
	{
		_autoAttributeItem = item;
	}

	public ItemInstance getAutoAttributeItem()
	{
		return _autoAttributeItem;
	}

	public void addExpAndCheckBonus(MonsterInstance mob, final double noRateExp, double noRateSp, double partyVitalityMod)
	{
		if(getActiveSubClass() == null)
			return;

		// Начисление душ камаэлям
		double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0, mob, null);
		if(neededExp > 0 && noRateExp > neededExp)
		{
			mob.broadcastPacket(new ExSpawnEmitterPacket(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000);
		}

		double vitalityBonus = 1.;
		int npcLevel = mob.getLevel();
		if(Config.ALT_VITALITY_ENABLED)
		{
			boolean blessActive = getNevitSystem().isBlessingActive();
			vitalityBonus = 1. + (mob.isRaid() ? 0. : (getVitalityLevel(blessActive) * 0.5));
			vitalityBonus *= Config.ALT_VITALITY_RATE;
	
			if(noRateExp > 0)
			{
				if(!mob.isRaid())
				{
					if(!(getVarBoolean("NoExp") && getExp() == Experience.LEVEL[getLevel() + 1] - 1))
					{
						double points = ((noRateExp / (npcLevel * npcLevel)) * 100) / 9;
						points *= Config.ALT_VITALITY_CONSUME_RATE;

						if(blessActive || getEffectList().getEffectByType(EffectType.Vitality) != null)
							points = -points;

						setVitality(getVitality() - points * partyVitalityMod);
					}
				}
				else
					setVitality(getVitality() + Config.ALT_VITALITY_RAID_BONUS);
			}
		}

		//При первом вызове, активируем таймеры бонусов.
		if(!isInPeaceZone())
		{
			setRecomTimerActive(true);
			getNevitSystem().startAdventTask();
			if((getLevel() - npcLevel) <= 9)
			{
				int nevitPoints = (int) Math.round(((noRateExp / (npcLevel * npcLevel)) * 100) / 20); //TODO: Формула от балды.
				getNevitSystem().addPoints(nevitPoints);
			}
		}

		final long expWithoutBonus = (long)  (noRateExp * Config.RATE_XP_BY_LVL[getLevel()]);
		final long spWithoutBonus = (long)  (noRateSp * Config.RATE_SP_BY_LVL[getLevel()]);

		long normalExp = (long)  (noRateExp * Config.RATE_XP_BY_LVL[getLevel()] * getRateExp());
		normalExp += expWithoutBonus * (vitalityBonus - 1);
		normalExp += expWithoutBonus * (getRecomBonusMul() - 1);

		long normalSp = (long)  (noRateSp * Config.RATE_SP_BY_LVL[getLevel()] * getRateSp() * vitalityBonus);
		
		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true);
	}

	@Override
	public void addExpAndSp(long exp, long sp)
	{
		addExpAndSp(exp, sp, 0, 0, false, false);
	}

	public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet)
	{
		if(getActiveSubClass() == null)
			return;

		if(addToExp < 0 && isFakePlayer())
			return;

		if(applyRate)
		{
			addToExp *= Config.RATE_XP_BY_LVL[getLevel()] * getRateExp();
			addToSp *= Config.RATE_SP_BY_LVL[getLevel()] * getRateSp();
		}
		final long _staticExp;
		Servitor pet = getServitor();
		if(addToExp > 0)
		{
			if(applyToPet)
			{
				if(pet != null && !pet.isDead() && !PetDataTable.isSpecialPet(pet.getNpcId()))
					// Sin Eater забирает всю экспу у персонажа
					if(pet.getNpcId() == PetDataTable.SIN_EATER_ID)
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.isPet() && pet.getExpPenalty() > 0f)
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1. - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.), 0);
							addToExp *= 1. - pet.getExpPenalty() / 5.;
						}
					else if(pet.isSummon())
						addToExp *= 1. - pet.getExpPenalty();
			}
			
			// Remove Karma when the player kills L2MonsterInstance
			//TODO [G1ta0] двинуть в метод начисления наград при убйистве моба
			if(!isCursedWeaponEquipped() && addToSp > 0 && _karma > 0)
				_karma -= addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP_BY_LVL[getLevel()]);

			if(_karma < 0)
				_karma = 0;
				
			_staticExp = addToExp;	
			long max_xp = getVarBoolean("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}
		else
			_staticExp = 0;
		int oldLvl = getActiveSubClass().getLevel();

		getActiveSubClass().addExp(addToExp);
		getActiveSubClass().addSp(addToSp);

		if(addToExp > 0 && addToSp > 0 && (bonusAddExp > 0 || bonusAddSp > 0))
			sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addLong(addToExp).addLong(bonusAddExp).addInteger(addToSp).addInteger((int)bonusAddSp));
		else if(addToSp > 0 && addToExp == 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(_staticExp).addNumber(addToSp));
		else if(addToSp > 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));
		else if(addToSp == 0 && addToExp > 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE).addNumber(addToExp));		

		int level = getActiveSubClass().getLevel();
		if(level != oldLvl)
		{
			int levels = level - oldLvl;
			if(levels > 0)
				getNevitSystem().addPoints(1950);
			levelSet(levels);
			getListeners().onLevelChange(oldLvl, level);

			for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_GLOBAL_LEVEL_UP))
				hook.onPlayerGlobalLevelUp(this, oldLvl, level);
		}

		if(pet != null && pet.isPet() && PetDataTable.isSpecialPet(pet.getNpcId()))
		{
			PetInstance _pet = (PetInstance) pet;
			_pet.setLevel(getLevel());
			_pet.setExp(_pet.getExpForNextLevel());
			_pet.broadcastStatusUpdate();
		}

		updateStats();
	}


	/**
	 * Give Expertise skill of this level.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2Player </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 * @param send
	 */
	public void rewardSkills(boolean send)
	{
		rewardSkills(send, Config.AUTO_LEARN_SKILLS);
	}

	public void rewardSkills(boolean send, boolean autolearn)
	{
		boolean update = false;
		if(autolearn)
		{
			int unLearnable = 0;
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			while(skills.size() > unLearnable)
			{
				unLearnable = 0;
				for(SkillLearn s : skills)
				{
					Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
					if(sk == null || !sk.getCanLearn(getClassId()) || (!Config.AUTO_LEARN_FORGOTTEN_SKILLS && s.isClicked()))
					{
						unLearnable++;
						continue;
					}
					addSkill(sk, true);
				}
				skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			}
			update = true;
		}
		else
			// Скиллы дающиеся бесплатно не требуют изучения
			for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL))
			{
				if(skill.isFreeAutoGet(AcquireType.NORMAL))
				{
					Skill sk = SkillHolder.getInstance().getSkill(skill.getId(), skill.getLevel());
					addSkill(sk, true);
					if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
					{
						for(ShortCut sc : getAllShortCuts())
						{
							if(sc.getId() == sk.getId() && sc.getType() == ShortCut.TYPE_SKILL)
							{
								ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
								sendPacket(new ShortCutRegisterPacket(this, newsc));
								registerShortCut(newsc);
							}
						}
					}
					update = true;
				}
			}

		if(send && update)
			sendSkillList();

		updateStats();
	}

	public Race getRace()
	{
		return ClassId.VALUES[getBaseClassId()].getRace();
	}

	public int getIntSp()
	{
		return (int) getSp();
	}

	public long getSp()
	{
		return getActiveSubClass() == null ? 0 : getActiveSubClass().getSp();
	}

	public void setSp(long sp)
	{
		if(getActiveSubClass() != null)
			getActiveSubClass().setSp(sp);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	public long getOnlineTime()
	{
		return _onlineTime / 1000;
	}	
	
	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
			_NoChannel = -1;

		if(_NoChannel > 0)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0;
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
	}

	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= Config.ALT_CLAN_LEAVE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= Config.ALT_CLAN_CREATE_PENALTY_TIME * 60 * 60 * 1000L)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IStaticPacket canJoinParty(Player inviter)
	{
		Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter); // занят
		if(isBlockAll() || getMessageRefusal()) // всех нафиг
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		if(isInParty()) // уже
			return new SystemMessagePacket(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(inviter.getReflection() != getReflection()) // в разных инстантах
			if(inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT)
				return SystemMsg.INVALID_TARGET.packet(inviter);
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) // зарич
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(inviter.isInOlympiadMode() || isInOlympiadMode() || inviter.getLfcGame() != null || getLfcGame() != null) // олимпиада
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) // низя
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(getTeam() != TeamType.NONE && !isInTvT() && !isInCtF() && !isInZombieVsHumans()) // участник пвп эвента или дуэли
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(isInFightClub() && !getFightClubEvent().canJoinParty(inviter, this))
			return SystemMsg.INVALID_TARGET.packet(inviter);
		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight()
	{
		return _freight;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(StaticObjectInstance throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving)
		{
			getAI().setNextAction(nextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

		if(throne == null)
			broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_SITTING));
		else
			broadcastPacket(new ChairSitPacket(this, throne));

		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead())
			return;

		//exit if has chamelion rest effect or Relax effect.
		if(getEffectList().getEffectsBySkillId(296) != null)
			getEffectList().stopEffect(296);
			
		if(getEffectList().getEffectsBySkillId(226) != null)
			getEffectList().stopEffect(226);
			
		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_STANDING));

		_sittingObject = null;
		setSitting(false);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500);
	}

	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
			_waitTimeWhenSit += 2;
	}

	public int getWaitSitTime()
	{
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime()
	{
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public ItemContainer getRefund()
	{
		return _refund;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena)
	{
		return reduceAdena(adena, false);
	}

	/**
	 * Забирает адену у игрока.<BR><BR>
	 *
	 * @param adena  - сколько адены забрать
	 * @param notify - отображать системное сообщение
	 * @return true если сняли
	 */
	public boolean reduceAdena(long adena, boolean notify)
	{
		if(adena < 0)
			return false;
		if(adena == 0)
			return true;
		boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
			sendPacket(SystemMessagePacket.removeItems(ItemTemplate.ITEM_ID_ADENA, adena));
		return result;
	}

	public ItemInstance addAdena(long adena)
	{
		return addAdena(adena, false);
	}

	/**
	 * Добавляет адену игроку.<BR><BR>
	 *
	 * @param adena  - сколько адены дать
	 * @param notify - отображать системное сообщение
	 * @return L2ItemInstance - новое количество адены
	 */
	public ItemInstance addAdena(long adena, boolean notify)
	{
		if(adena < 1)
			return null;
		ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
			sendPacket(SystemMessagePacket.obtainItems(ItemTemplate.ITEM_ID_ADENA, adena, 0));
		return item;
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{		
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}

		if(Events.onAction(player, this, shift))
		{			
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}
		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{	
			player.setTarget(this);	
		}
		else if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			if(!checkInteractionDistance(player) && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				else			
					player.sendPacket(ActionFailPacket.STATIC);
			}
			else
				player.doInteract(this);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else if(player != this)
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else			
					player.sendPacket(ActionFailPacket.STATIC);
			}
			else		
				player.sendPacket(ActionFailPacket.STATIC);
		}
		else	
			player.sendPacket(ActionFailPacket.STATIC);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate()) //По идее еше должно срезать траффик. Будут глюки с отображением - убрать это условие.
			return;

		StatusUpdatePacket su = makeStatusUpdate(StatusUpdatePacket.MAX_HP, StatusUpdatePacket.MAX_MP, StatusUpdatePacket.MAX_CP, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.CUR_MP, StatusUpdatePacket.CUR_CP);
		sendPacket(su);

		// Check if a party is in progress
		if(isInParty())
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdatePacket(this));

		final List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
			event.onStatusUpdate(this);

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			if(_olympiadGame != null)
				_olympiadGame.broadcastInfo(this, null, false);
		}
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastUserInfo(false);
	}

	/**
	 * Отправляет UserInfo даному игроку и CharInfo всем окружающим.<BR><BR>
	 * <p/>
	 * <B><U> Концепт</U> :</B><BR><BR>
	 * Сервер шлет игроку UserInfo.
	 * Сервер вызывает метод {@link Creature#broadcastPacketToOthers(l2s.gameserver.network.l2.s2c.L2GameServerPacket...)} для рассылки CharInfo<BR><BR>
	 * <p/>
	 * <B><U> Действия</U> :</B><BR><BR>
	 * <li>Отсылка игроку UserInfo(личные и общие данные)</li>
	 * <li>Отсылка другим игрокам CharInfo(Public data only)</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Внимание</U> : НЕ ПОСЫЛАЙТЕ UserInfo другим игрокам либо CharInfo даному игроку.<BR>
	 * НЕ ВЫЗЫВАЕЙТЕ ЭТОТ МЕТОД КРОМЕ ОСОБЫХ ОБСТОЯТЕЛЬСТВ(смена сабкласса к примеру)!!! Траффик дико кушается у игроков и начинаются лаги.<br>
	 * Используйте метод {@link Player#sendChanges()}</B></FONT><BR><BR>
	 */
	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(!isVisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
			force = true;

		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	private int _polyNpcId;

	public void setPolyId(int polyid)
	{
		_polyNpcId = polyid;

		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	public boolean isPolymorphed()
	{
		return _polyNpcId != 0;
	}

	public int getPolyId()
	{
		return _polyNpcId;
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		if(!isVisible())
			return;

		L2GameServerPacket exCi = new ExBrExtraUserInfo(this);
		L2GameServerPacket dominion = getEvent(DominionSiegeEvent.class) != null ? new ExDominionWarStart(this) : null;
		for(Player player : World.getAroundPlayers(this))
		{
			if(isInvisible(player))
				continue;

			player.sendPacket(isPolymorphed() ? new NpcInfoPoly(this) : new CIPacket(this, player));
			player.sendPacket(exCi);
			player.sendPacket(RelationChangedPacket.update(player, this, player));
			if(dominion != null)
				player.sendPacket(dominion);
		}
	}

	public void broadcastRelationChanged()
	{
		//if(!isVisible() || isInvisible())
		//	return;

		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket( new CIPacket(this, player));
			player.sendPacket(RelationChangedPacket.update(player, this, player));
	}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;

		sendPacket(new EtcStatusUpdatePacket(this));
	}

	private Future<?> _userInfoTask;

	private class UserInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UIPacket(this), new ExBrExtraUserInfo(this));
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
			sendPacket(new ExDominionWarStart(this));
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted() || isFakePlayer())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	public void sendSkillList(int learnedSkillId)
	{
		sendPacket(new SkillListPacket(this, learnedSkillId));
		sendPacket(new AcquireSkillListPacket(this));
	}

	public void sendSkillList()
	{
		sendSkillList(0);
	}

	@Override
	public StatusUpdatePacket makeStatusUpdate(int... fields)
	{
		StatusUpdatePacket su = new StatusUpdatePacket(getObjectId());
		for(int field : fields)
			switch(field)
			{
				case StatusUpdatePacket.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdatePacket.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdatePacket.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdatePacket.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdatePacket.CUR_LOAD:
					su.addAttribute(field, getCurrentLoad());
					break;
				case StatusUpdatePacket.MAX_LOAD:
					su.addAttribute(field, getMaxLoad());
					break;
				case StatusUpdatePacket.PVP_FLAG:
					su.addAttribute(field, _pvpFlag);
					break;
				case StatusUpdatePacket.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdatePacket.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdatePacket.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
			}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdatePacket su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;

		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>(withPet ? 2 : 1);
		if(withPet && getServitor() != null)
			packets.add(getServitor().makeStatusUpdate(fields));

		packets.add(su);

		if(!broadCast)
			sendPacket(packets);
		else if(entering)
			broadcastPacketToOthers(packets);
		else
			broadcastPacket(packets);
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IStaticPacket p)
	{
		if(p == null)
			return;

		if(isPacketIgnored(p.packet(this)))
			return;

		GameClient connection = getNetConnection();
		if(connection != null && connection.isConnected())
			_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(IStaticPacket... packets)
	{
		for(IStaticPacket p : packets)
			sendPacket(p);
	}

	@Override
	public void sendPacket(List<? extends IStaticPacket> packets)
	{
		if(packets == null)
			return;

		for(IStaticPacket p : packets)
			sendPacket(p);
	}

	private boolean isPacketIgnored(IStaticPacket p)
	{
		if(p == null)
			return true;
		if(_notShowBuffAnim && (p.getClass() == MagicSkillUse.class || p.getClass() == MagicSkillLaunchedPacket.class))
			return true;

		//if(_notShowTraders && (p.getClass() == PrivateStoreMsgBuy.class || p.getClass() == PrivateStoreMsgSell.class || p.getClass() == RecipeShopMsg.class))
		//		return true;

		return false;
	}

	public void doInteract(GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(checkInteractionDistance(target))
			{
				Player temp = (Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreList(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreBuyList(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellListPacket(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUFF)
				{
					OfflineBufferManager.getInstance().processBypass(this, "BuffStore bufflist " + temp.getObjectId());
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();

		if((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		if(!item.isAdena())
		{
			if(item.isHerb() && !AutoLootHerbs)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
		}
		//Champion mob drop
		if(!item.isAdena() && fromNpc.isChampion() && Config.CHAMPION_DROP_ONLY_ADENA)
		{
			item.deleteMe();
			return;
		}

		// Herbs
		if(item.isHerb())
		{
			if(fromNpc.isChampion() && !Config.ALT_CHAMPION_DROP_HERBS)
			{
				item.deleteMe();
				return;
			}
			if(!AutoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			Skill[] skills = item.getTemplate().getAttachedSkills();
			if(skills.length > 0)
				for(Skill skill : skills)
				{
					altUseSkill(skill, this);
					if(getServitor() != null && getServitor().isSummon() && !getServitor().isDead())
						getServitor().altUseSkill(skill, getServitor());
				}
			item.deleteMe();
			return;
		}

		if(!_autoLoot && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(Config.ALLOW_REFFERAL_SYSTEM)
				if(fromNpc != null && Config.ITEM_LIST.contains(item.getItemId()))
					RefferalSystem.applyBonus(this, item.getItemId(), (int)item.getCount());
					
			if(!pickupItem(item, Log.Pickup))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
		}
		else
			getParty().distributeItem(this, item, fromNpc);

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		sendActionFailed();
		stopMove();

		ItemInstance item = (ItemInstance) object;

		synchronized(item)
		{
			if(!item.isVisible())
				return;

			// Check if me not owner of item and, if in party, not in owner party and nonowner pickup delay still active
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			// Herbs
			if(item.isHerb())
			{
				Skill[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
					for(Skill skill : skills)
					{
						altUseSkill(skill, this);
						if(this.getServitor() != null && this.getServitor().isSummon())
							altUseSkill(skill, this.getServitor());
					}
				broadcastPacket(new GetItemPacket(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;

			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, Log.Pickup))
				{
					broadcastPacket(new GetItemPacket(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
				getParty().distributeItem(this, item, null);
		}
	}

	public boolean pickupItem(ItemInstance item, String log)
	{
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;

		if(!ItemFunctions.canAddItem(this, item))
			return false;

		if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA || item.getItemId() == 6353)//FIXME [G1ta0] хардкод
		{
			Quest q = QuestHolder.getInstance().getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE" + item.getItemId(), null);
		}

		Log.LogEvent(getName(), getIP(), "PickUpItem", "player pickup item "+log+": "+getName()+" item: "+item.getCount()+" of "+item.getItemId()+"");
		Log.LogItem(this, log, item);
		sendPacket(SystemMessagePacket.obtainItems(item));
		getInventory().addItem(item);

		if(attachment != null)
			attachment.pickUp(this);

		sendChanges();
		return true;
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;

		// Can't target and attack festival monsters if not participant
		if(newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant())
			newTarget = null;	

		Party party = getParty();

		// Can't target and attack rift invaders if not in the same room
		if(party != null && party.isInDimensionalRift())
		{
			int riftType = party.getDimensionalRift().getType();
			int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if(newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				newTarget = null;
		}

		GameObject oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
			{
				// Validate location of the target.
				if(newTarget != null && newTarget.getObjectId() != getObjectId())
				{
					sendPacket(new ValidateLocationPacket(newTarget));
				}
				return;
			}

			// Remove the L2Player from the _statusListener of the old target if it was a L2Character
			if(oldTarget.isCreature())
				((Creature) oldTarget).removeStatusListener(this);
		}

		if(newTarget != null)
		{
			if(newTarget.isCreature())
			{
				final Creature target = (Creature) newTarget;

				// Add the L2Player to the _statusListener of the new target if it's a L2Character
				target.addStatusListener(this);

				if(isHFClient())
				{
					// To others the new target, and not yourself!
					broadcastPacketToOthers(new TargetSelectedPacket(getObjectId(), target.getObjectId(), getLoc()));

					// Show the client his new target.
					sendPacket(new MyTargetSelectedPacket(this, target));

					// Send max/current hp.
					sendPacket(target.makeStatusUpdate(StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP));

					// Validate location of the new target.
					if(target.getObjectId() != getObjectId())
					{
						sendPacket(new ValidateLocationPacket(target));
					}
				}
				else
				{
					// Validate location of the new target.
					if(target.getObjectId() != getObjectId())
					{
						sendPacket(new ValidateLocationPacket(target));
					}

					// Show the client his new target.
					sendPacket(new MyTargetSelectedPacket(this, target));

					// Send max/current hp.
					sendPacket(target.makeStatusUpdate(StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP));

					// To others the new target, and not yourself!
					broadcastPacketToOthers(new TargetSelectedPacket(getObjectId(), target.getObjectId(), getLoc()));

					// Send buffs
					//sendPacket(target.getAbnormalStatusUpdate());
				}
			}
			/*else
			{
				sendPacket(new MyTargetSelectedPacket(this, newTarget));
				broadcastPacket(new TargetSelectedPacket(getObjectId(), newTarget.getObjectId(), getLoc()));
			}*/
		}
		else
			broadcastPacket(new TargetUnselectedPacket(this));

		super.setTarget(newTarget);
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		final ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return null;

		return (WeaponTemplate) weapon.getTemplate();
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return null;

		final ItemTemplate item = weapon.getTemplate();

		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;

		return null;
	}

	public boolean isWearingArmor(final ArmorType armorType)
	{
		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(chest == null)
			return armorType == ArmorType.NONE;

		if(chest.getItemType() != armorType)
			return false;

		if(chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return true;

		final ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);

		return legs == null ? armorType == ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}
		if(Config.ALT_DAMAGE_INVIS)
		{
			if(!GeoEngine.canSeeTarget(this, attacker))
				damage = 0.;//TODO(kiBerGen): Converted into the formula for%
		}

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	private Creature lastAttacker = null;
	private long lastAttackDate = 0L;
  
	public Creature getLastAttacker()
	{
		return lastAttacker;
	}
  
	public long getLastAttackDate()
	{
		return lastAttackDate;
	}
  
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(damage <= 0)
			return;

		if(standUp)
		{
			standUp();
			if(isFakeDeath())
				breakFakeDeath();
		}
		
		lastAttacker = attacker;
		lastAttackDate = System.currentTimeMillis();
		
		if(attacker.isPlayable())
		{
			if(!directHp && getCurrentCp() > 0)
			{
				double cp = getCurrentCp();
				if(isInOlympiadMode())
					addDamageOnOlympiad(attacker, skill, damage, cp);

				if(cp >= damage)
				{
					cp -= damage;
					damage = 0;
				}
				else
				{
					damage -= cp;
					cp = 0;
				}
				setCurrentCp(cp);
			}
		}

		double hp = getCurrentHp();

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
		{
			if((hp - damage) <= 1 && !isDeathImmune()) // если хп <= 1 - убит
			{
				setCurrentHp(1, true);
				duelEvent.onDie(this);
				return;
			}
		}

		if(getLfcGame() != null && !getPendingLfcStart())
		{
			if((hp - damage) <= 1 && !isDeathImmune()) // если хп <= 1 - убит
			{
				((Player) attacker).setPendingLfcEnd(true);
				setCurrentHp(1, true);
				attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				attacker.sendActionFailed();
				
				if(this != null)	
				{
					if(isDead())
					{
						broadcastPacket(new RevivePacket(this));
					}		
					setPendingLfcEnd(true);
					getLfcGame().endMatch(this);
					setLfcGame(null);
					return;
				}		
			}		
		}
		
		if(isInOlympiadMode())
		{
			addDamageOnOlympiad(attacker, skill, damage, hp);

			if((hp - damage) <= 1 && !isDeathImmune()) // если хп <= 1 - убит
			{
				if(_olympiadGame.getType() != CompType.TEAM)
				{
					setCurrentHp(1, true);
					_olympiadGame.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					
					_olympiadGame.endGame(20000, false, false);
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendActionFailed();
					if(attacker != null)
					{
						if(attacker.isPlayer())
							attacker.getPlayer().setPendingOlyEnd(true);
						for(Effect e : attacker.getEffectList().getAllEffects())
							if(e.getEffectType() != EffectType.Cubic && !e.getSkill().isToggle())
								e.exit();
						attacker.sendPacket(new ExOlympiadModePacket(0));					
					}		
					if(this != null)	
					{
						setPendingOlyEnd(true);
						for(Effect e : this.getEffectList().getAllEffects())
							if(e.getEffectType() != EffectType.Cubic && !e.getSkill().isToggle())
								e.exit();
						this.sendPacket(new ExOlympiadModePacket(0));	
						
						if(isDead())
						{
							broadcastPacket(new RevivePacket(this));
						}						
					}		
					return;
				}
				else if(_olympiadGame.doDie(this)) // Все умерли
				{
					_olympiadGame.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					_olympiadGame.endGame(20000, false, false);
				}
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	public void addDamageOnOlympiad(Creature attacker, Skill skill, double damage, double hpcp)
	{
		if(this != attacker && (skill == null || skill.isOffensive())) // consider damage from simple strokes and attacking skills
			_olympiadGame.addDamage(this, Math.min(hpcp, damage));
	}

	private void altDeathPenalty(final Creature killer)
	{
		// Reduce the Experience of the L2Player in function of the calculated Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle())
			return;
		if(isInZonePvP())
			return;	
		if(getNevitSystem().isBlessingActive())
			return;
		deathPenalty(killer);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final Player killer)
	{
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

		// Calculate the level difference Multiplier between attacker and killed L2Player
		//final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // * lvlDiffMulti);
		killer.setPkKills(killer.getPkKills() + 1);
	}

	public final void doKillInPeace(final Player killer) // Check if the L2Player killed haven't Karma
	{
		if(_karma <= 0)
		{
			doPurePk(killer);
		}
		else
		{
			PvPRewardManager.tryGiveReward(this, killer);
			killer.setPvpKills(killer.getPvpKills() + 1);
		}	
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); i++)
			array.add(items.remove(Rnd.get(items.size())));
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer)
	{
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
			attachment.onDeath(this, killer);

		if(killer == null || killer == _servitor || killer == this)
			return;

		if((isInZoneBattle() || killer.isInZoneBattle()) && !Config.ZONE_PVP_COUNT && !isInSpecialPvPZone())
			return;

		if(killer.isServitor() && (killer = killer.getPlayer()) == null)
			return;

		if(isInFightClub())
			return;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer())
		{
			final Player pk = (Player) killer;
			final int repValue = (int) ((getLevel() - pk.getLevel() >= 20 ? 2 : 1) * Config.CLAN_REPUTATION_MOD_ON_KILL_WAR_ENEMY);
			boolean war = atMutualWarWith(pk);

			//TODO [VISTALL] fix it
			if(war /*|| _clan.getSiege() != null && _clan.getSiege() == pk.getClan().getSiege() && (_clan.isDefender() && pk.getClan().isAttacker() || _clan.isAttacker() && pk.getClan().isDefender())*/)
			{
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}
			}

			CastleSiegeEvent siegeEvent = getEvent(CastleSiegeEvent.class);
			CastleSiegeEvent siegeEventPk = pk.getEvent(CastleSiegeEvent.class);
			if(siegeEvent != null && siegeEvent == siegeEventPk && (((siegeEventPk.getSiegeClan(CastleSiegeEvent.DEFENDERS, pk.getClan())) != (siegeEvent.getSiegeClan(CastleSiegeEvent.ATTACKERS, getClan()))) || ((siegeEventPk.getSiegeClan(CastleSiegeEvent.ATTACKERS, pk.getClan())) != (siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS, getClan())))))
			{
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}
			}

			FortressSiegeEvent fsiegeEvent = getEvent(FortressSiegeEvent.class);
			FortressSiegeEvent fsiegeEventPk = pk.getEvent(FortressSiegeEvent.class);
			if(fsiegeEvent != null && fsiegeEvent == fsiegeEventPk && (((fsiegeEventPk.getSiegeClan(FortressSiegeEvent.DEFENDERS, pk.getClan())) != (fsiegeEvent.getSiegeClan(FortressSiegeEvent.ATTACKERS, getClan()))) || ((fsiegeEventPk.getSiegeClan(FortressSiegeEvent.ATTACKERS, pk.getClan())) != (fsiegeEvent.getSiegeClan(FortressSiegeEvent.DEFENDERS, getClan())))))
			{
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}
			}

			ClanHallSiegeEvent chsiegeEvent = getEvent(ClanHallSiegeEvent.class);
			ClanHallSiegeEvent chsiegeEventPk = pk.getEvent(ClanHallSiegeEvent.class);
			if(chsiegeEvent != null && chsiegeEvent == chsiegeEventPk && (((chsiegeEventPk.getSiegeClan(ClanHallSiegeEvent.DEFENDERS, pk.getClan())) != (chsiegeEvent.getSiegeClan(ClanHallSiegeEvent.ATTACKERS, getClan()))) || ((chsiegeEventPk.getSiegeClan(ClanHallSiegeEvent.ATTACKERS, pk.getClan())) != (chsiegeEvent.getSiegeClan(ClanHallSiegeEvent.DEFENDERS, getClan())))))
			{
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}
			}

			if(isOnSiegeField() && !Config.SIEGE_PVP_COUNT)
				return;

			if(_pvpFlag > 0 || war || isOnSiegeField() && Config.SIEGE_PVP_COUNT || (isInZoneBattle() || killer.isInZoneBattle()) && (Config.ZONE_PVP_COUNT || isInSpecialPvPZone()) || isInZonePvP())
			{
				PvPRewardManager.tryGiveReward(this, pk);
				pk.setPvpKills(pk.getPvpKills() + 1);
				if(Config.ANNOUNCE_PVP_KILL)
					Announcements.getInstance().announceByCustomMessage("l2s.gameserver.model.Player.kill.PvP", new String[]{ pk.getName(), getName() });
			}
			else
			{
				doKillInPeace(pk);
				if(Config.ANNOUNCE_PK_KILL)
					Announcements.getInstance().announceByCustomMessage("l2s.gameserver.model.Player.kill.PK", new String[]{ pk.getName(), getName() });
			}

			pk.sendChanges();
		}

		int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);

		// в нормальных условиях вещи теряются только при смерти от гварда или игрока
		// кроме того, альт на потерю вещей при сметри позволяет терять вещи при смтери от монстра
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(isFakePlayer() || killer.isMonster() && !Config.DROP_ITEMS_ON_DIE // если убил монстр и альт выключен
				|| isPvP // если убил игрок или гвард и
				&& (_pkKills < Config.MIN_PK_TO_ITEMS_DROP // количество пк слишком мало
				|| karma <= 0 && Config.KARMA_NEEDED_TO_DROP) // кармы нет
				|| isFestivalParticipant() // в фестивале вещи не теряются
				|| !killer.isMonster() && !isPvP) // в прочих случаях тоже
			return;

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return;

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // базовый шанс в процентах
		if(isPvP)
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;

		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					dropItemCount++;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					dropEquipCount++;
				else
					dropWeaponCount++;
			}

		List<ItemInstance> drop = new LazyArrayList<ItemInstance>(), // общий массив с результатами выбора
		dropItem = new LazyArrayList<ItemInstance>(), dropEquip = new LazyArrayList<ItemInstance>(), dropWeapon = new LazyArrayList<ItemInstance>(); // временные

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
					continue;

				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
					dropWeapon.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					dropEquip.add(item);
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_OTHER)
					dropItem.add(item);
			}

			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);

			// Dropping items, if present
			if(drop.isEmpty())
				return;

			for(ItemInstance item : drop)
			{
				if(item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
				{
					item.setVariationStoneId(0);
					item.setVariation1Id(0);
					item.setVariation2Id(0);
				}

				item = getInventory().removeItem(item);
				Log.LogEvent(getName(), getIP(), "PickUpItem", "player pvp drop item: "+getName()+" dropped item: "+item.getCount()+" of "+item.getItemId()+"");
				Log.LogItem(this, Log.PvPDrop, item);

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				if(killer.isPlayable() && ((Config.AUTO_LOOT && Config.AUTO_LOOT_PK) || this.isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item);
					Log.LogEvent(killer.getPlayer().getName(), killer.getPlayer().getIP(), "PickUpItem", "player pickup item from pk: "+killer.getPlayer().getName()+" item: "+item.getCount()+" of "+item.getItemId()+"");
					Log.LogItem(this, Log.Pickup, item);

					killer.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item));
				}
				else
					item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		//firstly let's check the pvp special then other
		if(isInSpecialPvPZone() && killer.getPlayer() != null)
		{
			if(_pvpFlag == 0 && getKarma() == 0)
				manageDropSpecial(killer, 1); //white
			else if(_pvpFlag != 0 && getKarma() == 0)
				manageDropSpecial(killer, 2); //purple
			else if(getKarma() > 0)
				manageDropSpecial(killer, 3); //red
			if(Config.ENABLE_SPECIAL_PVP_EXP_LOSS_SPECIAL_PERCENT)
			{	
				int level = getLevel();
				int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * Config.PVP_LOSS_PERCENT_DEATH / 100);	
				long before = getExp();
				addExpAndSp(-lostexp, 0);
				long lost = before - getExp();

				if(lost > 0)
					setVar("lostexp", String.valueOf(lost), -1);		
			}	
		}
		//Check for active charm of luck for death penalty
		// Sir for fake players Fix
		DeathPenalty dp = getDeathPenalty();
		if(dp != null)
			dp.checkCharmOfLuck();

		if(isInStoreMode())
			setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				sendPacket(TradeDonePacket.FAIL);
				parthner.sendPacket(TradeDonePacket.FAIL);
			}
			request.cancel();
		}
		if(_cubics != null)
			getEffectList().stopAllSkillEffects(EffectType.Cubic);

		setAgathion(0);

		boolean checkPvp = true;
		if(Config.ALLOW_CURSED_WEAPONS)
		{
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			}
			else if(killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}
		}

		Player killerPlayer = killer.getPlayer();
		if(killerPlayer != null)
		{
			for(SingleMatchEvent event : getEvents(SingleMatchEvent.class))
			{
				if(!event.canIncreasePvPPKCounter(killerPlayer, this))
				{
					checkPvp = false;
					break;
				}
			}
		}

		if(checkPvp)
		{
			doPKPVPManage(killer);
			altDeathPenalty(killer);
		}

		//And in the end of process notify death penalty that owner died :)
		getDeathPenalty().notifyDead(killer);

		setIncreasedForce(0);

		if(isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift)
			((DimensionalRift) getParty().getReflection()).memberDead(this);

		stopWaterTask();

		if(!isSalvation() && isOnSiegeField() && isCharmOfCourage())
		{
			ask(new ConfirmDlgPacket(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100, false));
			setCharmOfCourage(false);
		}

		if(getLevel() < 6)
		{
			Quest q = QuestHolder.getInstance().getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE30", null);
		}
		if(getServitor() != null && Config.DISALLOW_PET_ACTIONS_IF_MASTER_DEAD)
		{
			getServitor().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_DIE))
			hook.onPlayerDie(this, killer);

		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		int lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100), 0);
	}

	public void deathPenalty(Creature killer)
	{
		if(killer == null || isInFightClub())
			return;
		final boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());

		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if(deathPenaltyBonus < 2)
			deathPenaltyBonus = 1;
		else
			deathPenaltyBonus = deathPenaltyBonus / 2;

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = 8.0;

		int level = getLevel();
		if(level >= 79)
			percentLost = 1.0;
		else if(level >= 78)
			percentLost = 1.5;
		else if(level >= 76)
			percentLost = 2.0;
		else if(level >= 40)
			percentLost = 4.0;

		if(Config.ALT_DEATH_PENALTY)
			percentLost = percentLost * Config.RATE_XP_BY_LVL[getLevel()] + _pkKills * Config.ALT_PK_DEATH_RATE;

		if(isFestivalParticipant() || atwar)
			percentLost = percentLost / 4.0;

		// Calculate the Experience loss
		int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100);
		lostexp *= deathPenaltyBonus;
		lostexp = lostexp / Config.MULTIPLIER_DECREASE; 

		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);

		// На зарегистрированной осаде нет потери опыта, на чужой осаде - как при обычной смерти от *моба*
		if(isOnSiegeField())
		{
			SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null)
				lostexp = 0;

			if(siegeEvent != null)
			{
				List<Effect> effect = getEffectList().getEffectsBySkillId(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
				if(effect != null)
				{
					int syndromeLvl = effect.get(0).getSkill().getLevel();
					if(syndromeLvl < 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
						skill.getEffects(this, this, false, false);
					}
					else if(syndromeLvl == 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
						skill.getEffects(this, this, false, false);
					}
				}
				else
				{
					Skill skill = SkillHolder.getInstance().getSkill(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
					if(skill != null)
						skill.getEffects(this, this, false, false);
				}
			}
		}

		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();

		if(lost > 0)
			setVar("lostexp", String.valueOf(lost), -1);
	}

	public void setRequest(Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	/**
	 * Проверка, занят ли игрок для ответа на зарос
	 *
	 * @return true, если игрок не может ответить на запрос
	 */
	public boolean isBusy()
	{
		if(isProcessingRequest() || isOutOfControl() || isInOlympiadMode() || getLfcGame() != null || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible(null) || !isActive())
			return true;
		if(getTeam() != TeamType.NONE && Config.DISABLE_PARTY_ON_EVENT)
			return true;
		if(isInTvT() && Config.DISABLE_PARTY_ON_EVENT_TVT)
			return true;	
		if(isInLastHero() && Config.DISABLE_PARTY_ON_EVENT_LH)
			return true;	
		if(isInZombieVsHumans())
			return true;
		return false;		
	}

	public boolean isProcessingRequest()
	{
		if(_request == null)
			return false;
		if(!_request.isInProgress())
			return false;
		return true;
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
	}
	public boolean isInPost()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.POST);
	}

	public List<L2GameServerPacket> addVisibleObject(GameObject object, Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible())
			return Collections.emptyList();

		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(isInvisible(forPlayer) && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();

		if(getPrivateStoreType() != STORE_PRIVATE_NONE && !isInBuffStore() && forPlayer.getVarBoolean("notraders"))
			return Collections.emptyList();

		// Если это фэйк обсервера - не показывать.
		if(isInObserverMode() && getCurrentRegion() != getObserverRegion() && getObserverRegion() == forPlayer.getCurrentRegion())
			return Collections.emptyList();

		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		if(forPlayer.getObjectId() != getObjectId())
			list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CIPacket(this, forPlayer));

		list.add(new ExBrExtraUserInfo(this));

		if(isSitting() && _sittingObject != null)
			list.add(new ChairSitPacket(this, _sittingObject));

		if(isInStoreMode())
			list.add(getPrivateStoreMsgPacket(forPlayer));

		if(isCastingNow())
		{
			Creature castingTarget = getCastingTarget();
			Skill castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0)
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
		}

		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));

		list.add(RelationChangedPacket.update(forPlayer, this, forPlayer));
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null)
			list.add(new ExDominionWarStart(this));

		if(isInBoat())
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		else
		{
			if(isMoving || isFollow)
				list.add(movePacket());
		}
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId()) // FIXME  || isTeleporting()
			return null;

		List<L2GameServerPacket> result = list == null ? object.deletePacketList() : list;

		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	public void levelSet(int levels)
	{
		if(levels > 0)
		{
			sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
			broadcastPacket(new SocialActionPacket(getObjectId(), SocialActionPacket.LEVEL_UP));

			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());

			Quest q = QuestHolder.getInstance().getQuest(255);
			if(q != null)
				processQuestEvent(q.getId(), "CE40", null);
		}
		else if(levels < 0)
			if(Config.ALT_REMOVE_SKILLS_ON_DELEVEL)
				checkSkills();

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));

		if(_matchingRoom != null)
			_matchingRoom.broadcastPlayerUpdate(this);

		// Give Expertise skill of this level
		if(Config.ALLOWED_REBORN_COUNT > 0)
		{
			removeAllSkills();
			restoreSkills();
			rewardSkills(false);

			checkSkills();

			refreshExpertisePenalty();

			getInventory().refreshEquip();
			getInventory().validateItems();

			recalcHennaStats();

			sendPacket(new SkillListPacket(this));

			updateStats();
		}
		else
			rewardSkills(true);

		if(levels > 0)
			notifyNewSkills();
	}

	public boolean notifyNewSkills()
	{
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		for(SkillLearn s : skills)
		{
			if(s.isFreeAutoGet(AcquireType.NORMAL))
				continue;

			Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
			if(sk == null)
				continue;

			sendPacket(ExNewSkillToLearnByLevelUp.STATIC);
			return true;
		}
		return false;
	}

	/**
	 * Удаляет все скиллы, которые учатся на уровне большем, чем текущий+maxDiff
	 */
	public void checkSkills()
	{
		for(Skill sk : getAllSkillsArray())
			SkillTreeTable.checkSkill(this, sk);
	}

	public void startTimers()
	{
		startAutoSaveTask();
		startPcBangPointsTask();
		startPremiumAccountTask();
		getInventory().startTimers();
		resumeQuestTimers();
	}

	public void stopAllTimers()
	{
		setAgathion(0);
		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopKickTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		getInventory().stopAllTimers();
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();
		stopP2pAccountTask();
	}

	@Override
	public boolean isMyServitor(int objId)
	{
		return _servitor != null && _servitor.getObjectId() == objId;
	}

	public boolean hasServitor()
	{
		return _servitor != null;
	}

	@Override
	public Servitor getServitor()
	{
		return _servitor;
	}

	public void setPet(Servitor servitor)
	{
		boolean isPet = false;
		if(_servitor != null && _servitor.isPet())
			isPet = true;
		unsetVar("pet");
		_servitor = servitor;
		autoShot();
		if(servitor == null)
		{
			if(isPet)
			{
				if(isLogoutStarted())
					if(getPetControlItem() != null)
						setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1);
				setPetControlItem(null);
			}
			getEffectList().stopEffect(4140);
		}
	}

	public void scheduleDelete()
	{
		long time = 0L;

		if(Config.SERVICES_ENABLE_NO_CARRIER)
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);

		scheduleDelete(time * 1000L);
			}

	/**
	 * Удалит персонажа из мира через указанное время, если на момент истечения времени он не будет присоединен.
	 * <br><br>
	 * TODO: через минуту делать его неуязвимым.<br>
	 * TODO: сделать привязку времени к контексту, для зон с лимитом времени оставлять в игре на все время в зоне.<br>
	 * <br>
	 *
	 * @param time время в миллисекундах
	 */
	public void scheduleDelete(long time)
	{
		if(isLogoutStarted() || isInOfflineMode())
			return;

		broadcastCharInfo();
		
		
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				if(!isConnected())
				{
					prepareToLogout();
					deleteMe();
				}
			}
		}, time);
	}

	@Override
	protected void onDelete()
	{
		super.onDelete();

		// Убираем фэйк в точке наблюдения
		WorldRegion observerRegion = getObserverRegion();
		if(observerRegion != null)
			observerRegion.removeObject(this);

		//Send friendlists to friends that this player has logged off
		_friendList.notifyFriends(false);

		bookmarks.clear();

		_inventory.clear();
		_warehouse.clear();
		_servitor = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_autoAttributeItem = null;
		_lastNpc = HardReferences.emptyRef();
		_observerRegion = null;
	}

	public void setTradeList(List<TradeItem> list)
	{
		_tradeList = list;
	}

	public List<TradeItem> getTradeList()
	{
		return _tradeList;
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setSellStoreName(String name)
	{
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, List<TradeItem> list)
	{
		if (packageSell)
			_packageSellList = list;
		else
			_sellList = list;
	}

	public List<TradeItem> getSellList()
	{
		return getSellList(_privatestore == STORE_PRIVATE_SELL_PACKAGE);
	}

	public List<TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public void setBuyStoreName(String name)
	{
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public void setBuyList(List<TradeItem> list)
	{
		_buyList = list;
	}

	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}

	public void setManufactureName(String name)
	{
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public List<ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
		if(type != STORE_PRIVATE_NONE)
			setVar("storemode", String.valueOf(type), -1);
		else
			unsetVar("storemode");
	}

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE;
	}

	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	public L2GameServerPacket getPrivateStoreMsgPacket(Player forPlayer)
	{
		switch(getPrivateStoreType())
		{
			case STORE_PRIVATE_BUY:
				return new PrivateStoreBuyMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_SELL:
				return new PrivateStoreMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_SELL_PACKAGE:
				return new ExPrivateStoreWholeMsg(this, canTalkWith(forPlayer));
			case STORE_PRIVATE_MANUFACTURE:
				return new RecipeShopMsgPacket(this, canTalkWith(forPlayer));
		}

		return null;
	}

	public void broadcastPrivateStoreInfo()
	{
		if(!isVisible() || _privatestore == STORE_PRIVATE_NONE)
			return;

		broadcastPacket(getPrivateStoreMsgPacket(this));
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 *
	 * @param clan the clat to set
	 */
	public void setClan(Clan clan)
	{
		if(_clan != clan && _clan != null)
		{
			unsetVar("canWhWithdraw");
			long autoacademyReward = getVarLong("autoacademy_reward", 0);
			if(autoacademyReward > 0)
				_clan.getWarehouse().addItem(ItemTemplate.ITEM_ID_ADENA, autoacademyReward);
			unsetVar("autoacademy_reward");
		}

		Clan oldClan = _clan;
		if(oldClan != null && clan == null)
			for(Skill skill : oldClan.getAllSkills())
				removeSkill(skill, false);

		_clan = clan;

		if(clan == null)
		{
			_pledgeType = Clan.SUBUNIT_NONE;
			_pledgeClass = 0;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().validateItems();
			return;
		}

		if(!clan.isAnyMember(getObjectId()))
		{
			setClan(null);
			if(!isNoble())
				setTitle("");
		}
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Fortress getFortress()
	{
		int id = _clan != null ? _clan.getHasFortress() : 0;
		return ResidenceHolder.getInstance().getResidence(Fortress.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(_arrowItem != null && !Config.ALLOW_ARROW_INFINITELY)
		{
			if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
				_arrowItem = null;
			}
		}
	}

	private boolean _jailed = false;
	
	public boolean isJailed()
	{
		return _jailed;
	}
    public void setJailed(boolean jailed)
    {
    	_jailed = jailed;
    }	

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2Player then return True.
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null)
			{
				if(activeWeapon.getItemType() == WeaponType.BOW)
					_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
				else if(activeWeapon.getItemType() == WeaponType.CROSSBOW)
					_arrowItem = getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
			}

			// Equip arrows needed in left hand
			if(_arrowItem != null)
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
		}
		else
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		return _arrowItem != null;
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(final Party party)
	{
		if(party != null)
			party.addPartyMember(this);
	}

	public void leaveParty()
	{
		if(isInParty())
			_party.removePartyMember(this, false);
	}

	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isGM()
	{
		return _playerAccess != null && _playerAccess.IsGM;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
			_playerAccess = pa;
		else
			_playerAccess = new PlayerAccess();

		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	/**
	 * Update Stats of the L2Player client side by sending Server->Client packet UserInfo/StatusUpdate to this L2Player and CharInfo/StatusUpdate to all players around (broadcast).<BR><BR>
	 */
	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;

		refreshOverloaded();
		if(Config.EXPERTISE_PENALTY)
		{
			refreshExpertisePenalty();
		}
		super.updateStats();

		if(getServitor() != null)
			getServitor().updateStats();
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, true, StatusUpdatePacket.KARMA);
		if(flagChanged)
		{
			broadcastRelationChanged();
			if(_karma <= 0)
				startPvPFlag(null);			
		}	
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	public void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, (isOnline() && !isInOfflineMode()) || (isInOfflineMode() && Config.SHOW_OFFLINE_MODE_IN_ONLINE) ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000L);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void increaseKarma(final long add_karma)
	{
		boolean flagChanged = _karma == 0;
		long new_karma = _karma + add_karma;

		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;

		if(_karma == 0 && new_karma > 0)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, StatusUpdatePacket.PVP_FLAG);
			}

			_karma = (int) new_karma;
		}
		else
			_karma = (int) new_karma;

		updateKarma(flagChanged);
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void decreaseKarma(final int i)
	{
		boolean flagChanged = _karma > 0;
		_karma -= i;
		if(_karma <= 0)
		{
			_karma = 0;
			updateKarma(flagChanged);
		}
		else
			updateKarma(false);
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and	the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param accountName The name of the L2Player
	 * @param name		The name of the L2Player
	 * @param hairStyle   The hair style Identifier of the L2Player
	 * @param hairColor   The hair color Identifier of the L2Player
	 * @param face		The face type Identifier of the L2Player
	 * @return The L2Player added to the database or null
	 */
	public static Player create(int classId, int sex, String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		if(classId < 0 || classId >= ClassId.VALUES.length)
			return null;

		ClassId classID = ClassId.VALUES[classId];
		if(classID.isDummy() || !classID.isOfLevel(ClassLevel.NONE))
			return null;

		PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classID, Sex.VALUES[sex]);

		// Create a new L2Player with an account name
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		// Add the player in the characters table of the database
		if(!CharacterDAO.getInstance().insert(player))
			return null;

		int level = Config.STARTING_LVL;
		double hp = classID.getBaseHp(level);
		double mp = classID.getBaseMp(level);
		double cp = classID.getBaseCp(level);
		long exp = Experience.getExpForLevel(level);
		long sp = Config.STARTING_SP;
		boolean active = true;

		// Add the player subclass in the character_subclasses table of the database
		if(!CharacterSubclassDAO.getInstance().insert(player.getObjectId(), classId, exp, sp, hp, mp, cp, hp, mp, cp, level, active, true, 0, 0))
			return null;

		return player;
	}

	/**
	 * Retrieve a L2Player from the characters table of the database and add it in _allObjects of the L2World
	 *
	 * @return The L2Player loaded from the database
	 */
	public static Player restore(final int objectId, boolean fake)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		Statement statement2 = null;
		PreparedStatement statement3 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		ResultSet rset3 = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement2 = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `isBase`=1 LIMIT 1");

			if(rset.next() && rset2.next())
			{
				final ClassId classId = ClassId.VALUES[rset2.getInt("class_id")];
				final PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(classId, Sex.VALUES[rset.getInt("sex")]);

				if(fake)
				{
					FakePlayerAITemplate fakeAiTemplate = FakePlayersHolder.getInstance().getAITemplate(classId.getRace(), classId.getType());
					if(fakeAiTemplate == null)
						return null;

					player = new Player(fakeAiTemplate, objectId, template);
				}
				else
					player = new Player(objectId, template);

				if(!player.getSubClassList().restore())
				{
					_log.warn("Player:restore: Could not restore character due to a failure when restoring sub-classes!");
					return null;
				}

				player.restoreVariables();
				player.loadInstanceReuses();
				player.loadPremiumItemList();
				player.bookmarks.setCapacity(rset.getInt("bookmarks"));
				player.bookmarks.restore();

				player.getFriendList().restore();
				player.getBlockList().restore();

				player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
				CharacterGroupReuseDAO.getInstance().select(player);
				player._login = rset.getString("account_name");
				player.setName(rset.getString("char_name"));

				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setHeading(0);
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
					player.setLeaveClanTime(0);
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);
				player.setNoChannel(rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
					player.setNoChannel(0);
				String tmp_hwid = rset.getString("hwid_lock");
				if(tmp_hwid != null && tmp_hwid.isEmpty())
                    player.setHwidLock(null);	
				else
					player.setHwidLock(tmp_hwid);
				player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

				final int clanId = rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}
				player.setCreateTime(rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(rset.getInt("deletetime"));

				player.setTitle(rset.getString("title"));

				if(player.getVar("titlecolor") != null)
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")), true);

				if(player.getVar("namecolor") == null)
					if(player.isGM())
						player.setNameColor(Config.GM_NAME_COLOUR, true);
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR, true);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR, true);
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")), true);
				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player._autoLoot = player.getVarBoolean("AutoLoot", Config.AUTO_LOOT);
					player.AutoLootHerbs = player.getVarBoolean("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				}
				if(Config.ENABLE_CERTAIN_DROP_INVIDUAL)
				{
					player._certainDropEnabled = player.getVarBoolean("CertainDrop", true);
				}
				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(rset.getLong("lastAccess"));

				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player.setRecomBonusTime(rset.getInt("rec_bonus_time"));

				if(player.getVar("recLeftToday") != null)
					player.setRecomLeftToday(Integer.parseInt(player.getVar("recLeftToday")));
				else
					player.setRecomLeftToday(0);

				if(!Config.USE_CLIENT_LANG && Config.CAN_SELECT_LANGUAGE)
					player.setLanguage(player.getVar(Language.LANG_VAR));

				player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));
				player.setKeyBindings(rset.getBytes("key_bindings"));
				player.setPcBangPoints(rset.getInt("pcBangPoints"), false);

				player.setFame(rset.getInt("fame"), null);

				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
				{
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
					player.setNoble(Olympiad.isNoble(player.getObjectId()));
				}
				if(Config.ENABLE_CUSTOM_HEROES && !Hero.getInstance().isHero(player.getObjectId()))
					if(CHeroDao.isCustomHero(player.getObjectId()) && !CHeroDao.isExpiredFor(player.getObjectId()))
						player.setHero(true);					

				player.updatePledgeClass();

				int reflection = 0;

				int jailExpireTime = player.getVarInt("jailed");
				if((System.currentTimeMillis() / 1000) < (jailExpireTime + 60))
				{
					reflection = ReflectionManager.JAIL.getId();
					if(!player.isInZone("[gm_prison]"))
						player.setLoc(Location.findPointToStay(player, AdminFunctions.JAIL_SPAWN, 50, 200));
					player.startUnjailTask(player, jailExpireTime);
				}
				else
				{
					player.unsetVar("jailed");
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection != ReflectionManager.PARNASSUS.getId() && reflection != ReflectionManager.GIRAN_HARBOR.getId()) // не портаем назад из ГХ, парнаса
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}
				player.setReflection(reflection);

				EventHolder.getInstance().findEvent(player);

				//TODO [G1ta0] запускать на входе
				Quest.restoreQuestStates(player);

				player.getInventory().restore();

				player.setActiveSubClass(player.getActiveClassId(), false, true);

				player.isntAfk();

				// 4 очка в минуту оффлайна
				player.setVitality(rset.getInt("vitality") + (int) ((System.currentTimeMillis() / 1000L - rset.getLong("lastAccess")) / 15.));
				player.setBotRating(rset.getInt("bot_rating"));
				
				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_ANIMATION_OF_CAST_VAR);
					if(var != null)
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_TRADERS_VAR);
					if(var != null)
						player.setNotShowTraders(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("pet");
					if(var != null)
						player.setPetControlItem(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
				try
				{
					String var = player.getVar("isPvPevents");
					if(var != null)
						player.unsetVar("isPvPevents");
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while(rset3.next())
				{
					final Integer charId = rset3.getInt("obj_Id");
					final String charName = rset3.getString("char_name");
					player._chars.put(charId, charName);
				}

				DbUtils.close(statement3, rset3);

				//if(!player.isGM())
				{
					LazyArrayList<Zone> zones = LazyArrayList.newInstance();

					World.getZones(zones, player.getLoc(), player.getReflection());

					if(!zones.isEmpty())
						for(Zone zone : zones)
							if(zone.getType() == ZoneType.no_restart)
							{
								if(System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
								{
									player.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.EnterWorld.TeleportedReasonNoRestart", player));
									player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
								}
							}
							else if(zone.getType() == ZoneType.SIEGE)
							{
								SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
								if(siegeEvent != null)
									player.setLoc(siegeEvent.getEnterLoc(player));
								else
								{
									Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
									player.setLoc(r.getNotOwnerRestartPoint(player));
								}
							}

					LazyArrayList.recycle(zones);

					if(DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false))
						player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
				}

				player._macroses.restore();

				//FIXME [VISTALL] нужно ли?
				player.refreshExpertisePenalty();
				player.refreshOverloaded();

				player.getWarehouse().restore();
				player.getFreight().restore();

				player.restoreTradeList();
				if(player.getVar("storemode") != null)
				{
					player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
					player.setSitting(true);
				}

				try
				{
					String var = player.getVar("FightClubRate");
					if(var != null)
						RestoreFightClub(player);
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("EnItemOlyRec");
					if(Config.OLY_ENCH_LIMIT_ENABLE && var != null)
						FixEnchantOlympiad.restoreEnchantItemsOly(player);
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				player.updateKetraVarka();
				player.updateRam();
				player.checkRecom();
				if(player.isCursedWeaponEquipped())
					player.restoreCursedWeapon();
			}
		}
		catch(final Exception e)
		{
			_log.error("Could not restore char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}
	
	public void addValue(int value)
	{
		setVar("UsedSocial", getVarInt("UsedSocial", -1) + value, System.currentTimeMillis() + 60000);
		//if(getVarInt("UsedSocial", -1) == 512)
			//HardReferences.calcStats();
	}
	
	public boolean isInJail()
	{
		return getVar("jailed") != null;
	}

	private void loadPremiumItemList()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int itemNum = rs.getInt("itemNum");
				int itemId = rs.getInt("itemId");
				long itemCount = rs.getLong("itemCount");
				String itemSender = rs.getString("itemSender");
				PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
				_premiumItems.put(itemNum, item);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deletePremiumItem(int itemNum)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Map<Integer, PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		if(!_storeLock.tryLock())
			return;

		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
						"UPDATE characters SET face=?,hairStyle=?,hairColor=?,sex=?,x=?,y=?,z=?" + //
								",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,clanid=?,deletetime=?," + //
								"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
								"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,vitality=?,fame=?,bookmarks=?,bot_rating=?,hwid_lock=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				statement.setInt(4, getSex());
				if(_stablePoint == null) // если игрок находится в точке в которой его сохранять не стоит (например на виверне) то сохраняются последние координаты
				{
					statement.setInt(5, getX());
					statement.setInt(6, getY());
					statement.setInt(7, getZ());
				}
				else
				{
					statement.setInt(5, _stablePoint.x);
					statement.setInt(6, _stablePoint.y);
					statement.setInt(7, _stablePoint.z);
				}
				statement.setInt(8, getKarma());
				statement.setInt(9, getPvpKills());
				statement.setInt(10, getPkKills());
				statement.setInt(11, getRecomHave());
				if(getRecomLeft() > 255)
					setRecomLeft(255);
				statement.setInt(12, getRecomLeft());
				statement.setInt(13, getRecomBonusTime());
				statement.setInt(14, getNevitSystem().getPoints());
				statement.setInt(15, getNevitSystem().getTime());
				statement.setInt(16, getClanId());
				statement.setInt(17, getDeleteTimer());
				statement.setString(18, _title);
				statement.setInt(19, _accessLevel);
				statement.setInt(20, isOnline() && !isInOfflineMode() ? 1 : 0);
				statement.setLong(21, getLeaveClanTime() / 1000L);
				statement.setLong(22, getDeleteClanTime() / 1000L);
				statement.setLong(23, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(24, (int) (_onlineBeginTime > 0 ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000L : _onlineTime / 1000L));
				statement.setInt(25, getPledgeType());
				statement.setInt(26, getPowerGrade());
				statement.setInt(27, getLvlJoinedAcademy());
				statement.setInt(28, getApprentice());
				statement.setBytes(29, getKeyBindings());
				statement.setInt(30, getPcBangPoints());
				statement.setString(31, getName(false));
				statement.setInt(32, (int) getVitality());
				statement.setInt(33, getFame());
				statement.setInt(34, bookmarks.getCapacity());
				statement.setInt(35, getBotRating());
				statement.setString(36, getHwidLock());
				statement.setInt(37, getObjectId());

				statement.executeUpdate();
				GameStats.increaseUpdatePlayerBase();

				if(!fast)
				{
					EffectsDAO.getInstance().insert(this);
					CharacterGroupReuseDAO.getInstance().insert(this);
					storeDisableSkills();
				}

				storeCharSubClasses();
				bookmarks.store();
			}
			catch(Exception e)
			{
				_log.error("Could not store char data: " + this + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public Skill addSkill(final Skill newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;

		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		Skill oldSkill = super.addSkill(newSkill);

		if(newSkill.equals(oldSkill))
			return oldSkill;

		// Add or update a L2Player skill in the character_skills table of the database
		if(store)
			storeSkill(newSkill, oldSkill);

		return oldSkill;
	}

	public Skill removeSkill(Skill skill, boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 *
	 * @return The L2Skill removed
	 */
	public Skill removeSkill(int id, boolean fromDB)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		Skill oldSkill = super.removeSkillById(id);

		if(!fromDB)
			return oldSkill;

		if(oldSkill != null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				// Remove or update a L2Player skill from the character_skills table of the database
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(final Exception e)
			{
				_log.error("Could not delete skill!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		return oldSkill;
	}

	/**
	 * Add or update a L2Player skill in the character_skills table of the database.
	 */
	private void storeSkill(final Skill newSkill, final Skill oldSkill)
	{
		if(newSkill == null) // вообще-то невозможно
		{
			_log.warn("could not store new skill. its NULL");
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setInt(4, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("Error could not store skills!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skills of this L2Player and add them to _skills.
	 */
	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			// Send the SQL query : SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? to the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			boolean multiclassEnabled = Config.ALT_ENABLE_MULTI_PROFESSION || Config.TRANS_SUBCLASS_SKILL_TO_MAIN && !isSubClassActive();
			// Go though the recordset of this SQL query
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");

				// Create a L2Skill object for each record
				final Skill skill = SkillHolder.getInstance().getSkill(id, level);

				if(skill == null)
					continue;

				// Remove skill if not possible
				if(!isGM() && !SkillAcquireHolder.getInstance().isSkillPossible(this, null, skill, multiclassEnabled))
				{
					//int ReturnSP = SkillTreeTable.getInstance().getSkillCost(this, skill);
					//if(ReturnSP == Integer.MAX_VALUE || ReturnSP < 0)
					//		ReturnSP = 0;
					removeSkill(skill, true);
					if(Config.ALLOWED_REBORN_COUNT == 0)
						removeSkillFromShortCut(skill.getId());
					//if(ReturnSP > 0)
					//		setSp(getSp() + ReturnSP);
					//TODO audit
					continue;
				}

				super.addSkill(skill);
			}

			if(multiclassEnabled)
			{
				DbUtils.closeQuietly(statement, rset);

				statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index!=?");
				statement.setInt(1, getObjectId());
				statement.setInt(2, getActiveClassId());
				rset = statement.executeQuery();

				// Go though the recordset of this SQL query
				while(rset.next())
				{
					final int id = rset.getInt("skill_id");
					final int level = rset.getInt("skill_level");

					// Create a L2Skill object for each record
					final Skill skill = SkillHolder.getInstance().getSkill(id, level);

					if(skill == null)
						continue;

					// Remove skill if not possible
					if(!isGM() && !SkillAcquireHolder.getInstance().isSkillPossible(this, null, skill, true))
					{
						//int ReturnSP = SkillTreeTable.getInstance().getSkillCost(this, skill);
						//if(ReturnSP == Integer.MAX_VALUE || ReturnSP < 0)
						//		ReturnSP = 0;
						removeSkill(skill, true);
						if(Config.ALLOWED_REBORN_COUNT == 0)
							removeSkillFromShortCut(skill.getId());
						//if(ReturnSP > 0)
						//		setSp(getSp() + ReturnSP);
						//TODO audit
						continue;
					}

					int haveSkillLevel = getSkillLevel(skill.getId());
					// Не заменяем скилл более слабым.
					if(haveSkillLevel > skill.getLevel() || haveSkillLevel == skill.getLevel() && SkillUtils.getSkillEnchantLevel(haveSkillLevel) > SkillUtils.getSkillEnchantLevel(skill.getLevel()))
						continue;

					super.addSkill(skill);
				}
			}

			// Restore noble skills
			if(isNoble())
				updateNobleSkills();

			// Restore Hero skills at main class only
			if(_hero && getBaseClassId() == getActiveClassId())
				Hero.addSkills(this);

			// Restore clan skills
			if (_clan != null)
			{
				_clan.addSkillsQuietly(this);

				// Restore clan leader siege skills
				if(_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= 5)
					SiegeUtils.addSiegeSkills(this);
			}

			// Give dwarven craft skill
			if(getActiveClassId() >= 53 && getActiveClassId() <= 57 || getActiveClassId() == 117 || getActiveClassId() == 118)
				super.addSkill(SkillHolder.getInstance().getSkill(1321, 1));

			super.addSkill(SkillHolder.getInstance().getSkill(1322, 1));

			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
				super.addSkill(SkillHolder.getInstance().getSkill(2099, 1));

			if(isGM())
				giveGMSkills();
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore skills for player objId: " + getObjectId());
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			boolean multiclass = Config.ALT_ENABLE_MULTI_PROFESSION || Config.TRANS_SUBCLASS_SKILL_TO_MAIN && !isSubClassActive();

			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + (multiclass ? getBaseClassId() : getActiveClassId()) + " AND `end_time` < " + System.currentTimeMillis());

			if(_skillReuses.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized(_skillReuses)
			{
				StringBuilder sb;
				for(TimeStamp timeStamp : _skillReuses.valueCollection())
				{
					if(timeStamp.hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(multiclass ? getBaseClassId() : getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			boolean multiclass = Config.ALT_ENABLE_MULTI_PROFESSION || Config.TRANS_SUBCLASS_SKILL_TO_MAIN && !isSubClassActive();

			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + (multiclass ? getBaseClassId() : getActiveClassId()));
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();

				Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
					_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, endTime, rDelayOrg));
			}
			DbUtils.close(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + (multiclass ? getBaseClassId() : getActiveClassId()) + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.error("Could not restore active skills data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Retrieve from the database all Henna of this L2Player, add them to _henna and calculate stats of the L2Player.<BR><BR>
	 */
	private void restoreHenna()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			for(int i = 0; i < 3; i++)
				_henna[i] = null;

			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot < 1 || slot > 3)
					continue;

				final int symbol_id = rset.getInt("symbol_id");

				if(symbol_id != 0)
				{
					final Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
					if(tpl != null)
					{
						_henna[slot - 1] = tpl;
					}
				}
			}
		}
		catch(final Exception e)
		{
			_log.warn("could not restore henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

	}

	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassLevel();
		for(int i = 0; i < 3; i++)
			if(_henna[i] != null)
				totalSlots--;

		if(totalSlots <= 0)
			return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;

		slot--;

		if(_henna[slot] == null)
			return false;

		final Henna henna = _henna[slot];
		final int dyeID = henna.getDyeId();

		_henna[slot] = null;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("could not remove char henna: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2Player
		sendPacket(new HennaInfoPacket(this));
		// Send Server->Client UserInfo packet to this L2Player
		sendUserInfo(true);

		// Add the recovered dyes to the player's inventory and notify them.
		ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2, true, "Henna cashback on remove");

		return true;
	}

	/**
	 * Add a Henna to the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 *
	 * @param henna L2Henna РґР»СЏ РґРѕР±Р°РІР»РµРЅРёСЏ
	 */
	public boolean addHenna(Henna henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}

		// int slot = 0;
		for(int i = 0; i < 3; i++)
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2Player
				recalcHennaStats();

				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getActiveClassId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warn("could not save char henna: " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}

				sendPacket(new HennaInfoPacket(this));
				sendUserInfo(true);

				return true;
			}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for(int i = 0; i < 3; i++)
		{
			Henna henna = _henna[i];
			if(henna == null)
				continue;
			if(!henna.isForThisClass(this))
				continue;

			_hennaINT += henna.getStatINT();
			_hennaSTR += henna.getStatSTR();
			_hennaMEN += henna.getStatMEN();
			_hennaCON += henna.getStatCON();
			_hennaWIT += henna.getStatWIT();
			_hennaDEX += henna.getStatDEX();
		}

		if(_hennaINT > 5)
			_hennaINT = 5;
		if(_hennaSTR > 5)
			_hennaSTR = 5;
		if(_hennaMEN > 5)
			_hennaMEN = 5;
		if(_hennaCON > 5)
			_hennaCON = 5;
		if(_hennaWIT > 5)
			_hennaWIT = 5;
		if(_hennaDEX > 5)
			_hennaDEX = 5;
	}

	/**
	 * @param slot id слота у перса
	 * @return the Henna of this L2Player corresponding to the selected slot.<BR><BR>
	 */
	public Henna getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
	}

	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		if(getInventory().destroyItemByItemId(itemConsumeId, itemCount))
		{
			sendPacket(SystemMessagePacket.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	@Override
	public boolean haveItemForConsume(int itemConsumeId, long itemCount)
	{
		ItemInstance item = getInventory().getItemByItemId(itemConsumeId);
		return item != null && item.getCount() >= itemCount;
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp)
	{
		for(ItemInstance item : getInventory().getPaperdollItems())
			if(item != null && item.getItemId() == itemId)
			{
				final int newMp = item.getLifeTime() - mp;
				if(newMp >= 0)
				{
					item.setLifeTime(newMp);
					sendPacket(new InventoryUpdatePacket().addModifiedItem(this, item));
					return true;
				}
				break;
			}
		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR><BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final void setRiding(boolean mode)
	{
		_riding = mode;
	}

	/**
	 * Проверяет, можно ли приземлиться в этой зоне.
	 *
	 * @return можно ли приземлится
	 */
	public boolean checkLandingState()
	{
		if(isInZone(ZoneType.no_landing))
			return false;

		SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			Residence unit = siege.getResidence();
			if(unit != null && getClan() != null && isClanLeader() && (getClan().getCastle() == unit.getId() || getClan().getHasFortress() == unit.getId()))
				return true;
			return false;
		}

		return true;
	}

	public void setMount(int npcId, int obj_id, int level)
	{
		if(isCursedWeaponEquipped())
			return;

		switch(npcId)
		{
			case 0: // Dismount
				setFlying(false);
				setRiding(false);
				if(getTransformation() > 0)
					setTransformation(0);
				removeSkillById(Skill.SKILL_STRIDER_ASSAULT);
				removeSkillById(Skill.SKILL_WYVERN_BREATH);
				getEffectList().stopEffect(Skill.SKILL_HINDER_STRIDER);
				break;
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				setRiding(true);
				if(isNoble())
					addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_STRIDER_ASSAULT, 1), false);
				break;
			case PetDataTable.WYVERN_ID:
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_WYVERN_BREATH, 1), false);
				break;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				setRiding(true);
				break;
		}

		if(npcId > 0)
			unEquipWeapon();

		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;

		broadcastUserInfo(true); // нужно послать пакет перед Ride для корректного снятия оружия с заточкой
		broadcastPacket(new RidePacket(this));
		broadcastUserInfo(true); // нужно послать пакет после Ride для корректного отображения скорости

		sendSkillList();
	}

	public void unEquipWeapon()
	{
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		abortAttack(true, true);
		abortCast(true, true);
	}

	/*
	@Override
	public double getMovementSpeedMultiplier()
	{
		int template_speed = _template.baseRunSpd;
		if(isMounted())
		{
			L2PetData petData = PetDataTable.getInstance().getSkill(_mountNpcId, _mountLevel);
			if(petData != null)
				template_speed = petData.getSpeed();
		}
		return getRunSpeed() * 1f / template_speed;
	}
	 */

	@Override
	public int getSpeed(double baseSpeed)
	{
		if(isMounted())
		{
			PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if(petData != null)
				speed = petData.getSpeed();
			double mod = 1.;
			int level = getLevel();
			if(_mountLevel > level && level - _mountLevel > 10)
				mod = 0.5; // Штраф на разницу уровней между игроком и петом
			baseSpeed = (int) (mod * speed);
			if(isRunning())
				baseSpeed += getTemplate().getBaseRideRunSpd();
			else
				baseSpeed += getTemplate().getBaseRideWalkSpd();
		}
		return super.getSpeed(baseSpeed);
	}

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;

	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	public int getMountObjId()
	{
		return _mountObjId;
	}

	public int getMountLevel()
	{
		return _mountLevel;
	}

	public void sendDisarmMessage(ItemInstance wpn)
	{
		if(wpn.getEnchantLevel() > 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DISARMED);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	/**
	 * Устанавливает тип используемого склада.
	 *
	 * @param type тип склада:<BR>
	 *             <ul>
	 *             <li>WarehouseType.PRIVATE
	 *             <li>WarehouseType.CLAN
	 *             <li>WarehouseType.CASTLE
	 *             </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ С‚РёРї РёСЃРїРѕР»СЊР·СѓРµРјРѕРіРѕ СЃРєР»Р°РґР°.
	 *
	 * @return null РёР»Рё С‚РёРї СЃРєР»Р°РґР°:<br>
	 *         <ul>
	 *         <li>WarehouseType.PRIVATE
	 *         <li>WarehouseType.CLAN
	 *         <li>WarehouseType.CASTLE
	 *         </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.<EffectCubic>emptyList() : _cubics.values();
	}

	public void addCubic(EffectCubic cubic)
	{
		if(_cubics == null)
			_cubics = new ConcurrentHashMap<Integer, EffectCubic>(3);
		_cubics.put(cubic.getId(), cubic);
		sendPacket(new ExUserInfoCubic(this));
	}

	public void removeCubic(int id)
	{
		if(_cubics != null)
			_cubics.remove(id);
		sendPacket(new ExUserInfoCubic(this));
	}

	public EffectCubic getCubic(int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[" + getObjectId() + "]";
	}

	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	public int getVariation1Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation1Id();
	}

	public int getVariation2Id()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		if(wpn == null)
			return 0;

		return wpn.getVariation2Id();
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
			_lastNpc = HardReferences.emptyRef();
		else
			_lastNpc = npc.getRef();
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	/**
	 * @return True if L2Player is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return getReflection() instanceof DarknessFestival;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
		else
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(Integer shotId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoSoulShot(shotId);
				continue;
			}
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null)
				continue;
			handler.useItem(this, item, false);
		}
	}

	public boolean getChargedFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT;
	}

	@Override
	public int getChargedSpiritShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return 0;
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if(observer != null)
		{
			if(getObjectId() == observer.getObjectId())
				return false;
			if(isMyServitor(observer.getObjectId()))
				return false;
			if(observer.isPlayer())
			{
				// TODO: Проверить на оффе.
				Player observPlayer = (Player) observer;
				if(isInSameParty(observPlayer))
					return false;
				if(observPlayer.getPlayerAccess().CanSeeInHide)
					return false;
			}
		}
		return super.isInvisible(observer) || isGMInvisible();
	}

	@Override
	public boolean startInvisible(Object owner, boolean withServitors)
	{
		if(super.startInvisible(owner, withServitors))
		{
			sendUserInfo(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean stopInvisible(Object owner, boolean withServitors)
	{
		if(super.stopInvisible(owner, withServitors))
		{
			sendUserInfo(true);
			return true;
		}
		return false;
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
	}

	public void teleToCastle()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
	}

	public void teleToFortress()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_FORTRESS), ReflectionManager.DEFAULT);
	}

	public void teleToClanhall()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendMessage(message.toString());
	}

	@Override
	public boolean onTeleported()
	{
		if(!super.onTeleported())
			return false;
			
		if(isFakeDeath())
			breakFakeDeath();

		if(isInBoat())
			setLoc(getBoat().getLoc());

		// 15 секунд после телепорта на персонажа не агрятся мобы	
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);

		spawnMe();

		if(isPendingRevive())
			doRevive();

		DuelEvent duel = getEvent(DuelEvent.class);
		if(duel != null)
			duel.abortDuel(this);

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

		if(isLockedTarget() && getTarget() != null)
			sendPacket(new MyTargetSelectedPacket(this, getTarget()));

		sendUserInfo(true);
		if(getServitor() != null)
			getServitor().teleportToOwner();

		for(ListenerHook hook : getListenerHooks(ListenerHookType.PLAYER_TELEPORT))
			hook.onPlayerTeleport(this, getReflectionId());

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_TELEPORT))
			hook.onPlayerTeleport(this, getReflectionId());

		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;
		if (!_observerMode.compareAndSet(OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(observerRegion);

		// Отображаем надпись над головой
		broadcastCharInfo();

		// Переходим в режим обсервинга
		sendPacket(new ObserverStartPacket(loc));

		return true;
	}

	public void appearObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTING, OBSERVER_STARTED))
			return;
		
		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Добавляем фэйк в точку наблюдения
		if(!observerRegion.equals(currentRegion))
			observerRegion.addObject(this);

		World.showObjectsToPlayer(this);

		OlympiadGame game = getOlympiadObserveGame();
		if(game != null)
		{
			game.addSpectator(this);
			game.broadcastInfo(null, this, true);
		}
	}

	public void leaveObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Убираем фэйк в точке наблюдения
		if(!observerRegion.equals(currentRegion))
			observerRegion.removeObject(this);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// Выходим из режима обсервинга
		sendPacket(new ObserverEndPacket(getLoc()));
	}

	public void returnFromObserverMode()
	{
		if (!_observerMode.compareAndSet(OBSERVER_LEAVING, OBSERVER_NONE))
		{
			_log.warn("Return from observ mode " + getName() + ". ObservMode ID: " + _observerMode.get());
			Thread.dumpStack();
			return;
		}

		unblock();
		standUp();
		setFlying(false);

		broadcastCharInfo();

		World.showObjectsToPlayer(this);
	}

	public void enterOlympiadObserverMode(Location loc, OlympiadGame game, Reflection reflect)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion oldObserver = getObserverRegion(); //Добавлено		
		if(observerRegion == null)
			return;

		OlympiadGame oldGame = getOlympiadObserveGame();
		if (!_observerMode.compareAndSet(oldGame != null ? OBSERVER_STARTED : OBSERVER_NONE, OBSERVER_STARTING))
		{
			//_log.warn("Not starting observ mode " + getName() + ". ObservMode ID: " + _observerMode.get());
			//Thread.dumpStack();
			return;
		}

		if(!isHFClient())
			sendPacket(new TeleportToLocationPacket(this, loc));

		setTarget(null);
		stopMove();

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);
		WorldRegion oldObserverRegion = getObserverRegion();
		if(oldObserverRegion != null)
		{
			oldObserverRegion.removeObject(this);
		}
		setObserverRegion(observerRegion);

		if(oldGame != null)
		{
			oldGame.removeSpectator(this);
			sendPacket(ExOlympiadMatchEndPacket.STATIC);
		}
		else
		{
			block();

			// Отображаем надпись над головой
			broadcastCharInfo();

			// Меняем интерфейс
			sendPacket(new ExOlympiadModePacket(3));
		}

		setOlympiadObserveGame(game);

		// "Телепортируемся"
		setReflection(reflect);

		if(isHFClient())
			sendPacket(new TeleportToLocationPacket(this, loc));
		else
			sendPacket(new ExTeleportToLocationActivate(this, loc));
	}

	public void leaveOlympiadObserverMode(boolean removeFromGame)
	{
		OlympiadGame game = getOlympiadObserveGame();
		if (game == null)
			return;
		if (!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
		{
			_log.warn("Leave observ mode " + getName() + ". ObservMode ID: " + _observerMode.get());
			//Thread.dumpStack();
			return;
		}

		if(!isHFClient())
			sendPacket(new TeleportToLocationPacket(this, getLoc()));

		if (removeFromGame)
			game.removeSpectator(this);
		setOlympiadObserveGame(null);

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Убираем фэйк в точке наблюдения
		if(observerRegion != null && currentRegion != null && !observerRegion.equals(currentRegion))
			observerRegion.removeObject(this);

		// Очищаем все видимые обьекты
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// Меняем интерфейс
		sendPacket(new ExOlympiadModePacket(0));
		sendPacket(ExOlympiadMatchEndPacket.STATIC);

		setReflection(ReflectionManager.DEFAULT);

		// "Телепортируемся"
		if(isHFClient())
			sendPacket(new TeleportToLocationPacket(this, getLoc()));
		else
			sendPacket(new ExTeleportToLocationActivate(this, getLoc()));
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	@Override
	public boolean isInObserverMode()
	{
		return _observerMode.get() > 0;
	}

	public int getObserverMode()
	{
		return _observerMode.get();
	}

	public WorldRegion getObserverRegion()
	{
		return _observerRegion;
	}

	public void setObserverRegion(WorldRegion region)
	{
		_observerRegion = region;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 2;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			if(isClanLeader() && getClan().getCastle() > 0)
				super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_WYVERN_AEGIS, 1));
			super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_NOBLESSE_BLESSING, 1));
			super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_SUMMON_CP_POTION, 1));
			super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_FORTUNE_OF_NOBLESSE, 1));
			super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_HARMONY_OF_NOBLESSE, 1));
			super.addSkill(SkillHolder.getInstance().getSkill(Skill.SKILL_SYMPHONY_OF_NOBLESSE, 1));
		}
		else
		{
			super.removeSkillById(Skill.SKILL_WYVERN_AEGIS);
			super.removeSkillById(Skill.SKILL_NOBLESSE_BLESSING);
			super.removeSkillById(Skill.SKILL_SUMMON_CP_POTION);
			super.removeSkillById(Skill.SKILL_FORTUNE_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_HARMONY_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_SYMPHONY_OF_NOBLESSE);
		}
	}

	public void setNoble(boolean noble)
	{
		if(noble) //broadcast skill animation: Presentation - Attain Noblesse
			broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));
		_noble = noble;
	}

	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0)
			_ketra = 5;
		else if(ItemFunctions.getItemCount(this, 7214) > 0)
			_ketra = 4;
		else if(ItemFunctions.getItemCount(this, 7213) > 0)
			_ketra = 3;
		else if(ItemFunctions.getItemCount(this, 7212) > 0)
			_ketra = 2;
		else if(ItemFunctions.getItemCount(this, 7211) > 0)
			_ketra = 1;
		else if(ItemFunctions.getItemCount(this, 7225) > 0)
			_varka = 5;
		else if(ItemFunctions.getItemCount(this, 7224) > 0)
			_varka = 4;
		else if(ItemFunctions.getItemCount(this, 7223) > 0)
			_varka = 3;
		else if(ItemFunctions.getItemCount(this, 7222) > 0)
			_varka = 2;
		else if(ItemFunctions.getItemCount(this, 7221) > 0)
			_varka = 1;
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(ItemFunctions.getItemCount(this, 7247) > 0)
			_ram = 2;
		else if(ItemFunctions.getItemCount(this, 7246) > 0)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void updatePledgeClass()
	{
		int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
		boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
		boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);

		boolean IS_GUARD_CAPTAIN = false, IS_KNIGHT_COMMANDER = false, IS_LEADER = false;

		SubUnit unit = getSubUnit();
		if(unit != null)
		{
			UnitMember unitMember = unit.getUnitMember(getObjectId());
			if(unitMember == null)
			{
				_log.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
				return;
			}
			IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.isLeaderOf());
			IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.isLeaderOf());
			IS_LEADER = unitMember.isLeaderOf() == Clan.SUBUNIT_MAIN_CLAN;
		}

		switch(CLAN_LEVEL)
		{
			case -1:
				_pledgeClass = RANK_VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				if(IS_LEADER)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_VASSAL;
				break;
			case 4:
				if(IS_LEADER)
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 5:
				if(IS_LEADER)
					_pledgeClass = RANK_WISEMAN;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 6:
				if(IS_LEADER)
					_pledgeClass = RANK_BARON;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_WISEMAN;
				else if(IS_GUARD)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_KNIGHT;
				break;
			case 7:
				if(IS_LEADER)
					_pledgeClass = RANK_COUNT;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_GUARD)
					_pledgeClass = RANK_KNIGHT;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_BARON;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_WISEMAN;
				break;
			case 8:
				if(IS_LEADER)
					_pledgeClass = RANK_MARQUIS;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_COUNT;
				else if(IS_GUARD)
					_pledgeClass = RANK_WISEMAN;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_BARON;
				break;
			case 9:
				if(IS_LEADER)
					_pledgeClass = RANK_DUKE;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_MARQUIS;
				else if(IS_GUARD)
					_pledgeClass = RANK_BARON;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_COUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_WISEMAN;
				else
					_pledgeClass = RANK_VISCOUNT;
				break;
			case 10:
				if(IS_LEADER)
					_pledgeClass = RANK_GRAND_DUKE;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_BARON;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_MARQUIS;
				else
					_pledgeClass = RANK_COUNT;
				break;
			case 11:
				if(IS_LEADER)
					_pledgeClass = RANK_DISTINGUISHED_KING;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD)
					_pledgeClass = RANK_COUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_GRAND_DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_DUKE;
				else
					_pledgeClass = RANK_MARQUIS;
				break;
		}

		if(_hero && _pledgeClass < RANK_MARQUIS)
			_pledgeClass = RANK_MARQUIS;
		else if(_noble && _pledgeClass < RANK_BARON)
			_pledgeClass = RANK_BARON;
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	public int getNameColor()
	{
		if(isInObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor, boolean store)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR && store)
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor), -1);
		else
			unsetVar("namecolor");
	}

	private void restoreVariables()
	{
		List<CharacterVariable> variables = CharacterVariablesDAO.getInstance().restore(getObjectId());
		for(CharacterVariable var : variables)
			_variables.put(var.getName(), var);
	}

	public Collection<CharacterVariable> getVariables()
	{
		return _variables.values();
	}

	public boolean setVar(String name, Object value)
	{
		return setVar(name, value, -1);
	}

	public boolean setVar(String name, Object value, long expirationTime)
	{
		CharacterVariable var = new CharacterVariable(name, String.valueOf(value), expirationTime);
		if(CharacterVariablesDAO.getInstance().insert(getObjectId(), var))
		{
			_variables.put(name, var);
			return true;
		}
		return false;
	}

	public boolean unsetVar(String name)
	{
		if(name == null || name.isEmpty())
			return false;

		if(_variables.containsKey(name) && CharacterVariablesDAO.getInstance().delete(getObjectId(), name))
			return _variables.remove(name) != null;

		return false;
	}

	public String getVar(String name)
	{
		return getVar(name, null);
	}

	public String getVar(String name, String defaultValue)
	{
		CharacterVariable var = _variables.get(name);
		if(var != null && !var.isExpired())
			return var.getValue();

		return defaultValue;
	}

	public long getVarExpireTime(String name)
	{
		CharacterVariable var = _variables.get(name);
		if(var != null)
			return var.getExpireTime();
		return 0;
	}

	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Integer.parseInt(var);

		return defaultValue;
	}

	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(String name, long defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Long.parseLong(var);

		return defaultValue;
	}

	public double getVarDouble(String name)
	{
		return getVarDouble(name, 0.);
	}

	public double getVarDouble(String name, double defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return Double.parseDouble(var);

		return defaultValue;
	}

	public boolean getVarBoolean(String name)
	{
		return getVarBoolean(name, false);
	}

	public boolean getVarBoolean(String name, boolean defaultValue)
	{
		String var = getVar(name);
		if(var != null)
			return !(var.equals("0") || var.equalsIgnoreCase("false"));

		return defaultValue;
	}

	public void setLanguage(String val)
	{
		_language = Language.getLanguage(val);
		setVar(Language.LANG_VAR, _language.getShortName(), -1);
	}

	public Language getLanguage()
	{
		if(Config.USE_CLIENT_LANG && getNetConnection() != null)
			return getNetConnection().getLanguage();
		if(Config.CAN_SELECT_LANGUAGE)
			return _language;
		return Config.DEFAULT_LANG;
	}

	public boolean isLangRus()
	{
		return getLanguage() == Language.RUSSIAN;
	}

	public boolean isAtWarWith(final Integer id)
	{
		return _clan != null && _clan.isAtWarWith(id);
	}

	public boolean isAtWar()
	{
		return _clan == null || _clan.isAtWarOrUnderAttack();
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.CYAN, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (calcStat(Stats.BREATH, getTemplate().getBaseBreathBonus(), null, null) * 1000L);
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.CYAN, timeinwater));
			if(getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped())
				setTransformation(0);
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setAgathionRes(false);
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();

		_resurrectionBuffBlockedTime = (System.currentTimeMillis() + 10000L);
	}

	public void reviveRequest(Player reviver, double percent, boolean pet)
	{
		ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener)_askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}

		if(pet && getServitor() != null && getServitor().isDead() || !pet && isDead())
		{
			ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addString(Math.round(percent) + " percent");

			ask(pkt, new ReviveAnswerListener(this, percent, pet));
		}
	}
	
	public void requestCheckBot()
	{
		BotCheckQuestion question = BotCheckManager.generateRandomQuestion();
		int qId = question.getId();
		String qDescr = question.getDescr(isLangRus());
		
		ConfirmDlgPacket pkt = new ConfirmDlgPacket(SystemMsg.S1, Config.ASK_ANSWER_DELAY * 60000).addString(qDescr);
		//ConfirmDlgPacket pkt = new ConfirmDlgPacket(qDescr, 60000);
		ask(pkt, new BotCheckAnswerListner(this, qId));
	}
	
	public void increaseBotRating()
	{
		int bot_points = getBotRating();
		if(bot_points + 1 >= Config.MAX_BOT_POINTS)
			return;
		setBotRating(bot_points + 1);	
	}
	
	public void decreaseBotRating()
	{
		int bot_points = getBotRating();
		if(bot_points - 1 <= Config.MINIMAL_BOT_RATING_TO_BAN)
		{
			toJail(Config.AUTO_BOT_BAN_JAIL_TIME);
			sendMessage("You moved to jail, time to escape - " + Config.AUTO_BOT_BAN_JAIL_TIME + " minutes, reason - botting .");
			if(Config.ANNOUNCE_AUTO_BOT_BAN)
				Announcements.getInstance().announceToAll("Player " + getName() + " jailed for botting!");
		}
		else
		{
			setBotRating(bot_points - 1);
			if(Config.ON_WRONG_QUESTION_KICK)
				kick();
		}	
	}
	
	public void setBotRating(int rating)
	{
		_botRating = rating;
	}
	
	public int getBotRating()
	{
		return _botRating;
	}

	public void toJail(int time)
	{
		setVar("jailed", time, -1);

		if(getReflection() == ReflectionManager.DEFAULT)
			setVar("backCoords", getLoc().toXYZString(), -1);

		startUnjailTask(this, time);
		teleToLocation(Location.findPointToStay(this, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);

		if(isInStoreMode())
			setPrivateStoreType(Player.STORE_PRIVATE_NONE);
	}

	public void fromJail()
	{
		String back = getVar("backCoords");
		if(back != null)
		{
			teleToLocation(Location.parseLoc(back), ReflectionManager.DEFAULT);
			unsetVar("backCoords");
		}
		stopUnjailTask();
		unsetVar("jailed");
	}

	public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal)
	{
		ConfirmDlgPacket cd = new ConfirmDlgPacket(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
		cd.addName(summoner).addZoneName(loc);

		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public void scriptRequest(String text, String scriptName, Object[] args)
	{
		ask(new ConfirmDlgPacket(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args));
	}

	public void updateNoChannel(final long time)
	{
		setNoChannel(time);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		sendPacket(new EtcStatusUpdatePacket(this));
	}

	public boolean canTalkWith(Player player)
	{
		return _NoChannel >= 0 || player == this;
	}

	private void checkRecom()
	{
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.HOUR_OF_DAY, 6);
		temp.set(Calendar.MINUTE, 30);
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		long count = Math.round((System.currentTimeMillis() / 1000 - _lastAccess) / 86400);
		if(count == 0 && _lastAccess < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis())
			count++;

		for(int i = 1; i < count; i++)
			setRecomHave(getRecomHave() - 20);

		if(count > 0)
			restartRecom();
	}

	public void restartRecom()
	{
		setRecomBonusTime(3600);
		setRecomLeftToday(0);
		setRecomLeft(20);
		setRecomHave(getRecomHave() - 20);
		stopRecomBonusTask(false);
		startRecomBonusTask();
		sendUserInfo(true);
		sendVoteSystemInfo();
	}

	@Override
	public boolean isInBoat()
	{
		return _boat != null;
	}

	public Boat getBoat()
	{
		return _boat;
	}

	public void setBoat(Boat boat)
	{
		_boat = boat;
	}

	public Location getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc)
	{
		_inBoatPosition = loc;
	}

	public SubClassList getSubClassList()
	{
		return _subClassList;
	}

	public SubClass getBaseSubClass()
	{
		return _subClassList.getBaseSubClass();
	}

	public int getBaseClassId()
	{
		if(getBaseSubClass() != null)
			return getBaseSubClass().getClassId();

		return -1;
	}

	public SubClass getActiveSubClass()
	{
		return _subClassList.getActiveSubClass();
	}

	public int getActiveClassId()
	{
		return getActiveSubClass().getClassId();
	}

	public boolean isBaseClassActive()
	{
		return getActiveSubClass().isBase();
	}

	public ClassId getClassId()
	{
		return ClassId.VALUES[getActiveClassId()];
	}

	public int getMaxLevel()
	{
		if(getActiveSubClass() != null)
			return getActiveSubClass().getMaxLevel();

		return Experience.getMaxLevel();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished professional quests
	 *
	 * @param oldclass
	 * @param newclass
	 */
	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(final SQLException e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Сохраняет информацию о классах в БД
	 */
	public void storeCharSubClasses()
	{
		SubClass main = getActiveSubClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
		}
		else
			_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);

		CharacterSubclassDAO.getInstance().store(this);
	}

	/**
	 * Добавить класс, используется только для сабклассов
	 *
	 * @param storeOld
	 * @param certification
	 */
	public boolean addSubClass(final int classId, boolean storeOld, int certification)
	{
		return addSubClass(-1, classId, storeOld, certification);
	}

	public boolean addSubClass(final int oldClassId, final int classId, boolean storeOld, int certification)
	{
		if(getSubClassList().size() >= getMaxSubClassCount())
			return false;

		final ClassId newId = ClassId.VALUES[classId];
		if(newId.isDummy() || newId.isOfLevel(ClassLevel.NONE) || newId.isOfLevel(ClassLevel.FIRST))
			return false;

		final SubClass newClass = new SubClass(this);
		newClass.setIsBase(false);
		newClass.setClassId(classId);
		newClass.setDeathPenalty(0);
		newClass.setCertification(certification);
		if(!getSubClassList().add(newClass))
			return false;

		final int level = newClass.getLevel();
		final double hp = newId.getBaseHp(level);
		final double mp = newId.getBaseMp(level);
		final double cp = newId.getBaseCp(level);
		if(!CharacterSubclassDAO.getInstance().insert(getObjectId(), newClass.getClassId(), newClass.getExp(), newClass.getSp(), hp, mp, cp, hp, mp, cp, level, false, false, 0, certification))
			return false;

		setActiveSubClass(classId, storeOld, false);

		boolean countUnlearnable = true;
		int unLearnable = 0;

		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			for(final SkillLearn s : skills)
			{
				final Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(newId))
				{
					if(countUnlearnable)
						unLearnable++;
					continue;
				}
				addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		}

		sendSkillList();
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());

		final ClassId oldId = oldClassId >= 0 ? ClassId.VALUES[oldClassId] : null;
		onReceiveNewClassId(oldId, newId);

		return true;
	}

	/**
	 * Удаляет всю информацию о классе и добавляет новую, только для сабклассов
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final SubClass originalClass = getSubClassList().getByClassId(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;

		final int certification = originalClass.getCertification();

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.warn("Could not delete char sub-class: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		getSubClassList().removeByClassId(oldClassId);

		return newClassId <= 0 || addSubClass(oldClassId, newClassId, false, certification);
	}

	/**
	 * РЈСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ Р°РєС‚РёРІРЅС‹Р№ СЃР°Р±РєР»Р°СЃСЃ
	 * <p/>
	 * <li>Retrieve from the database all skills of this L2Player and add them to _skills </li>
	 * <li>Retrieve from the database all macroses of this L2Player and add them to _macroses</li>
	 * <li>Retrieve from the database all shortCuts of this L2Player and add them to _shortCuts</li><BR><BR>
	 */
	public boolean setActiveSubClass(final int subId, final boolean store, final boolean onRestore)
	{
		abortAttack(true, false);
		abortCast(true, false);

		if(!onRestore)
		{
			SubClass oldActiveSub = getActiveSubClass();
			if(oldActiveSub != null)
			{
				EffectsDAO.getInstance().insert(this);
				storeDisableSkills();

				QuestState qs = getQuestState(422);
				if(qs != null)
					qs.abortQuest();

				if(store)
				{
					oldActiveSub.setCp(getCurrentCp());
					oldActiveSub.setHp(getCurrentHp());
					oldActiveSub.setMp(getCurrentMp());
				}
			}
		}

		SubClass newActiveSub = _subClassList.changeActiveSubClass(subId);
		if(newActiveSub == null)
			return false;

		setClassId(subId, false, false);

		removeAllSkills();

		getEffectList().stopAllEffects();

		if(getServitor() != null && (getServitor().isSummon() || Config.ALT_IMPROVED_PETS_LIMITED_USE && (getServitor().getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID && !isMageClass() || getServitor().getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID && isMageClass())))
			getServitor().unSummon();

		setAgathion(0);

		restoreSkills();
		rewardSkills(false);
		checkSkills();
		sendPacket(new ExStorageMaxCountPacket(this));

		refreshExpertisePenalty();

		sendSkillList();

		getInventory().refreshEquip();
		getInventory().validateItems();

		for(int i = 0; i < 3; i++)
			_henna[i] = null;

		restoreHenna();
		sendPacket(new HennaInfoPacket(this));

		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();

		setCurrentHpMp(newActiveSub.getHp(), newActiveSub.getMp());
		setCurrentCp(newActiveSub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInitPacket(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		sendPacket(new SkillCoolTimePacket(this));

		broadcastPacket(new SocialActionPacket(getObjectId(), SocialActionPacket.LEVEL_UP));

		getDeathPenalty().restore(this);

		setIncreasedForce(0);

		startHourlyTask();

		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
		return true;
	}

	/**
	 * Через delay миллисекунд выбросит игрока из игры
	 */
	public void startKickTask(long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public boolean givePremiumAccount(PremiumAccountTemplate premiumAccount, int delay)
	{
		if(getNetConnection() == null)
			return false;

		int type = premiumAccount.getType();
		if(type == 0)
			return false;

		int expireTime = (delay > 0) ? (int) ((delay * 60 * 60) + (System.currentTimeMillis() / 1000)) : Integer.MAX_VALUE;

		boolean extended = false;

		int oldAccountType = getNetConnection().getPremiumAccountType();
		int oldAccountExpire = getNetConnection().getPremiumAccountExpire();
		if(oldAccountType == type && oldAccountExpire > (System.currentTimeMillis() / 1000))
		{
			expireTime += (int) (oldAccountExpire - (System.currentTimeMillis() / 1000));
			extended = true;
		}

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
		{
			if(!PremiumAccountDAO.getInstance().insert(getAccountName(), type, expireTime))
				return false;
		}
		else
		{
			if(AuthServerCommunication.getInstance().isShutdown())
				return false;

			AuthServerCommunication.getInstance().sendPacket(new BonusRequest(getAccountName(), type, expireTime));
		}

		getNetConnection().setPremiumAccountType(type);
		getNetConnection().setPremiumAccountExpire(expireTime);

		if(startPremiumAccountTask())
		{
			if(!extended)
			{
				if(getParty() != null)
					getParty().recalculatePartyData();

				sendPacket(new ExBR_PremiumStatePacket(this, hasPremiumAccount()));
			}
			return true;
		}
		return false;
	}

	public boolean removePremiumAccount()
	{
		PremiumAccountTemplate oldPremiumAccount = getPremiumAccount();
		if(oldPremiumAccount.getType() == 0)
			return false;

		removePremiumAccountStats(oldPremiumAccount);

		_premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(0);

		if(getParty() != null)
			getParty().recalculatePartyData();

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().delete(getAccountName());
		else
			AuthServerCommunication.getInstance().sendPacket(new BonusRequest(getAccountName(), 0, 0));

		if(getNetConnection() != null)
		{
			getNetConnection().setPremiumAccountType(0);
			getNetConnection().setPremiumAccountExpire(0);
		}

		stopPremiumAccountTask();

		removePremiumAccountItems(true);

		sendPacket(new ExBR_PremiumStatePacket(this, hasPremiumAccount()));

		return true;
	}

	private boolean tryGiveFreePremiumAccount()
	{
		if(Config.FREE_PA_TYPE == 0 || Config.FREE_PA_DELAY <= 0)
			return false;

		PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(Config.FREE_PA_TYPE);
		if(premiumAccount == null)
			return false;

		boolean recieved = Boolean.parseBoolean(AccountVariablesDAO.getInstance().select(getAccountName(), FREE_PA_RECIEVED, "false"));
		if(recieved)
			return false;

		if(givePremiumAccount(premiumAccount, Config.FREE_PA_DELAY))
		{
			AccountVariablesDAO.getInstance().insert(getAccountName(), FREE_PA_RECIEVED, "true");

			if(Config.ENABLE_FREE_PA_NOTIFICATION)
			{
				CustomMessage message = null;
				int accountExpire = getNetConnection().getPremiumAccountExpire();
				if(accountExpire != Integer.MAX_VALUE)
				{
					message = new CustomMessage("l2s.gameserver.model.Player.GiveFreePA", this);
					message.addString(TimeUtils.toSimpleFormat(accountExpire * 1000L));
				}
				else
					message = new CustomMessage("l2s.gameserver.model.Player.GiveUnlimFreePA", this);

				sendPacket(new ExShowScreenMessage(message.toString(), 15000, ScreenMessageAlign.TOP_CENTER, true));
			}
			return true;
		}
		return false;
	}

	private boolean startPremiumAccountTask()
	{
		if(!Config.PREMIUM_ACCOUNT_ENABLED)
			return false;

		stopPremiumAccountTask();
		removePremiumAccountStats(_premiumAccount);

		if(getNetConnection() == null)
			return false;

		int accountType = getNetConnection().getPremiumAccountType();
		PremiumAccountTemplate premiumAccount = accountType == 0 ? null : PremiumAccountHolder.getInstance().getPremiumAccount(accountType);
		if(premiumAccount != null)
		{
			int accountExpire = getNetConnection().getPremiumAccountExpire();
			if(accountExpire > System.currentTimeMillis() / 1000L)
			{
				if(_premiumAccount != premiumAccount)
				{
					_premiumAccount = premiumAccount;

					int itemsReceivedType = getVarInt(PA_ITEMS_RECIEVED);
					if(itemsReceivedType != premiumAccount.getType())
					{
						removePremiumAccountItems(false);

						ItemData[] items = premiumAccount.getGiveItemsOnStart();
						if(items.length > 0)
						{
							if(!isInventoryFull())
							{
								sendPacket(SystemMsg.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED_IF_THE_PREMIUM_ACCOUNT_IS_TERMINATED_THIS_ITEM_WILL_BE_DELETED);
								for(ItemData item : items)
									ItemFunctions.addItem(this, item.getId(), item.getCount(), true, "Premium Account free items");
								setVar(PA_ITEMS_RECIEVED, accountType);
							}
							else
								sendPacket(SystemMsg.THE_PREMIUM_ITEM_CANNOT_BE_RECEIVED_BECAUSE_THE_INVENTORY_WEIGHTQUANTITY_LIMIT_HAS_BEEN_EXCEEDED);
						}
					}
				}

				addPremiumAccountStats(_premiumAccount);

				if(accountExpire != Integer.MAX_VALUE)
					_premiumAccountExpirationTask = LazyPrecisionTaskManager.getInstance().startPremiumAccountExpirationTask(this, accountExpire);
				return true;
			}
			else
			{
				if(!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
					AuthServerCommunication.getInstance().sendPacket(new BonusRequest(getAccountName(), 0, 0));
			}
		}

		removePremiumAccountItems(true);

		if(tryGiveFreePremiumAccount())
			return false;

		if(Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER)
			PremiumAccountDAO.getInstance().delete(getAccountName());

		if(getNetConnection() != null)
		{
			getNetConnection().setPremiumAccountType(0);
			getNetConnection().setPremiumAccountExpire(0);
		}
		return false;
	}

	private void addPremiumAccountStats(PremiumAccountTemplate premiumAccount)
	{
		double currentHpRatio = getCurrentHpRatio();
		double currentMpRatio = getCurrentMpRatio();
		double currentCpRatio = getCurrentCpRatio();

		addTriggers(premiumAccount);
		addStatFuncs(premiumAccount.getStatFuncs());

		Skill[] skills = premiumAccount.getAttachedSkills();
		for(Skill skill : skills)
			addSkill(skill);

		if(skills.length > 0)
			sendSkillList();

		setCurrentHp(getMaxHp() * currentHpRatio, false);
		setCurrentMp(getMaxMp() * currentMpRatio);
		setCurrentCp(getMaxCp() * currentCpRatio);

		updateStats();
	}

	private void removePremiumAccountStats(PremiumAccountTemplate premiumAccount)
	{
		double currentHpRatio = getCurrentHpRatio();
		double currentMpRatio = getCurrentMpRatio();
		double currentCpRatio = getCurrentCpRatio();

		removeStatsOwner(premiumAccount);
		removeTriggers(premiumAccount);

		Skill[] skills = premiumAccount.getAttachedSkills();
		for(Skill skill : skills)
			removeSkill(skill);

		if(skills.length > 0)
			sendSkillList();

		setCurrentHp(getMaxHp() * currentHpRatio, false);
		setCurrentMp(getMaxMp() * currentMpRatio);
		setCurrentCp(getMaxCp() * currentCpRatio);

		updateStats();
	}

	private void stopPremiumAccountTask()
	{
		if(_premiumAccountExpirationTask != null)
		{
			_premiumAccountExpirationTask.cancel(false);
			_premiumAccountExpirationTask = null;
		}
	}

	private void removePremiumAccountItems(boolean notify)
	{
		PremiumAccountTemplate premiumAccount = PremiumAccountHolder.getInstance().getPremiumAccount(getVarInt(PA_ITEMS_RECIEVED));
		if(premiumAccount != null)
		{
			ItemData[] items = premiumAccount.getTakeItemsOnEnd();
			if(items.length > 0)
			{
				if(notify)
					sendPacket(SystemMsg.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED_THE_PROVIDED_PREMIUM_ITEM_WAS_DELETED);
				for(ItemData item : items)
					ItemFunctions.deleteItem(this, item.getId(), item.getCount(), notify);
				for(ItemData item : items)
					ItemFunctions.deleteItemsEverywhere(this, item.getId());
			}
		}
		unsetVar(PA_ITEMS_RECIEVED);
	}

	@Override
	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0, null, null);
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	/**
	 * Возвращает тип атакующего элемента
	 */
	public Element getAttackElement()
	{
		return Formulas.getAttackElement(this, null);
	}

	/**
	 * Возвращает силу атаки элемента
	 *
	 * @return значение атаки
	 */
	public int getAttack(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getAttack(), 0., null, null);
	}

	/**
	 * Возвращает защиту от элемента
	 *
	 * @return значение защиты
	 */
	public int getDefence(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getDefence(), 0., null, null);
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		else
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return false;
		}
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		GameObject target = null;

		if(getTargetId() == id)
			target = getTarget();

		if(target == null && _party != null)
			for(Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}

		if(target == null)
			target = World.getAroundObjectById(this, id);

		return target == null || target.isInvisible(this) ? null : target;
	}

	@Override
	public int getMaxCp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_CP, getClassId().getBaseCp(getLevel()), null, null));
	}

	@Override
	public int getMaxHp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_HP, getClassId().getBaseHp(getLevel()), null, null));
	}

	@Override
	public int getMaxMp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_MP, getClassId().getBaseMp(getLevel()), null, null));
	}

	@Override
	public int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		if(weaponItem == null)
			return getTemplate().getBaseRandDam();

		return weaponItem.getRandomDamage();
	}

	@Override
	public int getPDef(final Creature target)
	{
		int init = 0;

		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
			init += getTemplate().getArmDef().getChestDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != ItemTemplate.SLOT_FULL_ARMOR))
			init += getTemplate().getArmDef().getLegsDef();

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
			init += getTemplate().getArmDef().getHelmetDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
			init += getTemplate().getArmDef().getGlovesDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
			init += getTemplate().getArmDef().getBootsDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER) == null)
			init += getTemplate().getArmDef().getUnderwearDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_BACK) == null)
			init += getTemplate().getArmDef().getCloakDef();

		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final Creature target, final Skill skill)
	{
		int init = 0;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null)
			init += getTemplate().getJewlDef().getLEaaringDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) == null)
			init += getTemplate().getJewlDef().getREaaringDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) == null)
			init += getTemplate().getJewlDef().getNecklaceDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null)
			init += getTemplate().getJewlDef().getLRingDef();
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null)
			init += getTemplate().getJewlDef().getRRingDef();

		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor, boolean save)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR && save)
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		else if(save)
			unsetVar("titlecolor");
		_titlecolor = titlecolor;
	}

	@Override
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	public final String getCursedWeaponName(Player activeChar)
	{
		if(isCursedWeaponEquipped())
			return new CustomMessage("cursed_weapon_name." + _cursedWeaponEquippedId, activeChar).toString();
		return null;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isInMovie();
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _isFishing;
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean value)
	{
		_isFishing = value;
	}

	public void startFishing(FishTemplate fish, int lureId)
	{
		_fishing.setFish(fish);
		_fishing.setLureId(lureId);
		_fishing.startFishing();
	}

	public void stopFishing()
	{
		_fishing.stopFishing();
	}

	public Location getFishLoc()
	{
		return _fishing.getFishLoc();
	}

	public PremiumAccountTemplate getPremiumAccount()
	{
		return _premiumAccount;
	}

	public boolean hasPremiumAccount()
	{
		return _premiumAccount.getType() > 0;
	}

	@Override
	public double getRateAdena()
	{
		return calcStat(Stats.RATE_ADENA, (_party == null || Config.ALT_PARTY_RATE_FORMULA ? getPremiumAccount().getRates().getAdena() : _party._rateAdena), null, null);
	}

	@Override
	public double getRateItems()
	{
		return calcStat(Stats.RATE_ITEMS, (_party == null || Config.ALT_PARTY_RATE_FORMULA ? getPremiumAccount().getRates().getDrop() : _party._rateDrop), null, null);
	}

	@Override
	public double getRateExp()
	{
		return calcStat(Stats.EXP, (_party == null || Config.ALT_PARTY_RATE_FORMULA ? getPremiumAccount().getRates().getExp() : _party._rateExp), null, null);
	}

	@Override
	public double getRateSp()
	{
		return calcStat(Stats.SP, (_party == null || Config.ALT_PARTY_RATE_FORMULA ? getPremiumAccount().getRates().getSp() : _party._rateSp), null, null);
	}

	@Override
	public double getRateSpoil()
	{
		return calcStat(Stats.RATE_SPOIL, (_party == null || Config.ALT_PARTY_RATE_FORMULA ? getPremiumAccount().getRates().getSpoil() : _party._rateSpoil), null, null);
	}

	public double getDropChanceMod()
	{
		double mod = Config.DROP_CHANCE_MODIFIER;
		mod *= isInParty() ? _party._dropChanceMod : getPremiumAccount().getModifiers().getDropChance();
//		mod *= 1. + getStat().calc(Stats.DROP_CHANCE_MODIFIER, 0, null, null);
		return mod;
	}

	public double getSpoilChanceMod()
	{
		double mod = Config.SPOIL_CHANCE_MODIFIER;
		mod *= isInParty() ? _party._spoilChanceMod : getPremiumAccount().getModifiers().getSpoilChance();
//		mod *= 1. + getStat().calc(Stats.SPOIL_CHANCE_MODIFIER, 0, null, null);
		return mod;
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	public boolean isGMInvisible()
	{
		return getPlayerAccess().GodMode && _gmInvisible.get();
	}

	public boolean setGMInvisible(boolean value)
	{
		if(value)
			return _gmInvisible.getAndSet(true);
		return _gmInvisible.setAndGet(false);
	}

	@Override
	public boolean isUndying()
	{
		return super.isUndying() || isGMUndying();
	}

	public boolean isGMUndying()
	{
		return getPlayerAccess().GodMode && _gmUndying.get();
	}

	public boolean setGMUndying(boolean value)
	{
		if(value)
			return _gmUndying.getAndSet(true);
		return _gmUndying.setAndGet(false);
	}

	private OnPlayerChatMessageReceive _snoopListener = null;
	private List<Player> _snoopListenerPlayers = new ArrayList<Player>();

	private class SnoopListener implements OnPlayerChatMessageReceive
	{
		@Override
		public void onChatMessageReceive(Player player, ChatType type, String charName, String text)
		{
			if(_snoopListenerPlayers.size() > 0)
			{
				SnoopPacket sn = new SnoopPacket(getObjectId(), getName(), type.ordinal(), charName, text);
				for(Player pci : _snoopListenerPlayers)
				{
					if(pci != null)
						pci.sendPacket(sn);
				}
			}
		}
	}

	public void addSnooper(Player pci)
	{
		if(!_snoopListenerPlayers.contains(pci))
			_snoopListenerPlayers.add(pci);

		if(!_snoopListenerPlayers.isEmpty() && _snoopListener == null)
		{
			_snoopListener = new SnoopListener();
			addListener(_snoopListener);
		}
	}

	public void removeSnooper(Player pci)
	{
		_snoopListenerPlayers.remove(pci);

		if(_snoopListenerPlayers.isEmpty() && _snoopListener != null)
		{
			removeListener(_snoopListener);
			_snoopListener = null;
		}
	}

	/**
	 * Reset all skill reyuza character.
	 */
	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public DeathPenalty getDeathPenalty()
	{
		return getActiveSubClass() == null ? null : getActiveSubClass().getDeathPenalty();
	}

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		if(!val)
			getEffectList().stopEffect(Skill.SKILL_CHARM_OF_COURAGE);

		sendEtcStatusUpdate();
	}

	private int _increasedForce = 0;
	private int _consumedSouls = 0;

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;

		int max = (int) calcStat(Stats.SOULS_LIMIT, 0, monster, null);

		if(i > max)
			i = max;

		if(i <= 0)
		{
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}

		if(_consumedSouls != i)
		{
			int diff = i - _consumedSouls;
			if(diff > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		}
		else if(max == i)
		{
			sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
			return;
		}

		_consumedSouls = i;
		sendPacket(new EtcStatusUpdatePacket(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		i = Math.min(i, Charge.MAX_CHARGE);
		i = Math.max(i, 0);

		if(i != 0 && i > _increasedForce)
			sendPacket(new SystemMessage(SystemMessage.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL).addNumber(i));

		_increasedForce = i;
		sendEtcStatusUpdate();
	}
	
	/**
	 * @param z
	 * @return true if character falling now on the start of fall return false for correct coord sync!
	 */
	public final boolean isFalling(int z)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return false;

		if(System.currentTimeMillis() < _fallingTimestamp)
			return true;

		final double deltaZ = Math.abs(getZ() - z);
		if(deltaZ <= getTemplate().getBaseSafeFallHeight())
			return false;

		// If there is no geodata loaded for the place we are client Z correction might cause falling damage.
		if(!GeoEngine.hasGeo(getX(), getY(), getGeoIndex()))
			return false;

		final int damage = (int) calcStat(Stats.FALL, (deltaZ * getMaxHp()) / 1000., null, null);
		if(damage > 0)
		{
			setCurrentHp(Math.max(1, (int) (getCurrentHp() - damage)), false);
			sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(damage));
		}
		
		setFalling();
		
		return false;
	}

	/**
	 * Set falling timestamp
	 */
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}

	/**
	 * Системные сообщения о текущем состоянии хп
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		//сюда пасивные скиллы
		int[] _hp = {
				30,
				30
		};
		int[] skills = {
				290,
				291
		};

		//сюда активные эффекты
		int[] _effects_skills_id = {
				139,
				176,
				292,
				292,
				420
		};
		int[] _effects_hp = {
				30,
				30,
				30,
				60,
				30
		};

		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;

		//check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}

		//check for active effects
		for(Integer i = 0; i < _effects_skills_id.length; i++)
			if(getEffectList().getEffectsBySkillId(_effects_skills_id[i]) != null)
				if(_curHpPercent > _effects_hp[i] && _newHpPercent <= _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[i] && _newHpPercent > _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}

		if(needsUpdate)
			sendChanges();
	}

	/**
	 * Системные сообщения для темных эльфов о вкл/выкл ShadowSence (skill id = 294)
	 */
	public void checkDayNightMessages()
	{
		int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(SystemMessage.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(SystemMessage.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
		sendChanges();
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	//TODO [G1ta0] переработать в лисенер?
	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;

		boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		boolean lastInDangerArea = (_zoneMask & ZONE_ALTERED_FLAG) == ZONE_ALTERED_FLAG;
		boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;
		boolean lastInPeaceZone = (_zoneMask & ZONE_PEACE_FLAG) == ZONE_PEACE_FLAG;
		//FIXME G1ta0 boolean lastInSSQZone = (_zoneMask & ZONE_SSQ_FLAG) == ZONE_SSQ_FLAG;

		boolean isInCombatZone = isInCombatZone();
		boolean isInDangerArea = isInDangerArea() || isInZone(ZoneType.CHANGED_ZONE);
		boolean isOnSiegeField = isOnSiegeField();
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();

		// обновляем компас, только если персонаж в мире
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;

		if(isInCombatZone)
			_zoneMask |= ZONE_PVP_FLAG;
		if(isInDangerArea)
			_zoneMask |= ZONE_ALTERED_FLAG;
		if(isOnSiegeField)
			_zoneMask |= ZONE_SIEGE_FLAG;
		if(isInPeaceZone)
			_zoneMask |= ZONE_PEACE_FLAG;
		if(isInSSQZone)
			_zoneMask |= ZONE_SSQ_FLAG;

		if(lastZoneMask != _zoneMask)
			sendPacket(new ExSetCompassZoneCode(this));

		if(lastInCombatZone != isInCombatZone)
			broadcastRelationChanged();

		if(lastInDangerArea != isInDangerArea)
			sendPacket(new EtcStatusUpdatePacket(this));

		if(lastOnSiegeField != isOnSiegeField)
		{
			broadcastRelationChanged();
			if(isOnSiegeField)
				sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			else
			{
				sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				if(!isTeleporting() && getPvpFlag() == 0)
					startPvPFlag(null);
			}
		}

		if(lastInPeaceZone != isInPeaceZone)
			if(isInPeaceZone)
			{
				setRecomTimerActive(false);
				if(getNevitSystem().isActive())
					getNevitSystem().stopAdventTask(true);
				startVitalityTask();
				DuelEvent duelEvent = getEvent(DuelEvent.class);
				if(duelEvent != null)
					duelEvent.abortDuel(this);
			}
			else
				stopVitalityTask();

		if(isInWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	public void startAutoSaveTask()
	{
		if(!Config.AUTOSAVE)
			return;
		if(_autoSaveTask == null)
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
			_autoSaveTask.cancel(false);
		_autoSaveTask = null;
	}

	public void startVitalityTask()
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;
		if(_vitalityTask == null)
			_vitalityTask = LazyPrecisionTaskManager.getInstance().addVitalityRegenTask(this);
	}

	public void stopVitalityTask()
	{
		if(_vitalityTask != null)
			_vitalityTask.cancel(false);
		_vitalityTask = null;
	}

	public void startPcBangPointsTask()
	{
		if(!Config.ALT_PCBANG_POINTS_ENABLED || Config.ALT_PCBANG_POINTS_DELAY <= 0)
			return;
		if(_pcCafePointsTask == null)
			_pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
	}

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
			_pcCafePointsTask.cancel(false);
		_pcCafePointsTask = null;
	}

	public void startUnjailTask(Player player, int time)
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player, false), time * 60000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
			_unjailTask.cancel(false);
		_unjailTask = null;
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(message));
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	public int getRelation(Player target)
	{
		int result = 0;

		if(getClan() != null)
		{
			result |= RelationChangedPacket.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
				result |= RelationChangedPacket.RELATION_CLAN_MATE;
			if(getClan().getAllyId() != 0)
				result |= RelationChangedPacket.RELATION_ALLY_MEMBER;
		}

		if(isClanLeader())
			result |= RelationChangedPacket.RELATION_LEADER;

		Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChangedPacket.RELATION_HAS_PARTY;

			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChangedPacket.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChangedPacket.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChangedPacket.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChangedPacket.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChangedPacket.RELATION_PARTY1; // 0x1
					break;
			}
		}

		if(isInSpecialPvPZone() && target.isInSpecialPvPZone())
		{
			result |= RelationChangedPacket.RELATION_INSIEGE;
			if(!isInSameParty(target) && !isInSameChannel(target) && !isInSameClan(target) && !isInSameAlly(target))
			{
				result |= RelationChangedPacket.RELATION_ATTACKER;
				result |= RelationChangedPacket.RELATION_ENEMY;
			}
			else
				result |= RelationChangedPacket.RELATION_ALLY;
		}
		
		Clan clan1 = getClan();
		Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY)
				if(clan2.isAtWarWith(clan1.getClanId()))
				{
					result |= RelationChangedPacket.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2.getClanId()))
						result |= RelationChangedPacket.RELATION_MUTUAL_WAR;
				}
			if(getBlockCheckerArena() != -1)
			{
				result |= RelationChangedPacket.RELATION_INSIEGE;
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
				if(holder.getPlayerTeam(this) == 0)
					result |= RelationChangedPacket.RELATION_ENEMY;
				else
					result |= RelationChangedPacket.RELATION_ALLY;
				result |= RelationChangedPacket.RELATION_ATTACKER;
			}
		}

		for(Event e : getEvents())
			result =  e.getRelation(this, target, result);

		return result;
	}

	/**
	 * 0=White, 1=Purple, 2=PurpleBlink
	 */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		if(_karma > 0)
			return;
		// Flagaemsya if not in siege zone
		if(isOnSiegeField())
			return;
			
		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
			startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;

		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(_handysBlockCheckerEventArena != -1)
			return;
		if(_pvpFlag == value)
			return;

		setPvpFlag(value);

		sendStatusUpdate(true, true, StatusUpdatePacket.PVP_FLAG);

		broadcastRelationChanged();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
	}

	public boolean isRegisteredInFightClub()
	{
		return getEvent(AbstractFightClub.class) != null;
	}

	public boolean isInFightClub()
	{
		AbstractFightClub event = getFightClubEvent();
		if(event == null)
			return false;
		return event.getFightClubPlayer(this) != null;
	}

	public FightClubGameRoom getFightClubGameRoom()
	{
		return _fightClubGameRoom;
	}

	public void setFightClubGameRoom(FightClubGameRoom room)
	{
		_fightClubGameRoom = room;
	}

	public AbstractFightClub getFightClubEvent()
	{
		return getEvent(AbstractFightClub.class);
	}

	private Map<Integer, TamedBeastInstance> _tamedBeasts = new ConcurrentHashMap<Integer, TamedBeastInstance>();

	public Map<Integer, TamedBeastInstance> getTrainedBeasts()
	{
		return _tamedBeasts;
	}

	public void addTrainedBeast(TamedBeastInstance tamedBeast)
	{
		_tamedBeasts.put(tamedBeast.getObjectId(), tamedBeast);
	}

	public void removeTrainedBeast(int npcId)
	{
		_tamedBeasts.remove(npcId);
	}

	private long _lastAttackPacket = 0;

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	private long _lastMovePacket = 0;

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		_keyBindings = keyBindings;
	}

	/**
	 * Устанавливает режим трансформаии<BR>
	 *
	 * @param transformationId идентификатор трансформации
	 *                         Известные режимы:<BR>
	 *                         <li>0 - стандартный вид чара
	 *                         <li>1 - Onyx Beast
	 *                         <li>2 - Death Blader
	 *                         <li>etc.
	 */
	public void setTransformation(int transformationId)
	{
		if(transformationId == _transformationId || _transformationId != 0 && transformationId != 0)
			return;

		// Для каждой трансформации свой набор скилов
		if(transformationId == 0) // Обычная форма
		{
			// Останавливаем текущий эффект трансформации
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getEffectType() == EffectType.Transformation)
				{
					if (effect.calc() == 0) // Не обрываем Dispel
						continue;
					effect.exit();
					preparateToTransform(effect.getSkill());
					break;
				}

			// Удаляем скилы трансформации
			if(!_transformationSkills.isEmpty())
			{
				for(Skill s : _transformationSkills.values())
					if(!s.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(this, null, s, true) && !s.isHeroic())
						super.removeSkill(s);
				_transformationSkills.clear();
			}
		}
		else
		{
			if(!isCursedWeaponEquipped())
			{
				// Добавляем скилы трансформации
				for(Effect effect : getEffectList().getAllEffects())
				{
					if(effect != null && effect.getEffectType() == EffectType.Transformation)
					{
						if(effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise)
						{
							for(Skill s : getAllSkills())
							{
								if(s != null && (s.isActive() || s.isToggle()))
									_transformationSkills.put(s.getId(), s);
						}
						}
						else
						{
							for(AddedSkill s : effect.getSkill().getAddedSkills())
							{
								if(s.level == 0) // трансформация позволяет пользоваться обычным скиллом
								{
									int s2 = getSkillLevel(s.id);
									if(s2 > 0)
										_transformationSkills.put(s.id, SkillHolder.getInstance().getSkill(s.id, s2));
								}
								else if(s.level == -2) // XXX: дикий изжоп для скиллов зависящих от уровня игрока
								{
									int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
									int maxLevel = SkillHolder.getInstance().getBaseLevel(s.id);
									int curSkillLevel = 1;
									if(maxLevel > 3)
										curSkillLevel += getLevel() - learnLevel;
									else
										curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel); // не спрашивайте меня что это такое
									curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
									_transformationSkills.put(s.id, SkillHolder.getInstance().getSkill(s.id, curSkillLevel));
								}
								else
									_transformationSkills.put(s.id, s.getSkill());
							}
						}
						preparateToTransform(effect.getSkill());
						break;
					}
			}
			}
			else
				preparateToTransform(null);

			if(!isInOlympiadMode() && !isCursedWeaponEquipped() && _hero && getBaseClassId() == getActiveClassId())
			{
				// Добавляем хиро скиллы проклятому трансформу
				_transformationSkills.put(395, SkillHolder.getInstance().getSkill(395, 1));
				_transformationSkills.put(396, SkillHolder.getInstance().getSkill(396, 1));
				_transformationSkills.put(1374, SkillHolder.getInstance().getSkill(1374, 1));
				_transformationSkills.put(1375, SkillHolder.getInstance().getSkill(1375, 1));
				_transformationSkills.put(1376, SkillHolder.getInstance().getSkill(1376, 1));
			}

			for(Skill s : _transformationSkills.values())
				addSkill(s, false);
		}

		_transformationId = transformationId;

		sendPacket(new ExBasicActionList(this));
		sendSkillList();
		sendPacket(new ShortCutInitPacket(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		broadcastUserInfo(true);
	}

	private void preparateToTransform(Skill transSkill)
	{
		if(transSkill == null || !transSkill.isBaseTransformation())
		{
			// Останавливаем тугл скиллы
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getSkill().isToggle())
					effect.exit();
		}
	}

	public boolean isInFlyingTransform()
	{
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	public boolean isInMountTransform()
	{
		return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
	}

	/**
	 * Возвращает режим трансформации
	 *
	 * @return ID режима трансформации
	 */
	public int getTransformation()
	{
		return _transformationId;
	}

	/**
	 * Возвращает имя трансформации
	 *
	 * @return String
	 */
	public String getTransformationName()
	{
		return _transformationName;
	}

	/**
	 * Устанавливает имя трансформаии
	 *
	 * @param name имя трансформации
	 */
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	/**
	 * Устанавливает шаблон трансформации, используется для определения коллизий
	 *
	 * @param template ID шаблона
	 */
	public void setTransformationTemplate(int template)
	{
		_transformationTemplate = template;
	}

	/**
	 * Возвращает шаблон трансформации, используется для определения коллизий
	 *
	 * @return NPC ID
	 */
	public int getTransformationTemplate()
	{
		return _transformationTemplate;
	}

	/**
	 * Возвращает коллекцию скиллов, с учетом текущей трансформации
	 */
	@Override
	public final Collection<Skill> getAllSkills()
	{
		// Трансформация неактивна
		if(_transformationId == 0)
			return super.getAllSkills();

		// Трансформация активна
		Map<Integer, Skill> tempSkills = new HashMap<Integer, Skill>();
		for(Skill s : super.getAllSkills())
			if(s != null && !s.isActive() && !s.isToggle())
				tempSkills.put(s.getId(), s);
		tempSkills.putAll(_transformationSkills); // Добавляем к пассивкам скилы текущей трансформации
		return tempSkills.values();
	}

	public void setAgathion(int id)
	{
		if(_agathionId == id)
			return;

		_agathionId = id;
		sendPacket(new ExUserInfoCubic(this));
		broadcastCharInfo();
	}

	public int getAgathionId()
	{
		return _agathionId;
	}

	/**
	 * Возвращает количество PcBangPoint'ов даного игрока
	 *
	 * @return количество PcCafe Bang Points
	 */
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	/**
	 * Устанавливает количество Pc Cafe Bang Points для даного игрока
	 *
	 * @param val новое количество PcCafeBangPoints
	 */
	public void setPcBangPoints(int val, boolean store)
	{
		_pcBangPoints = Math.max(0, Math.min(Config.ALT_MAX_PC_BANG_POINTS, val));

		if(store)
		{
			Connection con = null;
			PreparedStatement st = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement("UPDATE characters SET pcBangPoints = ? WHERE obj_Id = ?");
				st.setInt(1, getPcBangPoints());
				st.setInt(2, getObjectId());
				st.executeUpdate();
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, st);
			}
		}
	}

	public void addPcBangPoints(int count, boolean doublePoints, boolean notify)
	{
		if(doublePoints)
			count *= 2;

		int pcBangPoints = getPcBangPoints();
		setPcBangPoints(pcBangPoints + count, true);

		int addedCount = getPcBangPoints() - pcBangPoints;
		if(addedCount > 0)
		{
			if(notify)
				sendPacket(new SystemMessage(doublePoints ? SystemMessage.DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT : SystemMessage.YOU_ACQUIRED_S1_PC_BANG_POINT).addNumber(addedCount));
			sendPacket(new ExPCCafePointInfoPacket(this, addedCount, 1, 2, 12));
		}
	}

	public boolean reducePcBangPoints(int count, boolean notify)
	{
		int pcBangPoints = getPcBangPoints();
		if(pcBangPoints < count)
			return false;

		setPcBangPoints(pcBangPoints - count, true);

		if(notify)
			sendPacket(new SystemMessage(SystemMessage.YOU_ARE_USING_S1_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfoPacket(this, 0, 1, 2, 12));
		return true;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	/**
	 * Персонаж в процессе выхода из игры
	 *
	 * @return возвращает true если процесс выхода уже начался
	 */
	public boolean isLogoutStarted()
	{
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val)
	{
		if(!val)
			unsetVar("offline");
		_offline = val;
	}

	public boolean isInOfflineMode()
	{
		return _offline;
	}

	public void saveTradeList()
	{
		String val = "";

		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(TradeItem i : _sellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("selllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_packageSellList == null || _packageSellList.isEmpty())
			unsetVar("packageselllist");
		else
		{
			for(TradeItem i : _packageSellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("packageselllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
				setVar("sellstorename", getSellStoreName(), -1);
		}

		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(TradeItem i : _buyList)
				val += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("buylist", val, -1);
			val = "";
			if(_tradeList != null && getBuyStoreName() != null)
				setVar("buystorename", getBuyStoreName(), -1);
		}

		if(_createList == null || _createList.isEmpty())
			unsetVar("createlist");
		else
		{
			for(ManufactureItem i : _createList)
				val += i.getRecipeId() + ";" + i.getCost() + ":";
			setVar("createlist", val, -1);
			if(getManufactureName() != null)
				setVar("manufacturename", getManufactureName(), -1);
		}
	}

	public void restoreTradeList()
	{
		String var;
		var = getVar("selllist");
		if(var != null)
		{
			_sellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_sellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("packageselllist");
		if(var != null)
		{
			_packageSellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_packageSellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
				setSellStoreName(var);
		}
		var = getVar("buylist");
		if(var != null)
		{
			_buyList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;
				TradeItem i = new TradeItem();
				i.setItemId(Integer.parseInt(values[0]));
				i.setCount(Long.parseLong(values[1]));
				i.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i);
			}
			var = getVar("buystorename");
			if(var != null)
				setBuyStoreName(var);
		}
		var = getVar("createlist");
		if(var != null)
		{
			_createList = new CopyOnWriteArrayList<ManufactureItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 2)
					continue;
				int recId = Integer.parseInt(values[0]);
				long price = Long.parseLong(values[1]);
				if(findRecipe(recId))
					_createList.add(new ManufactureItem(recId, price));
			}
			var = getVar("manufacturename");
			if(var != null)
				setManufactureName(var);
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public DecoyInstance getDecoy()
	{
		return _decoy;
	}

	public void setDecoy(DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				return 1;
			case PetDataTable.WYVERN_ID:
				return 2;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				return 3;
		}
		return 0;
	}

	@Override
	public double getCollisionRadius()
	{
		if(getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if (template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if (npcTemplate != null)
					return npcTemplate.getCollisionRadius();
			}
		}
		else if(isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionRadius();
			}
		}
		return super.getCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		if(getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if (template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if (npcTemplate != null)
					return npcTemplate.getCollisionHeight();
			}
		}
		else if(isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if (mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if (mountNpcTemplate != null)
					return mountNpcTemplate.getCollisionHeight();
			}
		}
		return super.getCollisionHeight();
	}

	@Override
	public boolean setReflection(Reflection reflection)
	{
		if(getReflection() == reflection)
			return true;

		if(!super.setReflection(reflection))
			return false;

		if(_servitor != null && !_servitor.isDead())
			_servitor.setReflection(reflection);

		if(reflection != ReflectionManager.DEFAULT)
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
				setVar("reflection", String.valueOf(reflection.getId()), -1);
		}
		else
			unsetVar("reflection");

		if(getActiveSubClass() != null)
		{
			getInventory().validateItems();
			// Для квеста _129_PailakaDevilsLegacy
			if(getServitor() != null && (getServitor().getNpcId() == 14916 || getServitor().getNpcId() == 14917))
				getServitor().unSummon();
		}
		return true;
	}

	public boolean isTerritoryFlagEquipped()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isTerritoryFlag();
	}

	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getFame()
	{
		return _fame;
	}

	public void setFame(int fame, String log)
	{
		fame = Math.min(Config.LIM_FAME, fame);

		if(log != null && !log.isEmpty())
		{
			Log.LogEvent(getName(), getIP(), "Fame", "add fame: "+getName()+" count: "+fame+" for "+log+"");
			Log.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		}
		if(fame > _fame)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		_fame = fame;
		sendChanges();
	}

	public int getVitalityLevel(boolean blessActive)
	{
		return Config.ALT_VITALITY_ENABLED ? (blessActive ? 4 : _vitalityLevel) : 0;
	}

	public double getVitality()
	{
		return Config.ALT_VITALITY_ENABLED ? _vitality : 0;
	}

	public void addVitality(double val)
	{
		setVitality(getVitality() + val);
	}

	public void setVitality(double newVitality)
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;

		newVitality = Math.max(Math.min(newVitality, Config.VITALITY_LEVELS[4]), 0);

		if(newVitality >= _vitality || getLevel() >= 10)
		{
			if(newVitality != _vitality)
				if(newVitality == 0)
					sendPacket(Msg.VITALITY_IS_FULLY_EXHAUSTED);
				else if(newVitality == Config.VITALITY_LEVELS[4])
					sendPacket(Msg.YOUR_VITALITY_IS_AT_MAXIMUM);

			_vitality = newVitality;
		}

		int newLevel = 0;
		if(_vitality >= Config.VITALITY_LEVELS[3])
			newLevel = 4;
		else if(_vitality >= Config.VITALITY_LEVELS[2])
			newLevel = 3;
		else if(_vitality >= Config.VITALITY_LEVELS[1])
			newLevel = 2;
		else if(_vitality >= Config.VITALITY_LEVELS[0])
			newLevel = 1;

		if(_vitalityLevel > newLevel)
			getNevitSystem().addPoints(1500); //TODO: Количество от балды.

		if(_vitalityLevel != newLevel)
		{
			if(_vitalityLevel != -1) // при ините чара сообщения не шлём
				sendPacket(newLevel < _vitalityLevel ? Msg.VITALITY_HAS_DECREASED : Msg.VITALITY_HAS_INCREASED);
			_vitalityLevel = newLevel;
		}
		sendPacket(new ExVitalityPointInfo((int) _vitality));
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse)
	{
		_expandWarehouse = warehouse;
	}

	public boolean isNotShowBuffAnim()
	{
		return _notShowBuffAnim;
	}

	public void setNotShowBuffAnim(boolean value)
	{
		_notShowBuffAnim = value;
	}

	public void enterMovieMode()
	{
		if(isInMovie()) //already in movie
			return;

		setTarget(null);
		stopMove();
		setIsInMovie(true);
		sendPacket(new CameraModePacket(1));
	}

	public void leaveMovieMode()
	{
		setIsInMovie(false);
		sendPacket(new CameraModePacket(0));
		broadcastCharInfo();
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		sendPacket(new SpecialCameraPacket(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen ,unk));
	}

	private int _movieId = 0;
	private boolean _isInMovie;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _isInMovie && !isFakePlayer();
	}

	public void setIsInMovie(boolean state)
	{
		_isInMovie = state;
	}

	public void showQuestMovie(SceneMovie movie)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		setIsInMovie(true);
		sendPacket(movie.packet(this));
	}

	public void showQuestMovie(int movieId)
	{
		if(isInMovie()) //already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		setIsInMovie(true);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void setAutoLoot(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1);
		}
	}
	
	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
	}
	
	public void setCertainDropIndividual(boolean enable)
	{
		if(Config.ENABLE_CERTAIN_DROP_INVIDUAL)
		{
			_certainDropEnabled = enable;
			setVar("CertainDrop", String.valueOf(enable), -1);
		}	
	}
	
	public boolean isAutoLootEnabled()
	{
		return _autoLoot;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return AutoLootHerbs;
	}

	public boolean isCertainDropEnabled()
	{
		return _certainDropEnabled;
	}
	
	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			saveNameToDB();
		broadcastUserInfo(true);
		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdatePacket(this));
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	private List<String> getStoredBypasses(boolean bbs)
	{
		if(bbs)
		{
			if(bypasses_bbs == null)
				bypasses_bbs = new LazyArrayList<String>();
			return bypasses_bbs;
		}
		if(bypasses == null)
			bypasses = new LazyArrayList<String>();
		return bypasses;
	}

	public void cleanBypasses(boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized(bypassStorage)
		{
			bypassStorage.clear();
		}
	}

	public String encodeBypasses(String htmlCode, boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized(bypassStorage)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}

	public DecodedBypass decodeBypass(String bypass)
	{
		if(bypass.isEmpty())
			return null;
		BypassType bpType = BypassManager.getBypassType(bypass);
		boolean bbs = bpType == BypassType.ENCODED_BBS || bpType == BypassType.SIMPLE_BBS;
		List<String> bypassStorage = getStoredBypasses(bbs);
		switch(bpType)
		{
			case ENCODED:
			case ENCODED_BBS:
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
			case SIMPLE:
			return new DecodedBypass(bypass, false).trim();
			case SIMPLE_BBS:
				return BypassManager.STATIC_BBS_SIMPLE.get(bypass);
		}
		//_log.warn("Direct access to bypass: " + bypass + " / Player: " + getName()); //if needed uncomment
		return null;
	}

	public int getTalismanCount()
	{
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0, null, null);
	}

	public boolean getOpenCloak()
	{
		if(Config.ALT_OPEN_CLOAK_SLOT)
			return true;
		return (int) calcStat(Stats.CLOAK_SLOT, 0, null, null) > 0;
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	private ItemInstance _petControlItem = null;

	public void setPetControlItem(int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	public void isntAfk()
	{
		_lastNotAfkTime = System.currentTimeMillis();
	}

	public long getLastNotAfkTime()
	{
		return _lastNotAfkTime;
	}

	private AtomicBoolean isActive = new AtomicBoolean();

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		setNonAggroTime(0);
		setNonPvpTime(0);

		isntAfk();

		if(isActive.getAndSet(true))
			return;

		onActive();
	}

	private void onActive()
	{
		if(!isRegisteredInFightClub())
			sendPacket(Msg.YOU_ARE_PROTECTED_AGGRESSIVE_MONSTERS);
		if(getPetControlItem() != null)
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					if(getPetControlItem() != null)
						summonPet();
				}

			});
	}

	public void summonPet()
	{
		if(getServitor() != null)
			return;

		ItemInstance controlItem = getPetControlItem();
		if(controlItem == null)
			return;

		int npcId = PetDataTable.getSummonId(controlItem);
		if(npcId == 0)
			return;

		NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(petTemplate == null)
			return;

		PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
		if(pet == null)
			return;

		setPet(pet);
		pet.setTitle(getName());

		if(!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed());
			pet.updateControlItem();
			pet.store();
		}

		pet.getInventory().restore();

		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		pet.setReflection(getReflection());
		pet.spawnMe(Location.findPointToStay(this, 50, 70));
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();

		if(pet instanceof PetBabyInstance)
			((PetBabyInstance) pet).startBuffTask();

		getListeners().onSummonServitor(pet);
	}

	private Map<Integer, Long> _traps;

	public Collection<TrapInstance> getTraps()
	{
		if(_traps == null)
			return null;
		Collection<TrapInstance> result = new ArrayList<TrapInstance>(getTrapsCount());
		TrapInstance trap;
		for(Integer trapId : _traps.keySet())
			if((trap = (TrapInstance) GameObjectsStorage.get(_traps.get(trapId))) != null)
				result.add(trap);
			else
				_traps.remove(trapId);
		return result;
	}

	public int getTrapsCount()
	{
		return _traps == null ? 0 : _traps.size();
	}

	public void addTrap(TrapInstance trap)
	{
		if(_traps == null)
			_traps = new HashMap<Integer, Long>();
		_traps.put(trap.getObjectId(), trap.getStoredId());
	}

	public void removeTrap(TrapInstance trap)
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		traps.remove(trap.getObjectId());
	}

	public void destroyFirstTrap()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		TrapInstance trap;
		for(Integer trapId : traps.keySet())
		{
			if((trap = (TrapInstance) GameObjectsStorage.get(traps.get(trapId))) != null)
			{
				trap.deleteMe();
				return;
			}
			return;
		}
	}

	public void destroyAllTraps()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		List<TrapInstance> toRemove = new ArrayList<TrapInstance>();
		for(Integer trapId : traps.keySet())
			toRemove.add((TrapInstance) GameObjectsStorage.get(traps.get(trapId)));
		for(TrapInstance t : toRemove)
			if(t != null)
				t.deleteMe();
	}

	public void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}

	public int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
			synchronized(this)
			{
				if(listeners == null)
					listeners = new PlayerListenerList(this);
			}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized(this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new PlayerStatsChangeRecorder(this);
			}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	private Future<?> _hourlyTask;
	private int _hoursInGame = 0;

	public int getHoursInGame()
	{
		_hoursInGame++;
		return _hoursInGame;
	}

	public int getHoursInGames()
	{
		return _hoursInGame;
	}	
	
	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), 3600000L, 3600000L);
	}

	public void stopHourlyTask()
	{
		if(_hourlyTask != null)
		{
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	public long getPremiumPoints()
	{
		if(ProductHolder.PAYMENT_ITEM_ID > 0)
			return ItemFunctions.getItemCount(this, ProductHolder.PAYMENT_ITEM_ID);
			
		if(getNetConnection() != null)
			return getNetConnection().getPoints();
			
		return 0;
	}

	public boolean reducePremiumPoints(final int val)
	{
		if(ProductHolder.PAYMENT_ITEM_ID > 0)
		{
			if(ItemFunctions.deleteItem(this, ProductHolder.PAYMENT_ITEM_ID, val, true))
				return true;
			return false;
		}

		if(getNetConnection() != null)
		{
			getNetConnection().setPoints((int) (getPremiumPoints() - val));
			AuthServerCommunication.getInstance().sendPacket(new ReduceAccountPoints(getAccountName(), val));
			return true;
		}
		return false;
	}

	private boolean _agathionResAvailable = false;

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(boolean val)
	{
		_agathionResAvailable = val;
	}

	public boolean isClanAirShipDriver()
	{
		return isInBoat() && getBoat().isClanAirShip() && ((ClanAirShip) getBoat()).getDriver() == this;
	}

	/**
	 * _userSession - испольюзуется для хранения временных переменных.
	 */
	private Map<String, String> _userSession;

	public String getSessionVar(String key)
	{
		if(_userSession == null)
			return null;
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val)
	{
		if(_userSession == null)
			_userSession = new ConcurrentHashMap<String, String>();

		if(val == null || val.isEmpty())
			_userSession.remove(key);
		else
			_userSession.put(key, val);
	}

	public BlockList getBlockList()
	{
		return _blockList;
	}

	public FriendList getFriendList()
	{
		return _friendList;
	}

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	public boolean isDebug()
	{
		return _debug;
	}

	public void setDebug(boolean b)
	{
		_debug = b;
	}

	public void sendItemList(boolean show)
	{
		ItemInstance[] items = getInventory().getItems();
		LockType lockType = getInventory().getLockType();
		int[] lockItems = getInventory().getLockItems();

		int allSize = items.length;
		int questItemsSize = 0;
		int agathionItemsSize = 0;
		for(ItemInstance item : items)
		{
			if(item.getTemplate().isQuest())
				questItemsSize++;
			if(item.getTemplate().getAgathionEnergy() > 0)
				agathionItemsSize ++;
		}

		sendPacket(new ItemListPacket(1, this, allSize - questItemsSize, items, show, lockType, lockItems));
		sendPacket(new ItemListPacket(2, this, allSize - questItemsSize, items, show, lockType, lockItems));
		sendPacket(new ExQuestItemListPacket(1, questItemsSize, items, lockType, lockItems));
		sendPacket(new ExQuestItemListPacket(2, questItemsSize, items, lockType, lockItems));
		if(agathionItemsSize > 0)
			sendPacket(new ExBR_AgathionEnergyInfoPacket(agathionItemsSize, items));
	}

	public int getBeltInventoryIncrease()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT);
		if(item != null && item.getTemplate().getAttachedSkills() != null)
			for(Skill skill : item.getTemplate().getAttachedSkills())
				for(FuncTemplate func : skill.getAttachedFuncs())
					if(func._stat == Stats.INVENTORY_LIMIT)
						return (int) func._value;
		return 0;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(Player target)
	{
		if(target.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IN_PRIVATE_STORE).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_FISHING).addName(target));
			return false;
		}
		if(target.isInCombat())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_COMBAT).addName(target));
			return false;
		}
		if(target.isCursedWeaponEquipped())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_CURSED_WEAPON_EQUIPED).addName(target));
			return false;
		}
		if(target.isInOlympiadMode() || getLfcGame() != null)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_OLYMPIAD).addName(target));
			return false;
		}
		if(target.isOnSiegeField())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_SIEGE).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_VEHICLE_MOUNT_OTHER).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_TELEPORTING).addName(target));
			return false;
		}
		if(target.getTransformation() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_TRANSFORM).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_DEAD).addName(target));
			return false;
		}
		if(isInFightClub() && !getFightClubEvent().isFriend(this, target))
		{
			sendMessage("You cannot request couple action while player is your enemy!"); // TODO: Вынести в ДП.
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Servitor servitor = getServitor();
		if(servitor != null)
			servitor.startAttackStanceTask0();
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);

		if(miss)
		{
			/*TODOGOD:
			if(skill == null)*/
			if(!magic)
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
			else
				sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.EVADED));
			return;
		}

		if(crit)
		{
			/*TODOGOD:
			if(skill != null)
			{
				if(skill.isMagic())
					sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
				sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.CRITICAL));
			}*/
			if(magic)
			{
				sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
				sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.CRITICAL));
			}
			else
				sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(this));
		}

		if(target.isDamageBlocked())
		{
			sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), target.isInvul() ? ExMagicAttackInfo.IMMUNE : ExMagicAttackInfo.BLOCKED));
		}
		else if(target.isDoor() || (target instanceof SiegeToggleNpcInstance))
			sendPacket(new SystemMessagePacket(SystemMsg.YOU_HIT_FOR_S1_DAMAGE).addInteger(damage));
		else
		{
			sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(target).addInteger(damage).addHpChange(target.getObjectId(), getObjectId(), -damage));

			if(shld)
			{
				/*TODOGOD:
				if(damage == Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE)
				{
					if(skill != null && skill.isMagic())
					{
						sendPacket(new SystemMessagePacket(SystemMsg.C1_RESISTED_C2S_MAGIC).addName(target).addName(this));
						sendPacket(new ExMagicAttackInfo(getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					}
				}
				else if(damage > 0)
				{
					if(skill != null && skill.isMagic())
						sendPacket(new SystemMessagePacket(SystemMsg.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
				}*/
				if(target.isPlayer())
				{
					if(damage > 1)
						target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
					else if(damage == 1)
						target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				}
			}
		}
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		if(attacker != this)
			sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2).addName(this).addName(attacker).addInteger(damage).addHpChange(getObjectId(), attacker.getObjectId(), -damage));
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public boolean isSharedGroupDisabled(int groupId)
	{
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId)
	{
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp)
	{
		_sharedGroupReuses.put(group, stamp);
	}

	public Collection<IntObjectPair<TimeStamp>> getSharedGroupReuses()
	{
		return _sharedGroupReuses.entrySet();
	}

	public void sendReuseMessage(ItemInstance item)
	{
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);

		if(hours > 0)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addInteger(hours).addInteger(minutes).addInteger(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addInteger(minutes).addInteger(seconds));
		else
			sendPacket(new SystemMessagePacket(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addInteger(seconds));
	}

	public NevitSystem getNevitSystem()
	{
		return _nevitSystem;
	}

	public void ask(ConfirmDlgPacket dlg, OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		int rnd = Rnd.nextInt();
		_askDialog = new ImmutablePair<Integer, OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public Pair<Integer, OnAnswerListener> getAskListener(boolean clear)
	{
		if(!clear)
			return _askDialog;
		else
		{
			Pair<Integer, OnAnswerListener> ask = _askDialog;
			_askDialog = null;
			return ask;
		}
	}

	@Override
	public boolean isDead()
	{
		return (isInOlympiadMode() || isInDuel() || getLfcGame() != null) ? getCurrentHp() <= 1. : super.isDead();
	}

	@Override
	public int getAgathionEnergy()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		return item == null ? 0 : item.getAgathionEnergy();
	}

	@Override
	public void setAgathionEnergy(int val)
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null)
			return;
		item.setAgathionEnergy(val);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendPacket(new ExBR_AgathionEnergyInfoPacket(1, item));
	}

	public boolean hasPrivilege(Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
	}

	public void dispelBuffs()
	{
		for(Effect e : getEffectList().getAllEffects())
			if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
			{
				sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		if(getServitor() != null)
			for(Effect e : getServitor().getEffectList().getAllEffects())
				if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
					e.exit();
	}

	public void setInstanceReuse(int id, long time)
	{
		final SystemMessage msg = new SystemMessage(SystemMessage.INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE).addString(getName());
		sendPacket(msg);
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id)
	{
		if(_instancesReuses.remove(id) != null)
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId)
	{
		for(int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
				removeInstanceReuse(i);
	}

	public Long getInstanceReuse(int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	private void loadInstanceReuses()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public Reflection getActiveReflection()
	{
		for(Reflection r : ReflectionManager.getInstance().getAll())
			if(r != null && ArrayUtils.contains(r.getVisitors(), getObjectId()))
				return r;
		return null;
	}

	public boolean canEnterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);

		if(isDead())
			return false;

		if(ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT)
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		if(iz == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}

		if(ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels())
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		if(isTerritoryFlagEquipped())
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}		
		
		if(iz.getEntryType(this) == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}	
		return iz.getEntryType(this).canEnter(this, iz);
	}

	public boolean canReenterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId)
		{
			sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return false;
		}
		if(iz.isDispelBuffs())
			dispelBuffs();
		if(iz.getEntryType(this) == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}				
		return iz.getEntryType(this).canReEnter(this, iz);
	}

	public int getBattlefieldChatId()
	{
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(int battlefieldChatId)
	{
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public void broadCast(IStaticPacket... packet)
	{
		sendPacket(packet);
	}

	@Override
	public Iterator<Player> iterator()
	{
		return Collections.singleton(this).iterator();
	}

	public PlayerGroup getPlayerGroup()
	{
		if(getParty() != null)
		{
			if(getParty().getCommandChannel() != null)
				return getParty().getCommandChannel();
			else
				return getParty();
		}
		else
			return this;
	}

	public boolean isActionBlocked(String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions)
	{
		for(String action : actions)
			_blockedActions.remove(action);
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public void setOlympiadGame(OlympiadGame olympiadGame)
	{
		_olympiadGame = olympiadGame;
	}

	public OlympiadGame getOlympiadObserveGame()
	{
		return _olympiadObserveGame;
	}

	public void setOlympiadObserveGame(OlympiadGame olympiadObserveGame)
	{
		_olympiadObserveGame = olympiadObserveGame;
	}

	public void addRadar(int x, int y, int z)
	{
		sendPacket(new RadarControlPacket(2, 2, x, y, z));
		sendPacket(new RadarControlPacket(0, 1, x, y, z));
	}

	public void addRadar(Location loc)
	{
		addRadar(loc.getX(), loc.getY(), loc.getZ());
	}

	public void addRadarWithMap(int x, int y, int z)
	{
		sendPacket(new RadarControlPacket(2, 2, x, y, z));
		sendPacket(new RadarControlPacket(0, 2, x, y, z));
	}

	public void addRadarWithMap(Location loc)
	{
		addRadarWithMap(loc.getX(), loc.getY(), loc.getZ());
	}

	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(int lectureMark)
	{
		_lectureMark = lectureMark;
	}

	private static void RestoreFightClub(Player player)
	{
		String[] values = player.getVar("FightClubRate").split(";");
		int id = Integer.parseInt(values[0]);
		int count = Integer.parseInt(values[1]);
		ItemFunctions.addItem(player, id, count, true, "Fight Club reward");
		player.unsetVar("FightClubRate");
		player.unsetVar("isPvPevents");
	}

	private int[] _recentProductList = null;

	public int[] getRecentProductList()
	{
		if(_recentProductList == null)
		{
			String value = getVar(ProductHolder.RECENT_PRDCT_LIST_VAR);
			if(value == null)
				return null;

			String[] products_str = value.split(";");
			int[] result = new int[0];
			for(int i = 0; i < products_str.length; i++)
			{
				int productId = Integer.parseInt(products_str[i]);
				if(ProductHolder.getInstance().getProduct(productId) == null)
					continue;

				result = ArrayUtils.add(result, productId);
			}
			_recentProductList = result;
		}
		return _recentProductList;
	}

	public void updateRecentProductList(final int productId)
	{
		if(_recentProductList == null)
		{
			_recentProductList = new int[1];
			_recentProductList[0] = productId;
		}
		else
		{
			int[] newProductList = new int[1];
			newProductList[0] = productId;
			for(int i = 0; i < _recentProductList.length; i++)
			{
				if(newProductList.length >= ProductHolder.MAX_ITEMS_IN_RECENT_LIST)
					break;

				int itemId = _recentProductList[i];
				if(ArrayUtils.contains(newProductList, itemId))
					continue;

				newProductList = ArrayUtils.add(newProductList, itemId);
			}

			_recentProductList = newProductList;
		}

		String valueToUpdate = "";
		for(int itemId : _recentProductList)
		{
			valueToUpdate += itemId + ";";
		}
		setVar(ProductHolder.RECENT_PRDCT_LIST_VAR, valueToUpdate, -1);
	}

	@Override
	public int getINT()
	{
		return Math.max(getTemplate().getMinAttr().getINT(), Math.min(getTemplate().getMaxAttr().getINT(), super.getINT()));
	}

	@Override
	public int getSTR()
	{
		return Math.max(getTemplate().getMinAttr().getSTR(), Math.min(getTemplate().getMaxAttr().getSTR(), super.getSTR()));
	}

	@Override
	public int getCON()
	{
		return Math.max(getTemplate().getMinAttr().getCON(), Math.min(getTemplate().getMaxAttr().getCON(), super.getCON()));
	}

	@Override
	public int getMEN()
	{
		return Math.max(getTemplate().getMinAttr().getMEN(), Math.min(getTemplate().getMaxAttr().getMEN(), super.getMEN()));
	}

	@Override
	public int getDEX()
	{
		return Math.max(getTemplate().getMinAttr().getDEX(), Math.min(getTemplate().getMaxAttr().getDEX(), super.getDEX()));
	}

	@Override
	public int getWIT()
	{
		return Math.max(getTemplate().getMinAttr().getWIT(), Math.min(getTemplate().getMaxAttr().getWIT(), super.getWIT()));
	}
	//pvp events variables
	@Override
	public boolean isInTvT()
	{
		return _InTvT;
	}

	@Override
	public boolean isInZombieVsHumans()
	{
		return _inZombieVsHumans;
	}

	public void setInZombieVsHumans(boolean value)
	{
		_inZombieVsHumans = value;
	}

	public boolean IsInGameOfThrone()
	{
		return _isInGameThrones;
	}	
	
	@Override
	public boolean isInCtF()
	{
		return _inCtF;
	}
	@Override
	public boolean isInLastHero() 
	{
		return _inLastHero;
	}
	
	public boolean isInPvPEvent() 
	{
		return !_inZombieVsHumans && !_InTvT && !_inCtF && !_inLastHero ? false : true; 
	}
	
	public void setIsInTvT(boolean param)
	{
		_InTvT = param;
	}

	public void setIsInGameOfThrone(boolean param)
	{
		_isInGameThrones = param;
	}
	
	public void setIsInCtF(boolean param) 
	{
		_inCtF = param;
	}
	
	public void setIsInLastHero(boolean param) 
	{
		_inLastHero = param;
	}
	
	public void setMacroSkill(Skill skill)
	{
		_macroSkill = skill;
	}
	
	public Skill getMacroSkill()
	{
		return _macroSkill;
	}

	public void setFriendsEvent(Player player)
	{
		_friends_event = player;
	}

	public void setMostalStavka(int stavka)
	{
		_stavka = stavka;
	}

	public void setMortalCoeficient(double coeficient)
	{
		_coeficient = coeficient;
	}

	public Player getFriendsEvent()
	{
		return _friends_event;
	}

	public int getMostalStavka()
	{
		return _stavka;
	}

	public double getMortalCoeficient()
	{
		return _coeficient;
	}	
	public void setIsVoting(boolean value)
	{
		_isVoting = value;
	}
	public boolean isVoting()
	{
		return _isVoting;
	}
	public boolean hasCTFflag()
	{
		return _hasFlagCTF;
	}
	public void setCTFflag(boolean set)
	{
		_hasFlagCTF = set;
	}

	public boolean isInSameParty(Player target)
	{
		return getParty() != null && target.getParty() != null && getParty() == target.getParty();
	}

	public boolean isInSameChannel(Player target)
	{
		Party activeCharP = getParty();
		Party targetP = target.getParty();
		if (activeCharP != null && targetP != null)
		{
			CommandChannel chan = activeCharP.getCommandChannel();
			if (chan != null && chan == targetP.getCommandChannel())
			{
				return true;
			}
		}
		return false;
	}	

	public boolean isInSameClan(Player target)
	{
		return getClanId() != 0 && getClanId() == target.getClanId();
	}
	
	public final boolean isInSameAlly(Player target)
	{
		return getAllyId() != 0 && getAllyId() == target.getAllyId();
	}		
	
	public void onHero(boolean hero)
	{
		if(!hero)
		{
			for(ItemInstance item : getInventory().getItems())
				if(item.isHeroWeapon() || item.getItemId() == 6842)
					getInventory().destroyItemByItemId(item.getItemId(), 1);
		}
	}
	private void restoreCursedWeapon()
	{
		for(ItemInstance item : getInventory().getItems())
			if(item.isCursed())
			{
				int skillLvl = CursedWeaponsManager.getInstance().getLevel(item.getItemId());
				if(item.getItemId() == 8190)
					addSkill(SkillHolder.getInstance().getSkill(3603, skillLvl), false);
				else if(item.getItemId() == 8689)
					addSkill(SkillHolder.getInstance().getSkill(3629, skillLvl), false);
			}
		updateStats();
	}

	/**
	 * Initializes his _botPunish object with the specified punish
	 * and for the specified time
	 * 
	 * @param punishType
	 * @param minsOfPunish
	 */
	private BotPunish _botPunish = null;
	
	public synchronized void setPunishDueBotting(Punish punishType, int minsOfPunish)
	{
		if(_botPunish == null)
			_botPunish = new BotPunish(punishType, minsOfPunish);
	}

	/**
	 * Returns the current object-representative player punish
	 * 
	 * @return
	 */
	public BotPunish getPlayerPunish()
	{
		return _botPunish;
	}

	/**
	 * Returns the type of punish being applied
	 * 
	 * @return
	 */
	public BotPunish.Punish getBotPunishType()
	{
		return _botPunish.getBotPunishType();
	}

	/**
	 * Will return true if the player has any bot punishment
	 * active
	 * 
	 * @return true if bot punished
	 */
	public boolean isBeingPunished()
	{
		return _botPunish != null;
	}

	/**
	 * Will end the punishment once a player attempt to
	 * perform any forbid action and his punishment has
	 * expired
	 */
	public void endPunishment()
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM bot_reported_punish WHERE charId = ?");
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(SQLException ignored)
			{}
		}
		_botPunish = null;
		this.sendMessage("Наказание снято. Не используйте больше ботов!"); // FIXME: CustomMessage
	}

	public void startFlagKickTask()
	{
		if(_flagKickTask != null)
			_flagKickTask.cancel(false);
		_flagKickTask = ThreadPoolManager.getInstance().schedule(new FlagKickTask(this), 10 * 60000);
	}

	public void stopFlagKickTask()
	{
		if(_flagKickTask != null)
			_flagKickTask.cancel(false);
		_flagKickTask = null;
	}
	
	public long _firstExp;
	/**
	 * Sets exp holded by the character on log in
	 * @param value
	 */
	public void setFirstExp(long value)
	{
		_firstExp = value;
	}

	
	/**
	 * Will return true if the player has gained exp
	 * since logged in
	 * @return
	 */
	public boolean hasEarnedExp()
	{
		if(getExp() - _firstExp != 0)
			return true;
		return false;
	}
	
	public void setEventTargetA(Player player)
	{
		player = _playerA;
	}
	
	public void setEventTargetB(Player player)
	{
		player = _playerB;
	}

	public Player getEventTargetA()
	{
		return _playerA;
	}	
	
	public Player getEventTargetB()
	{
		return _playerB;
	}	
	
	public void setIsInHunterClub(boolean val)
	{
		_isInHunterClub = val;
	}
	
	public void setIsInMassivePvp(boolean val)
	{
		_isInMassPvP = val;
	}
	
	public void setIsInCrumaRace(boolean val)
	{
		_isInRaceEvent = val;
	}
	
	@Override
	public boolean isInHunterClub()
	{
		return _isInHunterClub;
	}
	
	@Override
	public boolean isInMassPvp()
	{
		return _isInMassPvP;
	}	
	
	@Override
	public boolean isInCrumaRace()
	{
		return _isInRaceEvent;
	}
	
	@Override
	public boolean isInEventModelEvent()
	{
		return _isInHunterClub || _isInMassPvP || _isInRaceEvent;
	}
	
	private long _blockUntilTime = 0;
	public void setblockUntilTime(long time)
	{
		_blockUntilTime = time;
	}
	public long getblockUntilTime()
	{
		return _blockUntilTime;
	}
	public boolean isInAwayingMode()
	{
		return _awaying;
	}	
	public void setAwayingMode(boolean awaying)
	{
		_awaying = awaying;
	}	

	public int getPing()
	{
		return _ping;
	}
  
	public void setPing(int ping)
	{
		_ping = ping;
	}	
	
	private boolean _partyMatchingVisible = true;
  
	public void setPartyMatchingVisible()
	{
		_partyMatchingVisible = (!_partyMatchingVisible);
	}
  
	public boolean isPartyMatchingVisible()
	{
		return _partyMatchingVisible;
	}
//tw	
	private boolean _isInTownWar = false;
	
	public void setIsInTownWarEvent(boolean val)
	{
		_isInTownWar = val;
	}
	
	public boolean isInTownWarEvent()
	{
		return _isInTownWar;
	}
		
	public boolean isInBuffStore()
	{
		return getPrivateStoreType() == STORE_PRIVATE_BUFF;
	}	

	public void offlineBuffStore()
	{
		if(getHwidGamer() != null)
			getHwidGamer().removePlayer(this);  
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerCloseSocketPacket.STATIC);
			setNetConnection(null);
		}
    
		setOfflineMode(true);
    
		Party party = getParty();
		if(party != null)
		{
			if (isFestivalParticipant())
			{
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			}
			leaveParty();
		}
    
		if(getServitor() != null)
			getServitor().unSummon();
    
		CursedWeaponsManager.getInstance().doLogout(this);
    
		Olympiad.logoutPlayer(this);
    
		if(isInObserverMode())
		{
			if(getOlympiadObserveGame() == null)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode(true);
			_observerMode.set(0);
		}
    
		setNameColor(Config.BUFF_STORE_OFFLINE_NAME_COLOR, false);
		broadcastCharInfo();
    

		OfflineBuffersTable.getInstance().onLogout(this);
    

		stopWaterTask();
		stopPremiumAccountTask();
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();
    
		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("Error while storing Player Inventory", t);
		}
    
		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("Error while storing Player", t);
		}
	}
	
	public void setLfcGame(LfcManager game)
	{
		lfcGame = game;
	}
	public LfcManager getLfcGame()
	{
		return lfcGame;
	}
	
	public void setPendingLfcEnd(boolean pending)
	{
		_pending_lfc = pending;
	}
	public boolean getPendingLfcEnd()
	{
		return _pending_lfc;
	}
	public void setPendingLfcStart(boolean start)
	{
		_pending_lfc_start = start;
	}
	public boolean getPendingLfcStart()
	{
		return _pending_lfc_start;
	}
	
	public void checkAndCancelLfcArena(Arenas arena)
	{
		arena.setPlayerOne(null);
		arena.setPlayerTwo(null);
	}
	
	public void addQuickVar(String name, Object value)
	{
		if (quickVars.containsKey(name))
			quickVars.remove(name);
		quickVars.put(name, value);
	}
  
	public String getQuickVarS(String name, String... defaultValue)
	{
		if(!quickVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return (String)quickVars.get(name);
	}
	
	public boolean getQuickVarB(String name, boolean... defaultValue)
	{
		if(!quickVars.containsKey(name))
		{
			if (defaultValue.length > 0)
				return defaultValue[0];
			return false;
		}
		return((Boolean)quickVars.get(name)).booleanValue();
	}
  
	public int getQuickVarI(String name, int... defaultValue)
	{
		if(!quickVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return -1;
		}
		return((Integer)quickVars.get(name)).intValue();
	}
  
	public long getQuickVarL(String name, long... defaultValue)
	{
		if(!quickVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return -1L;
		}
		return((Long)quickVars.get(name)).longValue();
	}
  
	public Object getQuickVarO(String name, Object... defaultValue)
	{
		if(!quickVars.containsKey(name))
		{
			if(defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return quickVars.get(name);
	}
  
	public boolean containsQuickVar(String name)
	{
		return quickVars.containsKey(name);
	}
  
	public void deleteQuickVar(String name)
	{
		quickVars.remove(name);
	}	

	public long getResurrectionBuffBlockedTime()
	{
		return _resurrectionBuffBlockedTime;
	}
	
	public HwidGamer getHwidGamer()
	{
		return _gamer;
	}
  
	public void setHwidGamer(HwidGamer gamer)
	{
		_gamer = gamer;
	}	
	
	public void setHwidLock(String hwid)
	{
		_hwidLock = hwid;
	}
  
	public String getHwidLock()
	{
		return _hwidLock;
	}
	
	private void manageDropSpecial(Creature killer, int type)
	{	
		int remain_items;
		switch(type)
		{
			case 1: //white
				remain_items = Config.SPECIAL_PVP_REMAIN_ITEMS_WHITE;
				break;
			case 2: //purple
				remain_items = Config.SPECIAL_PVP_REMAIN_ITEMS_PURPLE;
				break;
			case 3: //red
				remain_items = Config.SPECIAL_PVP_REMAIN_ITEMS_RED;
				break;
			default:
				remain_items = -1;
		}
		if(remain_items == -1) //disabled
			return;
		
		if(remain_items == 0 && getEffectList().getEffectsBySkillId(Config.SPECIAL_PVP_SAVE_SKILL) == null) //max if one of them not as spec. won't drop all never, will reserv something
		{
			dropPvPSpecial(killer.getPlayer(), true, 0);
			return;
		}	
		dropPvPSpecial(killer.getPlayer(), false, remain_items);
	}
	
	private void dropPvPSpecial(Player player, boolean dropall, int countRemain)
	{
		if(dropall) //what can I do
		{
			getInventory().writeLock();
			try
			{
				for(ItemInstance item : getInventory().getItems())
				{
					if(!ItemFunctions.checkIfCanDiscard(this, item))
						continue;	
					if(Config.NON_DROPABLE_PVP_ZONES.contains(item.getItemId()))
						continue;					
					if(item.isAugmented())
					{
						item.setVariationStoneId(0);
						item.setVariation1Id(0);
						item.setVariation2Id(0);
					}

					item = getInventory().removeItem(item);
					Log.LogEvent(getName(), getIP(), "PickUpItem", "player pvp drop item: "+getName()+" dropped item: "+item.getCount()+" of "+item.getItemId()+"");

					if(item.getEnchantLevel() > 0)
						sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
					else
						sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

					item.dropToTheGround(player, null, true);
				}
			}
			finally
			{
				getInventory().writeUnlock();
			}	
			return;
		}
		//
		getInventory().writeLock();
		try
		{
			int addon = 0;
			List<Effect> eff = getEffectList().getEffectsBySkillId(Config.SPECIAL_PVP_SAVE_SKILL);
			if(eff != null && !eff.isEmpty())
				addon = eff.get(0).getSkill().getLevel();
				
			for(ItemInstance item : getInventory().generateItemListWithoutEquipped())
			{	
				if(item.isAugmented())
				{
					item.setVariationStoneId(0);
					item.setVariation1Id(0);
					item.setVariation2Id(0);
				}

				item = getInventory().removeItem(item);
				Log.LogEvent(getName(), getIP(), "PickUpItem", "player pvp drop item: "+getName()+" dropped item: "+item.getCount()+" of "+item.getItemId()+"");

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				item.dropToTheGround(player, null, true);
			}				
			for(ItemInstance item : getInventory().generateRandomEquipItemList(countRemain + addon))
			{	
				if(item.isAugmented())
				{
					item.setVariationStoneId(0);
					item.setVariation1Id(0);
					item.setVariation2Id(0);
				}

				item = getInventory().removeItem(item);
				Log.LogEvent(getName(), getIP(), "PickUpItem", "player pvp drop item: "+getName()+" dropped item: "+item.getCount()+" of "+item.getItemId()+"");

				if(item.getEnchantLevel() > 0)
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));

				item.dropToTheGround(player, null, true);
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}		
	}

	public boolean checkActiveToggleEffects()
	{
		boolean dispelled = false;
		for(Effect effect : getEffectList().getAllEffects())
		{
			Skill skill = effect.getSkill();
			if(skill == null)
				continue;

			if(!skill.isToggle())
				continue;
			
			if(SkillAcquireHolder.getInstance().isSkillPossible(this, null, skill, true)) //since toggle skills cannot be buffed or got elsewhere.
				continue;

			effect.exit();
			dispelled = true;
		}
		return dispelled;
	}

	private class P2PAccountTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			final int leftTime = (int) ((_p2pAccountFinishTime - System.currentTimeMillis()) / 1000L);
			final int days = leftTime / 60 / 60 / 24;
			final int hours = leftTime / 60 / 60 - (days * 24);
			final int minutes = leftTime / 60 - (days * 24 * 60) - (hours * 60);
			final int second = leftTime - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
			if(leftTime > 0)
			{
				if(days > 0 && minutes == 0 && second == 0)
				{
					sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.P2PAccountTask.1", Player.this).add(days).add(hours).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
					return;
				}
				if(hours > 0 && minutes == 0 && second == 0)
				{
					sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.P2PAccountTask.2", Player.this).add(hours).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
					return;
				}
				if(minutes > 0 && second == 0)
				{
					sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.P2PAccountTask.3", Player.this).add(minutes).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
					return;
				}
				if(second > 0)
				{
					sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.P2PAccountTask.4", Player.this).add(second).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
					return;
				}
			}
			sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.P2PAccountTask.5", Player.this).toString(), 3600000, ScreenMessageAlign.MIDDLE_CENTER, false));
			kick();
		}
	}

	private long _p2pAccountFinishTime;
	private ScheduledFuture<?> _p2pAccountTask;

	public void checkP2PPeriod()
	{
		if(isGM())
			return;

		if(Config.FREE_GAME_TIME_PERIOD == -1)
			return;

		_p2pAccountFinishTime = System.currentTimeMillis();

		String varValue = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT period FROM p2p_accounts WHERE account=?");
			statement.setString(1, getHWID());
			rset = statement.executeQuery();
			if(rset.next())
				varValue = rset.getString("period");
		}
		catch(Exception e)
		{
			_log.info("Player.checkP2PPeriod(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		if(varValue == null || varValue.isEmpty())
		{
			if(Config.FREE_GAME_TIME_PERIOD > 0)
			{
				_p2pAccountFinishTime += Config.FREE_GAME_TIME_PERIOD * 60 * 60 * 1000L;

				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("REPLACE INTO p2p_accounts(account, period) VALUES (?,?)");
					statement.setString(1, getHWID());
					statement.setInt(2, (int) (_p2pAccountFinishTime / 1000L));
					statement.execute();
				}
				catch(Exception e)
				{
					_log.info("Player.checkP2PPeriod(): " + e, e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}

				sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.checkP2PPeriod.1", this).add(Config.FREE_GAME_TIME_PERIOD).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
			}
			else
			{
				_p2pAccountFinishTime += 30 * 1000L;
				sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.checkP2PPeriod.2", this).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
				block();
			}
		}
		else
		{
			_p2pAccountFinishTime = Integer.parseInt(varValue) * 1000L;
			if(_p2pAccountFinishTime < System.currentTimeMillis())
			{
				_p2pAccountFinishTime = System.currentTimeMillis() + 30 * 1000L;
				sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.checkP2PPeriod.2", this).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
				block();
			}
			else
				sendPacket(new ExShowScreenMessage(new CustomMessage("l2s.gameserver.model.Player.checkP2PPeriod.3", this).add(TimeUtils.toSimpleFormat(_p2pAccountFinishTime)).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, false));
		}

		_p2pAccountTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new P2PAccountTask(), 1000L, 1000L);
	}

	private void stopP2pAccountTask()
	{
		if(_p2pAccountTask != null)
		{
			_p2pAccountTask.cancel(false);
			_p2pAccountTask = null;		
		}
	}

	public void giveGMSkills()
	{
		if(!isGM())
			return;

		for(SkillLearn sl : SkillAcquireHolder.getInstance().getAvailableMaxLvlSkills(this, AcquireType.GM))
		{
			Skill skill = SkillHolder.getInstance().getSkill(sl.getId(), sl.getLevel());
			if(skill == null)
				continue;

			if(getSkillLevel(skill.getId()) < skill.getLevel())
				addSkill(skill, true);
		}
	}

	public final String getVisibleName(Player receiver)
	{
		if(isCursedWeaponEquipped())
		{
			String cursedName = getCursedWeaponName(receiver);
			if(cursedName == null || cursedName.isEmpty())
				return getName();

			return cursedName;
		}

		return getName();
	}

	public final String getVisibleTitle(Player receiver)
	{
		if(isCursedWeaponEquipped())
			return "";

		if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE && !isInBuffStore())
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return "";

			if(getReflection() == ReflectionManager.PARNASSUS)
				return "";
		}

		if(isInAwayingMode())
		{
			String awayText = AwayManager.getInstance().getAwayText(this);
			if(awayText == null || awayText.length() <= 1)
				return isLangRus() ? "<Отошел>" : "<Away>";
			else
				return (isLangRus() ? "<Отошел>" : "<Away>") + " - " + awayText + "*";
		}

		return getTitle();
	}

	public final int getVisibleNameColor(Player receiver)
	{
		return getNameColor();
	}

	public final int getVisibleTitleColor(Player receiver)
	{
		if(isInAwayingMode())
			return Config.AWAY_TITLE_COLOR;

		return getTitleColor();
	}

	public final boolean isPledgeVisible(Player receiver)
	{
		if(isCursedWeaponEquipped())
			return false;

		if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE && !isInBuffStore())
		{
			if(getReflection() == ReflectionManager.GIRAN_HARBOR)
				return false;

			if(getReflection() == ReflectionManager.PARNASSUS)
				return false;
		}

		return true;
	}

	public OptionDataTemplate addOptionData(OptionDataTemplate optionData)
	{
		if(optionData == null)
			return null;

		OptionDataTemplate oldOptionData = _options.get(optionData.getId());
		if(optionData == oldOptionData)
			return oldOptionData;

		_options.put(optionData.getId(), optionData);

		addTriggers(optionData);
		addStatFuncs(optionData.getStatFuncs(optionData));

		for(Skill skill : optionData.getSkills())
			addSkill(skill);

		return oldOptionData;
	}

	public OptionDataTemplate removeOptionData(int id)
	{
		OptionDataTemplate oldOptionData = _options.remove(id);
		if(oldOptionData != null)
		{
			removeTriggers(oldOptionData);
			removeStatsOwner(oldOptionData);

			for(Skill skill : oldOptionData.getSkills())
				removeSkill(skill);
		}
		return oldOptionData;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);
	}

	@Override
	protected void onDespawn()
	{
		getAI().notifyEvent(CtrlEvent.EVT_DESPAWN);
		super.onDespawn();
	}

	public int getExpertiseIndex()
	{
		return getSkillLevel(239, 0);
	}

	//------------------------------------------------------------------------------------------------------------------

	private final ConcurrentHashMap<ListenerHookType, CopyOnWriteArraySet<ListenerHook>> scriptHookTypeList = new ConcurrentHashMap<>();

	public void addListenerHook(ListenerHookType type, ListenerHook hook)
	{
		if(!scriptHookTypeList.containsKey(type))
		{
			CopyOnWriteArraySet<ListenerHook> hooks = new CopyOnWriteArraySet<>();
			hooks.add(hook);
			scriptHookTypeList.put(type, hooks);
		}
		else
		{
			CopyOnWriteArraySet<ListenerHook> hooks = scriptHookTypeList.get(type);
			hooks.add(hook);
		}
	}

	public void removeListenerHookType(ListenerHookType type, ListenerHook hook)
	{
		if(scriptHookTypeList.containsKey(type))
		{
			Set<ListenerHook> hooks = scriptHookTypeList.get(type);
			hooks.remove(hook);
		}
	}

	public Set<ListenerHook> getListenerHooks(ListenerHookType type)
	{
		Set<ListenerHook> hooks = scriptHookTypeList.get(type);
		if(hooks == null)
		{
			hooks = Collections.emptySet();
		}
		return hooks;
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean isFakePlayer()
	{
		return getAI()!= null && getAI().isFake();
	}

	@Override
	public int getPAtk(Creature target)
	{
		return (int) (super.getPAtk(target) * Config.PLAYER_P_ATK_MODIFIER);
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return (int) (super.getMAtk(target, skill) * Config.PLAYER_M_ATK_MODIFIER);
	}

	public void setSelectedMultiClassId(ClassId classId)
	{
		_selectedMultiClassId = classId;
	}

	public ClassId getSelectedMultiClassId()
	{
		return _selectedMultiClassId;
	}

	public int getMaxSubClassCount()
	{
		return 4 + Config.ALT_GAME_SUB_ADD;
	}

	public AntiFlood getAntiFlood()
	{
		return _antiFlood;
	}

	@Override
	public boolean canConsumeSkillItem(Skill skill)
	{
		return true;
	}

	public void setLastEnchantItemTime(long val)
	{
		_lastEnchantItemTime = val;
	}

	public long getLastEnchantItemTime()
	{
		return _lastEnchantItemTime;
	}

	@Override
	public PlayerFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new PlayerFlags(this);
		return (PlayerFlags) _statuses;
	}

	private Boolean _dmgOnScreenEnable = null;

	public boolean isDmgOnScreenEnable()
	{
		if(!Config.ENABLE_DAM_ON_SCREEN)
			return false;

		if(_dmgOnScreenEnable == null)
			_dmgOnScreenEnable = getVarBoolean("damtxt", true);
		return _dmgOnScreenEnable;
	}

	public void setDmgOnScreenEnable(boolean value)
	{
		_dmgOnScreenEnable = value;

		if(!_dmgOnScreenEnable)
			setVar("damtxt", false);
		else
			unsetVar("damtxt");
	}

	@Override
	public boolean isHFClient()
	{
		if(getNetConnection() != null)
			return getNetConnection().isHFClient();
		return true;
	}
}