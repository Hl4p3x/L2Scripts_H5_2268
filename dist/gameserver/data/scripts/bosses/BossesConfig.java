package bosses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.configuration.ExProperties;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.scripts.ScriptConfig;

/**
 * @author Bonux
**/
public class BossesConfig implements ScriptConfig
{
	private static final Logger _log = LoggerFactory.getLogger(BossesConfig.class);

	private static final String PROPERTIES_FILE = "config/bosses.properties";

	// Antharas
	public static SchedulingPattern ANTHARAS_RESPAWN_TIME_PATTERN;
	public static boolean ANTHARAS_STRONG_ONLY;
	public static int ANTHARAS_SPAWN_DELAY;
	public static int ANTHARAS_SLEEP_TIME;
	public static int[][] ANTHARAS_ENTERANCE_NECESSARY_ITEMS;
	public static boolean ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;

	// Valakas
	public static SchedulingPattern VALAKAS_RESPAWN_TIME_PATTERN;
	public static int VALAKAS_SPAWN_DELAY;
	public static int VALAKAS_SLEEP_TIME;
	public static int[][] VALAKAS_ENTERANCE_NECESSARY_ITEMS;
	public static boolean VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;

	// Baium
	public static SchedulingPattern BAIUM_RESPAWN_TIME_PATTERN;
	public static int BAIUM_SLEEP_TIME;
	public static int[][] BAIUM_ENTERANCE_NECESSARY_ITEMS;
	public static boolean BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;

	// Sailren
	public static SchedulingPattern SAILREN_RESPAWN_TIME_PATTERN;
	public static int SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY;
	public static int SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY;
	public static boolean SAILREN_SINGLE_ENTARANCE_AVAILABLE;

	// Baylor
	public static SchedulingPattern BAYLOR_RESPAWN_TIME_PATTERN;
	public static boolean BAYLOR_SINGLE_ENTARANCE_AVAILABLE;

	// Beleth
	public static SchedulingPattern BELETH_RESPAWN_TIME_PATTERN;
	public static int BELETH_INACTIVITY_CHECK_DELAY;

	@Override
	public void load()
	{
		ExProperties properties = Config.load(PROPERTIES_FILE);

		// Antharas
		ANTHARAS_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("ANTHARAS_RESPAWN_TIME_PATTERN", "~480:* * +8:* * *"));
		ANTHARAS_STRONG_ONLY = properties.getProperty("ANTHARAS_STRONG_ONLY", true);
		ANTHARAS_SPAWN_DELAY = properties.getProperty("ANTHARAS_SPAWN_DELAY", 20);
		ANTHARAS_SLEEP_TIME = properties.getProperty("ANTHARAS_SLEEP_TIME", 30);
		ANTHARAS_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("ANTHARAS_ENTERANCE_NECESSARY_ITEMS", "3865-1"), ";", "-");
		ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = properties.getProperty("ANTHARAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", false);

		// Valakas
		VALAKAS_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("VALAKAS_RESPAWN_TIME_PATTERN", "~480:* * +11:* * *"));
		VALAKAS_SPAWN_DELAY = properties.getProperty("VALAKAS_SPAWN_DELAY", 10);
		VALAKAS_SLEEP_TIME = properties.getProperty("VALAKAS_SLEEP_TIME", 20);
		VALAKAS_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("VALAKAS_ENTERANCE_NECESSARY_ITEMS", "7267-1"), ";", "-");
		VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = properties.getProperty("VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", true);

		// Baium
		BAIUM_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("BAIUM_RESPAWN_TIME_PATTERN", "~480:* * +5:* * *"));
		BAIUM_SLEEP_TIME = properties.getProperty("BAIUM_SLEEP_TIME", 30);
		BAIUM_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(properties.getProperty("BAIUM_ENTERANCE_NECESSARY_ITEMS", "4295-1"), ";", "-");
		BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = properties.getProperty("BAIUM_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", true);

		// Sailren
		SAILREN_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("SAILREN_RESPAWN_TIME_PATTERN", "~1440:* * +1:* * *"));
		SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY = properties.getProperty("SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY", 120);
		SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY = properties.getProperty("SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY", 1);
		SAILREN_SINGLE_ENTARANCE_AVAILABLE = properties.getProperty("SAILREN_SINGLE_ENTARANCE_AVAILABLE", true);

		// Baylor
		BAYLOR_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("BAYLOR_RESPAWN_TIME_PATTERN", "~1440:* * +1:* * *"));
		BAYLOR_SINGLE_ENTARANCE_AVAILABLE = properties.getProperty("BAYLOR_SINGLE_ENTARANCE_AVAILABLE", false);

		// Beleth
		BELETH_RESPAWN_TIME_PATTERN = new SchedulingPattern(properties.getProperty("BELETH_RESPAWN_TIME_PATTERN", "~1440:* * +1:* * *"));
		BELETH_INACTIVITY_CHECK_DELAY = properties.getProperty("BELETH_INACTIVITY_CHECK_DELAY", 120);
	}
}