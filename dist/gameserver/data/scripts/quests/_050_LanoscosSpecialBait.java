package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _050_LanoscosSpecialBait extends QuestScript
{
	// NPC
	int Lanosco = 31570;
	int SingingWind = 21026;
	// Items
	int EssenceofWind = 7621;
	int WindFishingLure = 7610;
	// Skill
	Integer FishSkill = 1315;

	public _050_LanoscosSpecialBait()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Lanosco);

		addTalkId(Lanosco);

		addKillId(SingingWind);

		addQuestItem(EssenceofWind);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_lanosco_q0050_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_lanosco_q0050_0201.htm"))
			if(st.getQuestItemsCount(EssenceofWind) < 100)
				htmltext = "fisher_lanosco_q0050_0202.htm";
			else
			{
				st.unset("cond");
				st.takeItems(EssenceofWind, -1);
				st.giveItems(WindFishingLure, 4, false, false);
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
		if(npcId == Lanosco)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 27)
					htmltext = "fisher_lanosco_q0050_0103.htm";
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 8)
					htmltext = "fisher_lanosco_q0050_0101.htm";
				else
					htmltext = "fisher_lanosco_q0050_0102.htm";
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(EssenceofWind) < 100)
				{
					htmltext = "fisher_lanosco_q0050_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_lanosco_q0050_0105.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == SingingWind && st.getCond() == 1)
			if(st.getQuestItemsCount(EssenceofWind) < 100 && Rnd.chance(30))
			{
				st.giveItems(EssenceofWind, 1, true, true);
				if(st.getQuestItemsCount(EssenceofWind) >= 100)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}