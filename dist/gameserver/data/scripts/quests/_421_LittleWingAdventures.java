package quests;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.PetDataTable.L2Pet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;


/*
 * Author DRiN, Last Updated: 2008/04/13
 */
public class _421_LittleWingAdventures extends QuestScript
{
	// NPCs
	private static int Cronos = 30610;
	private static int Mimyu = 30747;
	// Mobs
	private static int Fairy_Tree_of_Wind = 27185;
	private static int Fairy_Tree_of_Star = 27186;
	private static int Fairy_Tree_of_Twilight = 27187;
	private static int Fairy_Tree_of_Abyss = 27188;
	private static int Soul_of_Tree_Guardian = 27189;
	// Items
	private static int Dragonflute_of_Wind = L2Pet.HATCHLING_WIND.getControlItemId();
	private static int Dragonflute_of_Star = L2Pet.HATCHLING_STAR.getControlItemId();
	private static int Dragonflute_of_Twilight = L2Pet.HATCHLING_TWILIGHT.getControlItemId();
	private static int Dragon_Bugle_of_Wind = L2Pet.STRIDER_WIND.getControlItemId();
	private static int Dragon_Bugle_of_Star = L2Pet.STRIDER_STAR.getControlItemId();
	private static int Dragon_Bugle_of_Twilight = L2Pet.STRIDER_TWILIGHT.getControlItemId();
	// Quest Items
	private static int Fairy_Leaf = 4325;

	private static int Min_Fairy_Tree_Attaks = 110;

	public _421_LittleWingAdventures()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Cronos);
		addTalkId(Mimyu);
		addKillId(Fairy_Tree_of_Wind);
		addKillId(Fairy_Tree_of_Star);
		addKillId(Fairy_Tree_of_Twilight);
		addKillId(Fairy_Tree_of_Abyss);
		addAttackId(Fairy_Tree_of_Wind);
		addAttackId(Fairy_Tree_of_Star);
		addAttackId(Fairy_Tree_of_Twilight);
		addAttackId(Fairy_Tree_of_Abyss);
		addQuestItem(Fairy_Leaf);
	}

	private static ItemInstance GetDragonflute(QuestState st)
	{
		List<ItemInstance> Dragonflutes = new ArrayList<ItemInstance>();
		for(ItemInstance item : st.getPlayer().getInventory().getItems())
			if(item != null && (item.getItemId() == Dragonflute_of_Wind || item.getItemId() == Dragonflute_of_Star || item.getItemId() == Dragonflute_of_Twilight))
				Dragonflutes.add(item);

		if(Dragonflutes.isEmpty())
			return null;
		if(Dragonflutes.size() == 1)
			return Dragonflutes.get(0);
		if(st.getCond() == 0)
			return null;

		int dragonflute_id = st.getInt("dragonflute");

		for(ItemInstance item : Dragonflutes)
			if(item.getObjectId() == dragonflute_id)
				return item;

		return null;
	}

	private static boolean HatchlingSummoned(QuestState st, boolean CheckObjID)
	{
		Servitor _servitor = st.getPlayer().getServitor();
		if(_servitor == null)
			return false;
		if(CheckObjID)
		{
			int dragonflute_id = st.getInt("dragonflute");
			if(dragonflute_id == 0)
				return false;
			if(_servitor.getControlItemObjId() != dragonflute_id)
				return false;
		}
		ItemInstance dragonflute = GetDragonflute(st);
		if(dragonflute == null)
			return false;
		if(PetDataTable.getControlItemId(_servitor.getNpcId()) != dragonflute.getItemId())
			return false;
		return true;
	}

	private static boolean CheckTree(QuestState st, int Fairy_Tree_id)
	{
		return st.getInt(String.valueOf(Fairy_Tree_id)) == 1000000;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		if(event.equalsIgnoreCase("30610_05.htm"))
			st.setCond(1);
		else if((event.equalsIgnoreCase("30747_03.htm") || event.equalsIgnoreCase("30747_04.htm")) && cond == 1)
		{
			ItemInstance dragonflute = GetDragonflute(st);
			if(dragonflute == null)
				return NO_QUEST_DIALOG;

			if(dragonflute.getObjectId() != st.getInt("dragonflute"))
			{
				if(Rnd.chance(10))
				{
					st.takeItems(dragonflute.getItemId(), 1);
					st.finishQuest();
				}
				return "30747_00.htm";
			}
			if(!HatchlingSummoned(st, false))
				return event.equalsIgnoreCase("30747_04.htm") ? "30747_04a.htm" : "30747_02.htm";
			if(event.equalsIgnoreCase("30747_04.htm"))
			{
				st.setCond(2);
				st.takeItems(Fairy_Leaf, -1);
				st.giveItems(Fairy_Leaf, 4, false, false);
			}
		}

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		ItemInstance dragonflute = GetDragonflute(st);
		int dragonflute_id = st.getInt("dragonflute");
		if(cond == 0)
		{
			if(npcId == Cronos)
			{
				if(st.getPlayer().getLevel() < 45)
					return "30610_01.htm";
				if(dragonflute == null)
					return "30610_02.htm";
				if(dragonflute.getEnchantLevel() < 55)
					return "30610_03.htm";

				st.set("dragonflute", String.valueOf(dragonflute.getObjectId()));
				return "30610_04.htm";
			}
			else
				return NO_QUEST_DIALOG;
		}

		if(npcId == Cronos)
		{
			if(dragonflute == null)
				return "30610_02.htm";
			return dragonflute.getObjectId() == dragonflute_id ? "30610_07.htm" : "30610_06.htm";
		}

		if(npcId == Mimyu)
		{
			if(st.getQuestItemsCount(Dragon_Bugle_of_Wind) + st.getQuestItemsCount(Dragon_Bugle_of_Star) + st.getQuestItemsCount(Dragon_Bugle_of_Twilight) > 0)
				return "30747_00b.htm";
			if(dragonflute == null)
				return NO_QUEST_DIALOG;
			if(cond == 1)
				return "30747_01.htm";
			if(cond == 2)
			{
				if(!HatchlingSummoned(st, false))
					return "30747_09.htm";
				if(st.getQuestItemsCount(Fairy_Leaf) == 0)
				{
					st.finishQuest();
					return "30747_11.htm";
				}
				return "30747_10.htm";
			}
			if(cond == 3)
			{
				if(dragonflute.getObjectId() != dragonflute_id)
					return "30747_00a.htm";
				if(st.getQuestItemsCount(Fairy_Leaf) > 0)
				{
					st.finishQuest();
					return "30747_11.htm";
				}
				if(!(CheckTree(st, Fairy_Tree_of_Wind) && CheckTree(st, Fairy_Tree_of_Star) && CheckTree(st, Fairy_Tree_of_Twilight) && CheckTree(st, Fairy_Tree_of_Abyss)))
				{
					st.finishQuest();
					return "30747_11.htm";
				}
				if(st.getInt("welldone") == 0)
				{
					if(!HatchlingSummoned(st, false))
						return "30747_09.htm";
					st.set("welldone", "1");
					return "30747_12.htm";
				}
				if(HatchlingSummoned(st, false) || st.getPlayer().getServitor() != null)
					return "30747_13a.htm";

				dragonflute.setItemId(Dragon_Bugle_of_Wind + dragonflute.getItemId() - Dragonflute_of_Wind);
				dragonflute.setJdbcState(JdbcEntityState.UPDATED);
				dragonflute.update();
				st.getPlayer().sendPacket(new InventoryUpdatePacket().addModifiedItem(st.getPlayer(), dragonflute));
				st.finishQuest();
				return "30747_13.htm";
			}
		}

		return NO_QUEST_DIALOG;
	}

	/*
	 * благодаря ai.Quest421FairyTree вызовется только при атаке от L2PetInstance
	 */
	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		if(!st.isStarted() || st.getCond() != 2 || !HatchlingSummoned(st, true) || st.getQuestItemsCount(Fairy_Leaf) == 0)
			return null;

		String npcID = String.valueOf(npc.getNpcId());
		Integer attaked_times = st.getInt(npcID);
		if(CheckTree(st, npc.getNpcId()))
			return null;
		if(attaked_times > Min_Fairy_Tree_Attaks)
		{
			st.set(npcID, "1000000");
			Functions.npcSay(npc, "Give me the leaf!");
			st.takeItems(Fairy_Leaf, 1);
			if(CheckTree(st, Fairy_Tree_of_Wind) && CheckTree(st, Fairy_Tree_of_Star) && CheckTree(st, Fairy_Tree_of_Twilight) && CheckTree(st, Fairy_Tree_of_Abyss))
			{
				st.setCond(3);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		else
			st.set(npcID, String.valueOf(attaked_times + 1));
		return null;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		ThreadPoolManager.getInstance().schedule(new GuardiansSpawner(npc, st, Rnd.get(15, 20)), 1000);
		return null;
	}

	public class GuardiansSpawner extends RunnableImpl
	{
		private SimpleSpawner _spawn = null;
		private String agressor;
		private String agressors_pet = null;
		private List<String> agressors_party = null;
		private int tiks = 0;

		public GuardiansSpawner(NpcInstance npc, QuestState st, int _count)
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(Soul_of_Tree_Guardian);
			if(template == null)
				return;
			try
			{
				_spawn = new SimpleSpawner(template);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			for(int i = 0; i < _count; i++)
			{
				_spawn.setLoc(Location.findPointToStay(npc, 50, 200));
				_spawn.setHeading(Rnd.get(0, 0xFFFF));
				_spawn.setAmount(1);
				_spawn.doSpawn(true);

				agressor = st.getPlayer().getName();
				if(st.getPlayer().getServitor() != null)
					agressors_pet = st.getPlayer().getServitor().getName();
				if(st.getPlayer().getParty() != null)
				{
					agressors_party = new ArrayList<String>();
					for(Player _member : st.getPlayer().getParty().getPartyMembers())
						if(!_member.equals(st.getPlayer()))
							agressors_party.add(_member.getName());
				}
			}
			_spawn.stopRespawn();
			updateAgression();
		}

		private void AddAgression(Playable player, int aggro)
		{
			if(player == null)
				return;
			for(NpcInstance mob : _spawn.getAllSpawned())
			{
				mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, aggro);
			}
		}

		private void updateAgression()
		{
			Player _player = World.getPlayer(agressor);
			if(_player != null)
			{
				if(agressors_pet != null && _player.getServitor() != null && _player.getServitor().getName().equalsIgnoreCase(agressors_pet))
					AddAgression(_player.getServitor(), 10);
				AddAgression(_player, 2);
			}
			if(agressors_party != null)
				for(String _agressor : agressors_party)
					AddAgression(World.getPlayer(_agressor), 1);
		}

		@Override
		public void runImpl() throws Exception
		{
			if(_spawn == null)
				return;
			tiks++;
			if(tiks < 600)
			{
				updateAgression();
				ThreadPoolManager.getInstance().schedule(this, 1000);
				return;
			}
			_spawn.deleteAll();
		}
	}
}