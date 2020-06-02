package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _052_WilliesSpecialBait extends QuestScript
{
	private final static int Willie = 31574;
	private final static int[] TarlkBasilisks = {
			20573,
			20574
	};
	private final static int EyeOfTarlkBasilisk = 7623;
	private final static int EarthFishingLure = 7612;
	private final static Integer FishSkill = 1315;

	public _052_WilliesSpecialBait()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Willie);

		addKillId(TarlkBasilisks);

		addQuestItem(EyeOfTarlkBasilisk);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_willeri_q0052_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_willeri_q0052_0201.htm"))
			if(st.getQuestItemsCount(EyeOfTarlkBasilisk) < 100)
				htmltext = "fisher_willeri_q0052_0202.htm";
			else
			{
				st.unset("cond");
				st.takeItems(EyeOfTarlkBasilisk, -1);
				st.giveItems(EarthFishingLure, 4, false, false);
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
		if(npcId == Willie)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 48)
					htmltext = "fisher_willeri_q0052_0103.htm";
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 16)
					htmltext = "fisher_willeri_q0052_0101.htm";
				else
					htmltext = "fisher_willeri_q0052_0102.htm";
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(EyeOfTarlkBasilisk) < 100)
				{
					htmltext = "fisher_willeri_q0052_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_willeri_q0052_0105.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == TarlkBasilisks[0] || npcId == TarlkBasilisks[1] && st.getCond() == 1)
			if(st.getQuestItemsCount(EyeOfTarlkBasilisk) < 100 && Rnd.chance(30))
			{
				st.giveItems(EyeOfTarlkBasilisk, 1, true, true);
				if(st.getQuestItemsCount(EyeOfTarlkBasilisk) >= 100)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}