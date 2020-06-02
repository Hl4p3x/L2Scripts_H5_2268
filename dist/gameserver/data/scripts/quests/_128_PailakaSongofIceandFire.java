package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

public class _128_PailakaSongofIceandFire extends QuestScript
{
	// NPC
	private static int ADLER = 32497;
	private static int ADLER2 = 32510;
	private static int SINAI = 32500;
	private static int TINSPECTOR = 32507;

	// BOSS
	private static int HILLAS = 18610;
	private static int PAPION = 18609;
	private static int GARGOS = 18607;
	private static int KINSUS = 18608;
	private static int ADIANTUM = 18620;

	// MOBS
	private static int Bloom = 18616;
	private static int CrystalWaterBottle = 32492;
	private static int BurningBrazier = 32493;

	// ITEMS
	private static int PailakaInstantShield = 13032;
	private static int QuickHealingPotion = 13033;
	private static int FireAttributeEnhancer = 13040;
	private static int WaterAttributeEnhancer = 13041;
	private static int SpritesSword = 13034;
	private static int EnhancedSpritesSword = 13035;
	private static int SwordofIceandFire = 13036;
	private static int EssenceofWater = 13038;
	private static int EssenceofFire = 13039;

	private static int TempleBookofSecrets1 = 13130;
	private static int TempleBookofSecrets2 = 13131;
	private static int TempleBookofSecrets3 = 13132;
	private static int TempleBookofSecrets4 = 13133;
	private static int TempleBookofSecrets5 = 13134;
	private static int TempleBookofSecrets6 = 13135;
	private static int TempleBookofSecrets7 = 13136;

	// REWARDS
	private static int PailakaRing = 13294;
	private static int PailakaEarring = 13293;
	private static int ScrollofEscape = 736;

	private static int[] MOBS = new int[] { 18611, 18612, 18613, 18614, 18615 };
	private static int[] HPHERBS = new int[] { 8600, 8601, 8602 };
	private static int[] MPHERBS = new int[] { 8603, 8604, 8605 };

	private static final int izId = 43;

	public _128_PailakaSongofIceandFire()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(ADLER);
		addTalkId(ADLER2, SINAI);
		addFirstTalkId(TINSPECTOR);
		addKillId(HILLAS, PAPION, ADIANTUM, KINSUS, GARGOS, Bloom, CrystalWaterBottle, BurningBrazier);
		addKillId(MOBS);
		addQuestItem(SpritesSword, EnhancedSpritesSword, SwordofIceandFire, EssenceofWater, EssenceofFire);
		addQuestItem(TempleBookofSecrets1, TempleBookofSecrets2, TempleBookofSecrets3, TempleBookofSecrets4, TempleBookofSecrets5, TempleBookofSecrets6, TempleBookofSecrets7);
		addQuestItem(PailakaInstantShield, QuickHealingPotion, FireAttributeEnhancer, WaterAttributeEnhancer);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		int refId = player.getReflectionId();
		String htmltext = event;

		if(event.equalsIgnoreCase("Enter"))
		{
			enterInstance(player);
			return null;
		}
		else if(event.equalsIgnoreCase("32497-04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32500-06.htm"))
		{
			st.setCond(2);
			st.giveItems(SpritesSword, 1, false, false);
			st.giveItems(TempleBookofSecrets1, 1, false, false);
		}
		else if(event.equalsIgnoreCase("32507-03.htm"))
		{
			st.setCond(4);
			st.takeItems(TempleBookofSecrets2, -1);
			st.giveItems(TempleBookofSecrets3, 1, false, false);
			if(st.getQuestItemsCount(EssenceofWater) == 0)
				htmltext = "32507-01.htm";
			else
			{
				st.takeItems(SpritesSword, -1);
				st.takeItems(EssenceofWater, -1);
				st.giveItems(EnhancedSpritesSword, 1, false, false);
			}
			addSpawnToInstance(PAPION, new Location(-53903, 181484, -4555, 30456), 0, refId);
		}
		else if(event.equalsIgnoreCase("32507-07.htm"))
		{
			st.setCond(7);
			st.takeItems(TempleBookofSecrets5, -1);
			st.giveItems(TempleBookofSecrets6, 1, false, false);
			if(st.getQuestItemsCount(EssenceofFire) == 0)
				htmltext = "32507-04.htm";
			else
			{
				st.takeItems(EnhancedSpritesSword, -1);
				st.takeItems(EssenceofFire, -1);
				st.giveItems(SwordofIceandFire, 1, false, false);
			}
			addSpawnToInstance(GARGOS, new Location(-61354, 183624, -4821, 63613), 0, refId);
		}
		else if(event.equalsIgnoreCase("32510-02.htm"))
		{
			st.giveItems(PailakaRing, 1, false, false);
			st.giveItems(PailakaEarring, 1, false, false);
			st.giveItems(ScrollofEscape, 1, false, false);
			st.addExpAndSp(810000, 50000);
			st.finishQuest();
			player.setVitality(Config.VITALITY_LEVELS[4]);
			player.getReflection().startCollapseTimer(60000);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == ADLER)
		{
			if(cond == 0)
			{
				Player player = st.getPlayer();
				if(player.getLevel() < 36 || player.getLevel() > 42)
					htmltext = "32497-no.htm";
				else
					htmltext = "32497-01.htm";
			}
			else
				htmltext = "32497-05.htm";
		}
		else if(npcId == SINAI)
		{
			if(cond == 1)
				htmltext = "32500-01.htm";
			else
				htmltext = "32500-06.htm";
		}
		else if(npcId == ADLER2)
		{
			if(cond == 9)
				htmltext = "32510-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == ADLER)
			htmltext = "32497-no.htm";
		else if(npcId == ADLER2)
			htmltext = "32510-02.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		String htmltext = NO_QUEST_DIALOG;
		QuestState st = player.getQuestState(this);
		if(st == null || st.isCompleted())
			return htmltext;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == TINSPECTOR)
			if(cond == 2)
				htmltext = "32507-01.htm";
			else if(cond == 3)
				htmltext = "32507-02.htm";
			else if(cond == 6)
				htmltext = "32507-05.htm";
			else
				htmltext = "32507-04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int refId = player.getReflectionId();
		if(ArrayUtils.contains(MOBS, npcId))
		{
			int herbRnd = Rnd.get(2);
			if(Rnd.get(100) < 50)
				st.dropItem(npc, HPHERBS[herbRnd], 1);
			if(Rnd.get(100) < 50)
				st.dropItem(npc, MPHERBS[herbRnd], 1);
		}
		else if(npcId == HILLAS && cond == 2)
		{
			st.takeItems(TempleBookofSecrets1, -1);
			st.giveItems(EssenceofWater, 1, false, false);
			st.giveItems(TempleBookofSecrets2, 1, false, false);
			st.setCond(3);
		}
		else if(npcId == PAPION && cond == 4)
		{
			st.takeItems(TempleBookofSecrets3, -1);
			st.giveItems(TempleBookofSecrets4, 1, false, false);
			st.setCond(5);
			addSpawnToInstance(KINSUS, new Location(-61404, 181351, -4815, 63953), 0, refId);
		}
		else if(npcId == KINSUS && cond == 5)
		{
			st.takeItems(TempleBookofSecrets4, -1);
			st.giveItems(EssenceofFire, 1, false, false);
			st.giveItems(TempleBookofSecrets5, 1, false, false);
			st.setCond(6);
		}
		else if(npcId == GARGOS && cond == 7)
		{
			st.takeItems(TempleBookofSecrets6, -1);
			st.giveItems(TempleBookofSecrets7, 1, false, false);
			st.setCond(8);
			addSpawnToInstance(ADIANTUM, new Location(-53297, 185027, -4617, 1512), 0, refId);
		}
		else if(npcId == ADIANTUM && cond == 8)
		{
			st.takeItems(TempleBookofSecrets7, -1);
			st.setCond(9);
			addSpawnToInstance(ADLER2, new Location(npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()), 0, refId);
		}
		else if(npcId == Bloom)
		{
			if(Rnd.chance(50))
				st.dropItem(npc, PailakaInstantShield, Rnd.get(1, 7));
			if(Rnd.chance(30))
				st.dropItem(npc, QuickHealingPotion, Rnd.get(1, 7));
		}
		else if(npcId == CrystalWaterBottle)
		{
			if(Rnd.chance(50))
				st.dropItem(npc, PailakaInstantShield, Rnd.get(1, 10));
			if(Rnd.chance(30))
				st.dropItem(npc, QuickHealingPotion, Rnd.get(1, 10));
			if(Rnd.chance(10))
				st.dropItem(npc, WaterAttributeEnhancer, Rnd.get(1, 5));
		}
		else if(npcId == BurningBrazier)
		{
			if(Rnd.chance(50))
				st.dropItem(npc, PailakaInstantShield, Rnd.get(1, 10));
			if(Rnd.chance(30))
				st.dropItem(npc, QuickHealingPotion, Rnd.get(1, 10));
			if(Rnd.chance(10))
				st.dropItem(npc, FireAttributeEnhancer, Rnd.get(1, 5));
		}
		return null;
	}

	private void enterInstance(Player player)
	{
		Reflection r = player.getActiveReflection();
		if(r != null)
		{
			if(player.canReenterInstance(izId))
				player.teleToLocation(r.getTeleportLoc(), r);
		}
		else if(player.canEnterInstance(izId))
		{
			ReflectionUtils.enterReflection(player, izId);
		}
	}
}