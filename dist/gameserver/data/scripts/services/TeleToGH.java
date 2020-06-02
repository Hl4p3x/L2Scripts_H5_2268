package services;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.ReflectionUtils;

public class TeleToGH extends Functions implements ScriptFile
{
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	private static Zone _zone = ReflectionUtils.getZone("[giran_harbor_offshore]");
	private static ZoneListener _zoneListener;

	@Override
	public void onLoad()
	{
		if(!Config.SERVICES_GIRAN_HARBOR_ENABLED)
			return;

		ReflectionManager.GIRAN_HARBOR.setCoreLoc(new Location(47416, 186568, -3480));

		// spawn wh keeper
		SimpleSpawner spawn = new SimpleSpawner(30086);
		spawn.setLocx(47736);
		spawn.setLocy(186968);
		spawn.setLocz(-3511);
		spawn.setAmount(1);
		spawn.setHeading(44315);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		// spawn grocery trader
		spawn = new SimpleSpawner(32169);
		spawn.setLocx(48816);
		spawn.setLocy(186240);
		spawn.setLocz(-3484);
		spawn.setAmount(1);
		spawn.setHeading(37604);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		// spawn gk
		spawn = new SimpleSpawner(13129);
		spawn.setLocx(47984);
		spawn.setLocy(186832);
		spawn.setLocz(-3445);
		spawn.setAmount(1);
		spawn.setHeading(42000);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);


		// spawn blacksmith (Pushkin)
		spawn = new SimpleSpawner(30300);
		spawn.setLocx(47848);
		spawn.setLocy(186904);
		spawn.setLocz(-3511);
		spawn.setAmount(1);
		spawn.setHeading(40959);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		// spawn Item Broker
		spawn = new SimpleSpawner(32320);
		spawn.setLocx(47772);
		spawn.setLocy(186905);
		spawn.setLocz(-3480);
		spawn.setAmount(1);
		spawn.setHeading(42000);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		// spawn Item Broker
		spawn = new SimpleSpawner(32320);
		spawn.setLocx(46360);
		spawn.setLocy(187672);
		spawn.setLocz(-3480);
		spawn.setAmount(1);
		spawn.setHeading(42000);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		// spawn Item Broker
		spawn = new SimpleSpawner(32320);
		spawn.setLocx(49016);
		spawn.setLocy(185960);
		spawn.setLocz(-3480);
		spawn.setAmount(1);
		spawn.setHeading(42000);
		spawn.setRespawnDelay(5);
		spawn.setReflection(ReflectionManager.GIRAN_HARBOR);
		spawn.init();
		_spawns.add(spawn);

		_zoneListener = new ZoneListener();
		_zone.addListener(_zoneListener);
		_zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		_zone.setActive(true);
		Zone zone = ReflectionUtils.getZone("[giran_harbor_peace_alt]");
		zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		zone.setActive(true);
		zone = ReflectionUtils.getZone("[giran_harbor_no_trade]");
		zone.setReflection(ReflectionManager.GIRAN_HARBOR);
		zone.setActive(true);
	}

	@Override
	public void onReload()
	{
		_zone.removeListener(_zoneListener);
		for(SimpleSpawner spawn : _spawns)
			spawn.deleteAll();
		_spawns.clear();
	}

	@Override
	public void onShutdown()
	{
	}

	public void toGH()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		if(player.getAdena() < Config.SERVICES_GIRAN_HARBOR_PRICE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		player.reduceAdena(Config.SERVICES_GIRAN_HARBOR_PRICE, true);
		player.setVar("backCoords", player.getLoc().toXYZString(), -1);
		player.teleToLocation(Location.findPointToStay(_zone.getSpawn(), 30, 200, ReflectionManager.GIRAN_HARBOR.getGeoIndex()), ReflectionManager.GIRAN_HARBOR);
	}

	public void fromGH()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		String var = player.getVar("backCoords");
		if(var == null || var.equals(""))
		{
			teleOut();
			return;
		}
		player.teleToLocation(Location.parseLoc(var), ReflectionManager.DEFAULT);
	}

	public void teleOut()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		player.teleToLocation(46776, 185784, -3528, ReflectionManager.DEFAULT);
		show(player.isLangRus() ? "Я не знаю, как Вы попали сюда, но я могу Вас отправить за ограждение." : "I don't know from where you came here, but I can teleport you the another border side.", player, npc);
	}

	public String DialogAppend_30059(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30080(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30177(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30233(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30256(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30320(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30848(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30878(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30899(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31210(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31275(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31320(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31964(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30006(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30134(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30146(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_32163(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30576(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30540(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_50001(Integer val)
	{
		return getHtmlAppends(val);
	}

	private static final String en = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Giran Harbor (offshore zone) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Adena\"]";
	private static final String ru = "<br>[scripts_services.TeleToGH:toGH @811;Гавань Гирана|\"Гавань Гирана (оффшорная зона) - " + Config.SERVICES_GIRAN_HARBOR_PRICE + " Аден\"]";
	private static final String enFree = "<br>[scripts_services.TeleToGH:toGH @811;Giran Harbor|\"Giran Harbor (offshore zone) - Free\"]";
	private static final String ruFree = "<br>[scripts_services.TeleToGH:toGH @811;Гавань Гирана|\"Гавань Гирана (оффшорная зона) - Бесплатно\"]";

	public String getHtmlAppends(Integer val)
	{
		if(val != 0 || !Config.SERVICES_GIRAN_HARBOR_ENABLED)
			return "";
		Player player = getSelf();
		if(player == null)
			return "";
		if(Config.SERVICES_GIRAN_HARBOR_PRICE > 0)
			return player.isLangRus() ? ru : en;
		return player.isLangRus() ? ruFree : enFree;
	}

	public String DialogAppend_13129(Integer val)
	{
		return getHtmlAppends2(val);
	}

	private static final String en2 = "<br>[scripts_services.ManaRegen:DoManaRegen|Full MP Regeneration. (1 MP for 5 Adena)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|Exit the Giran Harbor.]<br>";
	private static final String ru2 = "%npcname%:<br>Чем я могу помочь? Я вижу, Вы очень устали.<br>[scripts_services.ManaRegen:DoManaRegen|Полное восстановление MP (1 MP за 10 Аден)]<br>[scripts_services.TeleToGH:fromGH @811;From Giran Harbor|Покинуть Гавань Гирана]<br>";

	public String getHtmlAppends2(Integer val)
	{
		if(val != 0 || !Config.SERVICES_GIRAN_HARBOR_ENABLED)
			return "";
		Player player = getSelf();
		if(player == null || player.getReflectionId() != -2)
			return "";
		return player.isLangRus() ? ru2 : en2;
	}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			// обрабатывать вход в зону не надо, только выход
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player != null)
				if(Config.SERVICES_GIRAN_HARBOR_ENABLED && player.getReflection() == ReflectionManager.GIRAN_HARBOR && player.isVisible())
				{
					double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // угол в градусах
					double radian = Math.toRadians(angle - 90); // угол в радианах
					cha.teleToLocation((int) (cha.getX() + 50 * Math.sin(radian)), (int) (cha.getY() - 50 * Math.cos(radian)), cha.getZ());
				}
		}
	}
}