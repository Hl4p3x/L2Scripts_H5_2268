package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _026_TiredOfWaiting extends QuestScript
{
	private final static int ISAEL = 30655;
	private final static int KITZKA = 31045;

	private final static int LARGE_DRAGON_BONE = 17248;
	private final static int WILL_OF_ANTHARAS = 17266;
	private final static int SEALED_BLOOD_CRYSTAL = 17267;

	public _026_TiredOfWaiting()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(ISAEL);
		addTalkId(KITZKA);
	}

	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "isael_q0026_05.htm";
			qs.setCond(1);
		}
		else if(event.equalsIgnoreCase("LARGE_DRAGON_BONE"))
		{
			htmltext = "kitzka_q0026_03.htm";
			qs.giveItems(LARGE_DRAGON_BONE, 1, false, false);
			qs.finishQuest();
		}
		else if(event.equalsIgnoreCase("WILL_OF_ANTHARAS"))
		{
			htmltext = "kitzka_q0026_04.htm";
			qs.giveItems(WILL_OF_ANTHARAS, 1, false, false);
			qs.finishQuest();
		}
		else if(event.equalsIgnoreCase("SEALED_BLOOD_CRYSTAL"))
		{
			htmltext = "kitzka_q0026_05.htm";
			qs.giveItems(SEALED_BLOOD_CRYSTAL, 1, false, false);
			qs.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case ISAEL:
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 80)
						htmltext = "isael_q0026_02.htm";
					else
						htmltext = "isael_q0026_01.htm";
				}
				else if(cond == 1)
					htmltext = "isael_q0026_03.htm";
				break;
			case KITZKA:
				if(cond == 1)
					htmltext = "kitzka_q0026_01.htm";
				break;
		}
		return htmltext;
	}
}