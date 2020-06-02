package handler.bbs.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.configuration.ExProperties;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.scripts.ScriptConfig;
import l2s.gameserver.templates.item.ItemTemplate.Grade;

/**
 * @author Bonux
**/
public class BBSConfig implements ScriptConfig
{
	private static final Logger _log = LoggerFactory.getLogger(BBSConfig.class);

	private static final String PROPERTIES_FILE = "config/bbs.properties";

	public static boolean CUSTOM_COMMUNITY_ENABLED;

	public static boolean GLOBAL_USE_FUNCTIONS_CONFIGS;
	public static boolean CAN_USE_FUNCTIONS_WHEN_DEAD;
	public static boolean CAN_USE_FUNCTIONS_IN_A_BATTLE;
	public static boolean CAN_USE_FUNCTIONS_IN_PVP;
	public static boolean CAN_USE_FUNCTIONS_ON_OLLYMPIAD;
	public static boolean CAN_USE_FUNCTIONS_IF_FLIGHT;
	public static boolean CAN_USE_FUNCTIONS_IF_IN_VEHICLE;
	public static boolean CAN_USE_FUNCTIONS_IF_MOUNTED;
	public static boolean CAN_USE_FUNCTIONS_IF_CANNOT_MOVE;
	public static boolean CAN_USE_FUNCTIONS_WHEN_IN_TRADE;
	public static boolean CAN_USE_FUNCTIONS_WHEN_FISHING;
	public static boolean CAN_USE_FUNCTIONS_IF_TELEPORTING;
	public static boolean CAN_USE_FUNCTIONS_IN_DUEL;
	public static boolean CAN_USE_FUNCTIONS_WITH_CURSED_WEAPON;
	public static boolean CAN_USE_FUNCTIONS_WHEN_IS_PK;
	public static boolean CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY;
	public static boolean CAN_USE_FUNCTIONS_NOBLESSES_ONLY;
	public static boolean CAN_USE_FUNCTIONS_ON_SIEGE;
	public static boolean CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY;
	public static boolean CAN_USE_FUNCTIONS_IN_EVENTS;

	public static boolean BUFF_SERVICE_ALLOW_RESTORE;
	public static boolean BUFF_SERVICE_ALLOW_CANCEL_BUFFS;
	public static boolean BUFF_SERVICE_CAN_CANCEL_NOT_DISPELLABLE_BUFFS;
	public static int BUFF_SERVICE_COST_ITEM_ID;
	public static long BUFF_SERVICE_COST_ITEM_COUNT;
	public static int BUFF_SERVICE_MAX_BUFFS_IN_SET;
	public static int BUFF_SERVICE_MAX_BUFF_SETS_PER_CHAR;
	public static int BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF;

	public static int TELEPORT_SERVICE_COST_ITEM_ID;
	public static long TELEPORT_SERVICE_COST_ITEM_COUNT;
	public static boolean TELEPORT_SERVICE_FOR_PREMIUM_ONLY;
	public static int TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID;
	public static long TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT;
	public static int TELEPORT_SERVICE_BM_SAVE_LIMIT;
	public static int TELEPORT_SERVICE_BM_COST_ITEM_ID;
	public static long TELEPORT_SERVICE_BM_COST_ITEM_COUNT;
	public static boolean TELEPORT_SERVICE_TELEPORT_IF_PK;

	public static int OCCUPATION_SERVICE_COST_ITEM_ID_1;
	public static long OCCUPATION_SERVICE_COST_ITEM_COUNT_1;
	public static int OCCUPATION_SERVICE_COST_ITEM_ID_2;
	public static long OCCUPATION_SERVICE_COST_ITEM_COUNT_2;
	public static int OCCUPATION_SERVICE_COST_ITEM_ID_3;
	public static long OCCUPATION_SERVICE_COST_ITEM_COUNT_3;

	public static int NOBLE_SERVICE_COST_ITEM_ID;
	public static long NOBLE_SERVICE_COST_ITEM_COUNT;

	public static int SUBCLASS_SERVICE_COST_ITEM_ID;
	public static long SUBCLASS_SERVICE_COST_ITEM_COUNT;

	public static int CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID;
	public static long CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT;

	public static int CHANGE_PET_NAME_SERVICE_COST_ITEM_ID;
	public static long CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT;

	public static int CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID;
	public static long CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT;

	public static int COLOR_NAME_SERVICE_COST_ITEM_ID;
	public static long COLOR_NAME_SERVICE_COST_ITEM_COUNT;
	public static String[] COLOR_NAME_SERVICE_COLORS;

	public static int COLOR_TITLE_SERVICE_COST_ITEM_ID;
	public static long COLOR_TITLE_SERVICE_COST_ITEM_COUNT;
	public static String[] COLOR_TITLE_SERVICE_COLORS;

	public static int ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_1_DAY;
	public static long ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_1_DAY;
	public static int[] ADD_WINDOW_SERVICE_PERIOD_VARIATIONS;
	public static int ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_FOREVER;
	public static long ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_FOREVER;

	public static int SALON_SERVICE_COST_ITEM_ID;
	public static long SALON_SERVICE_COST_ITEM_COUNT;
	public static boolean SALON_SERVICE_ACTIVE;

	public static int CHANGE_SEX_SERVICE_COST_ITEM_ID;
	public static long CHANGE_SEX_SERVICE_COST_ITEM_COUNT;

	public static int EXPAND_INVENTORY_SERVICE_COST_ITEM_ID;
	public static long EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT;

	public static int EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID;
	public static long EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT;

	public static int EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID;
	public static long EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT;

	public static long[][] CLAN_REPUTATION_SERVICE_PRICES_LIST;

	public static int KARMA_PK_SERVICE_COST_ITEM_ID;
	public static long KARMA_PK_SERVICE_COST_ITEM_COUNT;

	public static int CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_ID;
	public static long CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_COUNT;
	public static int[] CHANGE_VISUAL_ITEM_SERVICE_VISUAL_LIST;

	public static int STATISTIC_REFRESH_TIME;
	public static int STATISTIC_TOP_PK_COUNT;
	public static int STATISTIC_TOP_PVP_COUNT;
	public static int STATISTIC_TOP_LVL_COUNT;
	public static int STATISTIC_TOP_ADENA_COUNT;
	public static int STATISTIC_TOP_ONLINE_COUNT;
	public static int STATISTIC_TOP_ITEM_COUNT;
	public static int STATISTIC_TOP_OLYMPIAD_COUNT;
	public static int STATISTIC_BY_ITEM_ID;

	public static final IntObjectMap<int[][]> ENCHANT_SERVICE_WEAPON_ENCHANT_COST = new HashIntObjectMap<int[][]>();
	public static final IntObjectMap<int[][]> ENCHANT_SERVICE_ARMOR_ENCHANT_COST = new HashIntObjectMap<int[][]>();
	public static final IntObjectMap<int[][]> ENCHANT_SERVICE_JEWELRY_ENCHANT_COST = new HashIntObjectMap<int[][]>();

	public static boolean LEVEL_SERVICE_ENABLED;
	public static int[][] LEVEL_RISE_SERVICE_ITEM = new int[Experience.getMaxLevel() + 1][];
	public static int[][] LEVEL_DOWN_SERVICE_ITEM = new int[Experience.getMaxLevel() + 1][];

	public static int AUGMENT_SERVICE_COST_ITEM_ID;
	public static long AUGMENT_SERVICE_COST_ITEM_COUNT;
	public static int[] AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR;
	public static int[] AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR;
	public static int[] AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE;
	public static int[] AUGMENT_SERVICE_STATS_VARIATIONS_MAGE;

	@Override
	public void load()
	{
		ExProperties properties = Config.load(PROPERTIES_FILE);

		CUSTOM_COMMUNITY_ENABLED = properties.getProperty("CUSTOM_COMMUNITY_ENABLED", false);

		// Global
		GLOBAL_USE_FUNCTIONS_CONFIGS = properties.getProperty("GLOBAL_USE_FUNCTIONS_CONFIGS", false);
		CAN_USE_FUNCTIONS_WHEN_DEAD = properties.getProperty("CAN_USE_FUNCTIONS_WHEN_DEAD", true);
		CAN_USE_FUNCTIONS_IN_A_BATTLE = properties.getProperty("CAN_USE_FUNCTIONS_IN_A_BATTLE", true);
		CAN_USE_FUNCTIONS_IN_PVP = properties.getProperty("CAN_USE_FUNCTIONS_IN_PVP", true);
		CAN_USE_FUNCTIONS_ON_OLLYMPIAD = properties.getProperty("CAN_USE_FUNCTIONS_ON_OLLYMPIAD", true);
		CAN_USE_FUNCTIONS_IF_FLIGHT = properties.getProperty("CAN_USE_FUNCTIONS_IF_FLIGHT", true);
		CAN_USE_FUNCTIONS_IF_IN_VEHICLE = properties.getProperty("CAN_USE_FUNCTIONS_IF_IN_VEHICLE", true);
		CAN_USE_FUNCTIONS_IF_MOUNTED = properties.getProperty("CAN_USE_FUNCTIONS_IF_MOUNTED", true);
		CAN_USE_FUNCTIONS_IF_CANNOT_MOVE = properties.getProperty("CAN_USE_FUNCTIONS_IF_CANNOT_MOVE", true);
		CAN_USE_FUNCTIONS_WHEN_IN_TRADE = properties.getProperty("CAN_USE_FUNCTIONS_WHEN_IN_TRADE", true);
		CAN_USE_FUNCTIONS_WHEN_FISHING = properties.getProperty("CAN_USE_FUNCTIONS_WHEN_FISHING", true);
		CAN_USE_FUNCTIONS_IF_TELEPORTING = properties.getProperty("CAN_USE_FUNCTIONS_IF_TELEPORTING", true);
		CAN_USE_FUNCTIONS_IN_DUEL = properties.getProperty("CAN_USE_FUNCTIONS_IN_DUEL", true);
		CAN_USE_FUNCTIONS_WITH_CURSED_WEAPON = properties.getProperty("CAN_USE_FUNCTIONS_WITH_CURSED_WEAPON", true);
		CAN_USE_FUNCTIONS_WHEN_IS_PK = properties.getProperty("CAN_USE_FUNCTIONS_WHEN_IS_PK", true);
		CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY = properties.getProperty("CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY", false);
		CAN_USE_FUNCTIONS_NOBLESSES_ONLY = properties.getProperty("CAN_USE_FUNCTIONS_NOBLESSES_ONLY", false);
		CAN_USE_FUNCTIONS_ON_SIEGE = properties.getProperty("CAN_USE_FUNCTIONS_ON_SIEGE", true);
		CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY = properties.getProperty("CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY", false);
		CAN_USE_FUNCTIONS_IN_EVENTS = properties.getProperty("CAN_USE_FUNCTIONS_IN_EVENTS", false);

		// Buff service
		BUFF_SERVICE_ALLOW_RESTORE = properties.getProperty("BUFF_SERVICE_ALLOW_RESTORE", true);
		BUFF_SERVICE_ALLOW_CANCEL_BUFFS = properties.getProperty("BUFF_SERVICE_ALLOW_CANCEL_BUFFS", true);
		BUFF_SERVICE_CAN_CANCEL_NOT_DISPELLABLE_BUFFS = properties.getProperty("BUFF_SERVICE_CAN_CANCEL_NOT_DISPELLABLE_BUFFS", false);
		BUFF_SERVICE_COST_ITEM_ID = properties.getProperty("BUFF_SERVICE_COST_ITEM_ID", 57);
		BUFF_SERVICE_COST_ITEM_COUNT = properties.getProperty("BUFF_SERVICE_COST_ITEM_COUNT", 1000L);
		BUFF_SERVICE_MAX_BUFFS_IN_SET = properties.getProperty("BUFF_SERVICE_MAX_BUFFS_IN_SET", 20);
		BUFF_SERVICE_MAX_BUFF_SETS_PER_CHAR = properties.getProperty("BUFF_SERVICE_MAX_BUFF_SETS_PER_CHAR", 8);
		BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF = properties.getProperty("BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF", 84);

		// Teleport service
		TELEPORT_SERVICE_COST_ITEM_ID = properties.getProperty("TELEPORT_SERVICE_COST_ITEM_ID", 57);
		TELEPORT_SERVICE_COST_ITEM_COUNT = properties.getProperty("TELEPORT_SERVICE_COST_ITEM_COUNT", 10000);
		TELEPORT_SERVICE_FOR_PREMIUM_ONLY = properties.getProperty("TELEPORT_SERVICE_FOR_PREMIUM_ONLY", false);
		TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID = properties.getProperty("TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID", 57);
		TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT = properties.getProperty("TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT", 1000000);
		TELEPORT_SERVICE_BM_SAVE_LIMIT = properties.getProperty("TELEPORT_SERVICE_BM_SAVE_LIMIT", 10);
		TELEPORT_SERVICE_BM_COST_ITEM_ID = properties.getProperty("TELEPORT_SERVICE_BM_COST_ITEM_ID", 57);
		TELEPORT_SERVICE_BM_COST_ITEM_COUNT = properties.getProperty("TELEPORT_SERVICE_BM_COST_ITEM_COUNT", 100000);
		TELEPORT_SERVICE_TELEPORT_IF_PK = properties.getProperty("TELEPORT_SERVICE_TELEPORT_IF_PK", false);

		// Occupation purchase service
		OCCUPATION_SERVICE_COST_ITEM_ID_1 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_ID_1", 57);
		OCCUPATION_SERVICE_COST_ITEM_COUNT_1 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_COUNT_1", 10000L);
		OCCUPATION_SERVICE_COST_ITEM_ID_2 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_ID_2", 57);
		OCCUPATION_SERVICE_COST_ITEM_COUNT_2 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_COUNT_2", 1000000L);
		OCCUPATION_SERVICE_COST_ITEM_ID_3 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_ID_3", 57);
		OCCUPATION_SERVICE_COST_ITEM_COUNT_3 = properties.getProperty("OCCUPATION_SERVICE_COST_ITEM_COUNT_3", 100000000L);

		// Noble purchase service
		NOBLE_SERVICE_COST_ITEM_ID = properties.getProperty("NOBLE_SERVICE_COST_ITEM_ID", 57);
		NOBLE_SERVICE_COST_ITEM_COUNT = properties.getProperty("NOBLE_SERVICE_COST_ITEM_COUNT", 1000000000L);

		// Subclass purchase service
		SUBCLASS_SERVICE_COST_ITEM_ID = properties.getProperty("SUBCLASS_SERVICE_COST_ITEM_ID", 57);
		SUBCLASS_SERVICE_COST_ITEM_COUNT = properties.getProperty("SUBCLASS_SERVICE_COST_ITEM_COUNT", 100000000L);

		// Change player name service
		CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID = properties.getProperty("CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID", 57);
		CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT = properties.getProperty("CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT", 100000000L);

		// Change per name service
		CHANGE_PET_NAME_SERVICE_COST_ITEM_ID = properties.getProperty("CHANGE_PET_NAME_SERVICE_COST_ITEM_ID", 57);
		CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT = properties.getProperty("CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT", 100000000L);

		// Change clan name service
		CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID = properties.getProperty("CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID", 57);
		CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT = properties.getProperty("CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT", 100000000L);

		// Color name service
		COLOR_NAME_SERVICE_COST_ITEM_ID = properties.getProperty("COLOR_NAME_SERVICE_COST_ITEM_ID", 57);
		COLOR_NAME_SERVICE_COST_ITEM_COUNT = properties.getProperty("COLOR_NAME_SERVICE_COST_ITEM_COUNT", 100000000L);
		COLOR_NAME_SERVICE_COLORS = properties.getProperty("COLOR_NAME_SERVICE_COLORS", new String[0], ";");

		// Color title service
		COLOR_TITLE_SERVICE_COST_ITEM_ID = properties.getProperty("COLOR_TITLE_SERVICE_COST_ITEM_ID", 57);
		COLOR_TITLE_SERVICE_COST_ITEM_COUNT = properties.getProperty("COLOR_TITLE_SERVICE_COST_ITEM_COUNT", 100000000L);
		COLOR_TITLE_SERVICE_COLORS = properties.getProperty("COLOR_TITLE_SERVICE_COLORS", new String[0], ";");

		// Additional active windows service
		ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_1_DAY = properties.getProperty("ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_1_DAY", 57);
		ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_1_DAY = properties.getProperty("ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_1_DAY", 100000000L);
		ADD_WINDOW_SERVICE_PERIOD_VARIATIONS = properties.getProperty("ADD_WINDOW_SERVICE_PERIOD_VARIATIONS", new int[]{ 1 }, ",");
		ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_FOREVER = properties.getProperty("ADD_WINDOW_SERVICE_COST_ITEM_ID_PER_FOREVER", 57);
		ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_FOREVER = properties.getProperty("ADD_WINDOW_SERVICE_COST_ITEM_COUNT_PER_FOREVER", 10000000000L);

		// Salon service
		SALON_SERVICE_COST_ITEM_ID = properties.getProperty("SALON_SERVICE_COST_ITEM_ID", 57);
		SALON_SERVICE_COST_ITEM_COUNT = properties.getProperty("SALON_SERVICE_COST_ITEM_COUNT", 1000L);
		SALON_SERVICE_ACTIVE = properties.getProperty("SALON_SERVICE_ACTIVE", false);

		// Change sex service
		CHANGE_SEX_SERVICE_COST_ITEM_ID = properties.getProperty("CHANGE_SEX_SERVICE_COST_ITEM_ID", 57);
		CHANGE_SEX_SERVICE_COST_ITEM_COUNT = properties.getProperty("CHANGE_SEX_SERVICE_COST_ITEM_COUNT", 100000000L);

		// Expand inventory service
		EXPAND_INVENTORY_SERVICE_COST_ITEM_ID = properties.getProperty("EXPAND_INVENTORY_SERVICE_COST_ITEM_ID", 57);
		EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT = properties.getProperty("EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT", 100000L);

		// Expand warehouse service
		EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID = properties.getProperty("EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID", 57);
		EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT = properties.getProperty("EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT", 100000L);

		// Expand clan warehouse service
		EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID = properties.getProperty("EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID", 57);
		EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT = properties.getProperty("EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT", 100000L);

		CLAN_REPUTATION_SERVICE_PRICES_LIST = new long[0][];

		Pattern p = Pattern.compile("([0-9]+)-([0-9]+)\\[([0-9]+)\\];?", Pattern.DOTALL);
		Matcher m = p.matcher(properties.getProperty("CLAN_REPUTATION_SERVICE_PRICES_LIST", ""));
		while(m.find())
		{
			CLAN_REPUTATION_SERVICE_PRICES_LIST = ArrayUtils.add(CLAN_REPUTATION_SERVICE_PRICES_LIST, new long[]{ Long.parseLong(m.group(1)), Long.parseLong(m.group(2)), Long.parseLong(m.group(3)) });
		}

		// Expand warehouse service
		KARMA_PK_SERVICE_COST_ITEM_ID = properties.getProperty("KARMA_PK_SERVICE_COST_ITEM_ID", 57);
		KARMA_PK_SERVICE_COST_ITEM_COUNT = properties.getProperty("KARMA_PK_SERVICE_COST_ITEM_COUNT", 100000L);

		// Change item visual service
		CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_ID = properties.getProperty("CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_ID", 57);
		CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_COUNT = properties.getProperty("CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_COUNT", 100000L);
		CHANGE_VISUAL_ITEM_SERVICE_VISUAL_LIST = properties.getProperty("CHANGE_VISUAL_ITEM_SERVICE_VISUAL_LIST", new int[0]);

		// Game statistic
		STATISTIC_REFRESH_TIME = properties.getProperty("STATISTIC_REFRESH_TIME", 180);
		STATISTIC_TOP_PK_COUNT = properties.getProperty("STATISTIC_TOP_PK_COUNT", 20);
		STATISTIC_TOP_PVP_COUNT = properties.getProperty("STATISTIC_TOP_PVP_COUNT", 20);
		STATISTIC_TOP_LVL_COUNT = properties.getProperty("STATISTIC_TOP_LVL_COUNT", 20);
		STATISTIC_TOP_ADENA_COUNT = properties.getProperty("STATISTIC_TOP_ADENA_COUNT", 20);
		STATISTIC_TOP_ONLINE_COUNT = properties.getProperty("STATISTIC_TOP_ONLINE_COUNT", 20);
		STATISTIC_TOP_ITEM_COUNT = properties.getProperty("STATISTIC_TOP_ITEM_COUNT", 20);
		STATISTIC_TOP_OLYMPIAD_COUNT = properties.getProperty("STATISTIC_TOP_OLYMPIAD_COUNT", 20);
		STATISTIC_BY_ITEM_ID = properties.getProperty("STATISTIC_BY_ITEM_ID", 4037);

		// Enchant service
		ENCHANT_SERVICE_WEAPON_ENCHANT_COST.clear();
		ENCHANT_SERVICE_ARMOR_ENCHANT_COST.clear();
		ENCHANT_SERVICE_JEWELRY_ENCHANT_COST.clear();

		for(Grade grade : Grade.VALUES)
		{
			if(grade.ordinal() != grade.externalOrdinal) // Берем только основные грейды.
				continue;

			final int weaponMaxEnchantLevel = properties.getProperty("ENCHANT_SERVICE_" + grade + "_WEAPON_MAX_ENCHANT_LEVEL", grade == Grade.NONE ? 0 : 16);
			if(weaponMaxEnchantLevel > 0)
			{
				final int[][] enchantCost = new int[weaponMaxEnchantLevel + 1][];
				int[] previousCost = new int[]{ 0, 0 };
				for(int i = 1; i <= weaponMaxEnchantLevel; i++)
				{
					String costStr = properties.getProperty("ENCHANT_SERVICE_" + grade + "_WEAPON_ENCHANT_COST_" + i, "");
					if(!StringUtils.isEmpty(costStr))
						previousCost = StringArrayUtils.stringToIntArray(costStr, "-");
					enchantCost[i] = previousCost;
				}
				ENCHANT_SERVICE_WEAPON_ENCHANT_COST.put(grade.ordinal(), enchantCost);
			}

			final int armorMaxEnchantLevel = properties.getProperty("ENCHANT_SERVICE_" + grade + "_ARMOR_MAX_ENCHANT_LEVEL", grade == Grade.NONE ? 0 : 16);
			if(armorMaxEnchantLevel > 0)
			{
				final int[][] enchantCost = new int[armorMaxEnchantLevel + 1][];
				int[] previousCost = new int[]{ 0, 0 };
				for(int i = 1; i <= armorMaxEnchantLevel; i++)
				{
					String costStr = properties.getProperty("ENCHANT_SERVICE_" + grade + "_ARMOR_ENCHANT_COST_" + i, "");
					if(!StringUtils.isEmpty(costStr))
						previousCost = StringArrayUtils.stringToIntArray(costStr, "-");
					enchantCost[i] = previousCost;
				}
				ENCHANT_SERVICE_ARMOR_ENCHANT_COST.put(grade.ordinal(), enchantCost);
			}

			final int jewelryMaxEnchantLevel = properties.getProperty("ENCHANT_SERVICE_" + grade + "_JEWELRY_MAX_ENCHANT_LEVEL", grade == Grade.NONE ? 0 : 16);
			if(jewelryMaxEnchantLevel > 0)
			{
				final int[][] enchantCost = new int[jewelryMaxEnchantLevel + 1][];
				int[] previousCost = new int[]{ 0, 0 };
				for(int i = 1; i <= jewelryMaxEnchantLevel; i++)
				{
					String costStr = properties.getProperty("ENCHANT_SERVICE_" + grade + "_JEWELRY_ENCHANT_COST_" + i, "");
					if(!StringUtils.isEmpty(costStr))
						previousCost = StringArrayUtils.stringToIntArray(costStr, "-");
					enchantCost[i] = previousCost;
				}
				ENCHANT_SERVICE_JEWELRY_ENCHANT_COST.put(grade.ordinal(), enchantCost);
			}
		}

		// Level raise / down service
		LEVEL_SERVICE_ENABLED = properties.getProperty("LEVEL_SERVICE_ENABLED", false);
		LEVEL_RISE_SERVICE_ITEM[0] = new int[2];
		LEVEL_DOWN_SERVICE_ITEM[0] = new int[2];
		for(int level = 1; level <= Experience.getMaxLevel(); level++) {
			LEVEL_RISE_SERVICE_ITEM[level] = properties.getProperty("LEVEL_RISE_SERVICE_ITEM_" + level, LEVEL_RISE_SERVICE_ITEM[level - 1], "-");
			LEVEL_DOWN_SERVICE_ITEM[level] = properties.getProperty("LEVEL_DOWN_SERVICE_ITEM_" + level, LEVEL_DOWN_SERVICE_ITEM[level - 1], "-");
		}

		// Augment shop service
		AUGMENT_SERVICE_COST_ITEM_ID = properties.getProperty("AUGMENT_SERVICE_COST_ITEM_ID", 4037);
		AUGMENT_SERVICE_COST_ITEM_COUNT = properties.getProperty("AUGMENT_SERVICE_COST_ITEM_COUNT", 50);
		AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR = properties.getProperty("AUGMENT_SERVICE_SKILLS_VARIATIONS_WARRIOR", new int[0], ";");
		AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR = properties.getProperty("AUGMENT_SERVICE_STATS_VARIATIONS_WARRIOR", new int[0], ";");
		AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE = properties.getProperty("AUGMENT_SERVICE_SKILLS_VARIATIONS_MAGE", new int[0], ";");
		AUGMENT_SERVICE_STATS_VARIATIONS_MAGE = properties.getProperty("AUGMENT_SERVICE_STATS_VARIATIONS_MAGE", new int[0], ";");
	}
}