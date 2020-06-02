package quests;


import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _692_HowtoOpposeEvil extends QuestScript
{
	//NPC
	private static final int Dilios = 32549;
	private static final int Kutran = 32550;
	private static final int Lekon = 32557;

	//QuestItem
	private static final int NucleusofanIncompleteSoul = 13863;
	private static final int FleetSteedTroupsTotem = 13865;
	private static final int PortionofaSoul = 13866;
	private static final int BreathofTiat = 13867;

	private static final int ConcentratedSpiritEnergy = 15535;
	private static final int SpiritStoneDust = 15536;

	private static final int NucleusofaFreedSoul = 13796;
	private static final int FleetSteedTroupsCharm = 13841;
	private static final int SpiritStoneFragment = 15486;

	private static final int[] SOD = {
			22552,
			22541,
			22550,
			22551,
			22596,
			22544,
			22540,
			22547,
			22542,
			22543,
			22539,
			22546,
			22548,
			22536,
			22538,
			22537
	};
	private static final int[] SOI = {
			22509,
			22510,
			22511,
			22512,
			22513,
			22514,
			22515,
			22520,
			22522,
			22527,
			22531,
			22535,
			22516,
			22517,
			22518,
			22519,
			22521,
			22524,
			22528,
			22532,
			22530,
			22535
	};

	private static final int[] SOA = {
			22746,
			22747,
			22748,
			22749,
			22750,
			22751,
			22752,
			22753,
			22754,
			22755,
			22756,
			22757,
			22758,
			22759,
			22760,
			22761,
			22762,
			22763,
			22764,
			22765
	};

	public _692_HowtoOpposeEvil()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(Dilios);
		addTalkId(Kutran, Lekon);

		addKillId(SOD);
		addKillId(SOI);
		addKillId(SOA);

		addQuestItem(NucleusofanIncompleteSoul, FleetSteedTroupsTotem, PortionofaSoul);
		addQuestItem(BreathofTiat, ConcentratedSpiritEnergy, SpiritStoneDust);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int cond = st.getCond();
		if(event.equalsIgnoreCase("take_test") && cond == 0)
		{
			if(st.getPlayer().isQuestCompleted(10273))
			{
				st.setCond(2);
				htmltext = "dilios_q692_4.htm";
			}
			else
			{
				st.setCond(1);
				htmltext = "dilios_q692_3.htm";
			}
		}
		else if(event.equalsIgnoreCase("lekon_q692_2.htm") && cond == 1)
			st.finishQuest();
		else if(event.equalsIgnoreCase("kutran_q692_2.htm") && cond == 2)
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("exchange_sod") && cond == 3)
		{
			if(st.getQuestItemsCount(FleetSteedTroupsTotem) < 5)
				htmltext = "kutran_q692_7.htm";
			else
			{
				int _charmstogive = Math.round(st.getQuestItemsCount(FleetSteedTroupsTotem) / 5);
				st.takeItems(FleetSteedTroupsTotem, 5 * _charmstogive);
				st.giveItems(FleetSteedTroupsCharm, _charmstogive, false, false);
				htmltext = "kutran_q692_4.htm";
			}
		}
		else if(event.equalsIgnoreCase("exchange_soi") && cond == 3)
		{
			if(st.getQuestItemsCount(NucleusofanIncompleteSoul) < 5)
				htmltext = "kutran_q692_7.htm";
			else
			{
				int _soulstogive = Math.round(st.getQuestItemsCount(NucleusofanIncompleteSoul) / 5);
				st.takeItems(NucleusofanIncompleteSoul, 5 * _soulstogive);
				st.giveItems(NucleusofaFreedSoul, _soulstogive, false, false);
				htmltext = "kutran_q692_5.htm";
			}
		}
		else if(event.equalsIgnoreCase("exchange_soa") && cond == 3)
		{
			if(st.getQuestItemsCount(SpiritStoneDust) < 5)
				htmltext = "kutran_q692_7.htm";
			else
			{
				int _soulstogive = Math.round(st.getQuestItemsCount(SpiritStoneDust) / 5);
				st.takeItems(SpiritStoneDust, 5 * _soulstogive);
				st.giveItems(SpiritStoneFragment, _soulstogive, false, false);
				htmltext = "kutran_q692_5.htm";
			}
		}
		else if(event.equalsIgnoreCase("exchange_breath") && cond == 3)
		{
			if(st.getQuestItemsCount(BreathofTiat) == 0)
				htmltext = "kutran_q692_7.htm";
			else
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(BreathofTiat) * 2500, true, true);
				st.takeItems(BreathofTiat, -1);
				htmltext = "kutran_q692_5.htm";
			}
		}
		else if(event.equalsIgnoreCase("exchange_portion") && cond == 3)
		{
			if(st.getQuestItemsCount(PortionofaSoul) == 0)
				htmltext = "kutran_q692_7.htm";
			else
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(PortionofaSoul) * 2500, true, true);
				st.takeItems(PortionofaSoul, -1);
				htmltext = "kutran_q692_5.htm";
			}
		}
		else if(event.equalsIgnoreCase("exchange_energy") && cond == 3)
		{
			if(st.getQuestItemsCount(ConcentratedSpiritEnergy) == 0)
				htmltext = "kutran_q692_7.htm";
			else
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(ConcentratedSpiritEnergy) * 25000, true, true);
				st.takeItems(ConcentratedSpiritEnergy, -1);
				htmltext = "kutran_q692_5.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Dilios)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75)
					htmltext = "dilios_q692_1.htm";
				else
					htmltext = "dilios_q692_0.htm";
			}
		}
		else if(npcId == Kutran)
		{
			if(cond == 2)
				htmltext = "kutran_q692_1.htm";
			else if(cond == 3)
				htmltext = "kutran_q692_3.htm";
		}
		else if(npcId == Lekon)
			if(cond == 1)
				htmltext = "lekon_q692_1.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 3)
		{
			if(ArrayUtils.contains(SOD, npcId))
				st.rollAndGive(FleetSteedTroupsTotem, 1, 17); //Шанс на глаз и с потолка. Умножается на рейт квестов и делится на кол-во членов группы. Для нас это (nRate*3)/9
			else if(ArrayUtils.contains(SOI, npcId))
				st.rollAndGive(NucleusofanIncompleteSoul, 1, 17); //Шанс на глаз и с потолка. Умножается на рейт квестов и делится на кол-во членов группы. Для нас это (nRate*3)/9
			else if(ArrayUtils.contains(SOA, npcId))
				st.rollAndGive(SpiritStoneDust, 1, 20); //Шанс на глаз и с потолка. Умножается на рейт квестов и делится на кол-во членов группы. Для нас это (nRate*3)/9
		}
		return null;
	}
}