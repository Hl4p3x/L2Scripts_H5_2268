package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _051_OFullesSpecialBait extends QuestScript
{
	int OFulle = 31572;
	int FetteredSoul = 20552;

	int LostBaitIngredient = 7622;
	int IcyAirFishingLure = 7611;

	Integer FishSkill = 1315;

	public _051_OFullesSpecialBait()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(OFulle);

		addTalkId(OFulle);

		addKillId(FetteredSoul);

		addQuestItem(LostBaitIngredient);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_ofulle_q0051_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_ofulle_q0051_0201.htm"))
			if(st.getQuestItemsCount(LostBaitIngredient) < 100)
				htmltext = "fisher_ofulle_q0051_0202.htm";
			else
			{
				st.unset("cond");
				st.takeItems(LostBaitIngredient, -1);
				st.giveItems(IcyAirFishingLure, 4, false, false);
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
		if(npcId == OFulle)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 36)
					htmltext = "fisher_ofulle_q0051_0103.htm";
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 11)
					htmltext = "fisher_ofulle_q0051_0101.htm";
				else
					htmltext = "fisher_ofulle_q0051_0102.htm";
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(LostBaitIngredient) < 100)
				{
					htmltext = "fisher_ofulle_q0051_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_ofulle_q0051_0105.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == FetteredSoul && st.getCond() == 1)
			if(st.getQuestItemsCount(LostBaitIngredient) < 100 && Rnd.chance(30))
			{
				st.giveItems(LostBaitIngredient, 1, true, true);
				if(st.getQuestItemsCount(LostBaitIngredient) >= 100)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
