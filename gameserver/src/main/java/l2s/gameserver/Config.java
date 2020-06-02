package l2s.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.configuration.ExProperties;
import l2s.commons.net.nio.impl.SelectorConfig;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.network.authcomm.ServerType;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.velocity.VelocityVariable;

public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);

	public static final int NCPUS = Runtime.getRuntime().availableProcessors();

	/** Configuration files */
	public static final String ANTIFLOOD_CONFIG_FILE = "config/antiflood.properties";
	public static final String OTHER_CONFIG_FILE = "config/other.properties";
	public static final String RESIDENCE_CONFIG_FILE = "config/residence.properties";
	public static final String SPOIL_CONFIG_FILE = "config/spoil.properties";
	public static final String ALT_SETTINGS_FILE = "config/altsettings.properties";
	public static final String FORMULAS_CONFIGURATION_FILE = "config/formulas.properties";
	public static final String PVP_CONFIG_FILE = "config/pvp.properties";
	public static final String TELNET_CONFIGURATION_FILE = "config/telnet.properties";
	public static final String CONFIGURATION_FILE = "config/server.properties";
	public static final String AI_CONFIG_FILE = "config/ai.properties";
	public static final String GEODATA_CONFIG_FILE = "config/geodata.properties";
	public static final String EVENTS_CONFIG_FILE = "config/events.properties";
	public static final String SERVICES_FILE = "config/services.properties";
	public static final String OLYMPIAD = "config/olympiad.properties";
	public static final String EXT_FILE = "config/ext.properties";
	public static final String BBS_FILE = "config/bbs.properties";
	public static final String L2SCRIPTS_FILE = "config/l2scripts.properties";
	public static final String PVP_MANAGER_FILE = "config/pvp_manager.properties";
	public static final String NEW_CP_PANEL_FILE = "config/new_cfg_panel.properties";
	/* Zone: Dragon Valley */
	public static final String ZONE_DRAGONVALLEY_FILE = "config/zones/DragonValley.properties";
	/* Zone: Lair of Antharas */
	public static final String ZONE_LAIROFANTHARAS_FILE = "config/zones/LairOfAntharas.properties";	
	
	public static final String FAKE_PLAYERS_LIST = "config/fake_players.list";

	public static final String OLYMPIAD_DATA_FILE = "config/olympiad.properties";
	public static final String FIGHT_CLUB_FILE = "config/events/FightClub.properties";
	public static final String SCHEME_BUFFER_FILE = "config/npcbuffer.properties";
	public static final String BOT_FILE = "config/anti_bot_system.properties";
	public static final String ANUSEWORDS_CONFIG_FILE = "config/abusewords.txt";

	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";
	public static final String BUFF_STORE_CONFIG_FILE = "config/OfflineBuffer.properties";
	private static final String EVENT_PC_BANG_FILE = "config/PcBang.properties";
	private static final String SGUARD_FILE = "config/sguard.properties";
	//anti bot stuff

	/* Zone: Lair of Antharas */
    public static int DRAGONKNIGHT_2ND_D_CHANCE;
    public static int DRAGONKNIGHT_3ND_D_CHANCE;
    public static int BKARIK_D_M_CHANCE;
    public static int LOA_CIRCLE_MOB_UNSPAWN_TIME;
    /* Zone: Dragon Valley */
    public static int NECROMANCER_MS_CHANCE;
    public static double DWARRIOR_MS_CHANCE;
    public static double DHUNTER_MS_CHANCE;
    public static int BDRAKE_MS_CHANCE;
    public static int EDRAKE_MS_CHANCE;
	public static int DISSALOW_GLOBAL_CHATS_UNTIL_LEVEL;
	
	public static boolean ENABLE_ANTI_BOT_SYSTEM;
	public static int ASK_ANSWER_DELAY;
	public static int MINIMUM_TIME_QUESTION_ASK;
	public static int MAXIMUM_TIME_QUESTION_ASK;
	public static int MINIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAXIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAX_BOT_POINTS;
	public static int MINIMAL_BOT_RATING_TO_BAN;
	public static int AUTO_BOT_BAN_JAIL_TIME;
	public static boolean ANNOUNCE_AUTO_BOT_BAN;
	public static boolean ON_WRONG_QUESTION_KICK;
	
	public static int SPECIAL_ITEM_ID;
	public static long SPECIAL_ITEM_COUNT;
	
    /* Zone: Heine Fields */
    public static int ANCIENT_HERB_SPAWN_RADIUS;
    public static int ANCIENT_HERB_SPAWN_CHANCE;
    public static int ANCIENT_HERB_SPAWN_COUNT;
    public static int ANCIENT_HERB_RESPAWN_TIME;
    public static int ANCIENT_HERB_DESPAWN_TIME;
    public static List<Location> HEIN_FIELDS_LOCATIONS = new ArrayList<Location>();	
	public static List<RaidGlobalDrop> RAID_GLOBAL_DROP = new ArrayList<RaidGlobalDrop>();
	
	//offline buffer
	public static boolean BUFF_STORE_MP_ENABLED;
    public static boolean BUFF_STORE_ENABLED;
	public static double BUFF_STORE_MP_CONSUME_MULTIPLIER;
	public static boolean BUFF_STORE_ITEM_CONSUME_ENABLED;
	public static int BUFF_STORE_NAME_COLOR;
	public static int BUFF_STORE_TITLE_COLOR;
	public static int BUFF_STORE_OFFLINE_NAME_COLOR;
	public static List<Integer> BUFF_STORE_ALLOWED_CLASS_LIST;
	public static TIntSet BUFF_STORE_ALLOWED_SKILL_LIST = new TIntHashSet();
	
	public static boolean CAPTCHA_ALLOW;
	public static long CAPTCHA_ANSWER_SECONDS;
	public static long CAPTCHA_JAIL_SECONDS;
	public static long CAPTCHA_TIME_BETWEEN_TESTED_SECONDS;
	public static long CAPTCHA_TIME_BETWEEN_REPORTS_SECONDS;
	public static int CAPTCHA_MIN_LEVEL;	
//new stuff
	public static boolean CAN_BE_TRADED_NO_TARADEABLE;
	public static boolean CAN_BE_TRADED_NO_SELLABLE;
	public static boolean CAN_BE_TRADED_NO_STOREABLE;
	public static boolean CAN_BE_TRADED_SHADOW_ITEM;
	public static boolean CAN_BE_TRADED_HERO_WEAPON;
	public static boolean CAN_BE_CWH_IS_AUGMENTED;
	public static int MIN_ACADEM_POINT;
	public static int MAX_ACADEM_POINT;
	public static boolean CHAMPION_DROP_ONLY_ADENA;
	public static boolean ALT_CHAMPION_DROP_HERBS;
	public static boolean ALT_DAMAGE_INVIS;
	public static boolean SERVICES_PK_PVP_KILL_ENABLE;
	public static boolean SERVICES_PK_PVP_TIE_IF_SAME_IP;
	public static int SERVICES_PVP_KILL_REWARD_ITEM;
	public static long SERVICES_PVP_KILL_REWARD_COUNT;
	public static int SERVICES_PK_KILL_REWARD_ITEM;
	public static long SERVICES_PK_KILL_REWARD_COUNT;
	public static boolean ZONE_PVP_COUNT;
	public static boolean SIEGE_PVP_COUNT;
	public static int MULTIPLIER_DECREASE;
	public static boolean ALLOW_ARROW_INFINITELY;
	public static boolean SHOW_OFFLINE_MODE_IN_ONLINE;
	public static boolean ALT_ENABLE_MULTI_PROFESSION;
	public static boolean TRANS_SUBCLASS_SKILL_TO_MAIN;
	public static double SKILLS_MOB_CHANCE;
	public static double SKILLS_DEBUFF_MOB_CHANCE;
	public static int DISABLE_EFFECT_ON_LEVEL_DIFFERENCE;
	public static boolean ALT_SHOW_MONSTERS_LVL;
	public static boolean ALT_SHOW_MONSTERS_AGRESSION;
	public static double RATE_DROP_CHAMPION;
	public static double RATE_CHAMPION_DROP_ADENA;
	public static double ALT_VITALITY_NEVIT_UP_POINT;
	public static int ALT_CHAMPION_MIN_LEVEL;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_DROP_HERBS;
	public static boolean NO_RATE_RAIDBOSS;
	public static boolean NO_RATE_SIEGE_GUARD;
	public static boolean ALT_DROP_RATE;
	public static boolean NO_RATE_HERBS;
	public static boolean NO_RATE_ENCHANT_SCROLL;
	public static boolean NO_RATE_ATT;
	public static boolean NO_RATE_LIFE_STONE;
	public static boolean NO_RATE_CODEX_BOOK;
	public static boolean NO_RATE_FORGOTTEN_SCROLL;
	public static boolean FAKE_PLAYERS_SIT;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_HERBS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_EPOLET;
	public static boolean ENABLE_AUTO_HUNTING_REPORT;
	public static boolean SAVE_GM_SPAWN;
	public static int HELLBOUND_LEVEL;
	public static long MAX_PLAYER_CONTRIBUTION;
	public static boolean CASTLE_GENERATE_TIME_ALTERNATIVE;
	public static int CASTLE_GENERATE_TIME_LOW;
	public static int CASTLE_GENERATE_TIME_HIGH;
	public static boolean LOAD_CUSTOM_SPAWN;
	public static boolean OLYMPIAD_PLAYER_IP;
	public static boolean OLYMPIAD_PLAYER_HWID;
	public static int OLY_ENCHANT_LIMIT_JEWEL;
	public static boolean OLY_ENCH_LIMIT_ENABLE;
    public static int OLY_ENCHANT_LIMIT_WEAPON;
    public static int OLY_ENCHANT_LIMIT_ARMOR;	
	public static boolean SHIELD_SLAM_BLOCK_IS_MUSIC;
	public static boolean ALT_OLYMP_PERIOD_WEEKLY_MONDAY;
	//end new stuff
	public static int HTM_CACHE_MODE;
	public static boolean HTM_SHAPE_ARABIC;
	public static int SHUTDOWN_ANN_TYPE;
	public static long HERO_TIME_DELAY;

	public static String CLAN_DELETE_TIME;
	public static String CLAN_CHANGE_LEADER_TIME;
	public static int CLAN_MAX_LEVEL;
	public static int[] CLAN_LVL_UP_SP_COST;
	public static int[] CLAN_LVL_UP_RP_COST;
	public static int[] CLAN_LVL_UP_MIN_MEMBERS;
	public static long[][][][] CLAN_LVL_UP_ITEMS_REQUIRED;
	public static boolean[] CLAN_LVL_UP_NEED_DOMINION;

	public static double CLAN_REPUTATION_MOD_ON_FINISH_ACADEMY;
	public static double CLAN_REPUTATION_MOD_ON_SIEGE_WIN;
	public static double CLAN_REPUTATION_MOD_ON_RECEIVE_HERO;
	public static double CLAN_REPUTATION_MOD_ON_KILL_WAR_ENEMY;

	public static boolean AUTOACADEMY_ENABLED;

	// Buffer Scheme Sire
	public static boolean NpcBuffer_VIP;
	public static int NpcBuffer_VIP_ALV;
	public static boolean NpcBuffer_EnableBuff;
	public static boolean NpcBuffer_EnableScheme;
	public static boolean NpcBuffer_EnableHeal;
	public static boolean NpcBuffer_EnableBuffs;
	public static boolean NpcBuffer_EnableResist;
	public static boolean NpcBuffer_EnableSong;
	public static boolean NpcBuffer_EnableDance;
	public static boolean NpcBuffer_EnableChant;
	public static boolean NpcBuffer_EnableOther;
	public static boolean NpcBuffer_EnableSpecial;
	public static boolean NpcBuffer_EnableCubic;
	public static boolean NpcBuffer_EnableCancel;
	public static boolean NpcBuffer_EnableBuffSet;
	public static boolean NpcBuffer_EnableBuffPK;
	public static boolean NpcBuffer_EnableFreeBuffs;
	public static boolean NpcBuffer_EnableTimeOut;
	public static int NpcBuffer_TimeOutTime;
	public static int NpcBuffer_MinLevel;
	public static int NpcBuffer_PriceCancel;
	public static int NpcBuffer_PriceHeal;
	public static int NpcBuffer_PriceBuffs;
	public static int NpcBuffer_PriceResist;
	public static int NpcBuffer_PriceSong;
	public static int NpcBuffer_PriceDance;
	public static int NpcBuffer_PriceChant;
	public static int NpcBuffer_PriceOther;
	public static int NpcBuffer_PriceSpecial;
	public static int NpcBuffer_PriceCubic;
	public static int NpcBuffer_PriceSet;
	public static int NpcBuffer_PriceScheme;
	public static int NpcBuffer_MaxScheme;
	public static boolean SCHEME_ALLOW_FLAG;
	public static boolean IS_DISABLED_IN_REFLECTION;
	public static boolean ENABLE_NEW_PVP_SYSTEM;	
	
	public static int SPECIAL_PVP_REMAIN_ITEMS_WHITE;	
	public static int SPECIAL_PVP_REMAIN_ITEMS_PURPLE;	
	public static int SPECIAL_PVP_REMAIN_ITEMS_RED;	
	public static int SPECIAL_PVP_SAVE_SKILL;
	
	public static boolean ALLOW_AWAY_STATUS;
	public static boolean AWAY_ONLY_FOR_PREMIUM;
	public static int AWAY_TIMER;
	public static int BACK_TIMER;
	public static int AWAY_TITLE_COLOR;
	public static boolean AWAY_PLAYER_TAKE_AGGRO;
	public static boolean AWAY_PEACE_ZONE;
	

	public static boolean ALT_ENABLE_BOTREPORT;
	public static int ALT_MAIL_MIN_LVL;

	public static boolean UNSUMMON_SUMMONS_OLY;
	public static int OLY_WAIT_TO_TELEPORT;
	// END Scheme Buffer Sire

	public static String DATABASE_DRIVER;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static boolean DATABASE_AUTOUPDATE;
	public static long DELAY_HERO_CHAT_SERVICE;
	public static long STARTING_ADENA;
	public static boolean DISABLE_PARTY_LEAVE_INSTANCE;
	
	public static double RATE_HARVEST_COUNT;
	
	public static double REGEN_HP_REST_40_LOWER;
	public static double REGEN_REST;
	public static double REGEN_STAND;
	public static double REGEN_RUNNING;
	public static double ALT_SUMMONS_DAMAGE;
	//new event stuff won't be used unless client is never will use it again
	public static boolean EVENT_MODEL_ACTIVE;
	public static int DAY_OF_MAIN_EVENT_DATE;
	public static int MAIN_EVENT_START_DATE;
	public static int DAY_OF_MAIN_EVENT_DATE_END;
	public static int MAIN_EVENT_END_DATE_HOUR;
	
	public static int EVENT_MODEL1_DAY;
	public static int EVENT_MODEL1_HOUR;
	public static int EVENT_MODEL1_NUMBER;

	public static int EVENT_MODEL2_DAY;
	public static int EVENT_MODEL2_HOUR;	
	public static int EVENT_MODEL2_NUMBER;

	public static int EVENT_MODEL3_DAY;
	public static int EVENT_MODEL3_HOUR;	
	public static int EVENT_MODEL3_NUMBER;

	public static int EVENT_MODEL4_DAY;
	public static int EVENT_MODEL4_HOUR;	
	public static int EVENT_MODEL4_NUMBER;

	public static int EVENT_MODEL5_DAY;
	public static int EVENT_MODEL5_NUMBER;
	public static int EVENT_MODEL5_HOUR;
	
	public static int EVENT_MODEL6_DAY;
	public static int EVENT_MODEL6_HOUR;	
	public static int EVENT_MODEL6_NUMBER;

	public static int EVENT_MODEL7_DAY;
	public static int EVENT_MODEL7_HOUR;
	public static int EVENT_MODEL7_NUMBER;
	
	public static int EVENT_MODEL8_DAY;
	public static int EVENT_MODEL8_HOUR;
	public static int EVENT_MODEL8_NUMBER;
	
	public static int EVENT_MODEL9_DAY;
	public static int EVENT_MODEL9_HOUR;
	public static int EVENT_MODEL9_NUMBER;
	
	public static int EVENT_MODEL10_DAY;
	public static int EVENT_MODEL10_HOUR;
	public static int EVENT_MODEL10_NUMBER;
	
	public static int EVENT_MODEL11_DAY;
	public static int EVENT_MODEL11_HOUR;
	public static int EVENT_MODEL11_NUMBER;
	
	public static int EVENT_MODEL12_DAY;
	public static int EVENT_MODEL12_HOUR;
	public static int EVENT_MODEL12_NUMBER;
	
	public static int EVENT_MODEL13_DAY;
	public static int EVENT_MODEL13_HOUR;
	public static int EVENT_MODEL13_NUMBER;
	
	public static int EVENT_MODEL14_DAY;
	public static int EVENT_MODEL14_HOUR;
	public static int EVENT_MODEL14_NUMBER;
	
	public static int EVENT_MODEL15_DAY;
	public static int EVENT_MODEL15_NUMBER;
	public static int EVENT_MODEL15_HOUR;
	//break;
	public static int PLACES_TO_REWARD;
	//rewards
	public static int FIRST_PLACE_ID1;
	public static long FIRST_PLACE_COUNT1;
	public static int FIRST_PLACE_ID2;
	public static long FIRST_PLACE_COUNT2;
	
	public static int SECOND_PLACE_ID1;
	public static long SECOND_PLACE_COUNT1;
	public static int SECOND_PLACE_ID2;
	public static long SECOND_PLACE_COUNT2;

	public static int THIRD_PLACE_ID1;
	public static long THIRD_PLACE_COUNT1;
	public static int THIRD_PLACE_ID2;
	public static long THIRD_PLACE_COUNT2;

	public static int FOURTH_PLACE_ID1;
	public static long FOURTH_PLACE_COUNT1;
	public static int FOURTH_PLACE_ID2;
	public static long FOURTH_PLACE_COUNT2;

	public static int FIFTH_PLACE_ID1;
	public static long FIFTH_PLACE_COUNT1;
	public static int FIFTH_PLACE_ID2;
	public static long FIFTH_PLACE_COUNT2;

	public static int SIXTH_PLACE_ID1;
	public static long SIXTH_PLACE_COUNT1;
	public static int SIXTH_PLACE_ID2;
	public static long SIXTH_PLACE_COUNT2;

	public static int SEVENTH_PLACE_ID1;
	public static long SEVENTH_PLACE_COUNT1;
	public static int SEVENTH_PLACE_ID2;
	public static long SEVENTH_PLACE_COUNT2;

	public static int EIGHTH_PLACE_ID1;
	public static long EIGHTH_PLACE_COUNT1;
	public static int EIGHTH_PLACE_ID2;
	public static long EIGHTH_PLACE_COUNT2;

	public static int NINTH_PLACE_ID1;
	public static long NINTH_PLACE_COUNT1;
	public static int NINTH_PLACE_ID2;
	public static long NINTH_PLACE_COUNT2;

	public static int TENTH_PLACE_ID1;
	public static long TENTH_PLACE_COUNT1;
	public static int TENTH_PLACE_ID2;
	public static long TENTH_PLACE_COUNT2;	
	
	public static String[] EVENT_CrumaFighterBuffs;
	public static String[] EVENT_CrumaMageBuffs;	
	public static String[] EVENT_CrumaRewards;
	public static boolean EVENT_CrumaBuffPlayers;
	public static int MASS_PVP_MIN_LEVEL;
	public static int MASS_PVP_MAX_LEVEL;
	public static int MAX_CRUMA_MASS_PVP_PLAYERS;
	public static int MAX_PLACES_TO_REWARD;
	public static int RACE_MIN_LEVEL;
	public static int RACE_MAX_LEVEL;
	public static int MAX_CRUMA_RACE_PLAYERS;
	public static int RACE_KILL_ITEM_ID;
	public static long RACE_KILL_ITEM_COUNT;
	public static int HUNTER_CLUB_MIN_LEVEL;
	public static int HUNTER_CLUB_MAX_LEVEL;
	public static int MAX_HUNTER_CLUB_PLAYERS;
	
	//end of special event configs
	// Database additional options
	public static boolean AUTOSAVE;

	public static long USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;


	public static int EFFECT_TASK_MANAGER_COUNT;
	
	public static boolean LH_WINCHAR_HERO;
	
	//web serv
	public static String WEB_SERVER_ROOT;
	public static int WEB_SERVER_DELAY;
	
	public static boolean ALT_GAME_SUB_BOOK;
	
	public static boolean ENABLE_ALT_FAME_REWARD;
	public static long ALT_FAME_CASTLE;
	public static long ALT_FAME_FORTRESS;
	public static boolean DOM_SIEGE_EVERY_WEEK;
	
	public static boolean ENABLE_CUSTOM_HEROES;
	public static int MIN_LEVEL_TO_USE_SHOUT;

	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;

	public static int MAXIMUM_ONLINE_USERS;
	
	public static boolean EXPERTISE_PENALTY;
	
	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static boolean GUARD_CAN_KILL_AGGRO;
	
	public static int MAX_REFLECTIONS_COUNT;

	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;

	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static Pattern[] ABUSEWORD_LIST = {};
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;
	
	public static boolean GVG_LANG;
	
	public static boolean ALLOW_REFFERAL_SYSTEM;
	
	public static int REF_SAVE_INTERVAL;
	
	public static int MAX_REFFERALS_PER_CHAR;
	
	public static int MIN_ONLINE_TIME;
	
	public static int MIN_REFF_LEVEL;

	public static double REF_PERCENT_GIVE;

	public static boolean PREMIUM_ACCOUNT_ENABLED;
	public static boolean PREMIUM_ACCOUNT_BASED_ON_GAMESERVER;
	public static int FREE_PA_TYPE;
	public static int FREE_PA_DELAY;
	public static boolean ENABLE_FREE_PA_NOTIFICATION;
		
	public static boolean ENABLE_HELP;
	public static boolean ENABLE_HELLBOUND;
	public static boolean ENABLE_DELEVEL;
	public static boolean ENABLE_COMBINE_TALISMAN;
	public static boolean ENABLE_CFG;
	public static boolean ENABLE_OFFLINE;
	public static boolean ENABLE_REPAIR;
	public static boolean ENABLE_INFO;
	public static boolean ENABLE_WEDDING;
	public static boolean ENABLE_WHOAMI;
	public static boolean ENABLE_DEBUG;
	public static boolean ENABLE_ONLINE;
	public static boolean ENABLE_RELOCATE;
	public static boolean ENABLE_REFFERAL;
	public static boolean ENABLE_PASSWORD;
	public static boolean ENABLE_LOCK;
	public static boolean ENABLE_SECURITY;	
	public static boolean SIEGE_COMMAND_ENABLED;

	public static int SERVICES_DELEVEL_COUNT;
	public static int SERVICES_DELEVEL_ITEM;
	public static boolean SERVICES_DELEVEL_ENABLED;
	public static int SERVICES_DELEVEL_MIN_LEVEL;
	
	public static List<Integer> ITEM_LIST = new ArrayList<Integer>();
	
	public static List<Integer> DROP_ONLY_THIS = new ArrayList<Integer>();
	public static List<Integer> NON_DROPABLE_PVP_ZONES = new ArrayList<Integer>();

	public static int DELETE_SKILL_SERVICE_ITEM_ID;
	public static long DELETE_SKILL_SERVICE_ITEM_COUNT;
	
	public static boolean ENABLE_CERTAIN_DROP;
	public static boolean INCLUDE_RAID_DROP;
	public static boolean ENABLE_AUTO_BUFF;
	public static boolean ENABLE_LFC;
	public static boolean ENABLE_CERTAIN_DROP_INVIDUAL;

	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;

	public static boolean ALT_PARTY_RATE_FORMULA;
	public static int ALT_ADD_RECIPES;
	public static int ALT_MAX_ALLY_SIZE;

	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static double ALT_ABSORB_DAMAGE_MODIFIER;
	public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;

	public static double ALT_POLE_DAMAGE_MODIFIER;

	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static boolean ALT_USE_BOW_REUSE_MODIFIER;
	
	public static boolean TOW_INITED;

	public static boolean ALT_VITALITY_ENABLED;
	public static double ALT_VITALITY_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_RAID_BONUS;
	public static final int[] VITALITY_LEVELS = { 240, 2000, 13000, 17000, 20000 };

	public static boolean ALT_DEBUG_ENABLED;
	public static boolean ALT_DEBUG_PVP_ENABLED;
	public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
	public static boolean ALT_DEBUG_PVE_ENABLED;
	public static boolean ALLOW_KM_ALL_TO_ME;
	
	public static boolean ALLOW_PET_ATTACK_MASTER;
	public static boolean DISALLOW_PET_ACTIONS_IF_MASTER_DEAD;

	/** Thread pools size */
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;

	public static boolean ENABLE_RUNNABLE_STATS;

	/** Network settings */
	public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();

	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;

	/** Auto-loot for/from players with karma also? */
	public static boolean AUTO_LOOT_PK;

	/** Character name template */
	public static String CNAME_TEMPLATE;
	
	public static String APASSWD_TEMPLATE;

	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	
	public static int CNAME_MAXLEN = 32;

	/** Clan name template */
	public static String CLAN_NAME_TEMPLATE;

	/** Clan title template */
	public static String CLAN_TITLE_TEMPLATE;

	/** Ally name template */
	public static String ALLY_NAME_TEMPLATE;

	/** Global chat state */
	public static boolean GLOBAL_SHOUT;
	public static boolean GLOBAL_TRADE_CHAT;
	public static int CHAT_RANGE;
	public static int SHOUT_OFFSET;

	public static int CLAN_EXPELLED_MEMBER_PENALTY;
	public static int CLAN_LEAVED_ALLY_PENALTY;
	public static int CLAN_DISSOLVED_ALLY_PENALTY;
	public static int NEVIT_BONUS_TIME; // 180
	public static long ALLY_EXPELLED_MEMBER_PENALTY; //24
	public static boolean REQUIRE_SKILL_ITEM_TO_OPEN_CC;
	public static int CLAN_AIR_SHIP_MAX_FUEL;
	public static boolean ALLOW_FAKE_PLAYERS;
	public static int FAKE_PLAYERS_PERCENT;	
	public static boolean ALLOW_TOTAL_ONLINE;
	public static boolean ALLOW_ONLINE_PARSE;
	public static int FIRST_UPDATE;
	public static int DELAY_UPDATE;	
	public static boolean INFINITY_SHOT;
	
	public static boolean ENABLE_VOTE;
	public static String VOTE_ADDRESS;
  
	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;

	public static double ALT_RAID_RESPAWN_MULTIPLIER;

	public static boolean ALT_ALLOW_DROP_AUGMENTED;

	public static boolean ALT_GAME_UNREGISTER_RECIPE;

	/** Delay for announce SS period (in minutes) */
	public static int SS_ANNOUNCE_PERIOD;

	/** Petition manager */
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;

	/** Show mob stats/droplist to players? */
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALLOW_NPC_SHIFTCLICK;
	public static boolean SHOW_FULL_NPC_SHIFTCLICK;
	public static boolean ALLOW_VOICED_COMMANDS;
	public static boolean ALLOW_REVISION_COMMANDS;
	public static boolean ALLOW_AUTOHEAL_COMMANDS;

	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;

	public static int[] ALT_ALLOWED_PET_POTIONS;

	public static double SKILLS_CHANCE_MOD;
	public static double SKILLS_CHANCE_MIN;
	public static double SKILLS_CHANCE_POW;
	public static double SKILLS_CHANCE_CAP;
	public static double MONSTER_UD_CHANCE;
	public static boolean ALT_FORMULA_FOR_MOB_UD;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static int SKILLS_CAST_TIME_MIN;
	
	/** Титул при создании чара */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	
	public static boolean ALT_TELEPORTS_ONLY_FOR_GIRAN;

	/** Таймаут на использование social action */
	public static boolean ALT_SOCIAL_ACTION_REUSE;

	/** Отключение книг для изучения скилов */
	public static Set<AcquireType> DISABLED_SPELLBOOKS_FOR_ACQUIRE_TYPES;

	/** Alternative gameing - loss of XP on death */
	public static boolean ALT_GAME_DELEVEL;

	/** Разрешать ли на арене бои за опыт */
	public static boolean ALT_ARENA_EXP;

	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
	public static boolean ALT_KAMALOKA_ABYSS_REENTER;
	public static boolean ALT_KAMALOKA_LAB_REENTER;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;

	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	public static int ALT_MAMMON_EXCHANGE;
	public static int ALT_MAMMON_UPGRADE;
	public static boolean ALT_ALLOW_TATTOO;

	public static int ALT_BUFF_LIMIT;
	
	public static int MUSIC_LIMIT;
	
	public static int DEBUFF_LIMIT;
	
	public static int TRIGGER_LIMIT;

	public static int MULTISELL_SIZE;

	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
	public static boolean SERVICES_LOCK_ACCOUNT_IP;
	public static boolean SERVICES_ACC_MOVE_ENABLED;
	public static int SERVICES_ACC_MOVE_ITEM;
	public static int SERVICES_ACC_MOVE_PRICE;	
	
	public static boolean SERVICES_CHANGE_NICK_ALLOW_SYMBOL;	
	
	public static boolean SERVICES_CHANGE_TITLE_COLOR_ENABLED;
	public static String[] SERVICES_CHANGE_TITLE_COLOR_LIST;
	public static int SERVICES_CHANGE_TITLE_COLOR_PRICE;
	public static int SERVICES_CHANGE_TITLE_COLOR_ITEM;	
	
	public static boolean SERVICES_SUBCLASS_ACTIVATION_ENABLED;
	public static long SERVICES_SUBCLASS_ACTIVATION_PRICE;
	public static int SERVICES_SUBCLASS_ACTIVATION_ITEM;
	
	
	public static boolean SERVICES_CHANGE_PASSWORD;
	public static int PASSWORD_PAY_ID;
	public static long PASSWORD_PAY_COUNT;

	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;

	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;

	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;

	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;

	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;

	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;

	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;

	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;

	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;

	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;

	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;

	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;

	public static String SERVICES_SELLPETS;

	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static int SERVICES_OFFLINE_TRADE_ALLOW_ZONE;
	public static boolean OFFLINE_ONLY_IF_PREMIUM;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static long SERVICES_GIRAN_HARBOR_PRICE;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static long SERVICES_PARNASSUS_PRICE;

	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_ALT_LOTTERY_PRICE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;

	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;
	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static int HWID_LOCK_MASK;

	/** Olympaid Comptetition Period */
	public static long ALT_OLY_CPERIOD;
	/** Olympaid Weekly Period */
	public static long ALT_OLY_WPERIOD;
	/** Olympaid Validation Period */
	public static long ALT_OLY_VPERIOD;

	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	public static SchedulingPattern OLYMIAD_END_PERIOD_TIME;
	public static SchedulingPattern OLYMPIAD_START_TIME;

	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int TEAM_GAME_MIN;

	public static int[] OLY_CLASSED_GAMES_DAYS;

	public static int GAME_MAX_LIMIT;
	public static int GAME_CLASSES_COUNT_LIMIT;
	public static int GAME_NOCLASSES_COUNT_LIMIT;
	public static int GAME_TEAM_COUNT_LIMIT;

	public static int ALT_OLY_REG_DISPLAY;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_TEAM_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static boolean OLYMPIAD_SHOW_ENEMY_CLASS;

	public static long NONOWNER_ITEM_PICKUP_DELAY;
	public static long PVP_SPECIAL_NONOWNER_ITEM_PICKUP_DELAY;
	public static boolean ENABLE_SPECIAL_PVP_EXP_LOSS_SPECIAL_PERCENT;
	public static double PVP_LOSS_PERCENT_DEATH;
	
	public static int STARTING_LVL;
	public static int STARTING_SP;	

	/** Logging Chat Window */
	public static boolean LOG_CHAT;
	public static boolean TURN_LOG_SYSTEM;
	public static boolean USE_NEW_LOGGING_SYSTEM;
	
	public static boolean ALLOW_MACROS_REUSE_BUG;

	public static Map<Integer, PlayerAccess> gmlist = new HashMap<Integer, PlayerAccess>();

	/** Rate control */
	public static double DROP_CHANCE_MODIFIER;
	public static double SPOIL_CHANCE_MODIFIER;
	public static boolean DROP_LEVEL_PENALTY_ENABLED;
	public static boolean SPOIL_LEVEL_PENALTY_ENABLED;
	public static double RATE_QUESTS_REWARD;
	public static double RATE_QUESTS_DROP;
	public static double[] RATE_XP_BY_LVL;
	public static double[] RATE_SP_BY_LVL;
	public static int MAX_DROP_ITEMS_FROM_ONE_GROUP;

	public static boolean ALL_QUEST_NOT_RATED;
	public static boolean ALL_ITEM_QUEST_RATED;
	public static double RATE_QUESTS_ADENA_REWARD;
	
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_ITEMS;
	public static boolean ADENA_100_PERCENT;
	
	//new
	public static double RATE_DROP_COMMON_ITEMS;
	public static double RATE_DROP_SHADOW_ITEMS;
	public static double RATE_DROP_SEALED_ITEMS;
	public static double RATE_DROP_LIFE_STONES;
	public static double RATE_DROP_ENCHANT_SCROLLS;
	public static double RATE_DROP_FORGOTTEN_SCROLLS;
	public static double RATE_DROP_KEY_MATHETIRALS;
	public static double RATE_DROP_RECEPIES;
	public static double RATE_DROP_BELTS;
	public static double RATE_DROP_BRACELETS;
	public static double RATE_DROP_UNDERWEARS;
	public static double RATE_DROP_CLOAKS;
	public static double RATE_DROP_TALISMANS;
	public static double RATE_DROP_CODEX_BOOKS;
	public static double RATE_DROP_ATTRIBUTE_STONES;
	public static double RATE_DROP_ATTRIBUTE_CRYSTALS;
	public static double RATE_DROP_ATTRIBUTE_JEWELS;
	public static double RATE_DROP_ATTRIBUTE_ENERGY;
	public static double RATE_DROP_WEAPONS_ALL;
	public static double RATE_DROP_ARMOR_ALL;
	public static double RATE_DROP_ACCESSORY_ALL;
	public static double RATE_DROP_BY_GRADE_D;
	public static double RATE_DROP_BY_GRADE_C;
	public static double RATE_DROP_BY_GRADE_B;
	public static double RATE_DROP_BY_GRADE_A;
	public static double RATE_DROP_BY_GRADE_S;
	public static double RATE_DROP_BY_GRADE_S80;
	public static double RATE_DROP_BY_GRADE_S84;
	public static double RATE_DROP_BY_SLOT_RL_EAR;
	public static double RATE_DROP_BY_SLOT_NECK;
	public static double RATE_DROP_BY_SLOT_RL_FINGER;
	public static double RATE_DROP_BY_SLOT_HEAD;
	public static double RATE_DROP_BY_SLOT_R_HAND;
	public static double RATE_DROP_BY_SLOT_L_HAND;
	public static double RATE_DROP_BY_SLOT_GLOVES;
	public static double RATE_DROP_BY_SLOT_CHEST;
	public static double RATE_DROP_BY_SLOT_LEGS;
	public static double RATE_DROP_BY_SLOT_FEET;
	public static double RATE_DROP_BY_SLOT_BACK;
	public static double RATE_DROP_BY_SLOT_LR_HAND;
	public static double RATE_DROP_BY_SLOT_FULL_ARMOR;
	public static double RATE_DROP_BY_SLOT_HAIR;
	public static double RATE_DROP_BY_SLOT_HAIRALL;
	public static double RATE_DROP_BY_SLOT_BELT;
	
	public static int RATE_COUNT_MIN_MAX_LIFE_STONES;
	public static int RATE_COUNT_MIN_MAX_ENCHANT_SCROLLS;
	public static int RATE_COUNT_MIN_MAX_KEY_MATHERIAL;
	public static int RATE_COUNT_MIN_MAX_REPECIES;
	public static int RATE_COUNT_MIN_MAX_CODEX_BOOKS;
	public static int RATE_COUNT_MIN_MAX_ATTRIBUTE_STONES;
	
	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_SPOIL;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_EQUIPMENT;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_DROP_SIEGE_GUARD_PA;
	public static double RATE_MANOR;
	public static double RATE_FISH_DROP_COUNT;
	public static boolean RATE_PARTY_MIN;
	public static boolean IS_CCP_ENABLED;
	public static double RATE_HELLBOUND_CONFIDENCE;

	public static double RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;

	/** Player Drop Rate control */
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;

	public static int KARMA_DROP_ITEM_LIMIT;

	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;

	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;

	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;

	public static int DELETE_DAYS;

	public static int PURGE_BYPASS_TASK_FREQUENCY;

	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	public static File GEODATA_ROOT;

	public static double CLANHALL_BUFFTIME_MODIFIER;
	public static double BUFFTIME_MODIFIER;
	public static double SONGDANCETIME_MODIFIER;

	public static double MAXLOAD_MODIFIER;
	public static double GATEKEEPER_MODIFIER;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;

	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static int ALT_CHAMPION_TOP_LEVEL;

	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
	public static int MAIL_LIMIT_PER_DAY;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;

	/** protocol revision */
	public static TIntSet AVAILABLE_PROTOCOL_REVISIONS;

	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;

	public static boolean USE_CLIENT_LANG;
	public static boolean CAN_SELECT_LANGUAGE;
	public static Language DEFAULT_LANG;

	/** Время запланированного на определенное время суток рестарта */
	public static String RESTART_AT_TIME;

	public static int MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY;
	
	public static boolean RETAIL_MULTISELL_ENCHANT_TRANSFER;

	public static int REQUEST_ID;
	public static String EXTERNAL_HOSTNAME;
	public static int PORT_GAME;

	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;

	// Security
	public static boolean EX_SECOND_AUTH_ENABLED;
	public static int EX_SECOND_AUTH_MAX_ATTEMPTS;
	public static int EX_SECOND_AUTH_BAN_TIME;

	public static int ALLOWED_REBORN_COUNT;
	public static int REBORN_START_LEVEL;
	public static boolean CHANGE_CLASS_ON_REBORN;
	public static int[][] REBORN_REWARD_ITEMS;
	public static int[][] LAST_REBORN_RANDOM_REWARD_ITEMS;
	
	public static List<Integer> NON_RATED_QUESTS = new ArrayList<Integer>();

	public static TIntObjectMap<int[]> ALLOW_CLASS_MASTERS_LIST = new TIntObjectHashMap<int[]>();
	public static boolean ALLOW_EVENT_GATEKEEPER;

	public static boolean ITEM_BROKER_ITEM_SEARCH;

	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int QUEST_INVENTORY_MAXIMUM;

	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;

	public static int FREIGHT_SLOTS;

	/** Spoil Rates */
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;
	
	public static boolean SHOW_HTML_WELCOME;
	
	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;

	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	public static int KARMA_SP_DIVIDER;
	public static int KARMA_LOST_BASE;

	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;

	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();

	public static int PVP_TIME;

	/** Karma Punishment */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;

	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;

	public static boolean REGEN_SIT_WAIT;

	public static double RATE_RAID_REGEN;
	public static double RATE_RAID_DEFENSE;
	public static double RATE_RAID_ATTACK;
	public static double RATE_EPIC_DEFENSE;
	public static double RATE_EPIC_ATTACK;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;

	public static double ALT_PK_DEATH_RATE;

	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;

	/** telnet enabled */
	public static boolean IS_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;
	public static String TELNET_PASSWORD;
	public static String TELNET_HOSTNAME;
	public static int TELNET_PORT;

	/** Percent CP is restore on respawn */
	public static double RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static double RESPAWN_RESTORE_MP;

	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;

	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
	public static double SENDSTATUS_TRADE_MOD;
	public static int INTERVAL_FLAG_DROP;

	public static boolean ALLOW_CH_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	public static boolean ALT_CH_SIMPLE_DIALOG;

	public static int CH_BID_GRADE1_MINCLANLEVEL;
	public static int CH_BID_GRADE1_MINCLANMEMBERS;
	public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE2_MINCLANLEVEL;
	public static int CH_BID_GRADE2_MINCLANMEMBERS;
	public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE3_MINCLANLEVEL;
	public static int CH_BID_GRADE3_MINCLANMEMBERS;
	public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;

	public static boolean ANNOUNCE_MAMMON_SPAWN;

	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;

	/** AI */
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;

	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;
	public static long NONPVP_TIME_ONTELEPORT;

	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;
	
	public static int GM_LIM_MOVE;
	
	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;

	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
	
	public static int MAX_PIGS_52 ;
	public static int MAX_PIGS_70 ;
	public static int MAX_PIGS_80 ;
	public static int LAKFI_RESP_DELAY52 ;
	public static int LAKFI_RESP_DELAY_RND52 ;
	public static int LAKFI_RESP_DELAY70 ;
	public static int LAKFI_RESP_DELAY_RND70 ;
	public static int LAKFI_RESP_DELAY80 ;
	public static int LAKFI_RESP_DELAY_RND80 ;
	public static int MIN_ADENA_TO_EAT ;
	public static int TIME_IF_NOT_FEED ;
	public static int INTERVAL_EATING ;
	
	public static boolean HIDE_GM_STATUS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS; //Silence, gmspeed, etc...

	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FORGOTTEN_SKILLS;
	public static boolean CAN_RECEIVE_CERTIFICATION_WITHOUT_LEARN_PREVIOUS_SKILL;

	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;

	public static boolean DAMAGE_FROM_FALLING;

	/** Community Board */
	public static boolean BBS_ENABLED;
	public static String BBS_DEFAULT_PAGE;
	public static String BBS_COPYRIGHT;
	public static boolean BBS_WAREHOUSE_ENABLED;
	public static boolean BBS_SELL_ITEMS_ENABLED;
	
	/** Wedding Options */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;

	public static int FOLLOW_RANGE;

	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;

	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;

	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;

	/**limits of stats **/
	public static int LIM_PATK;
	public static int LIM_MATK;
	public static int LIM_PDEF;
	public static int LIM_MDEF;
	public static int LIM_MATK_SPD;
	public static int LIM_PATK_SPD;
	public static int LIM_CRIT_DAM;
	public static int LIM_CRIT;
	public static int LIM_MCRIT;
	public static int LIM_ACCURACY;
	public static int LIM_EVASION;
	public static int LIM_MOVE;
	public static int LIM_FAME;

	public static int MAX_HP_LIMIT;
	public static int MAX_MP_LIMIT;
	public static int MAX_CP_LIMIT;

	public static double PLAYER_P_ATK_MODIFIER;
	public static double PLAYER_M_ATK_MODIFIER;

	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;

	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static double FESTIVAL_RATE_PRICE;

	/** Dimensional Rift Config **/
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY; // Time in ms the party has to wait until the mobs spawn
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	
	public static boolean ALLOW_TALK_WHILE_SITTING;

	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;

	/** Разрешены ли клановые скилы? **/
	public static boolean ALLOW_CLANSKILLS;

	/** Разрешено ли изучение скилов трансформации и саб классов без наличия выполненного квеста */
	public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;

	/** Allow Manor system */
	public static boolean ALLOW_MANOR;

	/** Manor Refresh Starting time */
	public static int MANOR_REFRESH_TIME;

	/** Manor Refresh Min */
	public static int MANOR_REFRESH_MIN;

	/** Manor Next Period Approve Starting time */
	public static int MANOR_APPROVE_TIME;

	/** Manor Next Period Approve Min */
	public static int MANOR_APPROVE_MIN;

	/** Manor Maintenance Time */
	public static int MANOR_MAINTENANCE_PERIOD;

	public static double EVENT_CofferOfShadowsPriceRate;
	public static double EVENT_CofferOfShadowsRewardRate;

	public static double EVENT_APIL_FOOLS_DROP_CHANCE;

	//new events:
	
	public static int EVENT_LastHeroItemID;
	public static double EVENT_LastHeroItemCOUNT;
	public static boolean EVENT_LastHeroRate;
	public static double EVENT_LastHeroItemCOUNTFinal;
	public static boolean EVENT_LastHeroRateFinal;
	public static int EVENT_LHTime;
	public static String[] EVENT_LHStartTime;
	public static boolean EVENT_LHCategories;
	public static boolean EVENT_LHAllowSummons;
	public static boolean EVENT_LHAllowBuffs;
	public static boolean EVENT_LHAllowMultiReg;
	public static String EVENT_LHCheckWindowMethod;
	public static int EVENT_LHEventRunningTime;
	public static String[] EVENT_LHFighterBuffs;
	public static String[] EVENT_LHMageBuffs;
	public static boolean EVENT_LHBuffPlayers;
	public static boolean ALLOW_HEROES_LASTHERO;
	public static boolean LAST_HERO_HIDE_NAMES;
	public static String LAST_HERO_GLOBAL_NAME;
	
	
    public static String[] EVENT_TvTRewards;
	public static String[] EVENTS_DISALLOWED_SKILLS;
	public static int EVENT_TvTTime;
	public static boolean EVENT_TvT_rate;
	public static String[] EVENT_TvTStartTime;
	public static boolean EVENT_TvTCategories;
	public static int EVENT_TvTMaxPlayerInTeam;
	public static int EVENT_TvTMinPlayerInTeam;
	public static boolean EVENT_TvTAllowSummons;
	public static boolean EVENT_TvTAllowBuffs;
	public static boolean EVENT_TvTAllowMultiReg;
	public static String EVENT_TvTCheckWindowMethod;
	public static int EVENT_TvTEventRunningTime;
	public static String[] EVENT_TvTFighterBuffs;
	public static String[] EVENT_TvTMageBuffs;
	public static boolean EVENT_TvTBuffPlayers;
	public static boolean EVENT_TvTrate;
	public static boolean DISABLE_PARTY_ON_EVENT;
	public static boolean DISABLE_PARTY_ON_EVENT_TVT;
	public static boolean DISABLE_PARTY_ON_EVENT_LH;

	public static int EVENT_CtfTime;
	public static boolean EVENT_CtFrate;
	public static String[] EVENT_CtFStartTime;
	public static boolean EVENT_CtFCategories;
	public static int EVENT_CtFMaxPlayerInTeam;
	public static int EVENT_CtFMinPlayerInTeam;
	public static boolean EVENT_CtFAllowSummons;
	public static boolean EVENT_CtFAllowBuffs;
	public static boolean EVENT_CtFAllowMultiReg;
	public static String EVENT_CtFCheckWindowMethod;
	public static String[] EVENT_CtFFighterBuffs;
	public static String[] EVENT_CtFMageBuffs;
	public static boolean EVENT_CtFBuffPlayers;
	public static String[] EVENT_CtFRewards;
	public static int EVENT_CtFEventRunningTime;
	public static boolean EVENT_CtFOnDropSpawnFlagInStartLoc;
	
	//gvg
	
	public static int GvG_POINTS_FOR_BOX;
	public static int GvG_POINTS_FOR_BOSS;
	public static int GvG_POINTS_FOR_KILL;
	public static int GvG_POINTS_FOR_DEATH;
	public static int GvG_EVENT_TIME;
	public static long GvG_BOSS_SPAWN_TIME;
	public static int GvG_FAME_REWARD;
	public static int GvG_REWARD;
	public static long GvG_REWARD_COUNT;
	public static int GvG_ADD_IF_WITHDRAW;
	public static int GvG_HOUR_START;
	public static int GvG_MINUTE_START;
	public static int GVG_MIN_LEVEL;
	public static int GVG_MAX_LEVEL;
	public static int GVG_MAX_GROUPS;
	public static int GVG_MIN_PARTY_MEMBERS;
	public static long GVG_TIME_TO_REGISTER;
	
	public static int GVG_MAX_PARTY_MEMBERS;
	

	public static double EVENT_TFH_POLLEN_CHANCE;
	public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
	public static double EVENT_L2DAY_LETTER_CHANCE;
	public static double EVENT_CHANGE_OF_HEART_CHANCE;

	public static boolean EVENT_TREASURES_OF_THE_HERALD_ENABLE;
	public static int EVENT_TREASURES_OF_THE_HERALD_ITEM_ID;
	public static int EVENT_TREASURES_OF_THE_HERALD_ITEM_COUNT;
	public static int EVENT_TREASURES_OF_THE_HERALD_TIME;
	public static int EVENT_TREASURES_OF_THE_HERALD_MIN_LEVEL;
	public static int EVENT_TREASURES_OF_THE_HERALD_MAX_LEVEL;
	public static int EVENT_TREASURES_OF_THE_HERALD_MINIMUM_PARTY_MEMBER;
	public static int EVENT_TREASURES_OF_THE_HERALD_MAX_GROUP;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_BOX;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_BOSS;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_KILL;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_DEATH;
	
	public static double EVENT_TRICK_OF_TRANS_CHANCE;

	public static double EVENT_MARCH8_DROP_CHANCE;
	public static double EVENT_MARCH8_PRICE_RATE;

	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;

	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;

	
	public static boolean ALT_SAVE_ADMIN_SPAWN;
	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;

	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;

	public static boolean ALT_OPEN_CLOAK_SLOT;

	public static boolean ALT_SHOW_SERVER_TIME;

	/** Geodata config */
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static int REGION_EDGE_MAX_Z_DIFF;

	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static int PATHFIND_MAP_MUL;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;
	public static int NPC_PATH_FIND_MAX_HEIGHT;
	public static int PLAYABLE_PATH_FIND_MAX_HEIGHT;

	public static boolean DEBUG;
	public static boolean ALLOW_DUELS;
	
	public static boolean ENABLE_CAT_NEC_FREE_FARM;

	public static int WEAR_DELAY;

	public static boolean GOODS_INVENTORY_ENABLED = false;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;

	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;
	
	public static boolean EVENT_TVT_ARENA_ENABLED;
	public static int EVENT_TVT_ARENA_TECH_REASON;
	public static int EVENT_TVT_ARENA_NO_PLAYERS;
	public static int EVENT_TVT_ARENA_TEAM_DRAW;
	public static int EVENT_TVT_ARENA_TEAM_WIN;
	public static int EVENT_TVT_ARENA_TEAM_LOSS;
	public static boolean EVENT_TVT_ARENA_ALLOW_CLAN_SKILL;
	public static boolean EVENT_TVT_ARENA_ALLOW_HERO_SKILL;
	public static boolean EVENT_TVT_ARENA_ALLOW_BUFFS;
	public static int EVENT_TVT_ARENA_TEAM_COUNT;
	public static int EVENT_TVT_ARENA_TIME_TO_START;
	public static int EVENT_TVT_ARENA_TEAMLEADER_EXIT;
	public static int EVENT_TVT_ARENA_FIGHT_TIME;
	public static int[] EVENT_TVT_ARENA_DISABLED_ITEMS;
	public static int EVENT_TVT_ARENA_TEAM_COUNT_MIN;
	public static String[] EVENT_TVT_ARENA_START_TIME;
	public static String[] EVENT_TVT_ARENA_STOP_TIME;
	
	//new cp config
	public static boolean ALLOW_HWID_ENGINE;
	public static boolean ALLOW_CLEANING_AUTO_BANS;
	public static long SECONDS_BETWEEN_AUTO_BAN_CLEANING;
	public static int MAX_CHARS_PER_PC;
	public static boolean ENABLE_POLL_SYSTEM;
	public static int ANNOUNCE_POLL_EVERY_X_MIN;
	public static boolean ALLOW_SECOND_AUTH_CHECK;
	public static boolean ALLOW_SHOW_CHANGE_LOG;
	public static boolean ENABLE_NEW_CFG;
	
	//pvp manager
	public static boolean ALLOW_PVP_REWARD_SYSTEM;
	public static String[] PVP_REWARDS_ZONES;
	public static boolean PVP_REWARD_SEND_SUCC_NOTIF;
	public static int[] PVP_REWARD_REWARD_IDS;
	public static long[] PVP_REWARD_COUNTS;
	public static boolean PVP_REWARD_RANDOM_ONE;	
	public static int PVP_REWARD_DELAY_ONE_KILL;
	public static int PVP_REWARD_MIN_PL_PROFF;
	public static int PVP_REWARD_MIN_PL_UPTIME_MINUTE;
	public static int PVP_REWARD_MIN_PL_LEVEL;
	public static boolean PVP_REWARD_PK_GIVE;
	public static boolean PVP_REWARD_ON_EVENT_GIVE;
	public static boolean PVP_REWARD_ONLY_BATTLE_ZONE;
	public static boolean PVP_REWARD_ONLY_NOBLE_GIVE;
	public static boolean PVP_REWARD_SAME_PARTY_GIVE;
	public static boolean PVP_REWARD_SAME_CLAN_GIVE;
	public static boolean PVP_REWARD_SAME_ALLY_GIVE;
	public static boolean PVP_REWARD_SAME_HWID_GIVE;
	public static boolean PVP_REWARD_SAME_IP_GIVE;
	public static boolean PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER;
	public static int PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM;
	public static boolean PVP_REWARD_CHECK_EQUIP;
	public static int PVP_REWARD_WEAPON_GRADE_TO_CHECK;
	public static boolean PVP_REWARD_LOG_KILLS;

	public static int ALT_SELL_PRICE_DIV;
	public static boolean ALT_ENABLE_DISPELL_SONGS;
	public static boolean RAID_DROP_GLOBAL_ITEMS;
	public static int MIN_RAID_LEVEL_TO_DROP;

	// Fight Club
	public static boolean FIGHT_CLUB_ENABLED;
	public static int MINIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_DIFFERENCE;
	public static String[] ALLOWED_RATE_ITEMS;
	public static int PLAYERS_PER_PAGE;
	public static int ARENA_TELEPORT_DELAY;
	public static boolean CANCEL_BUFF_BEFORE_FIGHT;
	public static boolean UNSUMMON_PETS;
	public static boolean UNSUMMON_SUMMONS;
	public static boolean REMOVE_CLAN_SKILLS;
	public static boolean REMOVE_HERO_SKILLS;
	public static int TIME_TO_PREPARATION;
	public static int FIGHT_TIME;
	public static boolean ALLOW_DRAW;
	public static int TIME_TELEPORT_BACK;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN;
	public static boolean FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN;
	
	public static int ALT_GAME_START_LEVEL_TO_SUBCLASS;
	public static int MAX_PARTY_SIZE;
	public static boolean RETAIL_SS;
	public static int ATT_MOD_ARMOR;
	public static int ATT_MOD_WEAPON;
	public static int ATT_MOD_WEAPON1;
	public static int ATT_MOD_MAX_ARMOR;
	public static int ATT_MOD_MAX_WEAPON;
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean ADEPT_ENABLE;

	public static boolean ANNOUNCE_RAID_BOSS_RESPAWN;
	public static boolean ANNOUNCE_RAID_BOSS_DIE;
	public static boolean ANNOUNCE_EPIC_BOSS_RESPAWN;
	public static boolean ANNOUNCE_EPIC_BOSS_DIE;

	public static boolean ANNOUNCE_PVP_KILL;
	public static boolean ANNOUNCE_PK_KILL;

	public static boolean SERVICES_WASH_PK_ENABLED;
	public static int SERVICES_WASH_PK_ITEM;
	public static long SERVICES_WASH_PK_PRICE;
	public static boolean SERVICES_WASH_PK_CARMA_ENABLED;
	public static int SERVICES_WASH_PK_CARMA_ITEM;
	public static int SERVICES_WASH_PK_CARMA_PRICE;	
	public static boolean SERVICES_LVL_ENABLED;
	public static int SERVICES_LVL_UP_MAX;
	public static int SERVICES_LVL_UP_PRICE;
	public static int SERVICES_LVL_UP_ITEM;
	public static int SERVICES_LVL_DOWN_MAX;
	public static int SERVICES_LVL_DOWN_PRICE;
	public static int SERVICES_LVL_DOWN_ITEM;	
	public static boolean SERVICES_HERO_SELL_ENABLED;
	public static int[] SERVICES_HERO_SELL_DAY;
	public static int[] SERVICES_HERO_SELL_PRICE;
	public static int[] SERVICES_HERO_SELL_ITEM;	
	public static boolean ALWAYS_TELEPORT_HOME_RB;
	
	public static AbnormalEffect SERVICES_OFFLINE_ABNORMAL_EFFECT;
	public static int ALT_PCBANG_POINTS_MAX_CODE_ENTER_ATTEMPTS;
	public static boolean ALT_PCBANG_POINTS_ENABLED;
	public static boolean ALT_PCBANG_PA_ONLY;
	public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int ALT_PCBANG_POINTS_BONUS;
	public static int ALT_PCBANG_POINTS_DELAY;
	public static int ALT_PCBANG_POINTS_MIN_LVL;
	public static int ALT_PCBANG_POINTS_BAN_TIME;
	public static boolean PC_BANG_TO_ITEMMALL;
	public static int PC_BANG_TO_ITEMMALL_RATE;
	public static int PC_BANG_ENCHANT_MAX;
	public static int PC_BANG_SAFE_ENCHANT;
	public static int ALT_PCBANG_POINTS_ON_START;
	public static int ALT_MAX_PC_BANG_POINTS;
	public static int ALT_PC_BANG_WIVERN_PRICE;
	public static int ALT_PC_BANG_WIVERN_TIME;
	public static int OTHER_ITEM_MALL_MAX_BUY_COUNT;

	public static int ALT_DISALLOW_RECOMMEND_ON_SINGLE_DEVICE;

	public static Set<Language> AVAILABLE_LANGUAGES;

	public static int MAX_ACTIVE_ACCOUNTS_ON_ONE_IP;
	public static String[] MAX_ACTIVE_ACCOUNTS_IGNORED_IP;
	public static int MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID;

	public static int FREE_GAME_TIME_PERIOD;

	public static int CLAN_MIN_LEVEL;

	public static double BLEED_DEBUFF_CHANCE_MOD;
	public static double BOSS_DEBUFF_CHANCE_MOD;
	public static double DEATH_DEBUFF_CHANCE_MOD;
	public static double DERANGEMENT_DEBUFF_CHANCE_MOD;
	public static double ETC_DEBUFF_CHANCE_MOD;
	public static double GUST_DEBUFF_CHANCE_MOD;
	public static double HOLD_DEBUFF_CHANCE_MOD;
	public static double PARALYZE_DEBUFF_CHANCE_MOD;
	public static double PHYSICAL_BLOCKADE_DEBUFF_CHANCE_MOD;
	public static double POISON_DEBUFF_CHANCE_MOD;
	public static double SHOCK_DEBUFF_CHANCE_MOD;
	public static double SLEEP_DEBUFF_CHANCE_MOD;
	public static double VALAKAS_DEBUFF_CHANCE_MOD;

	public static int PDAM_TO_MONSTER_SUB_LVL_DIFF;
	public static int PDAM_TO_RAID_SUB_LVL_DIFF;
	public static int MDAM_TO_MONSTER_SUB_LVL_DIFF;
	public static int MDAM_TO_RAID_SUB_LVL_DIFF;

	public static boolean ALT_USE_TRANSFORM_IN_EPIC_ZONE;

	public static boolean ENABLE_AUCTION_SYSTEM;
	public static long AUCTION_FEE;
	public static int AUCTION_INACTIVITY_DAYS_TO_DELETE;
	public static boolean ALLOW_AUCTION_OUTSIDE_TOWN;
	public static int SECONDS_BETWEEN_ADDING_AUCTIONS;
	public static boolean AUCTION_PRIVATE_STORE_AUTO_ADDED;

	public static int FAKE_PLAYERS_COUNT;

	public static int ALT_CLAN_LEAVE_PENALTY_TIME;
	public static int ALT_CLAN_CREATE_PENALTY_TIME;

	public static boolean ENABLE_AUTO_ATTRIBUTE_SYSTEM;

	public static String[] ALLOWED_TRADE_ZONES;

	public static boolean MULTICLASS_SYSTEM_ENABLED;
	public static boolean MULTICLASS_SYSTEM_SHOW_LEARN_LIST_ON_OPEN_SKILL_LIST;
	public static double MULTICLASS_SYSTEM_NON_CLASS_SP_MODIFIER;
	public static double MULTICLASS_SYSTEM_1ST_CLASS_SP_MODIFIER;
	public static double MULTICLASS_SYSTEM_2ND_CLASS_SP_MODIFIER;
	public static double MULTICLASS_SYSTEM_3RD_CLASS_SP_MODIFIER;
	public static int MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID_BASED_ON_SP;
	public static int MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID_BASED_ON_SP;
	public static int MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID_BASED_ON_SP;
	public static int MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID_BASED_ON_SP;
	public static double MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
	public static double MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
	public static double MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
	public static double MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
	public static int MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID;
	public static int MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID;
	public static int MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID;
	public static int MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID;
	public static long MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT;
	public static long MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT;
	public static long MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT;
	public static long MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT;
	public static int MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER;

	public static int ALL_CHAT_USE_MIN_LEVEL;
	public static int ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int ALL_CHAT_USE_DELAY;
	public static int SHOUT_CHAT_USE_MIN_LEVEL;
	public static int SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int SHOUT_CHAT_USE_DELAY;
	public static int TRADE_CHAT_USE_MIN_LEVEL;
	public static int TRADE_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int TRADE_CHAT_USE_DELAY;
	public static int HERO_CHAT_USE_MIN_LEVEL;
	public static int HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int HERO_CHAT_USE_DELAY;
	public static int PRIVATE_CHAT_USE_MIN_LEVEL;
	public static int PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA;
	public static int PRIVATE_CHAT_USE_DELAY;
	public static int MAIL_USE_MIN_LEVEL;
	public static int MAIL_USE_MIN_LEVEL_WITHOUT_PA;
	public static int MAIL_USE_DELAY;

	@VelocityVariable
	public static boolean ENABLE_DAM_ON_SCREEN;
	public static int DAM_ON_SCREEN_FONT;
	public static int DAM_ON_SCREEN_FONT_COLOR_ATTACKER;
	public static int DAM_ON_SCREEN_FONT_COLOR_TARGET;

	public static boolean CAN_LEARN_SKILLS_FROM_INTERFACE;

	public static void loadServerConfig()
	{
		ExProperties serverSettings = load(CONFIGURATION_FILE);

		TOW_INITED	= serverSettings.getProperty("isTowProject", false);
		
		AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for(String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if(a.trim().isEmpty())
				continue;

			ServerType t = ServerType.valueOf(a.toUpperCase());
			AUTH_SERVER_SERVER_TYPE |= t.getMask();
		}
		
		WEB_SERVER_ROOT = serverSettings.getProperty("WebServerRoot", "./webserver/");
		WEB_SERVER_DELAY = serverSettings.getProperty("WebServerDelay", 10) * 1000;
	
		ENABLE_VOTE = serverSettings.getProperty("EnableVoteReward", false);
		VOTE_ADDRESS = serverSettings.getProperty("VoteAddress", "http://youaddress.com/check/StringTake.php?IP=");
	
		EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);

		HIDE_GM_STATUS = serverSettings.getProperty("HideGMStatus", false);
		SHOW_GM_LOGIN = serverSettings.getProperty("ShowGMLogin", true);
		SAVE_GM_EFFECTS = serverSettings.getProperty("SaveGMEffects", false);

		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		APASSWD_TEMPLATE = serverSettings.getProperty("ApasswdTemplate", "[A-Za-z0-9]{5,16}");

		MAX_CHARACTERS_NUMBER_PER_ACCOUNT = serverSettings.getProperty("MAX_CHARACTERS_NUMBER_PER_ACCOUNT", 7);

		GLOBAL_SHOUT = serverSettings.getProperty("GlobalShout", false);
		GLOBAL_TRADE_CHAT = serverSettings.getProperty("GlobalTradeChat", false);
		CHAT_RANGE = serverSettings.getProperty("ChatRange", 1250);
		SHOUT_OFFSET = serverSettings.getProperty("ShoutOffset", 0);

		LOG_CHAT = serverSettings.getProperty("LogChat", false);
		TURN_LOG_SYSTEM = serverSettings.getProperty("GlobalLogging", true);
		USE_NEW_LOGGING_SYSTEM = serverSettings.getProperty("USE_NEW_LOGGING_SYSTEM", false);
		
		ALLOW_MACROS_REUSE_BUG = serverSettings.getProperty("AllowMacrosReuseBug", true);

		double RATE_XP = serverSettings.getProperty("RateXp", 1.);
		RATE_XP_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateXp = RATE_XP;
		for(int i = 1; i < RATE_XP_BY_LVL.length; i++)
		{
			double rate = serverSettings.getProperty("RateXpByLevel" + i, prevRateXp);
			RATE_XP_BY_LVL[i] = rate;
			if(rate != prevRateXp)
				prevRateXp = rate;
		}

		double RATE_SP = serverSettings.getProperty("RateSp", 1.);
		RATE_SP_BY_LVL = new double[Experience.LEVEL.length];
		double prevRateSp = RATE_SP;
		for(int i = 1; i < RATE_SP_BY_LVL.length; i++)
		{
			double rate = serverSettings.getProperty("RateSpByLevel" + i, prevRateSp);
			RATE_SP_BY_LVL[i] = rate;
			if(rate != prevRateSp)
				prevRateSp = rate;
		}

		DROP_CHANCE_MODIFIER = serverSettings.getProperty("DROP_CHANCE_MODIFIER", 1.);
		SPOIL_CHANCE_MODIFIER = serverSettings.getProperty("SPOIL_CHANCE_MODIFIER", 1.);

		DROP_LEVEL_PENALTY_ENABLED = serverSettings.getProperty("DROP_LEVEL_PENALTY_ENABLED", true);
		SPOIL_LEVEL_PENALTY_ENABLED = serverSettings.getProperty("SPOIL_LEVEL_PENALTY_ENABLED", true);

		RATE_QUESTS_REWARD = serverSettings.getProperty("RateQuestsReward", 1.);
		RATE_QUESTS_DROP = serverSettings.getProperty("RateQuestsDrop", 1.);
		RATE_QUESTS_ADENA_REWARD = serverSettings.getProperty("RateQuestsAdenaReward", 1.);
		ALL_QUEST_NOT_RATED = serverSettings.getProperty("AllQuestsNotRated", false);
		ALL_ITEM_QUEST_RATED = serverSettings.getProperty("AllItemsAreRated", false);
		RATE_CLAN_REP_SCORE = serverSettings.getProperty("RateClanRepScore", 1.);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = serverSettings.getProperty("RateClanRepScoreMaxAffected", 2);

		CLAN_REPUTATION_MOD_ON_FINISH_ACADEMY = serverSettings.getProperty("CLAN_REPUTATION_MOD_ON_FINISH_ACADEMY", 1.0);
		CLAN_REPUTATION_MOD_ON_SIEGE_WIN = serverSettings.getProperty("CLAN_REPUTATION_MOD_ON_SIEGE_WIN", 1.0);
		CLAN_REPUTATION_MOD_ON_RECEIVE_HERO = serverSettings.getProperty("CLAN_REPUTATION_MOD_ON_RECEIVE_HERO", 1.0);
		CLAN_REPUTATION_MOD_ON_KILL_WAR_ENEMY = serverSettings.getProperty("CLAN_REPUTATION_MOD_ON_KILL_WAR_ENEMY", 1.0);

		AUTOACADEMY_ENABLED = serverSettings.getProperty("AUTOACADEMY_ENABLED", false);

		MAX_DROP_ITEMS_FROM_ONE_GROUP = serverSettings.getProperty("MAX_DROP_ITEMS_FROM_ONE_GROUP", 1);
		RATE_DROP_ADENA = serverSettings.getProperty("RateDropAdena", 1.);
		ADENA_100_PERCENT = serverSettings.getProperty("Adena100PercentDrop", false);
		RATE_DROP_ITEMS = serverSettings.getProperty("RateDropItems", 1.);
		
		//detailed
		RATE_DROP_COMMON_ITEMS = serverSettings.getProperty("RateDropCommonItems", 1.);
		RATE_DROP_SHADOW_ITEMS = serverSettings.getProperty("RateDropShadowItems", 1.);
		RATE_DROP_SEALED_ITEMS = serverSettings.getProperty("RateDropSealedItems", 1.);
		RATE_DROP_LIFE_STONES = serverSettings.getProperty("RateDropLifeStoneItems", 1.);
		RATE_DROP_ENCHANT_SCROLLS = serverSettings.getProperty("RateDropEnchantScrollItems", 1.);
		RATE_DROP_FORGOTTEN_SCROLLS = serverSettings.getProperty("RateDropForgottenScrollsItems", 1.);
		RATE_DROP_KEY_MATHETIRALS = serverSettings.getProperty("RateDropKeyMatherialItems", 1.);
		RATE_DROP_RECEPIES = serverSettings.getProperty("RateDropRecepieItems", 1.);
		RATE_DROP_BELTS = serverSettings.getProperty("RateDropBeltItems", 1.);
		RATE_DROP_BRACELETS = serverSettings.getProperty("RateDropBracleteItems", 1.);
		RATE_DROP_UNDERWEARS = serverSettings.getProperty("RateDropUnderwareItems", 1.);
		RATE_DROP_CLOAKS = serverSettings.getProperty("RateDropCloakItems", 1.);
		RATE_DROP_TALISMANS = serverSettings.getProperty("RateDropTalismanItems", 1.);
		RATE_DROP_CODEX_BOOKS = serverSettings.getProperty("RateDropCodexBookItems", 1.);
		RATE_DROP_ATTRIBUTE_STONES = serverSettings.getProperty("RateDropAttributeStoneItems", 1.);
		RATE_DROP_ATTRIBUTE_CRYSTALS = serverSettings.getProperty("RateDropAttributeCrystalItems", 1.);
		RATE_DROP_ATTRIBUTE_JEWELS = serverSettings.getProperty("RateDropAttributeJewelsItems", 1.);
		RATE_DROP_ATTRIBUTE_ENERGY = serverSettings.getProperty("RateDropAttributeEnergyItems", 1.);
		RATE_DROP_WEAPONS_ALL = serverSettings.getProperty("RateDropWeaponsAllItems", 1.);
		RATE_DROP_ARMOR_ALL = serverSettings.getProperty("RateDropArmorAllItems", 1.);
		RATE_DROP_ACCESSORY_ALL = serverSettings.getProperty("RateDropAccessoryAllItems", 1.);
		RATE_DROP_BY_GRADE_D = serverSettings.getProperty("RateDropByGradeDItems", 1.);
		RATE_DROP_BY_GRADE_C = serverSettings.getProperty("RateDropByGradeCItems", 1.);
		RATE_DROP_BY_GRADE_B = serverSettings.getProperty("RateDropByGradeBItems", 1.);
		RATE_DROP_BY_GRADE_A = serverSettings.getProperty("RateDropByGradeAItems", 1.);
		RATE_DROP_BY_GRADE_S = serverSettings.getProperty("RateDropByGradeSItems", 1.);
		RATE_DROP_BY_GRADE_S80 = serverSettings.getProperty("RateDropByGradeS80Items", 1.);
		RATE_DROP_BY_GRADE_S84 = serverSettings.getProperty("RateDropByGradeS84Items", 1.);
		RATE_DROP_BY_SLOT_RL_EAR = serverSettings.getProperty("RateDropBySlotRLEaringItems", 1.);
		RATE_DROP_BY_SLOT_NECK = serverSettings.getProperty("RateDropBySlotNecklaceItems", 1.);
		RATE_DROP_BY_SLOT_RL_FINGER = serverSettings.getProperty("RateDropBySlotRLRingItems", 1.);
		RATE_DROP_BY_SLOT_HEAD = serverSettings.getProperty("RateDropBySlotHeadItems", 1.);
		RATE_DROP_BY_SLOT_R_HAND = serverSettings.getProperty("RateDropBySlotRHandItems", 1.);
		RATE_DROP_BY_SLOT_L_HAND = serverSettings.getProperty("RateDropBySlotLHandItems", 1.);
		RATE_DROP_BY_SLOT_GLOVES = serverSettings.getProperty("RateDropBySlotGlovesItems", 1.);
		RATE_DROP_BY_SLOT_CHEST = serverSettings.getProperty("RateDropBySlotChestItems", 1.);
		RATE_DROP_BY_SLOT_LEGS = serverSettings.getProperty("RateDropBySlotLegsItems", 1.);
		RATE_DROP_BY_SLOT_FEET = serverSettings.getProperty("RateDropBySlotFeetItems", 1.);
		RATE_DROP_BY_SLOT_BACK = serverSettings.getProperty("RateDropBySlotBackItems", 1.);
		RATE_DROP_BY_SLOT_LR_HAND = serverSettings.getProperty("RateDropBySlotLRHandItems", 1.);
		RATE_DROP_BY_SLOT_FULL_ARMOR = serverSettings.getProperty("RateDropBySlotFullArmorItems", 1.);
		RATE_DROP_BY_SLOT_HAIR = serverSettings.getProperty("RateDropBySlotHairItems", 1.);
		RATE_DROP_BY_SLOT_HAIRALL = serverSettings.getProperty("RateDropBySlotHairTwoSlotsItems", 1.);
		RATE_DROP_BY_SLOT_BELT = serverSettings.getProperty("RateDropBySlotBeltItems", 1.);		
		RATE_COUNT_MIN_MAX_LIFE_STONES = serverSettings.getProperty("RateCountMinMaxLifeStones", 1);	
		RATE_COUNT_MIN_MAX_ENCHANT_SCROLLS = serverSettings.getProperty("RateCountMinMaxEnchantScrolls", 1);	
		RATE_COUNT_MIN_MAX_KEY_MATHERIAL = serverSettings.getProperty("RateCountMinMaxKeyMatherials", 1);	
		RATE_COUNT_MIN_MAX_REPECIES = serverSettings.getProperty("RateCountMinMaxRecepies", 1);	
		RATE_COUNT_MIN_MAX_CODEX_BOOKS = serverSettings.getProperty("RateCountMinMaxCodexBooks", 1);	
		RATE_COUNT_MIN_MAX_ATTRIBUTE_STONES = serverSettings.getProperty("RateCountMinMaxAttributeStones", 1);		
				
		RATE_DROP_RAIDBOSS = serverSettings.getProperty("RateRaidBoss", 1.);
		RATE_DROP_SPOIL = serverSettings.getProperty("RateDropSpoil", 1.);
		NO_RATE_ITEMS = serverSettings.getProperty("NoRateItemIds", new int[] {
				6660,
				6662,
				6661,
				6659,
				6656,
				6658,
				8191,
				6657,
				10170,
				10314,
				16025,
				16026 });
		NO_RATE_EQUIPMENT = serverSettings.getProperty("NoRateEquipment", true);
		NO_RATE_KEY_MATERIAL = serverSettings.getProperty("NoRateKeyMaterial", true);
		NO_RATE_RECIPES = serverSettings.getProperty("NoRateRecipes", true);
		RATE_DROP_SIEGE_GUARD = serverSettings.getProperty("RateSiegeGuard", 1.);
		RATE_DROP_SIEGE_GUARD_PA = serverSettings.getProperty("RateSiegeGuardPA", 1.);
		RATE_MANOR = serverSettings.getProperty("RateManor", 1.);
		RATE_FISH_DROP_COUNT = serverSettings.getProperty("RateFishDropCount", 1.);
		RATE_PARTY_MIN = serverSettings.getProperty("RatePartyMin", false);
		RATE_HELLBOUND_CONFIDENCE = serverSettings.getProperty("RateHellboundConfidence", 1.);

		for(int id : serverSettings.getProperty("NonRatedQuestIDs", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				NON_RATED_QUESTS.add(id);

		for(int id : serverSettings.getProperty("IgnoreAllDropButThis", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				DROP_ONLY_THIS.add(id);
		INCLUDE_RAID_DROP = serverSettings.getProperty("RemainRaidDropWithNoChanges", false);
		ENABLE_CERTAIN_DROP = serverSettings.getProperty("EnableCertainDrop", false);
		ENABLE_CERTAIN_DROP_INVIDUAL = serverSettings.getProperty("EnableCertainDropInduvidial", false);
		RATE_MOB_SPAWN = serverSettings.getProperty("RateMobSpawn", 1.);
		RATE_MOB_SPAWN_MIN_LEVEL = serverSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = serverSettings.getProperty("RateMobMaxLevel", 100);

		RATE_RAID_REGEN = serverSettings.getProperty("RateRaidRegen", 1.);
		RATE_RAID_DEFENSE = serverSettings.getProperty("RateRaidDefense", 1.);
		RATE_RAID_ATTACK = serverSettings.getProperty("RateRaidAttack", 1.);
		RATE_EPIC_DEFENSE = serverSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
		RATE_EPIC_ATTACK = serverSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
		RAID_MAX_LEVEL_DIFF = serverSettings.getProperty("RaidMaxLevelDiff", 8);
		PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);

		IS_CCP_ENABLED = serverSettings.getProperty("isCCPProtectionExist", false);
		
		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
		PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);

		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
		MAIL_LIMIT_PER_DAY = serverSettings.getProperty("MAIL_LIMIT_PER_DAY", -1);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
		ALLOW_CURSED_WEAPONS = serverSettings.getProperty("AllowCursedWeapons", false);
		DROP_CURSED_WEAPONS_ON_KICK = serverSettings.getProperty("DropCursedWeaponsOnKick", false);

		AVAILABLE_PROTOCOL_REVISIONS = new TIntHashSet();
		AVAILABLE_PROTOCOL_REVISIONS.addAll(serverSettings.getProperty("AvailableProtocolRevisions", new int[0]));

		MIN_NPC_ANIMATION = serverSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = serverSettings.getProperty("MaxNPCAnimation", 90);

		SERVER_SIDE_NPC_NAME = serverSettings.getProperty("ServerSideNpcName", false);
		SERVER_SIDE_NPC_TITLE = serverSettings.getProperty("ServerSideNpcTitle", false);

		AUTOSAVE = serverSettings.getProperty("Autosave", true);

		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);

		DATABASE_DRIVER = serverSettings.getProperty("DATABASE_DRIVER", "com.mysql.cj.jdbc.Driver");

		String databaseHost = serverSettings.getProperty("DATABASE_HOST", "localhost");
		int databasePort = serverSettings.getProperty("DATABASE_PORT", 3306);
		String databaseName = serverSettings.getProperty("DATABASE_NAME", "l2game");

		DATABASE_URL = serverSettings.getProperty("DATABASE_URL", "jdbc:mysql://" + databaseHost + ":" + databasePort + "/" + databaseName + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
		DATABASE_LOGIN = serverSettings.getProperty("DATABASE_LOGIN", "root");
		DATABASE_PASSWORD = serverSettings.getProperty("DATABASE_PASSWORD", "");

		DATABASE_AUTOUPDATE = serverSettings.getProperty("DATABASE_AUTOUPDATE", false);

		DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
		DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
		DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);

		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);

		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);

		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);

		ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);

		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);

		CHAT_MESSAGE_MAX_LEN = serverSettings.getProperty("ChatMessageLimit", 1000);
		ABUSEWORD_BANCHAT = serverSettings.getProperty("ABUSEWORD_BANCHAT", false);
		int counter = 0;
		for(int id : serverSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[] { 0 }))
		{
			BAN_CHANNEL_LIST[counter] = id;
			counter++;
		}
		ABUSEWORD_REPLACE = serverSettings.getProperty("ABUSEWORD_REPLACE", false);
		ABUSEWORD_REPLACE_STRING = serverSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
		BANCHAT_ANNOUNCE = serverSettings.getProperty("BANCHAT_ANNOUNCE", true);
		BANCHAT_ANNOUNCE_FOR_ALL_WORLD = serverSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
		BANCHAT_ANNOUNCE_NICK = serverSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
		ABUSEWORD_BANTIME = serverSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);

		USE_CLIENT_LANG = serverSettings.getProperty("UseClientLang", false);
		CAN_SELECT_LANGUAGE = serverSettings.getProperty("CAN_SELECT_LANGUAGE", !USE_CLIENT_LANG);
		DEFAULT_LANG = Language.valueOf(serverSettings.getProperty("DefaultLang", "ENGLISH").toUpperCase());
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");
		SHIFT_BY = serverSettings.getProperty("HShift", 12);
		
		MAX_DISTRIBUTE_MEMBER_LEVEL_PARTY = serverSettings.getProperty("MaxDiffLevelInParty", 15);
		
		RETAIL_MULTISELL_ENCHANT_TRANSFER = serverSettings.getProperty("RetailMultisellItemExchange", true);
		
		SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
		MAP_MIN_Z = serverSettings.getProperty("MapMinZ", Short.MIN_VALUE);
		MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", Short.MAX_VALUE);

		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 200);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);

		DAMAGE_FROM_FALLING = serverSettings.getProperty("DamageFromFalling", true);

		ALLOW_WEDDING = serverSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = serverSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = serverSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = serverSettings.getProperty("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = serverSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = serverSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = serverSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = serverSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = serverSettings.getProperty("WeddingDivorceCosts", 20);

		DONTLOADSPAWN = serverSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = serverSettings.getProperty("StartWithoutQuest", false);

		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);

		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);

		HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", HtmCache.LAZY);
		HTM_SHAPE_ARABIC = serverSettings.getProperty("HtmShapeArabic", false);
		SHUTDOWN_ANN_TYPE = serverSettings.getProperty("ShutdownAnnounceType", Shutdown.OFFLIKE_ANNOUNCES);
		
		ALT_SAVE_ADMIN_SPAWN = serverSettings.getProperty("SaveAdminSpawn", false);

		RATE_CHANCE_DROP_ITEMS = serverSettings.getProperty("RateChanceDropItems", 1.);
		RATE_DROP_HERBS = serverSettings.getProperty("RateDropHerbs", 1.);
		NO_RATE_RAIDBOSS = serverSettings.getProperty("NoRateRaidBoss", false);
		NO_RATE_SIEGE_GUARD = serverSettings.getProperty("NoRateSiegeGuard", false);
		ALT_DROP_RATE = serverSettings.getProperty("AltFormulaDrop", true);
		NO_RATE_HERBS = serverSettings.getProperty("NoRateHerbs", true);
		NO_RATE_ENCHANT_SCROLL = serverSettings.getProperty("NoRateEnchantScroll", true);
		NO_RATE_ATT = serverSettings.getProperty("NoRateAtt", true);
		NO_RATE_LIFE_STONE = serverSettings.getProperty("NoRateLifeStone", true);
		NO_RATE_CODEX_BOOK = serverSettings.getProperty("NoRateCodex", true);
		NO_RATE_FORGOTTEN_SCROLL = serverSettings.getProperty("NoRateForgottenScroll", true);
		RATE_CHANCE_GROUP_DROP_ITEMS = serverSettings.getProperty("RateChanceGroupDropItems", 1.);
		RATE_CHANCE_DROP_HERBS = serverSettings.getProperty("RateChanceDropHerbs", 1.);
		RATE_CHANCE_SPOIL = serverSettings.getProperty("RateChanceSpoil", 1.);
		RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = serverSettings.getProperty("RateChanceSpoilWAA", 1.);
		RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = serverSettings.getProperty("RateChanceDropWAA", 1.);
		RATE_CHANCE_DROP_EPOLET = serverSettings.getProperty("RateChanceDropEpolets", 1.);

		AVAILABLE_LANGUAGES = new HashSet<Language>();
		AVAILABLE_LANGUAGES.add(Language.ENGLISH);
		AVAILABLE_LANGUAGES.add(Language.RUSSIAN);
		AVAILABLE_LANGUAGES.add(DEFAULT_LANG);

		if(USE_CLIENT_LANG || CAN_SELECT_LANGUAGE) {
			String[] availableLanguages = serverSettings.getProperty("AVAILABLE_LANGUAGES", new String[0], ";");
			for(String availableLanguage : availableLanguages) {
				Language lang = Language.valueOf(availableLanguage.toUpperCase());
				if(!lang.isCustom() || CAN_SELECT_LANGUAGE)
					AVAILABLE_LANGUAGES.add(lang);
			}
		}

		MAX_ACTIVE_ACCOUNTS_ON_ONE_IP = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_ON_ONE_IP", -1);
		MAX_ACTIVE_ACCOUNTS_IGNORED_IP = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_IGNORED_IP", new String[0], ";");
		MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID = serverSettings.getProperty("MAX_ACTIVE_ACCOUNTS_ON_ONE_HWID", -1);

		FAKE_PLAYERS_COUNT = serverSettings.getProperty("FAKE_PLAYERS_COUNT", 0);

		FREE_GAME_TIME_PERIOD = serverSettings.getProperty("FREE_GAME_TIME_PERIOD", -1);
	}

	public static void loadTelnetConfig()
	{
		ExProperties telnetSettings = load(TELNET_CONFIGURATION_FILE);

		IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
		TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
		TELNET_PORT = telnetSettings.getProperty("Port", 7000);
		TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
		TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
	}

	public static void loadDragonValleyZoneSettings() 
	{
        ExProperties properties = load(ZONE_DRAGONVALLEY_FILE);
        NECROMANCER_MS_CHANCE = properties.getProperty("NecromancerMSChance", 0);
        DWARRIOR_MS_CHANCE = properties.getProperty("DWarriorMSChance", 0.0);
        DHUNTER_MS_CHANCE = properties.getProperty("DHunterMSChance", 0.0);
        BDRAKE_MS_CHANCE = properties.getProperty("BDrakeMSChance", 0);
        EDRAKE_MS_CHANCE = properties.getProperty("EDrakeMSChance", 0);
    }

    public static void loadLairOfAntharasZoneSettings() 
	{
        ExProperties properties = load(ZONE_LAIROFANTHARAS_FILE);
        BKARIK_D_M_CHANCE = properties.getProperty("BKarikDMSChance", 0);
    }
	
	public static void loadResidenceConfig()
	{
		ExProperties residenceSettings = load(RESIDENCE_CONFIG_FILE);

		CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
		CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
		CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
		CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
		CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
		CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
		CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.);
		RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.);
		
		ENABLE_ALT_FAME_REWARD = residenceSettings.getProperty("AltEnableCustomFame", false);
		ALT_FAME_CASTLE = residenceSettings.getProperty("CastleFame", 125);
		ALT_FAME_FORTRESS = residenceSettings.getProperty("FortressFame", 31);
		DOM_SIEGE_EVERY_WEEK = residenceSettings.getProperty("DomminionSiegeEveryWeek", false);
	}

	public static void loadAntiFloodConfig()
	{
		ExProperties properties = load(ANTIFLOOD_CONFIG_FILE);

		ALL_CHAT_USE_MIN_LEVEL = properties.getProperty("ALL_CHAT_USE_MIN_LEVEL", 1);
		ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("ALL_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		ALL_CHAT_USE_DELAY = properties.getProperty("ALL_CHAT_USE_DELAY", 0);

		SHOUT_CHAT_USE_MIN_LEVEL = properties.getProperty("SHOUT_CHAT_USE_MIN_LEVEL", 1);
		SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("SHOUT_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		SHOUT_CHAT_USE_DELAY = properties.getProperty("SHOUT_CHAT_USE_DELAY", 0);

		TRADE_CHAT_USE_MIN_LEVEL = properties.getProperty("TRADE_CHAT_USE_MIN_LEVEL", 1);
		TRADE_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("TRADE_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		TRADE_CHAT_USE_DELAY = properties.getProperty("TRADE_CHAT_USE_DELAY", 0);

		HERO_CHAT_USE_MIN_LEVEL = properties.getProperty("HERO_CHAT_USE_MIN_LEVEL", 1);
		HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("HERO_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		HERO_CHAT_USE_DELAY = properties.getProperty("HERO_CHAT_USE_DELAY", 0);

		PRIVATE_CHAT_USE_MIN_LEVEL = properties.getProperty("PRIVATE_CHAT_USE_MIN_LEVEL", 1);
		PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("PRIVATE_CHAT_USE_MIN_LEVEL_WITHOUT_PA", 1);
		PRIVATE_CHAT_USE_DELAY = properties.getProperty("PRIVATE_CHAT_USE_DELAY", 0);

		MAIL_USE_MIN_LEVEL = properties.getProperty("MAIL_USE_MIN_LEVEL", 1);
		MAIL_USE_MIN_LEVEL_WITHOUT_PA = properties.getProperty("MAIL_USE_MIN_LEVEL_WITHOUT_PA", 1);
		MAIL_USE_DELAY = properties.getProperty("MAIL_USE_DELAY", 0);
	}

	public static void loadOtherConfig()
	{
		ExProperties otherSettings = load(OTHER_CONFIG_FILE);

		DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);

		/* Inventory slots limits */
		INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_GM = otherSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		QUEST_INVENTORY_MAXIMUM = otherSettings.getProperty("MaximumSlotsForQuests", 100);

		MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);

		/* Warehouse slots limits */
		WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = otherSettings.getProperty("MaximumFreightSlots", 10);

		ENCHANT_ATTRIBUTE_STONE_CHANCE = otherSettings.getProperty("EnchantAttributeChance", 50);
		ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = otherSettings.getProperty("EnchantAttributeCrystalChance", 30);

		REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);

		UNSTUCK_SKILL = otherSettings.getProperty("UnstuckSkill", true);

		/* Amount of HP, MP, and CP is restored */
		RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", 0.) / 100;
		RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65.) / 100;
		RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", 0.) / 100;

		/* Maximum number of available slots for pvt stores */
		MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);

		SENDSTATUS_TRADE_JUST_OFFLINE = otherSettings.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = otherSettings.getProperty("SendStatusTradeMod", 1.);
		
		INTERVAL_FLAG_DROP = otherSettings.getProperty("MinutesUntillFlagDissapearIfOut", 5);

		ANNOUNCE_MAMMON_SPAWN = otherSettings.getProperty("AnnounceMammonSpawn", true);

		GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "FFFFFF"));
		GM_HERO_AURA = otherSettings.getProperty("GMHeroAura", false);
		NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
		
		SHOW_HTML_WELCOME = otherSettings.getProperty("ShowHTMLWelcome", false);
		
		CAPTCHA_ALLOW = otherSettings.getProperty("AllowCaptcha", false);
		CAPTCHA_ANSWER_SECONDS = otherSettings.getProperty("CaptchaAnswerTime", 15L);
		CAPTCHA_JAIL_SECONDS = otherSettings.getProperty("CaptchaJailTime", 1800L);
		CAPTCHA_TIME_BETWEEN_TESTED_SECONDS = otherSettings.getProperty("CaptchaDelayBetweenCaptchas", 1800L);
		CAPTCHA_TIME_BETWEEN_REPORTS_SECONDS = otherSettings.getProperty("CaptchaReportDelay", 7200);
		CAPTCHA_MIN_LEVEL = otherSettings.getProperty("CaptchaMinLevel", 40);		
		OTHER_ITEM_MALL_MAX_BUY_COUNT = otherSettings.getProperty("ItemMallMaxBuyCount", 99);
	}

	public static void loadSpoilConfig()
	{
		ExProperties spoilSettings = load(SPOIL_CONFIG_FILE);

		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.);
		ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 00);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 00);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
		RATE_HARVEST_COUNT = spoilSettings.getProperty("RateHarvestCount", 1.);
	}

	public static void loadFormulasConfig()
	{
		ExProperties formulasSettings = load(FORMULAS_CONFIGURATION_FILE);

		SKILLS_CHANCE_MOD = formulasSettings.getProperty("SkillsChanceMod", 11.);
		SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 5.);
		SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 95.);
		
		MONSTER_UD_CHANCE = formulasSettings.getProperty("MonsterUDChance", 30.);
		
		ALT_FORMULA_FOR_MOB_UD = formulasSettings.getProperty("SimpleFormulaForMobUD", false);
		
		SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);

		ALT_ABSORB_DAMAGE_MODIFIER = formulasSettings.getProperty("AbsorbDamageModifier", 1.0);
		
		LIM_PATK = formulasSettings.getProperty("LimitPatk", 20000);
		LIM_MATK = formulasSettings.getProperty("LimitMAtk", 25000);
		LIM_PDEF = formulasSettings.getProperty("LimitPDef", 15000);
		LIM_MDEF = formulasSettings.getProperty("LimitMDef", 15000);
		LIM_PATK_SPD = formulasSettings.getProperty("LimitPatkSpd", 1500);
		LIM_MATK_SPD = formulasSettings.getProperty("LimitMatkSpd", 1999);
		LIM_CRIT_DAM = formulasSettings.getProperty("LimitCriticalDamage", 2000);
		LIM_CRIT = formulasSettings.getProperty("LimitCritical", 500);
		LIM_MCRIT = formulasSettings.getProperty("LimitMCritical", 20);
		LIM_ACCURACY = formulasSettings.getProperty("LimitAccuracy", 200);
		LIM_EVASION = formulasSettings.getProperty("LimitEvasion", 200);
		LIM_MOVE = formulasSettings.getProperty("LimitMove", 250);

		LIM_FAME = formulasSettings.getProperty("LimitFame", 50000);

		MAX_HP_LIMIT = formulasSettings.getProperty("MAX_HP_LIMIT", 40000);
		MAX_MP_LIMIT = formulasSettings.getProperty("MAX_MP_LIMIT", 40000);
		MAX_CP_LIMIT = formulasSettings.getProperty("MAX_CP_LIMIT", 100000);

		PLAYER_P_ATK_MODIFIER = formulasSettings.getProperty("PLAYER_P_ATK_MODIFIER", 1.0);
		PLAYER_M_ATK_MODIFIER = formulasSettings.getProperty("PLAYER_M_ATK_MODIFIER", 1.0);

		ALT_NPC_PATK_MODIFIER = formulasSettings.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = formulasSettings.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = formulasSettings.getProperty("NpcMaxHpModifier", 1.58);
		ALT_NPC_MAXMP_MODIFIER = formulasSettings.getProperty("NpcMapMpModifier", 1.11);

		ALT_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0);
		
		GM_LIM_MOVE = formulasSettings.getProperty("GmLimitMove", 1500);
		
		REGEN_HP_REST_40_LOWER = formulasSettings.getProperty("RegenHealthMultIfNoob", 6.0);
		REGEN_REST = formulasSettings.getProperty("RegenRestMult", 1.5);
		REGEN_STAND = formulasSettings.getProperty("RegenStandMult", 1.1);
		REGEN_RUNNING = formulasSettings.getProperty("RegenRunningMult", 0.7);	
		ALT_SUMMONS_DAMAGE = formulasSettings.getProperty("AltSummonDamMultiplier", 1.0);

		BLEED_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("BLEED_DEBUFF_CHANCE_MOD", 1.);
		BOSS_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("BOSS_DEBUFF_CHANCE_MOD", 1.);
		DEATH_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("DEATH_DEBUFF_CHANCE_MOD", 1.);
		DERANGEMENT_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("DERANGEMENT_DEBUFF_CHANCE_MOD", 1.);
		ETC_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("ETC_DEBUFF_CHANCE_MOD", 1.);
		GUST_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("GUST_DEBUFF_CHANCE_MOD", 1.);
		HOLD_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("HOLD_DEBUFF_CHANCE_MOD", 1.);
		PARALYZE_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("PARALYZE_DEBUFF_CHANCE_MOD", 1.);
		PHYSICAL_BLOCKADE_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("PHYSICAL_BLOCKADE_DEBUFF_CHANCE_MOD", 1.);
		POISON_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("POISON_DEBUFF_CHANCE_MOD", 1.);
		SHOCK_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("SHOCK_DEBUFF_CHANCE_MOD", 1.);
		SLEEP_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("SLEEP_DEBUFF_CHANCE_MOD", 1.);
		VALAKAS_DEBUFF_CHANCE_MOD = formulasSettings.getProperty("VALAKAS_DEBUFF_CHANCE_MOD", 1.);

		PDAM_TO_MONSTER_SUB_LVL_DIFF = formulasSettings.getProperty("PDAM_TO_MONSTER_SUB_LVL_DIFF", 0);
		PDAM_TO_RAID_SUB_LVL_DIFF = formulasSettings.getProperty("PDAM_TO_RAID_SUB_LVL_DIFF", 0);
		MDAM_TO_MONSTER_SUB_LVL_DIFF = formulasSettings.getProperty("MDAM_TO_MONSTER_SUB_LVL_DIFF", 0);
		MDAM_TO_RAID_SUB_LVL_DIFF = formulasSettings.getProperty("MDAM_TO_RAID_SUB_LVL_DIFF", 0);
	}

	public static void loadExtSettings()
	{
		ExProperties properties = load(EXT_FILE);

		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);

		EX_SECOND_AUTH_ENABLED = properties.getProperty("SecondAuthEnabled", false);
		EX_SECOND_AUTH_MAX_ATTEMPTS = properties.getProperty("SecondAuthMaxAttempts", 5);
		EX_SECOND_AUTH_BAN_TIME = properties.getProperty("SecondAuthBanTime", 480);
	}
	
	public static void loadl2scriptsSettings()
	{
		ExProperties properties = load(L2SCRIPTS_FILE);
		
		CAN_BE_TRADED_NO_TARADEABLE = properties.getProperty("CanBeTradedNoTradeable", false);
		CAN_BE_TRADED_NO_SELLABLE = properties.getProperty("CanBeTradedNoSellable", false);
		CAN_BE_TRADED_NO_STOREABLE = properties.getProperty("CanBeTradedNoStoreable", false);
		CAN_BE_TRADED_SHADOW_ITEM = properties.getProperty("CanBeTradedShadowItem", false);
		CAN_BE_TRADED_HERO_WEAPON = properties.getProperty("CanBeTradedHeroWeapon", false);
		CAN_BE_CWH_IS_AUGMENTED = properties.getProperty("CanBeCwhIsAugmented", false);
		MIN_ACADEM_POINT = properties.getProperty("MinAcademPoint", 190);
		MAX_ACADEM_POINT = properties.getProperty("MaxAcademPoint", 650);
		CHAMPION_DROP_ONLY_ADENA = properties.getProperty("ChampionDropOnlyAdena", false);
		ALT_CHAMPION_DROP_HERBS = properties.getProperty("AltChampionDropHerbs", false);
		ALT_DAMAGE_INVIS = properties.getProperty("InDamage", false);
		SERVICES_PK_PVP_KILL_ENABLE = properties.getProperty("PkPvPKillEnable", false);
		SERVICES_PK_PVP_TIE_IF_SAME_IP = properties.getProperty("PkPvPTieifSameIP", true);
		SERVICES_PVP_KILL_REWARD_ITEM = properties.getProperty("PvPkillRewardItem", 4037);
		SERVICES_PVP_KILL_REWARD_COUNT = properties.getProperty("PvPKillRewardCount", 1L);
		SERVICES_PK_KILL_REWARD_ITEM = properties.getProperty("PkkillRewardItem", 4037);
		SERVICES_PK_KILL_REWARD_COUNT = properties.getProperty("PkKillRewardCount", 1L);
		SIEGE_PVP_COUNT = properties.getProperty("SiegePvpCount", false);
		ZONE_PVP_COUNT = properties.getProperty("ZonePvpCount", false);
		MULTIPLIER_DECREASE = properties.getProperty("MultiplierDecrease", 1);
		ALLOW_ARROW_INFINITELY = properties.getProperty("AllowArrowInfinitely", false);
		SHOW_OFFLINE_MODE_IN_ONLINE = properties.getProperty("ShowOfflineTradeInOnline", false);
		ALT_ENABLE_MULTI_PROFESSION = properties.getProperty("AltEnableMultiProfession", false);
		TRANS_SUBCLASS_SKILL_TO_MAIN = properties.getProperty("TransferSubClassSkillToMain", false);
		SKILLS_MOB_CHANCE = properties.getProperty("SkillsMobChance", 0.5);
		SKILLS_DEBUFF_MOB_CHANCE = properties.getProperty("SkillsDebuffMobChance", 0.5);
		DISABLE_EFFECT_ON_LEVEL_DIFFERENCE = properties.getProperty("DisableEffectOnLevelDifference", -1);
		ALT_SHOW_MONSTERS_AGRESSION = properties.getProperty("AltShowMonstersAgression", false);
		ALT_SHOW_MONSTERS_LVL = properties.getProperty("AltShowMonstersLvL", false);
		ALT_VITALITY_NEVIT_UP_POINT = properties.getProperty("WebServerDelay", 10);
		FAKE_PLAYERS_SIT = properties.getProperty("FakePlayersSit", false);
		ENABLE_AUTO_HUNTING_REPORT = properties.getProperty("AllowAutoHuntingReport", true);
		SAVE_GM_SPAWN = properties.getProperty("SaveGmSpawn", false);
		HELLBOUND_LEVEL = properties.getProperty("HellboundLevel", 0);
		MAX_PLAYER_CONTRIBUTION = properties.getProperty("MaxPlayerContribution", 1000000);
		CASTLE_GENERATE_TIME_ALTERNATIVE = properties.getProperty("CastleGenerateAlternativeTime", false);
		CASTLE_GENERATE_TIME_LOW = properties.getProperty("CastleGenerateTimeLow", 46800000);
		CASTLE_GENERATE_TIME_HIGH = properties.getProperty("CastleGenerateTimeHigh", 61200000);
		LOAD_CUSTOM_SPAWN = properties.getProperty("LoadAddGmSpawn", false);
		OLYMPIAD_PLAYER_IP = properties.getProperty("OlympiadPlayerIp", false);	
		OLYMPIAD_PLAYER_HWID = properties.getProperty("OlympiadPlayerHWID", false);	
		OLY_ENCH_LIMIT_ENABLE = properties.getProperty("OlyEnchantLimit", false);
        OLY_ENCHANT_LIMIT_WEAPON = properties.getProperty("OlyEnchantLimitWeapon", 0);
        OLY_ENCHANT_LIMIT_ARMOR = properties.getProperty("OlyEnchantLimitArmor", 0);
        OLY_ENCHANT_LIMIT_JEWEL = properties.getProperty("OlyEnchantLimitJewel", 0);	
		SHIELD_SLAM_BLOCK_IS_MUSIC = properties.getProperty("ShieldSlamBlockIsMusic", false);		
		
		ALT_GAME_START_LEVEL_TO_SUBCLASS = properties.getProperty("AltStartLevelToSubclass", 40);
		MAX_PARTY_SIZE = properties.getProperty("MaxPartySize", 9);
		RETAIL_SS = properties.getProperty("Retail_SevenSigns", true);
		ATT_MOD_ARMOR = properties.getProperty("att_mod_Armor", 6);
		ATT_MOD_WEAPON = properties.getProperty("att_mod_Weapon", 5);
		ATT_MOD_WEAPON1 = properties.getProperty("att_mod_Weapon1", 20);
		ATT_MOD_MAX_ARMOR = properties.getProperty("att_mod_max_armor", 60);
		ATT_MOD_MAX_WEAPON = properties.getProperty("att_mod_max_weapon", 150);		
		ADEPT_ENABLE = properties.getProperty("EnableAdept", true);

		ANNOUNCE_RAID_BOSS_RESPAWN = properties.getProperty("ANNOUNCE_RAID_BOSS_RESPAWN", false);
		ANNOUNCE_RAID_BOSS_DIE = properties.getProperty("ANNOUNCE_RAID_BOSS_DIE", false);
		ANNOUNCE_EPIC_BOSS_RESPAWN = properties.getProperty("ANNOUNCE_EPIC_BOSS_RESPAWN", false);
		ANNOUNCE_EPIC_BOSS_DIE = properties.getProperty("ANNOUNCE_EPIC_BOSS_DIE", false);

		ANNOUNCE_PVP_KILL = properties.getProperty("ANNOUNCE_PVP_KILL", false);
		ANNOUNCE_PK_KILL = properties.getProperty("ANNOUNCE_PK_KILL", false);

		ENABLE_AUCTION_SYSTEM = properties.getProperty("EnableAuctionSystem", false);
		AUCTION_FEE = Integer.parseInt(properties.getProperty("AuctionFee", "10000"));
		AUCTION_INACTIVITY_DAYS_TO_DELETE = Integer.parseInt(properties.getProperty("AuctionInactivityDaysToDelete", "7"));
		ALLOW_AUCTION_OUTSIDE_TOWN = properties.getProperty("AuctionOutsideTown", false);
		SECONDS_BETWEEN_ADDING_AUCTIONS = Integer.parseInt(properties.getProperty("AuctionAddDelay", "30"));
		AUCTION_PRIVATE_STORE_AUTO_ADDED = properties.getProperty("AuctionPrivateStoreAutoAdded", true);

		ALLOWED_REBORN_COUNT = properties.getProperty("ALLOWED_REBORN_COUNT", 0);
		REBORN_START_LEVEL = properties.getProperty("REBORN_START_LEVEL", 1);
		CHANGE_CLASS_ON_REBORN = properties.getProperty("CHANGE_CLASS_ON_REBORN", true);
		REBORN_REWARD_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("REBORN_REWARD_ITEMS", "0-0"), ";", "-");
		LAST_REBORN_RANDOM_REWARD_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("LAST_REBORN_RANDOM_REWARD_ITEMS", "0-0"), ";", "-");
	}

	public static void loadBBSSettings()
	{
		ExProperties properties = load(BBS_FILE);

		BBS_ENABLED = properties.getProperty("ENABLED", true);
		BBS_DEFAULT_PAGE = properties.getProperty("DEFAULT_PAGE", "_bbshome");
		BBS_COPYRIGHT = properties.getProperty("COPYRIGHT", "(c) L2-Scripts.ru 2018");
		BBS_WAREHOUSE_ENABLED = properties.getProperty("WAREHOUSE_ENABLED", false);
		BBS_SELL_ITEMS_ENABLED = properties.getProperty("SELL_ITEMS_ENABLED", false);
	}

	public static void loadAltSettings()
	{
		ExProperties altSettings = load(ALT_SETTINGS_FILE);

		ALT_ENABLE_BOTREPORT = altSettings.getProperty("AltEnableBotReport", false);
		ALT_MAIL_MIN_LVL = altSettings.getProperty("AltMailMinLvl", 0);
		STARTING_LVL = altSettings.getProperty("StartingLvl", 1);
		STARTING_SP = altSettings.getProperty("StartingSP", 0);			
		MIN_LEVEL_TO_USE_SHOUT = altSettings.getProperty("MinLevelToUseShoutChat", 1);
		ENABLE_CUSTOM_HEROES = altSettings.getProperty("EnableCustomHeroes", false);
		DISABLE_PARTY_LEAVE_INSTANCE = altSettings.getProperty("DisableLeavingPartyInInstance", false);
		MAX_PIGS_52 = altSettings.getProperty("MaxPigsFor52Level", 10);
		MAX_PIGS_70 = altSettings.getProperty("MaxPigsFor70Level", 10);
		MAX_PIGS_80 = altSettings.getProperty("MaxPigsFor80Level", 11);
		LAKFI_RESP_DELAY52 = altSettings.getProperty("Lakfi52LevelResp", 1800);
		LAKFI_RESP_DELAY_RND52 = altSettings.getProperty("Lakfi52LevelRespRnd", 600);
		LAKFI_RESP_DELAY70 = altSettings.getProperty("Lakfi70LevelResp", 1800);
		LAKFI_RESP_DELAY_RND70 = altSettings.getProperty("Lakfi70LevelRespRnd", 600);
		LAKFI_RESP_DELAY80 = altSettings.getProperty("Lakfi80LevelResp", 1800);
		LAKFI_RESP_DELAY_RND80 = altSettings.getProperty("Lakfi80LevelRespRnd", 600);
		MIN_ADENA_TO_EAT = altSettings.getProperty("MinAdenaLakfiEat", 10000);
		TIME_IF_NOT_FEED = altSettings.getProperty("TimeIfNotFeedDissapear", 10);
		INTERVAL_EATING = altSettings.getProperty("IntervalBetweenEating", 15);		
		HERO_TIME_DELAY = altSettings.getProperty("AltIntervalBetweenHeroChat", 10);	
		EXPERTISE_PENALTY = altSettings.getProperty("ExpertisePenalty", true);
		DISSALOW_GLOBAL_CHATS_UNTIL_LEVEL = altSettings.getProperty("DissalowChatLevelUntilLevel", 0);
		
		ALLOW_DUELS = altSettings.getProperty("AllowDuels", true);
		
		ENABLE_CAT_NEC_FREE_FARM = altSettings.getProperty("EnableCatacombNecropolisFreeEntrance", false);
		
		ALT_GAME_SUB_BOOK = altSettings.getProperty("AltSubBook", false);	
		GUARD_CAN_KILL_AGGRO = altSettings.getProperty("GuardCanKillAggro", true);	
		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALT_SAVE_UNSAVEABLE = altSettings.getProperty("AltSaveUnsaveable", false);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", false);
		SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		ALLOW_PET_ATTACK_MASTER = altSettings.getProperty("allowPetAttackMaster", true);
		DISALLOW_PET_ACTIONS_IF_MASTER_DEAD = altSettings.getProperty("dissallowPetActionsWhenMasterDead", false);
		ALT_RAID_RESPAWN_MULTIPLIER = altSettings.getProperty("AltRaidRespawnMultiplier", 1.0);
		ALT_ALLOW_DROP_AUGMENTED = altSettings.getProperty("AlowDropAugmented", false);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		SHOW_FULL_NPC_SHIFTCLICK = altSettings.getProperty("SHOW_FULL_NPC_SHIFTCLICK", false);
		ALLOW_VOICED_COMMANDS = altSettings.getProperty("AllowVoicedCommands", true);
		ALLOW_REVISION_COMMANDS = altSettings.getProperty("ALLOW_REVISION_COMMANDS", true);
		ALLOW_AUTOHEAL_COMMANDS = altSettings.getProperty("ALLOW_AUTOHEAL_COMMANDS", false);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = altSettings.getProperty("AltAllowSubClassWithoutBaium", true);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
		ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
		ALT_GAME_ALLOW_ADENA_DAWN = altSettings.getProperty("AltAllowAdenaDawn", true);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
		AUTO_LEARN_FORGOTTEN_SKILLS = altSettings.getProperty("AutoLearnForgottenSkills", false);
		CAN_RECEIVE_CERTIFICATION_WITHOUT_LEARN_PREVIOUS_SKILL = altSettings.getProperty("CAN_RECEIVE_CERTIFICATION_WITHOUT_LEARN_PREVIOUS_SKILL", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);

		DISABLED_SPELLBOOKS_FOR_ACQUIRE_TYPES = new HashSet<AcquireType>();
		for(String t : altSettings.getProperty("DISABLED_SPELLBOOKS_FOR_ACQUIRE_TYPES", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if(t.trim().isEmpty())
				continue;

			DISABLED_SPELLBOOKS_FOR_ACQUIRE_TYPES.add(AcquireType.valueOf(t.toUpperCase()));
		}

		ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
		ALT_TELE_TO_CATACOMBS = altSettings.getProperty("TeleToCatacombs", false);
		ALT_BS_CRYSTALLIZE = altSettings.getProperty("BSCrystallize", false);
		ALT_MAMMON_UPGRADE = altSettings.getProperty("MammonUpgrade", 6680500);
		ALT_MAMMON_EXCHANGE = altSettings.getProperty("MammonExchange", 10091400);
		ALT_ALLOW_TATTOO = altSettings.getProperty("AllowTattoo", false);
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		MUSIC_LIMIT = altSettings.getProperty("MusicLimit", 12);
		TRIGGER_LIMIT = altSettings.getProperty("TriggerLimit", 12);
		DEBUFF_LIMIT = altSettings.getProperty("DebuffLimit", 8);
		ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
		ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
		ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
		ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
		ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
		ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0.);
		NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		ALT_KAMALOKA_NIGHTMARE_REENTER = altSettings.getProperty("SellReenterNightmaresTicket", true);
		ALT_KAMALOKA_ABYSS_REENTER = altSettings.getProperty("SellReenterAbyssTicket", true);
		ALT_KAMALOKA_LAB_REENTER = altSettings.getProperty("SellReenterLabyrinthTicket", true);
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		CHAR_TITLE = altSettings.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = altSettings.getProperty("CharAddTitle", "");
		
		ALT_TELEPORTS_ONLY_FOR_GIRAN = altSettings.getProperty("AltAllRecallGoesToGiran", false);
		
		ALT_ALLOW_SELL_COMMON = altSettings.getProperty("AllowSellCommon", true);
		ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_UNALLOWED_ITEMS = altSettings.getProperty("ShopUnallowedItems", ArrayUtils.EMPTY_INT_ARRAY);
		STARTING_ADENA = altSettings.getProperty("AltStartAdena", 0);
		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[] { 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 6035, 6036 });

		FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);

		RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);

		RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALLOW_CLANSKILLS = altSettings.getProperty("AllowClanSkills", true);
		ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = altSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
		ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);

		CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 1.0);
		BUFFTIME_MODIFIER = altSettings.getProperty("BUFFTIME_MODIFIER", 1.0);
		SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 1.0);
		MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = altSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 65);
		ALT_IMPROVED_PETS_LIMITED_USE = altSettings.getProperty("ImprovedPetsLimitedUse", false);

		ALT_CHAMPION_CHANCE1 = altSettings.getProperty("AltChampionChance1", 0.);
		ALT_CHAMPION_CHANCE2 = altSettings.getProperty("AltChampionChance2", 0.);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_MIN_LEVEL = altSettings.getProperty("AltChampionMinLevel", 20);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		SPECIAL_ITEM_ID = altSettings.getProperty("ChampionSpecialItem", 0);
		SPECIAL_ITEM_COUNT = altSettings.getProperty("ChampionSpecialItemCount", 1);
		RATE_DROP_CHAMPION = altSettings.getProperty("RateDropChampion", 1.);
		RATE_CHAMPION_DROP_ADENA = altSettings.getProperty("RateChampionDropAdena", 1.);

		ALT_VITALITY_ENABLED = altSettings.getProperty("AltVitalityEnabled", true);
		ALT_VITALITY_RATE = altSettings.getProperty("AltVitalityRate", 1.);
		ALT_VITALITY_CONSUME_RATE = altSettings.getProperty("AltVitalityConsumeRate", 1.);
		ALT_VITALITY_RAID_BONUS = altSettings.getProperty("AltVitalityRaidBonus", 2000);

		ALT_DEBUG_ENABLED = altSettings.getProperty("AltDebugEnabled", false);
		ALT_DEBUG_PVP_ENABLED = altSettings.getProperty("AltDebugPvPEnabled", false);
		ALT_DEBUG_PVP_DUEL_ONLY = altSettings.getProperty("AltDebugPvPDuelOnly", true);
		ALT_DEBUG_PVE_ENABLED = altSettings.getProperty("AltDebugPvEEnabled", false);

		ALT_PARTY_RATE_FORMULA = altSettings.getProperty("ALT_PARTY_RATE_FORMULA", false);
		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[] { 1.00, 1.10, 1.20, 1.30, 1.40, 1.50, 2.00, 2.10, 2.20 });

		ALT_ALL_PHYS_SKILLS_OVERHIT = altSettings.getProperty("AltAllPhysSkillsOverhit", true);
		ALT_REMOVE_SKILLS_ON_DELEVEL = altSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALT_USE_BOW_REUSE_MODIFIER = altSettings.getProperty("AltUseBowReuseModifier", true);
		ALLOW_CH_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowChDoorOpenOnClick", true);
		ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
		ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
		ALT_CH_SIMPLE_DIALOG = altSettings.getProperty("AltChSimpleDialog", false);

		ALT_OPEN_CLOAK_SLOT = altSettings.getProperty("OpenCloakSlot", false);

		ALT_SHOW_SERVER_TIME = altSettings.getProperty("ShowServerTime", false);

		FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);

		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);

		ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);

		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Math.min(Math.max(altSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1), 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.);

		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);

		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 24);
		
		CLAN_EXPELLED_MEMBER_PENALTY = altSettings.getProperty("AltAllyClanExpelledPenalty", 24);
		CLAN_LEAVED_ALLY_PENALTY = altSettings.getProperty("AltClanLeavedAllyPenalty", 24);
		CLAN_DISSOLVED_ALLY_PENALTY = altSettings.getProperty("AltDissolvedAllyPenalty", 24);
		NEVIT_BONUS_TIME = altSettings.getProperty("AltNevitBonusTime", 180);
		ALLY_EXPELLED_MEMBER_PENALTY = altSettings.getProperty("AltAllyMemberExpelledPenalty", 24);
		REQUIRE_SKILL_ITEM_TO_OPEN_CC = altSettings.getProperty("AltRequireSkillOrItemForCC", true);
		CLAN_AIR_SHIP_MAX_FUEL = altSettings.getProperty("AltClanAirShipMaxFuel", 600);
		
		INFINITY_SHOT = altSettings.getProperty("InfinityShots", false);
		
		ENABLE_HELP = altSettings.getProperty("EnableHelp", true);
		ENABLE_DELEVEL = altSettings.getProperty("EnableDelevel", false);
		ENABLE_HELLBOUND = altSettings.getProperty("EnableHellBound", true);
		ENABLE_COMBINE_TALISMAN = altSettings.getProperty("EnableCombineTalisman", false);
		ENABLE_CFG = altSettings.getProperty("EnableCFG", true);
		ENABLE_OFFLINE = altSettings.getProperty("EnableOfflineTrade", true);
		ENABLE_REPAIR = altSettings.getProperty("EnableRepair", true);
		ENABLE_INFO = altSettings.getProperty("EnableInfo", true);
		ENABLE_WEDDING = altSettings.getProperty("EnableWedding", true);
		ENABLE_WHOAMI = altSettings.getProperty("EnableWhoAmI", true);
		ENABLE_DEBUG = altSettings.getProperty("EnableDebug", true);
		ENABLE_ONLINE = altSettings.getProperty("EnableOnline", true);
		ENABLE_RELOCATE = altSettings.getProperty("EnableRelocate", true);
		ENABLE_REFFERAL = altSettings.getProperty("EnableRefferal", true);
		ENABLE_PASSWORD = altSettings.getProperty("EnablePassword", true);
		ENABLE_LOCK = altSettings.getProperty("EnableLock", true);
		ENABLE_SECURITY = altSettings.getProperty("EnableSecurity", true);	
		SIEGE_COMMAND_ENABLED = altSettings.getProperty("EnableSiegeCommand", false);	

        ANCIENT_HERB_SPAWN_RADIUS = altSettings.getProperty("AncientHerbSpawnRadius", 600);
        ANCIENT_HERB_SPAWN_CHANCE = altSettings.getProperty("AncientHerbSpawnChance", 3);
        ANCIENT_HERB_SPAWN_COUNT = altSettings.getProperty("AncientHerbSpawnCount", 5);
        ANCIENT_HERB_RESPAWN_TIME = altSettings.getProperty("AncientHerbRespawnTime", 60) * 1000;
        ANCIENT_HERB_DESPAWN_TIME = altSettings.getProperty("AncientHerbDespawnTime", 60) * 1000;
        String[] locs = altSettings.getProperty("AncientHerbSpawnPoints", "").split(";");
        if (locs != null) 
		{
            for (String string : locs) 
			{
                if (string != null) 
				{
                    String[] cords = string.split(",");
                    int x = Integer.parseInt(cords[0]);
                    int y = Integer.parseInt(cords[1]);
                    int z = Integer.parseInt(cords[2]);
                    HEIN_FIELDS_LOCATIONS.add(new Location(x, y, z));
                }
            }
        }		

		ALT_SELL_PRICE_DIV = altSettings.getProperty("SellPriceDiv", 2);
		ALT_ENABLE_DISPELL_SONGS = altSettings.getProperty("AltEnableDispellSongsDances", false);
		
		MIN_RAID_LEVEL_TO_DROP = altSettings.getProperty("MinRaidLevelToDropItem", 0);
		
		RAID_DROP_GLOBAL_ITEMS = altSettings.getProperty("AltEnableGlobalRaidDrop", false);
		String[] infos = altSettings.getProperty("RaidGlobalDrop", new String[0], ";");
		for(String info : infos) 
		{
			if(info.isEmpty())
				continue;

			String[] data = info.split(",");
			int id = Integer.parseInt(data[0]);
			long count = Long.parseLong(data[1]);
			double chance = Double.parseDouble(data[2]);
			RAID_GLOBAL_DROP.add(new RaidGlobalDrop(id, count, chance));
		}

		CLAN_MAX_LEVEL = altSettings.getProperty("CLAN_MAX_LEVEL", 11);

		CLAN_LVL_UP_SP_COST = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_SP_COST.length; i++)
			CLAN_LVL_UP_SP_COST[i] = altSettings.getProperty("CLAN_LVL_UP_SP_COST_" + i, 0);

		CLAN_LVL_UP_RP_COST = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_RP_COST.length; i++)
			CLAN_LVL_UP_RP_COST[i] = altSettings.getProperty("CLAN_LVL_UP_RP_COST_" + i, 0);

		CLAN_LVL_UP_MIN_MEMBERS = new int[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_MIN_MEMBERS.length; i++)
			CLAN_LVL_UP_MIN_MEMBERS[i] = altSettings.getProperty("CLAN_LVL_UP_MIN_MEMBERS_" + i, 1);

		CLAN_LVL_UP_ITEMS_REQUIRED = new long[CLAN_MAX_LEVEL + 1][][][]; // TOOD: [Bonux] Сделать поменьше уровней массива..)
		for(int i = 1; i < CLAN_LVL_UP_ITEMS_REQUIRED.length; i++)
		{
			String[] itemsByLvlVariations = altSettings.getProperty("CLAN_LVL_UP_ITEMS_REQUIRED_" + i, "0-0").split("\\|");
			CLAN_LVL_UP_ITEMS_REQUIRED[i] = new long[itemsByLvlVariations.length][][];
			for(int j = 0; j < itemsByLvlVariations.length; j++)
				CLAN_LVL_UP_ITEMS_REQUIRED[i][j] = StringArrayUtils.stringToLong2X(itemsByLvlVariations[j], ";", "-");
		}

		CLAN_LVL_UP_NEED_DOMINION = new boolean[CLAN_MAX_LEVEL + 1];
		for(int i = 1; i < CLAN_LVL_UP_NEED_DOMINION.length; i++)
			CLAN_LVL_UP_NEED_DOMINION[i] = altSettings.getProperty("CLAN_LVL_UP_NEED_DOMINION_" + i, false);

		ALT_DISALLOW_RECOMMEND_ON_SINGLE_DEVICE = altSettings.getProperty("ALT_DISALLOW_RECOMMEND_ON_SINGLE_DEVICE", 0);

		CLAN_DELETE_TIME = altSettings.getProperty("CLAN_DELETE_TIME", "0 5 * * 2");
		CLAN_CHANGE_LEADER_TIME = altSettings.getProperty("CLAN_CHANGE_LEADER_TIME", "0 5 * * 2");

		CLAN_MIN_LEVEL = altSettings.getProperty("CLAN_MIN_LEVEL", 0);

		ALT_USE_TRANSFORM_IN_EPIC_ZONE = altSettings.getProperty("ALT_USE_TRANSFORM_IN_EPIC_ZONE", true);

		ALT_CLAN_LEAVE_PENALTY_TIME = altSettings.getProperty("ALT_CLAN_LEAVE_PENALTY_TIME", 24);
		ALT_CLAN_CREATE_PENALTY_TIME = altSettings.getProperty("ALT_CLAN_CREATE_PENALTY_TIME", 240);

		ENABLE_AUTO_ATTRIBUTE_SYSTEM = altSettings.getProperty("ENABLE_AUTO_ATTRIBUTE_SYSTEM", false);

		String allowedTradeZones = altSettings.getProperty("ALLOWED_TRADE_ZONES", "");
		if(StringUtils.isEmpty(allowedTradeZones))
			ALLOWED_TRADE_ZONES = new String[0];
		else
			ALLOWED_TRADE_ZONES = allowedTradeZones.split(";");

		MULTICLASS_SYSTEM_ENABLED = altSettings.getProperty("MULTICLASS_SYSTEM_ENABLED", false);
		MULTICLASS_SYSTEM_SHOW_LEARN_LIST_ON_OPEN_SKILL_LIST = altSettings.getProperty("MULTICLASS_SYSTEM_SHOW_LEARN_LIST_ON_OPEN_SKILL_LIST", false);
		MULTICLASS_SYSTEM_NON_CLASS_SP_MODIFIER = altSettings.getProperty("MULTICLASS_SYSTEM_NON_CLASS_SP_MODIFIER", 1.0);
		MULTICLASS_SYSTEM_1ST_CLASS_SP_MODIFIER = altSettings.getProperty("MULTICLASS_SYSTEM_1ST_CLASS_SP_MODIFIER", 1.0);
		MULTICLASS_SYSTEM_2ND_CLASS_SP_MODIFIER = altSettings.getProperty("MULTICLASS_SYSTEM_2ND_CLASS_SP_MODIFIER", 1.0);
		MULTICLASS_SYSTEM_3RD_CLASS_SP_MODIFIER = altSettings.getProperty("MULTICLASS_SYSTEM_3RD_CLASS_SP_MODIFIER", 1.0);
		MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID_BASED_ON_SP", 0);
		MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID_BASED_ON_SP", 0);
		MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID_BASED_ON_SP", 0);
		MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID_BASED_ON_SP", 0);
		MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP", 1.0);
		MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP", 1.0);
		MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP", 1.0);
		MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP = altSettings.getProperty("MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP", 1.0);
		MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID = altSettings.getProperty("MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID", 0);
		MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID = altSettings.getProperty("MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID", 0);
		MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID = altSettings.getProperty("MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID", 0);
		MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID = altSettings.getProperty("MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID", 0);
		MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT = altSettings.getProperty("MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT", 0L);
		MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT = altSettings.getProperty("MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT", 0L);
		MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT = altSettings.getProperty("MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT", 0L);
		MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT = altSettings.getProperty("MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT", 0L);
		MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER = altSettings.getProperty("MULTICLASS_SYSTEM_FORGOTTEN_SCROLLS_COUNT_MODIFIER", 1);

		CAN_LEARN_SKILLS_FROM_INTERFACE = altSettings.getProperty("CAN_LEARN_SKILLS_FROM_INTERFACE", false);
	}
	
	public static class RaidGlobalDrop
	{
		int _id;
		long _count;
		double _chance;
		
		public RaidGlobalDrop(int id, long count, double chance)
		{
			_id = id;
			_count = count;
			_chance = chance;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public double getChance()
		{
			return _chance;
		}
	}
	
	public static void loadServicesSettings()
	{
		ExProperties servicesSettings = load(SERVICES_FILE);

		ALLOW_CLASS_MASTERS_LIST.clear();
		String allowClassMasters = servicesSettings.getProperty("AllowClassMasters", "false");
		if(!allowClassMasters.equalsIgnoreCase("false"))
		{
			String[] allowClassLvls = allowClassMasters.split(";");
			for(String allowClassLvl : allowClassLvls)
			{
				String[] allosClassLvlInfo = allowClassLvl.split(",");
				int classLvl = Integer.parseInt(allosClassLvlInfo[0]);
				if(ALLOW_CLASS_MASTERS_LIST.containsKey(classLvl))
					continue;

				int[] needItemInfo = new int[]{ 0, 0 };
				if(allosClassLvlInfo.length >= 3)
					needItemInfo = new int[]{ Integer.parseInt(allosClassLvlInfo[1]), Integer.parseInt(allosClassLvlInfo[2]) };
				ALLOW_CLASS_MASTERS_LIST.put(classLvl, needItemInfo);
			}
		}

		SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
		SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
		SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);
	
		SERVICES_LOCK_ACCOUNT_IP = servicesSettings.getProperty("LockAccountIpService", false);
		
		SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesSettings.getProperty("ClanNameChangeEnabled", false);
		SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 100);
		SERVICES_CHANGE_CLAN_NAME_ITEM = servicesSettings.getProperty("ClanNameChangeItem", 4037);
		
		SERVICES_CHANGE_PASSWORD = servicesSettings.getProperty("ChangePassword", true);
		PASSWORD_PAY_ID = servicesSettings.getProperty("ChangePasswordPayId", 0);
		PASSWORD_PAY_COUNT = servicesSettings.getProperty("ChangePassowrdPayCount", 0);
		
		ALLOW_FAKE_PLAYERS = servicesSettings.getProperty("AllowFakePlayers", false);
		FAKE_PLAYERS_PERCENT = servicesSettings.getProperty("FakePlayersPercent", 100);	
		ALLOW_TOTAL_ONLINE = servicesSettings.getProperty("AllowVoiceCommandOnline", false);
		ALLOW_ONLINE_PARSE = servicesSettings.getProperty("AllowParsTotalOnline", false);
		FIRST_UPDATE = servicesSettings.getProperty("FirstOnlineUpdate", 1);
		DELAY_UPDATE = servicesSettings.getProperty("OnlineUpdate", 5);
		
		SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
		SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
		SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);

		SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
		SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
		SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);

		SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
		SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
		SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);

		SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
		SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
		SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);

		SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
		SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
		SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);

		SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
		SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
		SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
		SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", new String[] { "00FF00" });

		SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
		SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
		SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);

		SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
		SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
		SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
		SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);

		SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
		SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
		SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);

		SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
		SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
		SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);

		ALLOW_KM_ALL_TO_ME = servicesSettings.getProperty("AllowKmAllToMe", false);

		SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
		SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
		SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);

		SERVICES_SELLPETS = servicesSettings.getProperty("SellPets", "");

		SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
		SERVICES_OFFLINE_TRADE_ALLOW_ZONE = servicesSettings.getProperty("AllowOfflineTradeZone", 0);
		OFFLINE_ONLY_IF_PREMIUM = servicesSettings.getProperty("OfflineFeatureOnlyWithPremium", false);
		SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
		SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));
		SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
		SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
		SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
		SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);
		SERVICES_OFFLINE_ABNORMAL_EFFECT = AbnormalEffect.valueOf(servicesSettings.getProperty("OfflineAbnormalEffect", "NULL"));	

		SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
		SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0);
		SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0);
		SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
		SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
		SERVICES_TRADE_MIN_LEVEL = servicesSettings.getProperty("MinLevelForTrade", 0);
		SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);

		SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
		SERVICES_GIRAN_HARBOR_PRICE = servicesSettings.getProperty("GiranHarborTeleportPrice", 500000);

		SERVICES_PARNASSUS_ENABLED = servicesSettings.getProperty("ParnassusZone", false);
		SERVICES_PARNASSUS_PRICE = servicesSettings.getProperty("ParnassusTeleportPrice", 500000);

		SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
		SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
		SERVICES_ALT_LOTTERY_PRICE = servicesSettings.getProperty("AltLotteryPrice", 2000);
		SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
		SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 0.6);
		SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 0.4);
		SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 0.2);
		SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 200);

		SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
		SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
		SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
		SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);

		ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);

		ALLOW_EVENT_GATEKEEPER = servicesSettings.getProperty("AllowEventGatekeeper", false);
		ALLOW_IP_LOCK = servicesSettings.getProperty("AllowLockIP", false);
		ALLOW_HWID_LOCK = servicesSettings.getProperty("AllowLockHwid", false);
		HWID_LOCK_MASK = servicesSettings.getProperty("HwidLockMask", 10);
		
		SERVICES_DELEVEL_ITEM = servicesSettings.getProperty("DelevelItem", 4037);
		SERVICES_DELEVEL_COUNT = servicesSettings.getProperty("DelevelCount", 100);
		SERVICES_DELEVEL_ENABLED = servicesSettings.getProperty("EnableDelevel", true);
		SERVICES_DELEVEL_MIN_LEVEL = servicesSettings.getProperty("DelevelMinLvl", 80);	
		
		/** Away System **/
		ALLOW_AWAY_STATUS = servicesSettings.getProperty("AllowAwayStatus", false); // FIXME: скорее всего не корректно
		AWAY_ONLY_FOR_PREMIUM = servicesSettings.getProperty("AwayOnlyForPremium", true);
		AWAY_PLAYER_TAKE_AGGRO = servicesSettings.getProperty("AwayPlayerTakeAggro", false);
		AWAY_TITLE_COLOR = Integer.decode("0x" + servicesSettings.getProperty("AwayTitleColor", "0000FF"));
		AWAY_TIMER = servicesSettings.getProperty("AwayTimer", 30);
		BACK_TIMER = servicesSettings.getProperty("BackTimer", 30);
		AWAY_PEACE_ZONE = servicesSettings.getProperty("AwayOnlyInPeaceZone", false);		

		SERVICES_SUBCLASS_ACTIVATION_ENABLED = servicesSettings.getProperty("SubClassActivationEnabled", false);
		SERVICES_SUBCLASS_ACTIVATION_PRICE = servicesSettings.getProperty("SubClassActivationPrice", 1);
		SERVICES_SUBCLASS_ACTIVATION_ITEM = servicesSettings.getProperty("SubClassActivationItem", 57);			

		ENABLE_NEW_PVP_SYSTEM = servicesSettings.getProperty("EnableNewPvPSystem", false);
		SPECIAL_PVP_REMAIN_ITEMS_WHITE = servicesSettings.getProperty("RemainItemWhenWhite", 3);
		SPECIAL_PVP_REMAIN_ITEMS_PURPLE = servicesSettings.getProperty("RemainItemWhenPurple", 2);
		SPECIAL_PVP_REMAIN_ITEMS_RED = servicesSettings.getProperty("RemainItemWhenRed", 0);
		SPECIAL_PVP_SAVE_SKILL = servicesSettings.getProperty("SpecialSaveSkillPlusOneItem", 6401);
		PVP_SPECIAL_NONOWNER_ITEM_PICKUP_DELAY = servicesSettings.getProperty("NonOwnerItemPickupDelayForSpecialPvP", 15L) * 1000L;
		ENABLE_SPECIAL_PVP_EXP_LOSS_SPECIAL_PERCENT = servicesSettings.getProperty("EnableSpecialPvPEXPLoss", false);
		PVP_LOSS_PERCENT_DEATH = servicesSettings.getProperty("SpecialPvPEXPLossValue", 0.01);
		
		for(int id : servicesSettings.getProperty("DoNotDropInPvPItems", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				NON_DROPABLE_PVP_ZONES.add(id);		

		DELETE_SKILL_SERVICE_ITEM_ID = servicesSettings.getProperty("DELETE_SKILL_SERVICE_ITEM_ID", 0);
		DELETE_SKILL_SERVICE_ITEM_COUNT = servicesSettings.getProperty("DELETE_SKILL_SERVICE_ITEM_COUNT", 0);
	}

	public static void loadPvPSettings()
	{
		ExProperties pvpSettings = load(PVP_CONFIG_FILE);

		/* KARMA SYSTEM */
		KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
		KARMA_SP_DIVIDER = pvpSettings.getProperty("SPDivider", 7);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);

		KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);

		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);

		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);

		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);

		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
		for(int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[] {
				57,
				1147,
				425,
				1146,
				461,
				10,
				2368,
				7,
				6,
				2370,
				2369,
				3500,
				3501,
				3502,
				4422,
				4423,
				4424,
				2375,
				6648,
				6649,
				6650,
				6842,
				6834,
				6835,
				6836,
				6837,
				6838,
				6839,
				6840,
				5575,
				7694,
				6841,
				8181 }))
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);

		PVP_TIME = pvpSettings.getProperty("PvPTime", 40000);
	}

	public static void loadAISettings()
	{
		ExProperties aiSettings = load(AI_CONFIG_FILE);

		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);

		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);

		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		NONPVP_TIME_ONTELEPORT = aiSettings.getProperty("NonPvPTimeOnTeleport", 0);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
		ALWAYS_TELEPORT_HOME_RB = aiSettings.getProperty("AlwaysTeleportHomeRB", false);
	}

	public static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);

		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);

		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);

		try
		{
			GEODATA_ROOT = new File(geodataSettings.getProperty("GeodataRoot", "./geodata/")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATHFIND_MAP_MUL = geodataSettings.getProperty("PathFindMapMul", 2);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		REGION_EDGE_MAX_Z_DIFF = geodataSettings.getProperty("RegionEdgeMaxZDiff", 128);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
		NPC_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("NPC_PATH_FIND_MAX_HEIGHT", 1024);
		PLAYABLE_PATH_FIND_MAX_HEIGHT = geodataSettings.getProperty("PLAYABLE_PATH_FIND_MAX_HEIGHT", 256);
	}

	public static void newCpPanelSettings()
	{
		ExProperties cp_panel = load(NEW_CP_PANEL_FILE);	
		
		ALLOW_HWID_ENGINE = cp_panel.getProperty("AllowHWIDEngine", false);
		ALLOW_CLEANING_AUTO_BANS = cp_panel.getProperty("AutoBanCleaning", false);
		SECONDS_BETWEEN_AUTO_BAN_CLEANING = cp_panel.getProperty("AutoBanCleaningDelay", 60);
		MAX_CHARS_PER_PC = cp_panel.getProperty("MaxChars", 2);
		ENABLE_POLL_SYSTEM = cp_panel.getProperty("EnablePoll", false);
		ANNOUNCE_POLL_EVERY_X_MIN = cp_panel.getProperty("AnnounceToVoteInMin", 10);
		ALLOW_SECOND_AUTH_CHECK = cp_panel.getProperty("AllowSecondPassCheck", false);
		ALLOW_SHOW_CHANGE_LOG = cp_panel.getProperty("AllowShowChangeLog", false);	
		ENABLE_NEW_CFG = cp_panel.getProperty("EnableNewCfgPanel", false);	
	}
	
	public static void pvpManagerSettings()
	{
		ExProperties pvp_manager = load(PVP_MANAGER_FILE);

		ALLOW_PVP_REWARD_SYSTEM = pvp_manager.getProperty("AllowPvPManagerSystem", true);
		PVP_REWARDS_ZONES = pvp_manager.getProperty("PVP_REWARDS_ZONES", new String[0], ";");
		PVP_REWARD_SEND_SUCC_NOTIF = pvp_manager.getProperty("sendNotification", true);
		PVP_REWARD_REWARD_IDS = pvp_manager.getProperty("PvpRewardsIDs", new int[]{57, 6673});
		PVP_REWARD_COUNTS = pvp_manager.getProperty("PvpRewardsCounts", new long[]{1, 2});
		PVP_REWARD_RANDOM_ONE = pvp_manager.getProperty("GiveJustOneRandom", true);
		PVP_REWARD_DELAY_ONE_KILL = pvp_manager.getProperty("DelayBetweenKillsOneCharSec", 60);
		PVP_REWARD_MIN_PL_PROFF = pvp_manager.getProperty("ToRewardMinProff", 0);
		PVP_REWARD_MIN_PL_UPTIME_MINUTE = pvp_manager.getProperty("ToRewardMinPlayerUptimeMinutes", 60);
		PVP_REWARD_MIN_PL_LEVEL = pvp_manager.getProperty("ToRewardMinPlayerLevel", 75);
		PVP_REWARD_PK_GIVE = pvp_manager.getProperty("RewardPK", false);
		PVP_REWARD_ON_EVENT_GIVE = pvp_manager.getProperty("ToRewardIfInEvent", false);
		PVP_REWARD_ONLY_BATTLE_ZONE = pvp_manager.getProperty("ToRewardOnlyIfInBattleZone", false);
		PVP_REWARD_ONLY_NOBLE_GIVE = pvp_manager.getProperty("ToRewardOnlyIfNoble", false);
		PVP_REWARD_SAME_PARTY_GIVE = pvp_manager.getProperty("ToRewardIfInSameParty", false);
		PVP_REWARD_SAME_CLAN_GIVE = pvp_manager.getProperty("ToRewardIfInSameClan", false);
		PVP_REWARD_SAME_ALLY_GIVE = pvp_manager.getProperty("ToRewardIfInSameAlly", false);
		PVP_REWARD_SAME_HWID_GIVE = pvp_manager.getProperty("ToRewardIfInSameHWID", false);
		PVP_REWARD_SAME_IP_GIVE = pvp_manager.getProperty("ToRewardIfInSameIP", false);
		PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER = pvp_manager.getProperty("SpecialAntiTwinkCharCreateDelay", false);
		PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM = pvp_manager.getProperty("SpecialAntiTwinkDelayInHours", 24);
		PVP_REWARD_CHECK_EQUIP = pvp_manager.getProperty("EquipCheck", false);
		PVP_REWARD_WEAPON_GRADE_TO_CHECK = pvp_manager.getProperty("MinimumGradeToCheck", 0);
		PVP_REWARD_LOG_KILLS = pvp_manager.getProperty("LogKillsToDB", false);
	}
	
	public static void loadEventsSettings()
	{
		ExProperties eventSettings = load(EVENTS_CONFIG_FILE);

		EVENT_CofferOfShadowsPriceRate = eventSettings.getProperty("CofferOfShadowsPriceRate", 1.);
		EVENT_CofferOfShadowsRewardRate = eventSettings.getProperty("CofferOfShadowsRewardRate", 1.);

		EVENTS_DISALLOWED_SKILLS = eventSettings.getProperty("DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
		
        EVENT_TvTRewards = eventSettings.getProperty("TvT_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTTime = eventSettings.getProperty("TvT_time", 3);
		EVENT_TvTStartTime = eventSettings.getProperty("TvT_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_TvTCategories = eventSettings.getProperty("TvT_Categories", false);
		EVENT_TvTMaxPlayerInTeam = eventSettings.getProperty("TvT_MaxPlayerInTeam", 20);
		EVENT_TvTMinPlayerInTeam = eventSettings.getProperty("TvT_MinPlayerInTeam", 2);
		EVENT_TvTAllowSummons = eventSettings.getProperty("TvT_AllowSummons", false);
		EVENT_TvTAllowBuffs = eventSettings.getProperty("TvT_AllowBuffs", false);
		EVENT_TvTAllowMultiReg = eventSettings.getProperty("TvT_AllowMultiReg", false);
		EVENT_TvTCheckWindowMethod = eventSettings.getProperty("TvT_CheckWindowMethod", "IP");
		EVENT_TvTEventRunningTime = eventSettings.getProperty("TvT_EventRunningTime", 20);
		EVENT_TvTFighterBuffs = eventSettings.getProperty("TvT_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTMageBuffs = eventSettings.getProperty("TvT_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTBuffPlayers = eventSettings.getProperty("TvT_BuffPlayers", false);	
		EVENT_TvTrate = eventSettings.getProperty("TvT_rate", true);
		DISABLE_PARTY_ON_EVENT = eventSettings.getProperty("DisablePartyOnEventsGlobal", false);
		DISABLE_PARTY_ON_EVENT_TVT = eventSettings.getProperty("DisablePartyOnEventsOnlyTvT", false);
		DISABLE_PARTY_ON_EVENT_LH = eventSettings.getProperty("DisablePartyOnEventsOnlyLastHero", false);
		
		EVENT_CtFRewards = eventSettings.getProperty("CtF_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtfTime = eventSettings.getProperty("CtF_time", 3);
		EVENT_CtFrate = eventSettings.getProperty("CtF_rate", true);
		EVENT_CtFStartTime = eventSettings.getProperty("CtF_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_CtFCategories = eventSettings.getProperty("CtF_Categories", false);
		EVENT_CtFMaxPlayerInTeam = eventSettings.getProperty("CtF_MaxPlayerInTeam", 20);
		EVENT_CtFMinPlayerInTeam = eventSettings.getProperty("CtF_MinPlayerInTeam", 2);
		EVENT_CtFAllowSummons = eventSettings.getProperty("CtF_AllowSummons", false);
		EVENT_CtFAllowBuffs = eventSettings.getProperty("CtF_AllowBuffs", false);
		EVENT_CtFAllowMultiReg = eventSettings.getProperty("CtF_AllowMultiReg", false);
		EVENT_CtFCheckWindowMethod = eventSettings.getProperty("CtF_CheckWindowMethod", "IP");
		EVENT_CtFFighterBuffs = eventSettings.getProperty("CtF_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFMageBuffs = eventSettings.getProperty("CtF_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFBuffPlayers = eventSettings.getProperty("CtF_BuffPlayers", false);
		EVENT_CtFEventRunningTime = eventSettings.getProperty("CtF_EventRunningTime", 20);
		EVENT_CtFOnDropSpawnFlagInStartLoc = eventSettings.getProperty("CtF_OnDropSpawnFlagInStartLoc", false);
		
		EVENT_LastHeroItemID = eventSettings.getProperty("LastHero_bonus_id", 57);
		EVENT_LastHeroItemCOUNT = eventSettings.getProperty("LastHero_bonus_count", 5000.);
		EVENT_LastHeroRate = eventSettings.getProperty("LastHero_rate", true);
		EVENT_LastHeroItemCOUNTFinal = eventSettings.getProperty("LastHero_bonus_count_final", 10000.);
		EVENT_LastHeroRateFinal = eventSettings.getProperty("LastHero_rate_final", true);
		EVENT_LHTime = eventSettings.getProperty("LH_time", 3);
		EVENT_LHStartTime = eventSettings.getProperty("LH_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_LHCategories = eventSettings.getProperty("LH_Categories", false);
		EVENT_LHAllowSummons = eventSettings.getProperty("LH_AllowSummons", false);
		EVENT_LHAllowBuffs = eventSettings.getProperty("LH_AllowBuffs", false);
		EVENT_LHAllowMultiReg = eventSettings.getProperty("LH_AllowMultiReg", false);
		EVENT_LHCheckWindowMethod = eventSettings.getProperty("LH_CheckWindowMethod", "IP");
		EVENT_LHEventRunningTime = eventSettings.getProperty("LH_EventRunningTime", 20);
		EVENT_LHFighterBuffs = eventSettings.getProperty("LH_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_LHMageBuffs = eventSettings.getProperty("LH_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_LHBuffPlayers = eventSettings.getProperty("LH_BuffPlayers", false);	
		ALLOW_HEROES_LASTHERO = eventSettings.getProperty("LH_AllowHeroes", true);	
		LH_WINCHAR_HERO = eventSettings.getProperty("LH_WinCharHero", false);
		LAST_HERO_HIDE_NAMES = eventSettings.getProperty("LH_HideNames", false);
		LAST_HERO_GLOBAL_NAME = eventSettings.getProperty("LH_GlobalNamesDesc", "LastHero");	
		EVENT_TFH_POLLEN_CHANCE = eventSettings.getProperty("TFH_POLLEN_CHANCE", 5.);

		EVENT_GLITTMEDAL_NORMAL_CHANCE = eventSettings.getProperty("MEDAL_CHANCE", 10.);
		EVENT_GLITTMEDAL_GLIT_CHANCE = eventSettings.getProperty("GLITTMEDAL_CHANCE", 0.1);

		EVENT_L2DAY_LETTER_CHANCE = eventSettings.getProperty("L2DAY_LETTER_CHANCE", 1.);
		EVENT_CHANGE_OF_HEART_CHANCE = eventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.);

		EVENT_APIL_FOOLS_DROP_CHANCE = eventSettings.getProperty("AprilFollsDropChance", 50.);

		EVENT_BOUNTY_HUNTERS_ENABLED = eventSettings.getProperty("BountyHuntersEnabled", true);

		EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
		EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSettings.getProperty("SavingSnowmanRewarderChance", 2);

		EVENT_TRICK_OF_TRANS_CHANCE = eventSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.);

		EVENT_MARCH8_DROP_CHANCE = eventSettings.getProperty("March8DropChance", 10.);
		EVENT_MARCH8_PRICE_RATE = eventSettings.getProperty("March8PriceRate", 1.);
		
		//hero chat service config
		DELAY_HERO_CHAT_SERVICE = eventSettings.getProperty("DelayHeroChatService", 1);
		
		GVG_LANG = eventSettings.getProperty("GvGLangRus", true);
		
		ALLOW_REFFERAL_SYSTEM = eventSettings.getProperty("EnableReffSystem", false); //test only
		
		REF_SAVE_INTERVAL = eventSettings.getProperty("refferSystemSaveInterval", 1); //test only
		
		MAX_REFFERALS_PER_CHAR = eventSettings.getProperty("maxRefferalsPerChar", 1); //test only
		
		MIN_ONLINE_TIME = eventSettings.getProperty("MinOnlineTimeForReffering", 30); //test only
		
		MIN_REFF_LEVEL = eventSettings.getProperty("MinLevelForReffering", 2); //test only
		
		REF_PERCENT_GIVE = eventSettings.getProperty("RefferPercentToGive", 0.1); //test only
		
		GvG_POINTS_FOR_BOX = eventSettings.getProperty("GvGPointsKillBox", 20); //test only
		GvG_POINTS_FOR_BOSS = eventSettings.getProperty("GvGPointsKillBoss", 50); //test only
		GvG_POINTS_FOR_KILL = eventSettings.getProperty("GvGPointsKillPlayer", 5); //test only
		GvG_POINTS_FOR_DEATH = eventSettings.getProperty("GvGPointsIfDead", 3); //test only
		GvG_EVENT_TIME = eventSettings.getProperty("GvGEventTime", 10); //test only
		GvG_BOSS_SPAWN_TIME = eventSettings.getProperty("GvGBossSpawnTime", 10); //test only
		GvG_FAME_REWARD = eventSettings.getProperty("GvGRewardFame", 200); //test only
		GvG_REWARD = eventSettings.getProperty("GvGRewardStatic", 57); //test only
		GvG_REWARD_COUNT = eventSettings.getProperty("GvGRewardCountStatic", 10000); //test only
		GvG_ADD_IF_WITHDRAW = eventSettings.getProperty("GvGAddPointsIfPartyWithdraw", 200); //test only
		GvG_HOUR_START = eventSettings.getProperty("GvGHourStart", 20); //test only
		GvG_MINUTE_START = eventSettings.getProperty("GvGMinuteStart", 00); //test only
		GVG_MIN_LEVEL = eventSettings.getProperty("GvGMinLevel", 1); //test only
		GVG_MAX_LEVEL = eventSettings.getProperty("GvGMaxLevel", 85); //test only
		GVG_MAX_GROUPS = eventSettings.getProperty("GvGMaxGroupsInEvent", 100); //test only
		GVG_MIN_PARTY_MEMBERS = eventSettings.getProperty("GvGMinPlayersInParty", 6); //test only
		GVG_MAX_PARTY_MEMBERS = eventSettings.getProperty("GvGMaxPlayersInParty", 9); 
		GVG_TIME_TO_REGISTER = eventSettings.getProperty("GvGTimeToRegister", 10); //test only
	
		for(int id : eventSettings.getProperty("ReffItemsList", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				ITEM_LIST.add(id);
		//new confs		
		EVENT_MODEL_ACTIVE = eventSettings.getProperty("EventModelActive", false);
		DAY_OF_MAIN_EVENT_DATE = eventSettings.getProperty("DayOfMainEventStart", 1); //day
		MAIN_EVENT_START_DATE = eventSettings.getProperty("HourOfMainEventStart", 18); //hour
		DAY_OF_MAIN_EVENT_DATE_END = eventSettings.getProperty("DayOfMainEventEnd", 6); //day
		MAIN_EVENT_END_DATE_HOUR = eventSettings.getProperty("HourOfMainEventEnd", 18); //hour

		EVENT_MODEL1_DAY = eventSettings.getProperty("DayOfEvent1Starts", 2); //events props
		EVENT_MODEL1_HOUR = eventSettings.getProperty("HoursOfEvent1Starts", 14); //events props
		EVENT_MODEL1_NUMBER = eventSettings.getProperty("NumberOfEvent1", 1); //events props
		
		EVENT_MODEL2_DAY = eventSettings.getProperty("DayOfEvent2Starts", 2); //events props
		EVENT_MODEL2_HOUR = eventSettings.getProperty("HoursOfEvent2Starts", 16); //events props
		EVENT_MODEL2_NUMBER = eventSettings.getProperty("NumberOfEvent2", 2); //events props
		
		EVENT_MODEL3_DAY = eventSettings.getProperty("DayOfEvent3Starts", 2); //events props
		EVENT_MODEL3_HOUR = eventSettings.getProperty("HoursOfEvent3Starts", 18); //events props
		EVENT_MODEL3_NUMBER = eventSettings.getProperty("NumberOfEvent3", 3); //events props
		
		EVENT_MODEL4_DAY = eventSettings.getProperty("DayOfEvent4Starts", 2); //events props
		EVENT_MODEL4_HOUR = eventSettings.getProperty("HoursOfEvent4Starts", 20); //events props
		EVENT_MODEL4_NUMBER = eventSettings.getProperty("NumberOfEvent4", 1); //events props
		
		EVENT_MODEL5_DAY = eventSettings.getProperty("DayOfEvent5Starts", 3); //events props
		EVENT_MODEL5_HOUR = eventSettings.getProperty("HoursOfEvent5Starts", 10); //events props
		EVENT_MODEL5_NUMBER = eventSettings.getProperty("NumberOfEvent5", 2); //events props
		
		EVENT_MODEL6_DAY = eventSettings.getProperty("DayOfEvent6Starts", 3); //events props
		EVENT_MODEL6_HOUR = eventSettings.getProperty("HoursOfEvent6Starts", 12); //events props
		EVENT_MODEL6_NUMBER = eventSettings.getProperty("NumberOfEvent6", 3); //events props
		
		EVENT_MODEL7_DAY = eventSettings.getProperty("DayOfEvent7Starts", 3); //events props
		EVENT_MODEL7_HOUR = eventSettings.getProperty("HoursOfEvent7Starts", 14); //events props
		EVENT_MODEL7_NUMBER = eventSettings.getProperty("NumberOfEvent7", 1); //events props
		
		EVENT_MODEL8_DAY = eventSettings.getProperty("DayOfEvent8Starts", 4); //events props
		EVENT_MODEL8_HOUR = eventSettings.getProperty("HoursOfEvent8Starts", 00); //events props
		EVENT_MODEL8_NUMBER = eventSettings.getProperty("NumberOfEvent8", 2); //events props
		
		EVENT_MODEL9_DAY = eventSettings.getProperty("DayOfEvent9Starts", 4); //events props
		EVENT_MODEL9_HOUR = eventSettings.getProperty("HoursOfEvent9Starts", 02); //events props
		EVENT_MODEL9_NUMBER = eventSettings.getProperty("NumberOfEvent9", 3); //events props
		
		EVENT_MODEL10_DAY = eventSettings.getProperty("DayOfEvent10Starts", 4); //events props
		EVENT_MODEL10_HOUR = eventSettings.getProperty("HoursOfEvent10Starts", 06); //events props
		EVENT_MODEL10_NUMBER = eventSettings.getProperty("NumberOfEvent10", 1); //events props
		
		EVENT_MODEL11_DAY = eventSettings.getProperty("DayOfEvent11Starts", 5); //events props
		EVENT_MODEL11_HOUR = eventSettings.getProperty("HoursOfEvent11Starts", 12); //events props
		EVENT_MODEL11_NUMBER = eventSettings.getProperty("NumberOfEvent11", 2); //events props
		
		EVENT_MODEL12_DAY = eventSettings.getProperty("DayOfEvent12Starts", 6); //events props
		EVENT_MODEL12_HOUR = eventSettings.getProperty("HoursOfEvent12Starts", 10); //events props
		EVENT_MODEL12_NUMBER = eventSettings.getProperty("NumberOfEvent12", 3); //events props
		
		EVENT_MODEL13_DAY = eventSettings.getProperty("DayOfEvent13Starts", 6); //events props
		EVENT_MODEL13_HOUR = eventSettings.getProperty("HoursOfEvent13Starts", 13); //events props
		EVENT_MODEL13_NUMBER = eventSettings.getProperty("NumberOfEvent13", 1); //events props
		
		EVENT_MODEL14_DAY = eventSettings.getProperty("DayOfEvent14Starts", 6); //events props
		EVENT_MODEL14_HOUR = eventSettings.getProperty("HoursOfEvent14Starts", 14); //events props
		EVENT_MODEL14_NUMBER = eventSettings.getProperty("NumberOfEvent14", 2); //events props
		
		EVENT_MODEL15_DAY = eventSettings.getProperty("DayOfEvent15Starts", 6); //events props
		EVENT_MODEL15_NUMBER = eventSettings.getProperty("HoursOfEvent15Starts", 17); //events props
		EVENT_MODEL15_HOUR = eventSettings.getProperty("NumberOfEvent15", 3); //events props
		
		PLACES_TO_REWARD = eventSettings.getProperty("HowManyPlacesToReward", 10); //events props
		
		FIRST_PLACE_ID1 = eventSettings.getProperty("FirstPlaceItemId1", 57); //reward props
		FIRST_PLACE_COUNT1 = eventSettings.getProperty("FirstPlaceItemCount1", 1000); //reward props
		FIRST_PLACE_ID2 = eventSettings.getProperty("FirstPlaceItemId2", 57); //reward props
		FIRST_PLACE_COUNT2 = eventSettings.getProperty("FirstPlaceItemCount2", 1000); //reward props

		SECOND_PLACE_ID1 = eventSettings.getProperty("SecondPlaceItemId1", 57); //reward props
		SECOND_PLACE_COUNT1 = eventSettings.getProperty("SecondPlaceItemCount1", 1000); //reward props
		SECOND_PLACE_ID2 = eventSettings.getProperty("SecondPlaceItemId2", 57); //reward props
		SECOND_PLACE_COUNT2 = eventSettings.getProperty("SecondPlaceItemCount2", 1000); //reward props
		
		THIRD_PLACE_ID1 = eventSettings.getProperty("ThirdPlaceItemId1", 57); //reward props
		THIRD_PLACE_COUNT1 = eventSettings.getProperty("ThirdPlaceItemCount1", 1000); //reward props
		THIRD_PLACE_ID2 = eventSettings.getProperty("ThirdPlaceItemId2", 57); //reward props
		THIRD_PLACE_COUNT2 = eventSettings.getProperty("ThirdPlaceItemCount2", 1000); //reward props

		FOURTH_PLACE_ID1 = eventSettings.getProperty("FourthPlaceItemId1", 57); //reward props
		FOURTH_PLACE_COUNT1 = eventSettings.getProperty("FourthPlaceItemCount1", 1000); //reward props
		FOURTH_PLACE_ID2 = eventSettings.getProperty("FourthPlaceItemId2", 57); //reward props
		FOURTH_PLACE_COUNT2 = eventSettings.getProperty("FourthPlaceItemCount2", 1000); //reward props

		FIFTH_PLACE_ID1 = eventSettings.getProperty("FifthPlaceItemId1", 57); //reward props
		FIFTH_PLACE_COUNT1 = eventSettings.getProperty("FifthPlaceItemCount1", 1000); //reward props
		FIFTH_PLACE_ID2 = eventSettings.getProperty("FifthPlaceItemId2", 57); //reward props
		FIFTH_PLACE_COUNT2 = eventSettings.getProperty("FifthPlaceItemCount2", 1000); //reward props

		SIXTH_PLACE_ID1 = eventSettings.getProperty("SixthPlaceItemId1", 57); //reward props
		SIXTH_PLACE_COUNT1 = eventSettings.getProperty("SixthPlaceItemCount1", 1000); //reward props
		SIXTH_PLACE_ID2 = eventSettings.getProperty("SixthPlaceItemId2", 57); //reward props
		SIXTH_PLACE_COUNT2 = eventSettings.getProperty("SixthPlaceItemCount2", 1000); //reward props

		SEVENTH_PLACE_ID1 = eventSettings.getProperty("SeventhPlaceItemId1", 57); //reward props
		SEVENTH_PLACE_COUNT1 = eventSettings.getProperty("SeventhPlaceItemCount1", 1000); //reward props
		SEVENTH_PLACE_ID2 = eventSettings.getProperty("SeventhPlaceItemId2", 57); //reward props
		SEVENTH_PLACE_COUNT2 = eventSettings.getProperty("SeventhPlaceItemCount2", 1000); //reward props

		EIGHTH_PLACE_ID1 = eventSettings.getProperty("EighthPlaceItemId1", 57); //reward props
		EIGHTH_PLACE_COUNT1 = eventSettings.getProperty("EighthPlaceItemCount1", 1000); //reward props
		EIGHTH_PLACE_ID2 = eventSettings.getProperty("EighthPlaceItemId2", 57); //reward props
		EIGHTH_PLACE_COUNT2 = eventSettings.getProperty("EighthPlaceItemCount2", 1000); //reward props

		NINTH_PLACE_ID1 = eventSettings.getProperty("NinthPlaceItemId1", 57); //reward props
		NINTH_PLACE_COUNT1 = eventSettings.getProperty("NinthPlaceItemCount1", 1000); //reward props
		NINTH_PLACE_ID2 = eventSettings.getProperty("NinthPlaceItemId2", 57); //reward props
		NINTH_PLACE_COUNT2 = eventSettings.getProperty("NinthPlaceItemCount2", 1000); //reward props

		TENTH_PLACE_ID1 = eventSettings.getProperty("TenthPlaceItemId1", 57); //reward props
		TENTH_PLACE_COUNT1 = eventSettings.getProperty("TenthPlaceItemCount1", 1000); //reward props
		TENTH_PLACE_ID2 = eventSettings.getProperty("TenthPlaceItemId2", 57); //reward props
		TENTH_PLACE_COUNT2 = eventSettings.getProperty("TenthPlaceItemCount2", 1000); //reward props

		EVENT_CrumaFighterBuffs = eventSettings.getProperty("CrumaFightersBuff", "").trim().replaceAll(" ", "").split(";");
		EVENT_CrumaMageBuffs = eventSettings.getProperty("CrumaMageBuff", "").trim().replaceAll(" ", "").split(";");	
		EVENT_CrumaRewards = eventSettings.getProperty("CrumaRewards", "").trim().replaceAll(" ", "").split(";");	
		EVENT_CrumaBuffPlayers = eventSettings.getProperty("CrumaBuffPlayers", true);
		MASS_PVP_MIN_LEVEL = eventSettings.getProperty("MassPvPMinLevel", 1);
		MASS_PVP_MAX_LEVEL = eventSettings.getProperty("MassPvPMaxLevel", 85);
		MAX_CRUMA_MASS_PVP_PLAYERS = eventSettings.getProperty("MassPvPMaxPlayers", 500);
		MAX_PLACES_TO_REWARD = eventSettings.getProperty("MaxPlacesToReward", 10);
		RACE_MIN_LEVEL = eventSettings.getProperty("RaceMinLevel", 1);
		RACE_MAX_LEVEL = eventSettings.getProperty("RaceMaxLevel", 85);
		MAX_CRUMA_RACE_PLAYERS = eventSettings.getProperty("RaceMaxPlayers", 500);
		RACE_KILL_ITEM_ID = eventSettings.getProperty("RaceIdReward", 57);
		RACE_KILL_ITEM_COUNT = eventSettings.getProperty("RaceCountReward", 5000);
		HUNTER_CLUB_MIN_LEVEL = eventSettings.getProperty("HunterClubMinLevel", 1);
		HUNTER_CLUB_MAX_LEVEL = eventSettings.getProperty("HunterClubMaxLevel", 85);		
		MAX_HUNTER_CLUB_PLAYERS = eventSettings.getProperty("HunterClubMaxPlayers", 500);	
		
		EVENT_TVT_ARENA_ENABLED = eventSettings.getProperty("Enabled", false);
		EVENT_TVT_ARENA_TECH_REASON = eventSettings.getProperty("TechReason", 0);
		EVENT_TVT_ARENA_NO_PLAYERS = eventSettings.getProperty("NoPlayers", 0);
		EVENT_TVT_ARENA_TEAM_DRAW = eventSettings.getProperty("Drow", 0);
		EVENT_TVT_ARENA_TEAM_WIN = eventSettings.getProperty("Win", 0);
		EVENT_TVT_ARENA_TEAM_LOSS = eventSettings.getProperty("Loss", 0);
		EVENT_TVT_ARENA_TEAMLEADER_EXIT = eventSettings.getProperty("TeamLeaderExit", 0);
		EVENT_TVT_ARENA_ALLOW_CLAN_SKILL = eventSettings.getProperty("AllowClanSkills", false);
		EVENT_TVT_ARENA_ALLOW_HERO_SKILL = eventSettings.getProperty("AllowHeroSkills", false);
		EVENT_TVT_ARENA_ALLOW_BUFFS = eventSettings.getProperty("AllowBuffs", false);
		EVENT_TVT_ARENA_TEAM_COUNT = eventSettings.getProperty("TeamCount", 0);
		EVENT_TVT_ARENA_TIME_TO_START = eventSettings.getProperty("TimeToStart", 0);
		EVENT_TVT_ARENA_FIGHT_TIME = eventSettings.getProperty("FightTime", 10);
		EVENT_TVT_ARENA_DISABLED_ITEMS = eventSettings.getProperty("DisabledItems", new int[]{10179, 15357, 20394, 21094, 21231, 21232});
		EVENT_TVT_ARENA_TEAM_COUNT_MIN = eventSettings.getProperty("MinTeamCount", 1);
		EVENT_TVT_ARENA_START_TIME = eventSettings.getProperty("EventStartTime", "20:12").trim().replaceAll(" ", "").split(",");
		EVENT_TVT_ARENA_STOP_TIME = eventSettings.getProperty("EventStopTime", "21:12").trim().replaceAll(" ", "").split(",");		
		
		EVENT_TREASURES_OF_THE_HERALD_ENABLE = eventSettings.getProperty("Enable", false);
		EVENT_TREASURES_OF_THE_HERALD_ITEM_ID = eventSettings.getProperty("RewardId", 13067);
		EVENT_TREASURES_OF_THE_HERALD_ITEM_COUNT = eventSettings.getProperty("RewardCount", 30);
		EVENT_TREASURES_OF_THE_HERALD_TIME = eventSettings.getProperty("Time", 1200);
		EVENT_TREASURES_OF_THE_HERALD_MIN_LEVEL = eventSettings.getProperty("MinLevel", 80);
		EVENT_TREASURES_OF_THE_HERALD_MAX_LEVEL = eventSettings.getProperty("MaxLevel", 85);
		EVENT_TREASURES_OF_THE_HERALD_MINIMUM_PARTY_MEMBER = eventSettings.getProperty("MinPartyMember", 6);
		EVENT_TREASURES_OF_THE_HERALD_MAX_GROUP = eventSettings.getProperty("MaxGroup", 100);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_BOX = eventSettings.getProperty("ScoreBox", 20);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_BOSS = eventSettings.getProperty("ScoreBoss", 100);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_KILL = eventSettings.getProperty("ScoreKill", 5);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_DEATH = eventSettings.getProperty("ScoreDeath", 3);		
	}

	public static void loadOlympiadSettings()
	{
		ExProperties olympSettings = load(OLYMPIAD);

		ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
		ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
		OLYMIAD_END_PERIOD_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMIAD_END_PERIOD_TIME", "00 00 01 * *"));
		OLYMPIAD_START_TIME = new SchedulingPattern(olympSettings.getProperty("OLYMPIAD_START_TIME", "00 18 * * *"));
		ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000);
		ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);
		CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 5);
		NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 9);
		TEAM_GAME_MIN = olympSettings.getProperty("TeamGameMin", 4);

		OLY_CLASSED_GAMES_DAYS = olympSettings.getProperty("OLY_CLASSED_GAMES_DAYS", new int[]{ Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY });

		GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 70);
		GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
		GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 60);
		GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeamCountLimit", 10);

		ALT_OLY_REG_DISPLAY = olympSettings.getProperty("AltOlyRegistrationDisplayNumber", 100);
		ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 13722);
		ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
		ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 40);
		ALT_OLY_TEAM_RITEM_C = olympSettings.getProperty("AltOlyTeamRewItemCount", 50);
		ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 13722);
		ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 1000);
		ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 180);
		ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 120);
		ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 80);
		ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 55);
		ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 35);
		ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 20);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 15);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 50);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 10);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);
		UNSUMMON_SUMMONS_OLY = olympSettings.getProperty("UnSummonSummonsOnOlympiad", true);
		OLY_WAIT_TO_TELEPORT = olympSettings.getProperty("OlympiadTotalTeleportWait", 120);
		OLYMPIAD_SHOW_ENEMY_CLASS = olympSettings.getProperty("OLYMPIAD_SHOW_ENEMY_CLASS", false);
	}

	public static void loadBuffStoreConfig()
	{
		ExProperties buffStoreConfig = load(BUFF_STORE_CONFIG_FILE);
    
		BUFF_STORE_ENABLED = buffStoreConfig.getProperty("BuffStoreEnabled", false);
		BUFF_STORE_MP_ENABLED = buffStoreConfig.getProperty("BuffStoreMpEnabled", true);
		BUFF_STORE_MP_CONSUME_MULTIPLIER = buffStoreConfig.getProperty("BuffStoreMpConsumeMultiplier", 1.0D);
		BUFF_STORE_ITEM_CONSUME_ENABLED = buffStoreConfig.getProperty("BuffStoreItemConsumeEnabled", true);
    
		BUFF_STORE_NAME_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreNameColor", "808080")).intValue();
		BUFF_STORE_TITLE_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreTitleColor", "808080")).intValue();
		BUFF_STORE_OFFLINE_NAME_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreOfflineNameColor", "808080")).intValue();

		String[] classes = buffStoreConfig.getProperty("BuffStoreAllowedClassList", "").split(",");
		BUFF_STORE_ALLOWED_CLASS_LIST = new ArrayList<Integer>();
		if (classes.length > 0)
		{
			for(String classId : classes)
				BUFF_STORE_ALLOWED_CLASS_LIST.add(Integer.valueOf(Integer.parseInt(classId)));
		}
		BUFF_STORE_ALLOWED_SKILL_LIST.addAll(StringArrayUtils.stringToIntArray(buffStoreConfig.getProperty("BUFF_STORE_ALLOWED_SKILL_LIST", ""), ","));
	}	
	
	public static void loadPcBangSettings()
	{
		ExProperties eventPcBangSettings = load(EVENT_PC_BANG_FILE);

		ALT_PCBANG_POINTS_ENABLED = eventPcBangSettings.getProperty("AltPcBangPointsEnabled", false);
		ALT_PCBANG_PA_ONLY = eventPcBangSettings.getProperty("AltPcBangPAOnly", false);
		ALT_MAX_PC_BANG_POINTS = eventPcBangSettings.getProperty("AltPcBangPointsMaxCount", 20000);
		ALT_PCBANG_POINTS_ON_START = eventPcBangSettings.getProperty("AltPcBangPointsOnStart", 300);
		ALT_PCBANG_POINTS_BONUS = eventPcBangSettings.getProperty("AltPcBangPointsBonus", 100);
		ALT_PCBANG_POINTS_DELAY = eventPcBangSettings.getProperty("AltPcBangPointsDelay", 5);
		ALT_PCBANG_POINTS_MIN_LVL = eventPcBangSettings.getProperty("AltPcBangPointsMinLvl", 1);
		ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = eventPcBangSettings.getProperty("AltPcBangPointsDoubleChance", 10.);
		ALT_PCBANG_POINTS_MAX_CODE_ENTER_ATTEMPTS = eventPcBangSettings.getProperty("AltPcBangPointsMaxEnterAttempts", 5);
		ALT_PCBANG_POINTS_BAN_TIME = eventPcBangSettings.getProperty("AltPcBangPointsBanTime", 480);
		PC_BANG_TO_ITEMMALL = eventPcBangSettings.getProperty("AltPcBangPointToItemMall", false);
		PC_BANG_TO_ITEMMALL_RATE = eventPcBangSettings.getProperty("AltPcBangPointToItemMallRate", 100);
		PC_BANG_ENCHANT_MAX = eventPcBangSettings.getProperty("AltPcBangEnchantMaxLevel", 23);
		PC_BANG_SAFE_ENCHANT = eventPcBangSettings.getProperty("AltPcBangEnchantSafeLevel", 3);
		ALT_PC_BANG_WIVERN_PRICE = eventPcBangSettings.getProperty("AltPcBangWiwernRentPrice", 2500);
		ALT_PC_BANG_WIVERN_TIME = eventPcBangSettings.getProperty("AltPcBangWiwernRentTime", 5);
	}

	public static void loadSGuardSettings()
	{
		ExProperties properties = load(SGUARD_FILE);

		ENABLE_DAM_ON_SCREEN = properties.getProperty("ENABLE_DAM_ON_SCREEN", false);
		DAM_ON_SCREEN_FONT = properties.getProperty("DAM_ON_SCREEN_FONT", 3);
		DAM_ON_SCREEN_FONT_COLOR_ATTACKER = properties.getProperty("DAM_ON_SCREEN_FONT_COLOR_ATTACKER", 16777215);
		DAM_ON_SCREEN_FONT_COLOR_TARGET = properties.getProperty("DAM_ON_SCREEN_FONT_COLOR_TARGET", 16711680);
	}
	
	public static void load()
	{
		loadServerConfig();
		loadTelnetConfig();
		loadResidenceConfig();
		loadAntiFloodConfig();
		loadOtherConfig();
		loadSpoilConfig();
		loadFormulasConfig();
		loadAltSettings();
		loadServicesSettings();
		loadPvPSettings();
		loadAISettings();
		loadGeodataSettings();
		loadEventsSettings();
		loadOlympiadSettings();
		loadExtSettings();
		loadFightClubSettings();
		loadSchemeBuffer();
		loadBBSSettings();
		loadl2scriptsSettings();
		loadAntiBotSettings();
		loadDragonValleyZoneSettings();
		loadLairOfAntharasZoneSettings();
		abuseLoad();
		loadGMAccess();
		pvpManagerSettings();
		newCpPanelSettings();
		loadBuffStoreConfig();
		loadPcBangSettings();
		loadSGuardSettings();
	}

	private Config()
	{}

	public static void abuseLoad()
	{
		List<Pattern> tmp = new ArrayList<Pattern>();

		LineNumberReader lnr = null;
		try
		{
			String line;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(ANUSEWORDS_CONFIG_FILE), "UTF-8"));

			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}

			ABUSEWORD_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e2)
			{
				// nothing
			}
		}
	}
	

	public static void loadAntiBotSettings()
	{
		ExProperties botSettings = load(BOT_FILE);
		
		ENABLE_ANTI_BOT_SYSTEM = botSettings.getProperty("EnableAntiBotSystem", true);
		ASK_ANSWER_DELAY = botSettings.getProperty("ASK_ANSWER_DELAY", 3);
		MINIMUM_TIME_QUESTION_ASK = botSettings.getProperty("MinimumTimeQuestionAsk", 60);
		MAXIMUM_TIME_QUESTION_ASK = botSettings.getProperty("MaximumTimeQuestionAsk", 120);
		MINIMUM_BOT_POINTS_TO_STOP_ASKING = botSettings.getProperty("MinimumBotPointsToStopAsking", 10);
		MAXIMUM_BOT_POINTS_TO_STOP_ASKING = botSettings.getProperty("MaximumBotPointsToStopAsking", 15);
		MAX_BOT_POINTS = botSettings.getProperty("MaxBotPoints", 15);
		MINIMAL_BOT_RATING_TO_BAN = botSettings.getProperty("MinimalBotPointsToBan", -5);
		AUTO_BOT_BAN_JAIL_TIME = botSettings.getProperty("AutoBanJailTime", 24);
		ANNOUNCE_AUTO_BOT_BAN = botSettings.getProperty("AnounceAutoBan", true);
		ON_WRONG_QUESTION_KICK = botSettings.getProperty("IfWrongKick", true);
	}

	public static void loadFightClubSettings()
	{
		ExProperties eventFightClubSettings = load(FIGHT_CLUB_FILE);

		FIGHT_CLUB_ENABLED = eventFightClubSettings.getProperty("FightClubEnabled", false);
		MINIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MinimumLevel", 1);
		MAXIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MaximumLevel", 85);
		MAXIMUM_LEVEL_DIFFERENCE = eventFightClubSettings.getProperty("MaximumLevelDifference", 10);
		ALLOWED_RATE_ITEMS = eventFightClubSettings.getProperty("AllowedItems", "").trim().replaceAll(" ", "").split(",");
		PLAYERS_PER_PAGE = eventFightClubSettings.getProperty("RatesOnPage", 10);
		ARENA_TELEPORT_DELAY = eventFightClubSettings.getProperty("ArenaTeleportDelay", 5);
		CANCEL_BUFF_BEFORE_FIGHT = eventFightClubSettings.getProperty("CancelBuffs", true);
		UNSUMMON_PETS = eventFightClubSettings.getProperty("UnsummonPets", true);
		UNSUMMON_SUMMONS = eventFightClubSettings.getProperty("UnsummonSummons", true);
		REMOVE_CLAN_SKILLS = eventFightClubSettings.getProperty("RemoveClanSkills", false);
		REMOVE_HERO_SKILLS = eventFightClubSettings.getProperty("RemoveHeroSkills", false);
		TIME_TO_PREPARATION = eventFightClubSettings.getProperty("TimeToPreparation", 10);
		FIGHT_TIME = eventFightClubSettings.getProperty("TimeToDraw", 300);
		ALLOW_DRAW = eventFightClubSettings.getProperty("AllowDraw", true);
		TIME_TELEPORT_BACK = eventFightClubSettings.getProperty("TimeToBack", 10);
		FIGHT_CLUB_ANNOUNCE_RATE = eventFightClubSettings.getProperty("AnnounceRate", false);
		FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceRateToAllScreen", false);
		FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceStartBatleToAllScreen", false);
	}

	public static void loadSchemeBuffer()
	{
		ExProperties npcbuffer = load(SCHEME_BUFFER_FILE);

		NpcBuffer_VIP = npcbuffer.getProperty("EnableVIP", false);
		NpcBuffer_VIP_ALV = npcbuffer.getProperty("VipAccesLevel", 1);
		NpcBuffer_EnableBuff = npcbuffer.getProperty("EnableBuffSection", true);
		NpcBuffer_EnableScheme = npcbuffer.getProperty("EnableScheme", true);
		NpcBuffer_EnableHeal = npcbuffer.getProperty("EnableHeal", true);
		NpcBuffer_EnableBuffs = npcbuffer.getProperty("EnableBuffs", true);
		NpcBuffer_EnableResist = npcbuffer.getProperty("EnableResist", true);
		NpcBuffer_EnableSong = npcbuffer.getProperty("EnableSongs", true);
		NpcBuffer_EnableDance = npcbuffer.getProperty("EnableDances", true);
		NpcBuffer_EnableChant = npcbuffer.getProperty("EnableChants", true);
		NpcBuffer_EnableOther = npcbuffer.getProperty("EnableOther", true);
		NpcBuffer_EnableSpecial = npcbuffer.getProperty("EnableSpecial", true);
		NpcBuffer_EnableCubic = npcbuffer.getProperty("EnableCubic", true);
		NpcBuffer_EnableCancel = npcbuffer.getProperty("EnableRemoveBuffs", true);
		NpcBuffer_EnableBuffSet = npcbuffer.getProperty("EnableBuffSet", true);
		NpcBuffer_EnableBuffPK = npcbuffer.getProperty("EnableBuffForPK", false);
		NpcBuffer_EnableFreeBuffs = npcbuffer.getProperty("EnableFreeBuffs", true);
		NpcBuffer_EnableTimeOut = npcbuffer.getProperty("EnableTimeOut", true);
		SCHEME_ALLOW_FLAG = npcbuffer.getProperty("EnableBuffforFlag", false);
		NpcBuffer_TimeOutTime = npcbuffer.getProperty("TimeoutTime", 10);
		NpcBuffer_MinLevel = npcbuffer.getProperty("MinimumLevel", 20);
		NpcBuffer_PriceCancel = npcbuffer.getProperty("RemoveBuffsPrice", 100000);
		NpcBuffer_PriceHeal = npcbuffer.getProperty("HealPrice", 100000);
		NpcBuffer_PriceBuffs = npcbuffer.getProperty("BuffsPrice", 100000);
		NpcBuffer_PriceResist = npcbuffer.getProperty("ResistPrice", 100000);
		NpcBuffer_PriceSong = npcbuffer.getProperty("SongPrice", 100000);
		NpcBuffer_PriceDance = npcbuffer.getProperty("DancePrice", 100000);
		NpcBuffer_PriceChant = npcbuffer.getProperty("ChantsPrice", 100000);
		NpcBuffer_PriceOther = npcbuffer.getProperty("OtherPrice", 100000);
		NpcBuffer_PriceSpecial = npcbuffer.getProperty("SpecialPrice", 100000);
		NpcBuffer_PriceCubic = npcbuffer.getProperty("CubicPrice", 100000);
		NpcBuffer_PriceSet = npcbuffer.getProperty("SetPrice", 100000);
		NpcBuffer_PriceScheme = npcbuffer.getProperty("SchemePrice", 100000);
		NpcBuffer_MaxScheme = npcbuffer.getProperty("MaxScheme", 4);
		IS_DISABLED_IN_REFLECTION = npcbuffer.getProperty("DisableBufferInReflection", true);
	}
	
	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File(GM_PERSONAL_ACCESS_FILE));
		File dir = new File(GM_ACCESS_FILES_DIR);
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists.");
			return;
		}
		for(File f : dir.listFiles())
			// hidden файлы НЕ игнорируем
			if(!f.isDirectory() && f.getName().endsWith(".xml"))
				loadGMAccess(f);
	}

	public static void loadGMAccess(File file)
	{
		try
		{
			Field fld;
			//File file = new File(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);

			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if(!n.getNodeName().equalsIgnoreCase("char"))
						continue;

					PlayerAccess pa = new PlayerAccess();
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						Class<?> cls = pa.getClass();
						String node = d.getNodeName();

						if(node.equalsIgnoreCase("#text"))
							continue;
						try
						{
							fld = cls.getField(node);
						}
						catch(NoSuchFieldException e)
						{
							_log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
							continue;
						}

						if(fld.getType().getName().equalsIgnoreCase("boolean"))
							fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
						else if(fld.getType().getName().equalsIgnoreCase("int"))
							fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()));
					}
					gmlist.put(pa.PlayerID, pa);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getField(String fieldName)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return null;

		try
		{
			return String.valueOf(field.get(null));
		}
		catch(IllegalArgumentException e)
		{

		}
		catch(IllegalAccessException e)
		{

		}

		return null;
	}

	public static boolean setField(String fieldName, String value)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return false;

		try
		{
			if(field.getType() == boolean.class)
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			else if(field.getType() == int.class)
				field.setInt(null, NumberUtils.toInt(value));
			else if(field.getType() == long.class)
				field.setLong(null, NumberUtils.toLong(value));
			else if(field.getType() == double.class)
				field.setDouble(null, NumberUtils.toDouble(value));
			else if(field.getType() == String.class)
				field.set(null, value);
			else
				return false;
		}
		catch(IllegalArgumentException e)
		{
			return false;
		}
		catch(IllegalAccessException e)
		{
			return false;
		}

		return true;
	}

	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}

	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();

		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}

		return result;
	}

	public static boolean containsAbuseWord(String s)
	{
		for(Pattern pattern : ABUSEWORD_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}
}