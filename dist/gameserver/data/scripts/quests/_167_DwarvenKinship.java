package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _167_DwarvenKinship extends QuestScript
{
	//NPC
	private static final int Carlon = 30350;
	private static final int Haprock = 30255;
	private static final int Norman = 30210;
	//Quest Items
	private static final int CarlonsLetter = 1076;
	private static final int NormansLetter = 1106;

	public _167_DwarvenKinship()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Carlon);

		addTalkId(Haprock);
		addTalkId(Norman);

		addQuestItem(CarlonsLetter, NormansLetter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30350-04.htm"))
		{
			st.giveItems(CarlonsLetter, 1, false, false);
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30255-03.htm"))
		{
			st.takeItems(CarlonsLetter, -1);
			st.giveItems(ADENA_ID, 2000, true, true);
			st.giveItems(NormansLetter, 1, false, false);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30255-04.htm"))
		{
			st.takeItems(CarlonsLetter, -1);
			st.giveItems(ADENA_ID, 15000, true, true);
			st.playSound(SOUND_GIVEUP);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("30210-02.htm"))
		{
			st.takeItems(NormansLetter, -1);
			st.giveItems(ADENA_ID, 22000, true, true);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Carlon)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
					htmltext = "30350-03.htm";
				else
					htmltext = "30350-02.htm";
			}
			else
				htmltext = "30350-05.htm";
		}
		else if(npcId == Haprock)
		{
			if(cond == 1)
				htmltext = "30255-01.htm";
			else if(cond > 1)
				htmltext = "30255-05.htm";
		}
		else if(npcId == Norman && cond == 2)
			htmltext = "30210-01.htm";
		return htmltext;
	}
}