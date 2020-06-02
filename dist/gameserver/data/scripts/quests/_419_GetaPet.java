package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.HtmlUtils;

/**
 * Get A Pet (419)
 * @author ivantotov
 * @reworked by Bonux
**/
public class _419_GetaPet extends QuestScript
{
	// NPCs
	private static final int GUARD_METTY = 30072;
	private static final int ACCESSORY_MERCHANT_ELICE = 30091;
	private static final int GATEKEEPER_BELLA = 30256;
	private static final int PET_MENAGER_MARTIN = 30731;

	// Items
	private static final int ANIMAL_LOVERS_LIST = 3417;
	private static final int ANIMAL_SLAYERS_1ST_LIST = 3418;
	private static final int ANIMAL_SLAYERS_2ND_LIST = 3419;
	private static final int ANIMAL_SLAYERS_3RD_LIST = 3420;
	private static final int ANIMAL_SLAYERS_4TH_LIST = 3421;
	private static final int ANIMAL_SLAYERS_5TH_LIST = 3422;
	private static final int BLOODY_FANG = 3423;
	private static final int BLOODY_CLAW = 3424;
	private static final int BLOODY_NAIL = 3425;
	private static final int BLOODY_KASHA_FANG = 3426;
	private static final int BLOODY_TARANTULA_NAIL = 3427;
	private static final int ANIMAL_SLAYERS_LIST = 10164;
	private static final int BLOODY_RED_CLAW = 10165;

	// Reward
	private static final int WOLF_COLLAR = 2375;

	// Monster
	private static final int LESSER_DARK_HORROR = 20025;
	private static final int PROWLER = 20034;
	private static final int GIANT_SPIDER = 20103;
	private static final int DARK_HORROR = 20105;
	private static final int TALON_SPIDER = 20106;
	private static final int BLADE_SPIDER = 20108;
	private static final int HOOK_SPIDER = 20308;
	private static final int HUNTER_TARANTULA = 20403;
	private static final int CRIMSON_SPIDER = 20460;
	private static final int PINCER_SPIDER = 20466;
	private static final int KASHA_SPIDER = 20474;
	private static final int KASHA_FANG_SPIDER = 20476;
	private static final int KASHA_BLADE_SPIDER = 20478;
	private static final int PLUNDER_TARANTULA = 20508;
	private static final int CRIMSON_SPIDER2 = 22244;

	// Misc
	private static final int MIN_LEVEL = 15;

	public _419_GetaPet()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(PET_MENAGER_MARTIN);

		addTalkId(PET_MENAGER_MARTIN);
		addTalkId(GUARD_METTY);
		addTalkId(ACCESSORY_MERCHANT_ELICE);
		addTalkId(GATEKEEPER_BELLA);

		addKillId(LESSER_DARK_HORROR);
		addKillId(PROWLER);
		addKillId(GIANT_SPIDER);
		addKillId(DARK_HORROR);
		addKillId(TALON_SPIDER);
		addKillId(BLADE_SPIDER);
		addKillId(HOOK_SPIDER);
		addKillId(HUNTER_TARANTULA);
		addKillId(CRIMSON_SPIDER);
		addKillId(PINCER_SPIDER);
		addKillId(KASHA_SPIDER);
		addKillId(KASHA_FANG_SPIDER);
		addKillId(KASHA_BLADE_SPIDER);
		addKillId(PLUNDER_TARANTULA);
		addKillId(CRIMSON_SPIDER2);

		addQuestItem(ANIMAL_LOVERS_LIST);
		addQuestItem(ANIMAL_SLAYERS_1ST_LIST);
		addQuestItem(ANIMAL_SLAYERS_2ND_LIST);
		addQuestItem(ANIMAL_SLAYERS_3RD_LIST);
		addQuestItem(ANIMAL_SLAYERS_4TH_LIST);
		addQuestItem(ANIMAL_SLAYERS_5TH_LIST);
		addQuestItem(BLOODY_FANG);
		addQuestItem(BLOODY_CLAW);
		addQuestItem(BLOODY_NAIL);
		addQuestItem(BLOODY_KASHA_FANG);
		addQuestItem(BLOODY_TARANTULA_NAIL);
		addQuestItem(ANIMAL_SLAYERS_LIST);
		addQuestItem(BLOODY_RED_CLAW);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("ACCEPT"))
		{
			if(st.isNotAccepted())
			{
				st.setCond(1);
				switch(st.getPlayer().getRace())
				{
					case HUMAN:
					{
						st.giveItems(ANIMAL_SLAYERS_1ST_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_04.htm";
						break;
					}
					case ELF:
					{
						st.giveItems(ANIMAL_SLAYERS_2ND_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_05.htm";
						break;
					}
					case DARKELF:
					{
						st.giveItems(ANIMAL_SLAYERS_3RD_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_06.htm";
						break;
					}
					case ORC:
					{
						st.giveItems(ANIMAL_SLAYERS_4TH_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_07.htm";
						break;
					}
					case DWARF:
					{
						st.giveItems(ANIMAL_SLAYERS_5TH_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_08.htm";
						break;
					}
					case KAMAEL:
					{
						st.giveItems(ANIMAL_SLAYERS_LIST, 1, false);
						htmltext = "pet_manager_martin_q0419_08a.htm";
						break;
					}
				}
			}
		}
		else if(event.equalsIgnoreCase("pet_manager_martin_q0419_12.htm"))
		{
			switch(st.getPlayer().getRace())
			{
				case HUMAN:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_1ST_LIST) && st.haveQuestItem(BLOODY_FANG, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_1ST_LIST, -1);
						st.takeItems(BLOODY_FANG, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
				case ELF:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_2ND_LIST) && st.haveQuestItem(BLOODY_CLAW, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_2ND_LIST, -1);
						st.takeItems(BLOODY_CLAW, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
				case DARKELF:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_3RD_LIST) && st.haveQuestItem(BLOODY_NAIL, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_3RD_LIST, -1);
						st.takeItems(BLOODY_NAIL, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
				case ORC:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_4TH_LIST) && st.haveQuestItem(BLOODY_KASHA_FANG, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_4TH_LIST, -1);
						st.takeItems(BLOODY_KASHA_FANG, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
				case DWARF:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_5TH_LIST) && st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_5TH_LIST, -1);
						st.takeItems(BLOODY_TARANTULA_NAIL, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
				case KAMAEL:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_LIST) && st.haveQuestItem(BLOODY_RED_CLAW, 50))
					{
						st.takeItems(ANIMAL_SLAYERS_LIST, -1);
						st.takeItems(BLOODY_RED_CLAW, -1);
						st.giveItems(ANIMAL_LOVERS_LIST, 1, false);
					}
					break;
				}
			}
			st.set("memoState", 0);
		}
		return htmltext;
	}

	@Override
	public String onMenuSelect(int reply, QuestState st, NpcInstance npc)
	{
		String htmltext = null;
		if(npc.getNpcId() == PET_MENAGER_MARTIN)
		{
			if(reply == 0)
			{
				htmltext = "pet_manager_martin_q0419_14.htm";
				st.set("memoState", 0);
			}
			else if(reply == 1)
			{
				if(((st.getInt("memoState") & 15) == 10) && st.haveQuestItem(ANIMAL_LOVERS_LIST))
				{
					st.takeItems(ANIMAL_LOVERS_LIST, -1);
					st.giveItems(WOLF_COLLAR, 1, false);
					st.finishQuest();
					htmltext = "pet_manager_martin_q0419_15.htm";
				}
				else
				{
					boolean findResponse = false;
					int linkId = 0;
					while(!findResponse)
					{
						final int randomLinkOffset = Rnd.get(14) + 4;
						int i7 = 1;
						for(int i = 1; i <= randomLinkOffset; i++)
						{
							i7 = i7 * 2;
						}
						
						if(((i7 & st.getInt("memoState")) == 0) && (randomLinkOffset < 18))
						{
							findResponse = true;
							st.set("memoState", (st.getInt("memoState") + 1) | i7);
							linkId = 1110000 + (5 * (randomLinkOffset - 4));
							htmltext = "pet_manager_martin_q0419_" + (20 + (randomLinkOffset - 4)) + ".htm";
						}
					}
					
					int linkCount = 1;
					int replyOffset1 = 0;
					int replyOffset2 = 0;
					int replyOffset3 = 0;
					int replyOffset4 = 0;
					int i8 = 0;
					while(linkCount < 5)
					{
						final int randomReplyOffset = Rnd.get(4) + 1;
						int i7 = 1;
						for(int i = 1; i <= randomReplyOffset; i++)
						{
							i7 = i7 * 2;
						}
						
						if(((i7 & i8) == 0) && (randomReplyOffset < 5))
						{
							if(linkCount == 1)
							{
								replyOffset1 = randomReplyOffset;
							}
							else if(linkCount == 2)
							{
								replyOffset2 = randomReplyOffset;
							}
							else if(linkCount == 3)
							{
								replyOffset3 = randomReplyOffset;
							}
							else if(linkCount == 4)
							{
								replyOffset4 = randomReplyOffset;
							}
							linkCount++;
							i8 = i8 | i7;
						}
					}
					
					htmltext = HtmCache.getInstance().getHtml("quests/" + getClass().getSimpleName() + "/" + htmltext, st.getPlayer());
					htmltext = htmltext.replace("<?reply1?>", HtmlUtils.htmlNpcString(linkId + replyOffset1));
					htmltext = htmltext.replace("<?reply2?>", HtmlUtils.htmlNpcString(linkId + replyOffset2));
					htmltext = htmltext.replace("<?reply3?>", HtmlUtils.htmlNpcString(linkId + replyOffset3));
					htmltext = htmltext.replace("<?reply4?>", HtmlUtils.htmlNpcString(linkId + replyOffset4));
					htmltext = htmltext.replace("<?reply5?>", HtmlUtils.htmlNpcString(linkId + 5));
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(st.isNotAccepted())
		{
			if(npc.getNpcId() == PET_MENAGER_MARTIN)
			{
				if(st.getPlayer().getLevel() < MIN_LEVEL)
					htmltext = "pet_manager_martin_q0419_01.htm";
				else
					htmltext = "pet_manager_martin_q0419_02.htm";
			}
		}
		else if(st.isStarted())
		{
			switch(npc.getNpcId())
			{
				case PET_MENAGER_MARTIN:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_LIST))
					{
						if(!st.haveQuestItem(BLOODY_RED_CLAW, 50))
						{
							if(!st.haveQuestItem(BLOODY_RED_CLAW))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_SLAYERS_1ST_LIST))
					{
						if(!st.haveQuestItem(BLOODY_FANG, 50))
						{
							if(!st.haveQuestItem(BLOODY_FANG))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_SLAYERS_2ND_LIST))
					{
						if(!st.haveQuestItem(BLOODY_CLAW, 50))
						{
							if(!st.haveQuestItem(BLOODY_CLAW))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_SLAYERS_3RD_LIST))
					{
						if(!st.haveQuestItem(BLOODY_NAIL, 50))
						{
							if(!st.haveQuestItem(BLOODY_NAIL))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_SLAYERS_4TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_KASHA_FANG, 50))
						{
							if(!st.haveQuestItem(BLOODY_KASHA_FANG))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_SLAYERS_5TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50))
						{
							if(!st.haveQuestItem(BLOODY_TARANTULA_NAIL))
								htmltext = "pet_manager_martin_q0419_09.htm";
							else
								htmltext = "pet_manager_martin_q0419_10.htm";
						}
						else
							htmltext = "pet_manager_martin_q0419_11.htm";
					}
					else if(st.haveQuestItem(ANIMAL_LOVERS_LIST))
					{
						if((st.getInt("memoState") != 14) && (st.getInt("memoState") != 1879048192))
						{
							st.set("memoState", 0);
							htmltext = "pet_manager_martin_q0419_16.htm";
						}
						else
						{
							st.set("memoState", 1879048192);
							htmltext = "pet_manager_martin_q0419_13.htm";
						}
					}
					break;
				}
				case GUARD_METTY:
				{
					if(st.haveQuestItem(ANIMAL_LOVERS_LIST))
					{
						st.set("memoState", st.getInt("memoState") | 4);
						htmltext = "metty_q0419_01.htm";
					}
					break;
				}
				case ACCESSORY_MERCHANT_ELICE:
				{
					if(st.haveQuestItem(ANIMAL_LOVERS_LIST))
					{
						st.set("memoState", st.getInt("memoState") | 8);
						htmltext = "elliany_q0419_01.htm";
					}
					break;
				}
				case GATEKEEPER_BELLA:
				{
					if(st.haveQuestItem(ANIMAL_LOVERS_LIST))
					{
						st.set("memoState", st.getInt("memoState") | 2);
						htmltext = "gatekeeper_belladonna_q0419_01.htm";
					}
					break;
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.isStarted())
		{
			switch(npc.getNpcId())
			{
				case LESSER_DARK_HORROR:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_3RD_LIST) && !st.haveQuestItem(BLOODY_NAIL, 50))
					{
						if(Rnd.chance(60))
						{
							st.giveItems(BLOODY_NAIL, 1, false);
							if(st.haveQuestItem(BLOODY_NAIL, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case PROWLER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_3RD_LIST) && !st.haveQuestItem(BLOODY_NAIL, 50))
					{
						st.giveItems(BLOODY_NAIL, 1, false);
						if(st.haveQuestItem(BLOODY_NAIL, 50))
							st.playSound(SOUND_MIDDLE);
						else
							st.playSound(SOUND_ITEMGET);
					}
					break;
				}
				case GIANT_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_1ST_LIST))
					{
						if(!st.haveQuestItem(BLOODY_FANG, 50) && Rnd.chance(60))
						{
							st.giveItems(BLOODY_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case DARK_HORROR:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_3RD_LIST))
					{
						if(!st.haveQuestItem(BLOODY_NAIL, 50) && Rnd.chance(75))
						{
							st.giveItems(BLOODY_NAIL, 1, false);
							if(st.haveQuestItem(BLOODY_NAIL, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case TALON_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_1ST_LIST))
					{
						if(!st.haveQuestItem(BLOODY_FANG, 50) && Rnd.chance(75))
						{
							st.giveItems(BLOODY_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case BLADE_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_1ST_LIST))
					{
						if(!st.haveQuestItem(BLOODY_FANG, 50))
						{
							st.giveItems(BLOODY_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case HOOK_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_2ND_LIST))
					{
						if(!st.haveQuestItem(BLOODY_CLAW, 50) && Rnd.chance(75))
						{
							st.giveItems(BLOODY_CLAW, 1, false);
							if(st.haveQuestItem(BLOODY_CLAW, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case HUNTER_TARANTULA:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_5TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50) && Rnd.chance(75))
						{
							st.giveItems(BLOODY_TARANTULA_NAIL, 1, false);
							if(st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case CRIMSON_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_2ND_LIST) && !st.haveQuestItem(BLOODY_CLAW, 50))
					{
						if(Rnd.chance(60))
						{
							st.giveItems(BLOODY_CLAW, 1, false);
							if(st.haveQuestItem(BLOODY_CLAW, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case PINCER_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_2ND_LIST))
					{
						if(!st.haveQuestItem(BLOODY_CLAW, 50))
						{
							st.giveItems(BLOODY_CLAW, 1, false);
							if(st.haveQuestItem(BLOODY_CLAW, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case KASHA_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_4TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_KASHA_FANG, 50) && Rnd.chance(60))
						{
							st.giveItems(BLOODY_KASHA_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_KASHA_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case KASHA_FANG_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_4TH_LIST) && !st.haveQuestItem(BLOODY_KASHA_FANG, 50))
					{
						if(Rnd.chance(75))
						{
							st.giveItems(BLOODY_KASHA_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_KASHA_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case KASHA_BLADE_SPIDER:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_4TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_KASHA_FANG, 50))
						{
							st.giveItems(BLOODY_KASHA_FANG, 1, false);
							if(st.haveQuestItem(BLOODY_KASHA_FANG, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case PLUNDER_TARANTULA:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_5TH_LIST))
					{
						if(!st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50))
						{
							st.giveItems(BLOODY_TARANTULA_NAIL, 1, false);
							if(st.haveQuestItem(BLOODY_TARANTULA_NAIL, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
				case CRIMSON_SPIDER2:
				{
					if(st.haveQuestItem(ANIMAL_SLAYERS_LIST))
					{
						if(!st.haveQuestItem(BLOODY_RED_CLAW, 50) && Rnd.chance(75))
						{
							st.giveItems(BLOODY_RED_CLAW, 1, false);
							if(st.haveQuestItem(BLOODY_RED_CLAW, 50))
								st.playSound(SOUND_MIDDLE);
							else
								st.playSound(SOUND_ITEMGET);
						}
					}
					break;
				}
			}
		}
		return null;
	}
}