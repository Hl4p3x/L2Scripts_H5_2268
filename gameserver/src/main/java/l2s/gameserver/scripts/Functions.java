package l2s.gameserver.scripts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.MapUtils;
import l2s.gameserver.utils.NpcUtils;

/**
 * @Author: Diamond
 * @Date: 7/6/2007
 * @Time: 5:22:23
 */
public class Functions
{
	public HardReference<Player> self = HardReferences.emptyRef();
	public HardReference<NpcInstance> npc = HardReferences.emptyRef();

	/**
	 * Вызывает метод с задержкой
	 *
	 * @param object	 - от чьего имени вызывать
	 * @param sClass<?>  - вызываемый класс
	 * @param methodName - вызываемый метод
	 * @param args	   - массив аргуметов
	 * @param variables  - список выставляемых переменных
	 * @param delay	  - задержка в миллисекундах
	 */
	public static ScheduledFuture<?> executeTask(final Player caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables, long delay)
	{
		return ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				callScripts(caller, className, methodName, args, variables);
			}
		}, delay);
	}

	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, Map<String, Object> variables, long delay)
	{
		return executeTask(null, className, methodName, args, variables, delay);
	}

	public static ScheduledFuture<?> executeTask(Player player, String className, String methodName, Object[] args, long delay)
	{
		return executeTask(player, className, methodName, args, null, delay);
	}

	public static ScheduledFuture<?> executeTask(String className, String methodName, Object[] args, long delay)
	{
		return executeTask(className, methodName, args, null, delay);
	}

	public static Object callScripts(String className, String methodName, Object[] args)
	{
		return callScripts(className, methodName, args, null);
	}

	public static Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}

	public static Object callScripts(Player player, String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return Scripts.getInstance().callScripts(player, className, methodName, args, variables);
	}

	/**
	 * Вызывать только из скриптов
	 */
	public void show(String text, Player self)
	{
		show(text, self, getNpc());
	}
		
	/**
	 * Статический метод, для вызова из любых мест
	 */
	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
			return;

		NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(self, npc);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(HtmlUtils.bbParse(text));

		if(arg != null && arg.length % 2 == 0)
		{
			for(int i = 0; i < arg.length; i = +2)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}

		self.sendPacket(msg);
	}

	public static void show(CustomMessage message, Player self)
	{
		show(message.toString(), self, null);
	}

	public static void sendMessage(String text, Player self)
	{
		self.sendMessage(text);
	}

	public static void sendMessage(CustomMessage message, Player self)
	{
		self.sendMessage(message);
	}

	// Белый чат
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		npcSayInRange(npc, range, NpcString.NONE, text);
	}

	// Белый чат
	public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String... params)
	{
		if(npc == null)
			return;
		NSPacket cs = new NSPacket(npc, ChatType.NPC_ALL, fStringId, params);
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
			if(npc.getReflection() == player.getReflection())
				player.sendPacket(cs);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, 1500);
	}

	// Белый чат
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		npcSayInRange(npc, 1500, npcString, params);
	}

	// Белый чат
	public static void npcSayInRangeCustomMessage(NpcInstance npc, int range, String address, Object... replacements)
	{
		if(npc == null)
			return;
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
			if(npc.getReflection() == player.getReflection())
				player.sendPacket(new NSPacket(npc, ChatType.NPC_ALL, new CustomMessage(address, player, replacements).toString()));
	}

	// Белый чат
	public static void npcSayCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		npcSayInRangeCustomMessage(npc, 1500, address, replacements);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, String text)
	{
		npcSayToPlayer(npc, player, NpcString.NONE, text);
	}

	// private message
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		if(npc == null)
			return;
		player.sendPacket(new NSPacket(npc, ChatType.TELL, npcString, params));
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, String text)
	{
		npcShout(npc, NpcString.NONE, text);
	}

	// Shout (желтый) чат
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		if(npc == null)
			return;
		NSPacket cs = new NSPacket(npc, ChatType.NPC_SHOUT, npcString, params);

		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;

			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset)
				player.sendPacket(cs);
		}
	}

	// Shout (желтый) чат
	public static void npcShoutCustomMessage(NpcInstance npc, String address, Object... replacements)
	{
		if(npc == null)
			return;

		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getReflection() != npc.getReflection())
				continue;

			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset || npc.isInRange(player, Config.CHAT_RANGE))
				player.sendPacket(new NSPacket(npc, ChatType.NPC_SHOUT, new CustomMessage(address, player, replacements).toString()));
		}
	}

	public static void npcSay(NpcInstance npc, NpcString address, ChatType type, int range, String... replacements)
	{
		if(npc == null)
			return;
		for(Player player : World.getAroundPlayers(npc, range, Math.max(range / 2, 200)))
		{
			if(player.getReflection() == npc.getReflection())
				player.sendPacket(new NSPacket(npc, type, address, replacements));
		}
	}

	/**
	 * Проверяет к-во предметов у игрока и удаляет их.
	 * @param player
	 * @param itemid
	 * @param count
	 * @return true - если операция успешна, false - если ошибка
	 */
	public static boolean price(Player player, int itemid, int count)
	{
		String itemName = player.isLangRus() ? ItemHolder.getInstance().getTemplate(itemid).getName() : ItemHolder.getInstance().getTemplate(itemid).getName();
		if(player.getInventory().getItemByItemId(itemid) == null)
		{
			player.sendMessage("Цена: " + count + " " + itemName);
			player.sendMessage("У вас вообще нет " + itemName);
			return false;
		}
		else if(ItemFunctions.getItemCount(player, itemid) < count)
		{
			player.sendMessage("Цена: " + count + " " + itemName);
			player.sendMessage("У вас нет " + count + " " + itemName);
			return false;
		}
		else
		{
			ItemFunctions.deleteItem(player, itemid, count);
			return true;
		}
	}

	public static boolean ride(Player player, int pet)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);

		if(player.getServitor() != null)
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}

		player.setMount(pet, 0, 0);
		return true;
	}

	public static void unRide(Player player)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);
	}

	public static void unSummonPet(Player player, boolean onlyPets)
	{
		Servitor pet = player.getServitor();
		if(pet == null)
			return;
		if(pet.isPet() || !onlyPets)
			pet.unSummon();
	}

	//@Deprecated
	//TODO [VISTALL] use NpcUtils
	public static NpcInstance spawn(Location loc, int npcId)
	{
		return spawn(loc, npcId, ReflectionManager.DEFAULT);
	}

	//@Deprecated
	public static NpcInstance spawn(Location loc, int npcId, Reflection reflection)
	{
		return NpcUtils.spawnSingle(npcId, loc, reflection, 0);
	}

	public Player getSelf()
	{
		return self.get();
	}

	public NpcInstance getNpc()
	{
		return npc.get();
	}

	//@Deprecated
	//TODO [VISTALL] use NpcUtils
	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(new Location(location[0], location[1], location[2]));
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			sp.init();
			if(list != null)
				list.add(sp);
		}
	}
	public static void SpawnNPCs(int npcId, int[][] locations, List<SimpleSpawner> list, int respawn)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
		{
			SimpleSpawner sp = new SimpleSpawner(template);
			sp.setLoc(new Location(location[0], location[1], location[2]));
			sp.setAmount(1);
			sp.setRespawnDelay(respawn);
			sp.setRespawnDelay(0);
			sp.init();
			if(list != null)
				list.add(sp);
		}
	}

	public static void deSpawnNPCs(List<SimpleSpawner> list)
	{
		for(SimpleSpawner sp : list)
			sp.deleteAll();

		list.clear();
	}

	public static boolean IsActive(String name)
	{
		return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
	}

	public static boolean SetActive(String name, boolean active)
	{
		if(active == IsActive(name))
			return false;
		if(active)
			ServerVariables.set(name, "on");
		else
			ServerVariables.unset(name);
		return true;
	}

	public static boolean SimpleCheckDrop(Creature mob, Creature killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}

	public static boolean isPvPEventStarted()
	{
		if(isEventStarted("events.TvT.TvT"))
			return true;
		if(isEventStarted("events.lastHero.LastHero"))
			return true;
		if(isEventStarted("events.CtF.CtF"))
			return true;
		return false;
	}

	public static boolean isEventStarted(String event)
	{
		try
		{
			if(Scripts.getInstance().containsClass(event) && (Boolean) callScripts(event, "isRunned", new Object[] {}))
				return true;
		}
		catch(Exception e)
		{
			//
		}
		return false;
	}

	public static void sendDebugMessage(Player player, String message)
	{
		if(!player.isGM())
			return;
		player.sendMessage(message);
	}

	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
			return;
		if(title == null)
			return;
		if(items.keySet().size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		for(Map.Entry<Integer, Long> itm : items.entrySet())
		{
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
	}

	/*
		 * Проверяет, хватает ли владельцу инвентаря предметов...
		 * @param playable Владелец инвентаря
		 * @param item_id ID предмета
		 * @param count нужное кол-во
		 * @return true - хватает
		 */
	public static boolean haveItem(Playable playable, int item_id, long count, boolean sendMessage)
	{
		long cnt = count - ItemFunctions.getItemCount(playable, item_id);
		if (cnt>0)
		{
			if (sendMessage)
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessage.YOU_LACK_S2_OF_S1).addItemName(item_id).addNumber(cnt));
			return false;
		}
		return true;
	}

	/*
	 * Проверяет хватает ли предметов, где предмет задан в массиве:
	 * item[0] - ID
	 * item[1] - Кол-во
	 *
	 * @param playable Владелец инвентаря
	 * @param item[]
	 * @param count нужное кол-во
	 * @return true - хватает
	 */
	public static boolean haveItem(Playable playable, long[] item, boolean sendMessage)
	{
		return haveItem(playable, (int)item[0], item[1], sendMessage);
	}


	/*
	 * Проверяет хватает ли предметов, где предметы заданы в двумерном массиве массиве:
	 * item[0][0] - ID
	 * item[0][1] - Кол-во
	 *
	 * @param playable Владелец инвентаря
	 * @param item[][]
	 * @param count нужное кол-во
	 * @return true - хватает
	 */
	public static boolean haveItem(Playable playable, long[][] items, boolean sendMessage)
	{
		for(long item[] : items)
			if (!haveItem(playable, item, sendMessage))
				return false;

		return true;
	}
	
	public static String GetStringCount(long count)
	{
		String scount = Long.toString(count);
		if (count < 1000L) 
		{
			return scount;
		}
		if ((count > 999L) && (count < 1000000L))
		{
			return scount.substring(0, scount.length() - 3) + "к";
		}
		if ((count > 999999L) && (count < 1000000000L))
		{
			return scount.substring(0, scount.length() - 6) + "кк";
		}
		if (count > 999999999L)
		{
			return scount.substring(0, scount.length() - 9) + "ккк";
		}
		if (count == 0L)
		{
			return "00.00";
		}
		return "ERROR";
	}	
}