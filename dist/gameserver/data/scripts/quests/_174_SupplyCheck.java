package quests;

import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _174_SupplyCheck extends QuestScript
{

	int Marcela = 32173;
	int Benis = 32170; // warehouse keeper
	int Nika = 32167; // grocerer
	//int Erinu = 32164; // weapon seller
	//int Casca = 32139; // vice hierarch

	int WarehouseManifest = 9792;
	int GroceryStoreManifest = 9793;
	//int WeaponShopManifest = 9794;
	//int SupplyReport = 9795;

	int WoodenBreastplate = 23;
	int WoodenGaiters = 2386;
	int LeatherTunic = 429;
	int LeatherStockings = 464;
	int WoodenHelmet = 43;
	int LeatherShoes = 37;
	int Gloves = 49;

	public _174_SupplyCheck()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Marcela);
		addTalkId(Benis, Nika); //Erinu, Casca
		addQuestItem(WarehouseManifest, GroceryStoreManifest); // WeaponShopManifest, SupplyReport
	}

	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("zerstorer_morsell_q0174_04.htm"))
		{
			qs.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Marcela)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() == 1)
					htmltext = "zerstorer_morsell_q0174_02.htm";
				else
					htmltext = "zerstorer_morsell_q0174_01.htm";
			}
			else if(cond == 1)
				htmltext = "zerstorer_morsell_q0174_05.htm";
			else if(cond == 2)
			{
				st.setCond(3);
				st.takeItems(WarehouseManifest, -1);
				htmltext = "zerstorer_morsell_q0174_06.htm";
			}
			else if(cond == 3)
				htmltext = "zerstorer_morsell_q0174_07.htm";
			else if(cond == 4)
			{
				if(st.getPlayer().getClassId().isMage() && !st.getPlayer().getClassId().equalsOrChildOf(ClassId.ORC_MAGE))
				{
					st.giveItems(LeatherTunic, 1, false, false);
					st.giveItems(LeatherStockings, 1, false, false);
				}
				else
				{
					st.giveItems(WoodenBreastplate, 1, false, false);
					st.giveItems(WoodenGaiters, 1, false, false);
				}
				st.giveItems(WoodenHelmet, 1, false, false);
				st.giveItems(LeatherShoes, 1, false, false);
				st.giveItems(Gloves, 1, false, false);
				st.giveItems(ADENA_ID, 2466, true, true);
				st.addExpAndSp(5672, 446);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.finishQuest();
				htmltext = "zerstorer_morsell_q0174_12.htm";
			}
			/*
			{
				st.setCond(5);
				st.takeItems(GroceryStoreManifest, -1);
				htmltext = "zerstorer_morsell_q0174_08.htm";
			}
			else if(cond == 5)
				htmltext = "zerstorer_morsell_q0174_09.htm";

			else if(cond == 6)
			{
				st.setCond(7);
				st.takeItems(WeaponShopManifest, -1);
				st.giveItems(SupplyReport, 1);
				htmltext = "zerstorer_morsell_q0174_10.htm";
			}
			else if(cond == 7)
				htmltext = "zerstorer_morsell_q0174_11.htm";

			else if(cond == 8)
			{
				if(st.getPlayer().getClassId().isMage() && !st.getPlayer().getClassId().equalsOrChildOf(ClassId.orcMage))
				{
					st.giveItems(LeatherTunic, 1);
					st.giveItems(LeatherStockings, 1);
				}
				else
				{
					st.giveItems(WoodenBreastplate, 1);
					st.giveItems(WoodenGaiters, 1);
				}
				st.giveItems(WoodenHelmet, 1);
				st.giveItems(LeatherShoes, 1);
				st.giveItems(Gloves, 1);
				st.giveItems(ADENA_ID, 2466, true);
				st.addExpAndSp(5672, 446, false, false);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.finishQuest();
				htmltext = "zerstorer_morsell_q0174_12.htm";
			}
			 */
		}

		else if(npcId == Benis)
			if(cond == 1)
			{
				st.setCond(2);
				st.giveItems(WarehouseManifest, 1, false, false);
				htmltext = "warehouse_keeper_benis_q0174_01.htm";
			}
			else
				htmltext = "warehouse_keeper_benis_q0174_02.htm";

		else if(npcId == Nika)
			if(cond < 3)
				htmltext = "subelder_casca_q0174_01.htm";
			else if(cond == 3)
			{
				st.setCond(4);
				st.giveItems(GroceryStoreManifest, 1, false, false);
				htmltext = "trader_neagel_q0174_02.htm";
			}
			else
				htmltext = "trader_neagel_q0174_03.htm";
		/*
		else if(npcId == Erinu)
			if(cond < 5)
				htmltext = "subelder_casca_q0174_01.htm";
			else if(cond == 5)
			{
				st.setCond(6);
				st.giveItems(WeaponShopManifest, 1);
				htmltext = "trader_erinu_q0174_02.htm";
			}
			else
				htmltext = "subelder_casca_q0174_03.htm";

		else if(npcId == Casca)
			if(cond < 7)
				htmltext = "subelder_casca_q0174_01.htm";
			else if(cond == 7)
			{
				st.setCond(8);
				st.takeItems(SupplyReport, -1);
				htmltext = "subelder_casca_q0174_02.htm";
			}
			else
				htmltext = "subelder_casca_q0174_03.htm";
		 */
		return htmltext;
	}
}