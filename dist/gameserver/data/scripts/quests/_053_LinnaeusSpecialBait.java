package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _053_LinnaeusSpecialBait extends QuestScript
{
	int Linnaeu = 31577;
	int CrimsonDrake = 20670;
	int HeartOfCrimsonDrake = 7624;
	int FlameFishingLure = 7613;
	Integer FishSkill = 1315;

	public _053_LinnaeusSpecialBait()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Linnaeu);

		addTalkId(Linnaeu);

		addKillId(CrimsonDrake);

		addQuestItem(HeartOfCrimsonDrake);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_linneaus_q0053_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_linneaus_q0053_0201.htm"))
			if(st.getQuestItemsCount(HeartOfCrimsonDrake) < 100)
				htmltext = "fisher_linneaus_q0053_0202.htm";
			else
			{
				st.unset("cond");
				st.takeItems(HeartOfCrimsonDrake, -1);
				st.giveItems(FlameFishingLure, 4, false, false);
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Linnaeu)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 60)
					htmltext = "fisher_linneaus_q0053_0103.htm";
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 21)
					htmltext = "fisher_linneaus_q0053_0101.htm";
				else
					htmltext = "fisher_linneaus_q0053_0102.htm";
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(HeartOfCrimsonDrake) < 100)
				{
					htmltext = "fisher_linneaus_q0053_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_linneaus_q0053_0105.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == CrimsonDrake && st.getCond() == 1)
			if(st.getQuestItemsCount(HeartOfCrimsonDrake) < 100 && Rnd.chance(30))
			{
				st.giveItems(HeartOfCrimsonDrake, 1, true, true);
				if(st.getQuestItemsCount(HeartOfCrimsonDrake) >= 100)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}