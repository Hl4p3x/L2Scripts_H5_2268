package services.villagemasters;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.VillageMasterInstance;
import l2s.gameserver.scripts.Functions;

public class Occupation extends Functions
{
	public void onTalk30026()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		//fighter
		if(classId == ClassId.HUMAN_FIGHTER)
			htmltext = "bitz003h.htm";

		//warrior, knight, rogue
		else if(classId == ClassId.WARRIOR || classId == ClassId.KNIGHT || classId == ClassId.ROGUE)
			htmltext = "bitz004.htm";
		//warlord, paladin, treasureHunter
		else if(classId == ClassId.WARLORD || classId == ClassId.PALADIN || classId == ClassId.TREASURE_HUNTER)
			htmltext = "bitz005.htm";
		//gladiator, darkAvenger, hawkeye
		else if(classId == ClassId.GLADIATOR || classId == ClassId.DARK_AVENGER || classId == ClassId.HAWKEYE)
			htmltext = "bitz005.htm";
		else
			htmltext = "bitz002.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30026/" + htmltext);
	}

	public void onTalk30031()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.WIZARD || classId == ClassId.CLERIC)
			htmltext = "06.htm";
		else if(classId == ClassId.SORCERER || classId == ClassId.NECROMANCER || classId == ClassId.WARLOCK || classId == ClassId.BISHOP || classId == ClassId.PROPHET)
			htmltext = "07.htm";
		else if(classId == ClassId.HUMAN_MAGE)
			htmltext = "01.htm";
		else
			// All other Races must be out
			htmltext = "08.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30031/" + htmltext);
	}

	public void onTalk30037()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_MAGE)
			htmltext = "01.htm";
		else if(classId == ClassId.HUMAN_MAGE)
			htmltext = "08.htm";
		else if(classId == ClassId.WIZARD || classId == ClassId.CLERIC || classId == ClassId.ELVEN_WIZARD || classId == ClassId.ORACLE)
			htmltext = "31.htm";
		else if(classId == ClassId.SORCERER || classId == ClassId.NECROMANCER || classId == ClassId.BISHOP || classId == ClassId.WARLOCK || classId == ClassId.PROPHET)
			htmltext = "32.htm";
		else if(classId == ClassId.SPELLSINGER || classId == ClassId.ELDER || classId == ClassId.ELEMENTAL_SUMMONER)
			htmltext = "32.htm";
		else
			htmltext = "33.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30037/" + htmltext);
	}

	public void onChange30037(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_FAITH_ID = 1201;
		int ETERNITY_DIAMOND_ID = 1230;
		int LEAF_OF_ORACLE_ID = 1235;
		int BEAD_OF_SEASON_ID = 1292;
		int classid = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		String htmltext = "33.htm";

		if(classid == 26 && pl.getClassId() == ClassId.ELVEN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
				htmltext = "15.htm";
			else if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
				htmltext = "16.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
				htmltext = "17.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ETERNITY_DIAMOND_ID, 1);
				pl.setClassId(classid, false, true);
				htmltext = "18.htm";
			}
		}
		else if(classid == 29 && pl.getClassId() == ClassId.ELVEN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
				htmltext = "19.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
				htmltext = "20.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
				htmltext = "21.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(LEAF_OF_ORACLE_ID, 1);
				pl.setClassId(classid, false, true);
				htmltext = "22.htm";
			}
		}
		else if(classid == 11 && pl.getClassId() == ClassId.HUMAN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
				htmltext = "23.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
				htmltext = "24.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
				htmltext = "25.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEAD_OF_SEASON_ID, 1);
				pl.setClassId(classid, false, true);
				htmltext = "26.htm";
			}
		}
		else if(classid == 15 && pl.getClassId() == ClassId.HUMAN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
				htmltext = "27.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
				htmltext = "28.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
				htmltext = "29.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_FAITH_ID, 1);
				pl.setClassId(classid, false, true);
				htmltext = "30.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30037/" + htmltext);
	}

	public void onTalk30066()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.HUMAN_FIGHTER)
			htmltext = "08.htm";
		else if(classId == ClassId.ELVEN_KNIGHT || classId == ClassId.ELVEN_SCOUT || classId == ClassId.WARRIOR || classId == ClassId.KNIGHT || classId == ClassId.ROGUE)
			htmltext = "38.htm";
		else if(classId == ClassId.TEMPLE_KNIGHT || classId == ClassId.PLAIN_WALKER || classId == ClassId.SWORDSINGER || classId == ClassId.SILVER_RANGER)
			htmltext = "39.htm";
		else if(classId == ClassId.WARLORD || classId == ClassId.PALADIN || classId == ClassId.TREASURE_HUNTER)
			htmltext = "39.htm";
		else if(classId == ClassId.GLADIATOR || classId == ClassId.DARK_AVENGER || classId == ClassId.HAWKEYE)
			htmltext = "39.htm";
		else
			htmltext = "40.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30066/" + htmltext);
	}

	public void onChange30066(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MEDALLION_OF_WARRIOR_ID = 1145;
		int SWORD_OF_RITUAL_ID = 1161;
		int BEZIQUES_RECOMMENDATION_ID = 1190;
		int ELVEN_KNIGHT_BROOCH_ID = 1204;
		int REORIA_RECOMMENDATION_ID = 1217;
		int newclass = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(newclass == 19 && classId == ClassId.ELVEN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) == null)
				htmltext = "18.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) != null)
				htmltext = "19.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) == null)
				htmltext = "20.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ELVEN_KNIGHT_BROOCH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ELVEN_KNIGHT_BROOCH_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "21.htm";
			}
		}

		if(newclass == 22 && classId == ClassId.ELVEN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) == null)
				htmltext = "22.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) != null)
				htmltext = "23.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) == null)
				htmltext = "24.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(REORIA_RECOMMENDATION_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(REORIA_RECOMMENDATION_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "25.htm";
			}
		}

		if(newclass == 1 && classId == ClassId.HUMAN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) == null)
				htmltext = "26.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) != null)
				htmltext = "27.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) == null)
				htmltext = "28.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MEDALLION_OF_WARRIOR_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MEDALLION_OF_WARRIOR_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "29.htm";
			}
		}

		if(newclass == 4 && classId == ClassId.HUMAN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) == null)
				htmltext = "30.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) != null)
				htmltext = "31.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) == null)
				htmltext = "32.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(SWORD_OF_RITUAL_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(SWORD_OF_RITUAL_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "33.htm";
			}
		}

		if(newclass == 7 && classId == ClassId.HUMAN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) == null)
				htmltext = "34.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) != null)
				htmltext = "35.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) == null)
				htmltext = "36.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEZIQUES_RECOMMENDATION_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEZIQUES_RECOMMENDATION_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "37.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30066/" + htmltext);
	}

	public void onTalk30511()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.SCAVENGER)
			htmltext = "01.htm";
		else if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "09.htm";
		else if(classId == ClassId.BOUNTY_HUNTER || classId == ClassId.WARSMITH)
			htmltext = "10.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30511/" + htmltext);
	}

	public void onChange30511(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_SEARCHER_ID = 2809;
		int MARK_OF_GUILDSMAN_ID = 3119;
		int MARK_OF_PROSPERITY_ID = 3238;
		int newclass = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(newclass == 55 && classId == ClassId.SCAVENGER)
			if(Level <= 39)
			{
				if(pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
					htmltext = "05.htm";
				else
					htmltext = "06.htm";
			}
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
				htmltext = "07.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GUILDSMAN_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_PROSPERITY_ID, 1);
				pl.setClassId(newclass, false, true);
				htmltext = "08.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30511/" + htmltext);
	}

	public void onTalk30070()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_MAGE)
			htmltext = "01.htm";
		else if(classId == ClassId.WIZARD || classId == ClassId.CLERIC || classId == ClassId.ELVEN_WIZARD || classId == ClassId.ORACLE)
			htmltext = "31.htm";
		else if(classId == ClassId.SORCERER || classId == ClassId.NECROMANCER || classId == ClassId.BISHOP || classId == ClassId.WARLOCK || classId == ClassId.PROPHET || classId == ClassId.SPELLSINGER || classId == ClassId.ELDER || classId == ClassId.ELEMENTAL_SUMMONER)
			htmltext = "32.htm";
		else if(classId == ClassId.HUMAN_MAGE)
			htmltext = "08.htm";
		else
			htmltext = "33.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30070/" + htmltext);
	}

	public void onChange30070(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_FAITH_ID = 1201;
		int ETERNITY_DIAMOND_ID = 1230;
		int LEAF_OF_ORACLE_ID = 1235;
		int BEAD_OF_SEASON_ID = 1292;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 26 && classId == ClassId.ELVEN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
				htmltext = "15.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
				htmltext = "16.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) == null)
				htmltext = "17.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ETERNITY_DIAMOND_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ETERNITY_DIAMOND_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "18.htm";
			}
		}
		else if(event == 29 && classId == ClassId.ELVEN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
				htmltext = "19.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
				htmltext = "20.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) == null)
				htmltext = "21.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(LEAF_OF_ORACLE_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(LEAF_OF_ORACLE_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "22.htm";
			}
		}
		else if(event == 11 && classId == ClassId.HUMAN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
				htmltext = "23.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
				htmltext = "24.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) == null)
				htmltext = "25.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(BEAD_OF_SEASON_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(BEAD_OF_SEASON_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "26.htm";
			}
		}
		else if(event == 15 && classId == ClassId.HUMAN_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
				htmltext = "27.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
				htmltext = "28.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) == null)
				htmltext = "29.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_FAITH_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_FAITH_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "30.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30070/" + htmltext);
	}

	public void onTalk30154()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.ELVEN_MAGE)
			htmltext = "02.htm";
		else if(classId == ClassId.ELVEN_WIZARD || classId == ClassId.ORACLE || classId == ClassId.ELVEN_KNIGHT || classId == ClassId.ELVEN_SCOUT)
			htmltext = "12.htm";
		else if(pl.getRace() == Race.ELF)
			htmltext = "13.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30154/" + htmltext);
	}

	public void onTalk30358()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DARK_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.DARK_MAGE)
			htmltext = "02.htm";
		else if(classId == ClassId.DARK_WIZARD || classId == ClassId.SHILLEN_ORACLE || classId == ClassId.PALUS_KNIGHT || classId == ClassId.ASSASIN)
			htmltext = "12.htm";
		else if(pl.getRace() == Race.DARKELF)
			htmltext = "13.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30358/" + htmltext);
	}

	public void onTalk30498()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.SCAVENGER || classId == ClassId.ARTISAN)
			htmltext = "09.htm";
		else if(pl.getRace() == Race.DWARF)
			htmltext = "10.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30498/" + htmltext);
	}

	public void onChange30498(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int RING_OF_RAVEN_ID = 1642;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 54 && classId == ClassId.DWARVEN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) == null)
				htmltext = "05.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) != null)
				htmltext = "06.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) == null)
				htmltext = "07.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(RING_OF_RAVEN_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(RING_OF_RAVEN_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "08.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30498/" + htmltext);
	}

	public void onTalk30499()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.SCAVENGER || classId == ClassId.ARTISAN)
			htmltext = "09.htm";
		else if(pl.getRace() == Race.DWARF)
			htmltext = "10.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30499/" + htmltext);
	}

	public void onChange30499(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int PASS_FINAL_ID = 1635;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 56 && classId == ClassId.DWARVEN_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) == null)
				htmltext = "05.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) != null)
				htmltext = "06.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) == null)
				htmltext = "07.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(PASS_FINAL_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(PASS_FINAL_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "08.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30499/" + htmltext);
	}

	public void onTalk30525()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.ARTISAN)
			htmltext = "05.htm";
		else if(classId == ClassId.WARSMITH)
			htmltext = "06.htm";
		else
			htmltext = "07.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30525/" + htmltext);
	}

	public void onTalk30520()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.ARTISAN || classId == ClassId.SCAVENGER)
			htmltext = "05.htm";
		else if(classId == ClassId.WARSMITH || classId == ClassId.BOUNTY_HUNTER)
			htmltext = "06.htm";
		else
			htmltext = "07.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30520/" + htmltext);
	}

	public void onTalk30512()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ARTISAN)
			htmltext = "01.htm";
		else if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "09.htm";
		else if(classId == ClassId.WARSMITH || classId == ClassId.BOUNTY_HUNTER)
			htmltext = "10.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30512/" + htmltext);
	}

	public void onChange30512(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_MAESTRO_ID = 2867;
		int MARK_OF_GUILDSMAN_ID = 3119;
		int MARK_OF_PROSPERITY_ID = 3238;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 57 && classId == ClassId.ARTISAN)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_MAESTRO_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
					htmltext = "05.htm";
				else
					htmltext = "06.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_MAESTRO_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GUILDSMAN_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_PROSPERITY_ID) == null)
				htmltext = "07.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_GUILDSMAN_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAESTRO_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_PROSPERITY_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "08.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30512/" + htmltext);
	}

	public void onTalk30565()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ORC_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.ORC_RAIDER || classId == ClassId.ORC_MONK || classId == ClassId.ORC_SHAMAN)
			htmltext = "09.htm";
		else if(classId == ClassId.ORC_MAGE)
			htmltext = "16.htm";
		else if(pl.getRace() == Race.ORC)
			htmltext = "10.htm";
		else
			htmltext = "11.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30565/" + htmltext);
	}

	public void onTalk30109()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_KNIGHT)
			htmltext = "01.htm";
		else if(classId == ClassId.KNIGHT)
			htmltext = "08.htm";
		else if(classId == ClassId.ROGUE)
			htmltext = "15.htm";
		else if(classId == ClassId.ELVEN_SCOUT)
			htmltext = "22.htm";
		else if(classId == ClassId.WARRIOR)
			htmltext = "29.htm";
		else if(classId == ClassId.ELVEN_FIGHTER || classId == ClassId.HUMAN_FIGHTER)
			htmltext = "76.htm";
		else if(classId == ClassId.TEMPLE_KNIGHT || classId == ClassId.PLAIN_WALKER || classId == ClassId.SWORDSINGER || classId == ClassId.SILVER_RANGER)
			htmltext = "77.htm";
		else if(classId == ClassId.WARLORD || classId == ClassId.PALADIN || classId == ClassId.TREASURE_HUNTER)
			htmltext = "77.htm";
		else if(classId == ClassId.GLADIATOR || classId == ClassId.DARK_AVENGER || classId == ClassId.HAWKEYE)
			htmltext = "77.htm";
		else
			htmltext = "78.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30109/" + htmltext);
	}

	public void onChange30109(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_CHALLENGER_ID = 2627;
		int MARK_OF_DUTY_ID = 2633;
		int MARK_OF_SEEKER_ID = 2673;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_DUELIST_ID = 2762;
		int MARK_OF_SEARCHER_ID = 2809;
		int MARK_OF_HEALER_ID = 2820;
		int MARK_OF_LIFE_ID = 3140;
		int MARK_OF_CHAMPION_ID = 3276;
		int MARK_OF_SAGITTARIUS_ID = 3293;
		int MARK_OF_WITCHCRAFT_ID = 3307;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 20 && classId == ClassId.ELVEN_KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
					htmltext = "36.htm";
				else
					htmltext = "37.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
				htmltext = "38.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "39.htm";
			}

		else if(event == 21 && classId == ClassId.ELVEN_KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
					htmltext = "40.htm";
				else
					htmltext = "41.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
				htmltext = "42.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "43.htm";
			}

		else if(event == 5 && classId == ClassId.KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
					htmltext = "44.htm";
				else
					htmltext = "45.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
				htmltext = "46.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "47.htm";
			}

		else if(event == 6 && classId == ClassId.KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
					htmltext = "48.htm";
				else
					htmltext = "49.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
				htmltext = "50.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRAFT_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "51.htm";
			}

		else if(event == 8 && classId == ClassId.ROGUE)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
					htmltext = "52.htm";
				else
					htmltext = "53.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
				htmltext = "54.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "55.htm";
			}

		else if(event == 9 && classId == ClassId.ROGUE)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
					htmltext = "56.htm";
				else
					htmltext = "57.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
				htmltext = "58.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "59.htm";
			}

		else if(event == 23 && classId == ClassId.ELVEN_SCOUT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
					htmltext = "60.htm";
				else
					htmltext = "61.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
				htmltext = "62.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "63.htm";
			}

		else if(event == 24 && classId == ClassId.ELVEN_SCOUT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
					htmltext = "64.htm";
				else
					htmltext = "65.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
				htmltext = "66.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "67.htm";
			}

		else if(event == 2 && classId == ClassId.WARRIOR)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
					htmltext = "68.htm";
				else
					htmltext = "69.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
				htmltext = "70.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "71.htm";
			}

		else if(event == 3 && classId == ClassId.WARRIOR)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
					htmltext = "72.htm";
				else
					htmltext = "73.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
				htmltext = "74.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_CHAMPION_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "75.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30109/" + htmltext);
	}

	public void onTalk30115()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ELVEN_WIZARD)
			htmltext = "01.htm";
		else if(classId == ClassId.WIZARD)
			htmltext = "08.htm";
		else if(classId == ClassId.SORCERER || classId == ClassId.NECROMANCER || classId == ClassId.WARLOCK)
			htmltext = "39.htm";
		else if(classId == ClassId.SPELLSINGER || classId == ClassId.ELEMENTAL_SUMMONER)
			htmltext = "39.htm";
		else if((pl.getRace() == Race.ELF || pl.getRace() == Race.HUMAN) && classId.isMage())
			htmltext = "38.htm";
		else
			htmltext = "40.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30115/" + htmltext);
	}

	public void onChange30115(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_SCHOLAR_ID = 2674;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_MAGUS_ID = 2840;
		int MARK_OF_LIFE_ID = 3140;
		int MARK_OF_WITCHCRFAT_ID = 3307;
		int MARK_OF_SUMMONER_ID = 3336;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 27 && classId == ClassId.ELVEN_WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
					htmltext = "18.htm";
				else
					htmltext = "19.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
				htmltext = "20.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "21.htm";
			}

		else if(event == 28 && classId == ClassId.ELVEN_WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
					htmltext = "22.htm";
				else
					htmltext = "23.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
				htmltext = "24.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "25.htm";
			}

		else if(event == 12 && classId == ClassId.WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
					htmltext = "26.htm";
				else
					htmltext = "27.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
				htmltext = "28.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "29.htm";
			}

		else if(event == 13 && classId == ClassId.WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRFAT_ID) == null)
					htmltext = "30.htm";
				else
					htmltext = "31.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRFAT_ID) == null)
				htmltext = "32.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRFAT_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "33.htm";
			}

		else if(event == 14 && classId == ClassId.WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
					htmltext = "34.htm";
				else
					htmltext = "35.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
				htmltext = "36.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "37.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30115/" + htmltext);
	}

	public void onTalk30120()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ORACLE)
			htmltext = "01.htm";
		else if(classId == ClassId.CLERIC)
			htmltext = "05.htm";
		else if(classId == ClassId.ELDER || classId == ClassId.BISHOP || classId == ClassId.PROPHET)
			htmltext = "25.htm";
		else if((pl.getRace() == Race.HUMAN || pl.getRace() == Race.ELF) && classId.isMage())
			htmltext = "24.htm";
		else
			htmltext = "26.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30120/" + htmltext);
	}

	public void onChange30120(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_PILGRIM_ID = 2721;
		int MARK_OF_TRUST_ID = 2734;
		int MARK_OF_HEALER_ID = 2820;
		int MARK_OF_REFORMER_ID = 2821;
		int MARK_OF_LIFE_ID = 3140;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 30 || classId == ClassId.ORACLE)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
					htmltext = "12.htm";
				else
					htmltext = "13.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LIFE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
				htmltext = "14.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LIFE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "15.htm";
			}

		else if(event == 16 && classId == ClassId.CLERIC)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
					htmltext = "16.htm";
				else
					htmltext = "17.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_HEALER_ID) == null)
				htmltext = "18.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_HEALER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "19.htm";
			}

		else if(event == 17 && classId == ClassId.CLERIC)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
					htmltext = "20.htm";
				else
					htmltext = "21.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_TRUST_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
				htmltext = "22.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_TRUST_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_REFORMER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "23.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30120/" + htmltext);
	}

	public void onTalk30500()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ORC_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.ORC_MAGE)
			htmltext = "06.htm";
		else if(classId == ClassId.ORC_RAIDER || classId == ClassId.ORC_MONK || classId == ClassId.ORC_SHAMAN)
			htmltext = "21.htm";
		else if(classId == ClassId.DESTROYER || classId == ClassId.TYRANT || classId == ClassId.OVERLORD || classId == ClassId.WARCRYER)
			htmltext = "22.htm";
		else
			htmltext = "23.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30500/" + htmltext);
	}

	public void onChange30500(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_RAIDER_ID = 1592;
		int KHAVATARI_TOTEM_ID = 1615;
		int MASK_OF_MEDIUM_ID = 1631;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 45 && classId == ClassId.ORC_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) == null)
				htmltext = "09.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) != null)
				htmltext = "10.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) == null)
				htmltext = "11.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MARK_OF_RAIDER_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_RAIDER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "12.htm";
			}
		}

		else if(event == 47 && classId == ClassId.ORC_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) == null)
				htmltext = "13.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) != null)
				htmltext = "14.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) == null)
				htmltext = "15.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(KHAVATARI_TOTEM_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(KHAVATARI_TOTEM_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "16.htm";
			}
		}

		else if(event == 50 && classId == ClassId.ORC_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) == null)
				htmltext = "17.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) != null)
				htmltext = "18.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) == null)
				htmltext = "19.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(MASK_OF_MEDIUM_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(MASK_OF_MEDIUM_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "20.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30500/" + htmltext);
	}

	public void onTalk30290()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.DARK_FIGHTER)
			htmltext = "01.htm";
		else if(classId == ClassId.DARK_MAGE)
			htmltext = "08.htm";
		else if(classId == ClassId.PALUS_KNIGHT || classId == ClassId.ASSASIN || classId == ClassId.DARK_WIZARD || classId == ClassId.SHILLEN_ORACLE)
			htmltext = "31.htm";
		else if(pl.getRace() == Race.DARKELF)
			htmltext = "32.htm";
		else
			htmltext = "33.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30290/" + htmltext);
	}

	public void onChange30290(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int GAZE_OF_ABYSS_ID = 1244;
		int IRON_HEART_ID = 1252;
		int JEWEL_OF_DARKNESS_ID = 1261;
		int ORB_OF_ABYSS_ID = 1270;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 32 && classId == ClassId.DARK_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) == null)
				htmltext = "15.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) != null)
				htmltext = "16.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) == null)
				htmltext = "17.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(GAZE_OF_ABYSS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(GAZE_OF_ABYSS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "18.htm";
			}
		}

		else if(event == 35 && classId == ClassId.DARK_FIGHTER)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(IRON_HEART_ID) == null)
				htmltext = "19.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(IRON_HEART_ID) != null)
				htmltext = "20.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(IRON_HEART_ID) == null)
				htmltext = "21.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(IRON_HEART_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(IRON_HEART_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "22.htm";
			}
		}

		else if(event == 39 && classId == ClassId.DARK_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) == null)
				htmltext = "23.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) != null)
				htmltext = "24.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) == null)
				htmltext = "25.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(JEWEL_OF_DARKNESS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(JEWEL_OF_DARKNESS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "26.htm";
			}
		}

		else if(event == 42 && classId == ClassId.DARK_MAGE)
		{
			if(Level <= 19 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) == null)
				htmltext = "27.htm";
			if(Level <= 19 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) != null)
				htmltext = "28.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) == null)
				htmltext = "29.htm";
			if(Level >= 20 && pl.getInventory().getItemByItemId(ORB_OF_ABYSS_ID) != null)
			{
				pl.getInventory().destroyItemByItemId(ORB_OF_ABYSS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "30.htm";
			}
		}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30290/" + htmltext);
	}

	public void onTalk30513()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.ORC_MONK)
			htmltext = "01.htm";
		else if(classId == ClassId.ORC_RAIDER)
			htmltext = "05.htm";
		else if(classId == ClassId.ORC_SHAMAN)
			htmltext = "09.htm";
		else if(classId == ClassId.DESTROYER || classId == ClassId.TYRANT || classId == ClassId.OVERLORD || classId == ClassId.WARCRYER)
			htmltext = "32.htm";
		else if(classId == ClassId.ORC_FIGHTER || classId == ClassId.ORC_MAGE)
			htmltext = "33.htm";
		else
			htmltext = "34.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30513/" + htmltext);
	}

	public void onChange30513(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_CHALLENGER_ID = 2627;
		int MARK_OF_PILGRIM_ID = 2721;
		int MARK_OF_DUELIST_ID = 2762;
		int MARK_OF_WARSPIRIT_ID = 2879;
		int MARK_OF_GLORY_ID = 3203;
		int MARK_OF_CHAMPION_ID = 3276;
		int MARK_OF_LORD_ID = 3390;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 48 && classId == ClassId.ORC_MONK)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
					htmltext = "16.htm";
				else
					htmltext = "17.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
				htmltext = "18.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "19.htm";
			}

		else if(event == 46 && classId == ClassId.ORC_RAIDER)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
					htmltext = "20.htm";
				else
					htmltext = "21.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_CHAMPION_ID) == null)
				htmltext = "22.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_CHAMPION_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "23.htm";
			}

		else if(event == 51 && classId == ClassId.ORC_SHAMAN)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LORD_ID) == null)
					htmltext = "24.htm";
				else
					htmltext = "25.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_LORD_ID) == null)
				htmltext = "26.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_LORD_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "27.htm";
			}

		else if(event == 52 && classId == ClassId.ORC_SHAMAN)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WARSPIRIT_ID) == null)
					htmltext = "28.htm";
				else
					htmltext = "29.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_GLORY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WARSPIRIT_ID) == null)
				htmltext = "30.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_GLORY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WARSPIRIT_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "31.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30513/" + htmltext);
	}

	public void onTalk30474()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(npc.getNpcId() == 30175)
		{
			if(classId == ClassId.SHILLEN_ORACLE)
				htmltext = "08.htm";
			else if(classId == ClassId.DARK_WIZARD)
				htmltext = "19.htm";
			else if(classId == ClassId.SPELLHOWLER || classId == ClassId.SHILLEN_ELDER || classId == ClassId.PHANTOM_SUMMONER)
				htmltext = "54.htm";
			else if(classId == ClassId.DARK_MAGE)
				htmltext = "55.htm";
			else
				htmltext = "56.htm";
		}
		else if(classId == ClassId.PALUS_KNIGHT)
			htmltext = "01.htm";
		else if(classId == ClassId.SHILLEN_ORACLE)
			htmltext = "08.htm";
		else if(classId == ClassId.ASSASIN)
			htmltext = "12.htm";
		else if(classId == ClassId.DARK_WIZARD)
			htmltext = "19.htm";
		else if(classId == ClassId.SHILLEN_KNIGHT || classId == ClassId.ABYSS_WALKER || classId == ClassId.BLADEDANCER || classId == ClassId.PHANTOM_RANGER)
			htmltext = "54.htm";
		else if(classId == ClassId.SPELLHOWLER || classId == ClassId.SHILLEN_ELDER || classId == ClassId.PHANTOM_SUMMONER)
			htmltext = "54.htm";
		else if(classId == ClassId.DARK_FIGHTER || classId == ClassId.DARK_MAGE)
			htmltext = "55.htm";
		else
			htmltext = "56.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30474/" + htmltext);
	}

	public void onChange30474(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int MARK_OF_CHALLENGER_ID = 2627;
		int MARK_OF_DUTY_ID = 2633;
		int MARK_OF_SEEKER_ID = 2673;
		int MARK_OF_SCHOLAR_ID = 2674;
		int MARK_OF_PILGRIM_ID = 2721;
		int MARK_OF_DUELIST_ID = 2762;
		int MARK_OF_SEARCHER_ID = 2809;
		int MARK_OF_REFORMER_ID = 2821;
		int MARK_OF_MAGUS_ID = 2840;
		int MARK_OF_FATE_ID = 3172;
		int MARK_OF_SAGITTARIUS_ID = 3293;
		int MARK_OF_WITCHCRAFT_ID = 3307;
		int MARK_OF_SUMMONER_ID = 3336;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "No Quest";

		if(event == 33 && classId == ClassId.PALUS_KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
					htmltext = "26.htm";
				else
					htmltext = "27.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_DUTY_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_WITCHCRAFT_ID) == null)
				htmltext = "28.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_DUTY_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_WITCHCRAFT_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "29.htm";
			}

		else if(event == 34 && classId == ClassId.PALUS_KNIGHT)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
					htmltext = "30.htm";
				else
					htmltext = "31.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_CHALLENGER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_DUELIST_ID) == null)
				htmltext = "32.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_CHALLENGER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_DUELIST_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "33.htm";
			}

		else if(event == 43 && classId == ClassId.SHILLEN_ORACLE)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
					htmltext = "34.htm";
				else
					htmltext = "35.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_PILGRIM_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_REFORMER_ID) == null)
				htmltext = "36.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_PILGRIM_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_REFORMER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "37.htm";
			}

		else if(event == 36 && classId == ClassId.ASSASIN)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
					htmltext = "38.htm";
				else
					htmltext = "39.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SEARCHER_ID) == null)
				htmltext = "40.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SEARCHER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "41.htm";
			}

		else if(event == 37 && classId == ClassId.ASSASIN)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
					htmltext = "42.htm";
				else
					htmltext = "43.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SEEKER_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SAGITTARIUS_ID) == null)
				htmltext = "44.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SEEKER_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SAGITTARIUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "45.htm";
			}

		else if(event == 40 && classId == ClassId.DARK_WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
					htmltext = "46.htm";
				else
					htmltext = "47.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_MAGUS_ID) == null)
				htmltext = "48.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_MAGUS_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "49.htm";
			}

		else if(event == 41 && classId == ClassId.DARK_WIZARD)
			if(Level <= 39)
				if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
					htmltext = "50.htm";
				else
					htmltext = "51.htm";
			else if(pl.getInventory().getItemByItemId(MARK_OF_SCHOLAR_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_FATE_ID) == null || pl.getInventory().getItemByItemId(MARK_OF_SUMMONER_ID) == null)
				htmltext = "52.htm";
			else
			{
				pl.getInventory().destroyItemByItemId(MARK_OF_SCHOLAR_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_FATE_ID, 1);
				pl.getInventory().destroyItemByItemId(MARK_OF_SUMMONER_ID, 1);
				pl.setClassId(event, false, true);
				htmltext = "53.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/30474/" + htmltext);
	}

	public void onChange32145(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int SteelrazorEvaluation = 9772;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "04.htm";

		if(event == 126 && classId == ClassId.KAMAEL_F_SOLDIER)
			if(Level >= 20 && pl.getInventory().getItemByItemId(SteelrazorEvaluation) != null)
			{
				pl.getInventory().destroyItemByItemId(SteelrazorEvaluation, 1);
				pl.setClassId(event, false, true);
				htmltext = "03.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/32145/" + htmltext);
	}

	public void onTalk32145()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.KAMAEL_F_SOLDIER)
			htmltext = "01.htm";
		else
			htmltext = "02.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/32145/" + htmltext);
	}

	public void onChange32146(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int GwainsRecommendation = 9753;
		int event = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		ClassId classId = pl.getClassId();
		String htmltext = "04.htm";

		if(event == 125 && classId == ClassId.KAMAEL_M_SOLDIER)
			if(Level >= 20 && pl.getInventory().getItemByItemId(GwainsRecommendation) != null)
			{
				pl.getInventory().destroyItemByItemId(GwainsRecommendation, 1);
				pl.setClassId(event, false, true);
				htmltext = "03.htm";
			}

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/32146/" + htmltext);
	}

	public void onTalk32146()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.KAMAEL_M_SOLDIER)
			htmltext = "01.htm";
		else
			htmltext = "02.htm";

		npc.showChatWindow(pl, "villagemaster/32146/" + htmltext);
	}

	public void onTalk32199()
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();

		if(classId == ClassId.WARDER)
			htmltext = "01.htm";
		else if(classId == ClassId.TROOPER)
			htmltext = "11.htm";
		else
			htmltext = "02.htm";

		npc.showChatWindow(pl, "villagemaster/32199/" + htmltext);
	}

	public void onTalk32157()
	{
		String prefix = "head_blacksmith_mokabred";
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();

		if(race != Race.DWARF)
			htmltext = "002.htm";
		else if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "003f.htm";
		else if(classId.isOfLevel(ClassLevel.SECOND))
			htmltext = "004.htm";
		else
			htmltext = "005.htm";

		npc.showChatWindow(pl, "villagemaster/32157/" + prefix + htmltext);
	}

	public void onTalk32160()
	{
		String prefix = "grandmagister_devon";
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();

		if(race != Race.DARKELF)
			htmltext = "002.htm";
		else if(classId == ClassId.DARK_FIGHTER)
			htmltext = "003f.htm";
		else if(classId == ClassId.DARK_MAGE)
			htmltext = "003m.htm";
		else if(classId.isOfLevel(ClassLevel.SECOND))
			htmltext = "004.htm";
		else
			htmltext = "005.htm";

		npc.showChatWindow(pl, "villagemaster/32160/" + prefix + htmltext);
	}

	public void onChange32199(String[] args)
	{
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		int KamaelInquisitorMark = 9782;
		int SB_Certificate = 9806;
		int OrkurusRecommendation = 9760;
		int classid = Integer.parseInt(args[0]);

		int Level = pl.getLevel();
		String htmltext = "02.htm";

		if(classid == 130 && pl.getClassId() == ClassId.WARDER)
		{
			if(Level <= 39 && pl.getInventory().getItemByItemId(KamaelInquisitorMark) == null)
				htmltext = "03.htm";
			else if(Level <= 39 && pl.getInventory().getItemByItemId(KamaelInquisitorMark) != null)
				htmltext = "04.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(KamaelInquisitorMark) == null)
				htmltext = "05.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(KamaelInquisitorMark) != null)
			{
				pl.getInventory().destroyItemByItemId(KamaelInquisitorMark, 1);
				pl.setClassId(classid, false, true);
				htmltext = "06.htm";
			}
		}
		else if(classid == 129 && pl.getClassId() == ClassId.WARDER)
		{
			if(Level <= 39 && pl.getInventory().getItemByItemId(SB_Certificate) == null)
				htmltext = "07.htm";
			else if(Level <= 39 && pl.getInventory().getItemByItemId(SB_Certificate) != null)
				htmltext = "08.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(SB_Certificate) == null)
				htmltext = "09.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(SB_Certificate) != null)
			{
				pl.getInventory().destroyItemByItemId(SB_Certificate, 1);
				pl.setClassId(classid, false, true);
				htmltext = "10.htm";
			}
		}
		else if(classid == 127 && pl.getClassId() == ClassId.TROOPER)
		{
			if(Level <= 39 && pl.getInventory().getItemByItemId(OrkurusRecommendation) == null)
				htmltext = "12.htm";
			else if(Level <= 39 && pl.getInventory().getItemByItemId(OrkurusRecommendation) != null)
				htmltext = "13.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(OrkurusRecommendation) == null)
				htmltext = "14.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(OrkurusRecommendation) != null)
			{
				pl.getInventory().destroyItemByItemId(OrkurusRecommendation, 1);
				pl.setClassId(classid, false, true);
				htmltext = "15.htm";
			}
		}
		else if(classid == 128 && pl.getClassId() == ClassId.TROOPER)
		{
			if(Level <= 39 && pl.getInventory().getItemByItemId(SB_Certificate) == null)
				htmltext = "16.htm";
			else if(Level <= 39 && pl.getInventory().getItemByItemId(SB_Certificate) != null)
				htmltext = "17.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(SB_Certificate) == null)
				htmltext = "18.htm";
			if(Level >= 40 && pl.getInventory().getItemByItemId(SB_Certificate) != null)
			{
				pl.getInventory().destroyItemByItemId(SB_Certificate, 1);
				pl.setClassId(classid, false, true);
				htmltext = "19.htm";
			}
		}
		npc.showChatWindow(pl, "villagemaster/32199/" + htmltext);
	}

	public void onTalk32158()
	{
		String prefix = "warehouse_chief_fisser";
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();

		if(race != Race.DWARF)
			htmltext = "002.htm";
		else if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "003f.htm";
		else if(classId.isOfLevel(ClassLevel.SECOND))
			htmltext = "004.htm";
		else
			htmltext = "005.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/32158/" + prefix + htmltext);
	}

	public void onTalk32171()
	{
		String prefix = "warehouse_chief_hufran";
		Player pl = getSelf();
		NpcInstance npc = getNpc();
		if(pl == null || npc == null)
			return;
		if(!(npc instanceof VillageMasterInstance))
		{
			show("I have nothing to say you", pl, npc);
			return;
		}

		String htmltext;
		ClassId classId = pl.getClassId();
		Race race = pl.getRace();

		if(race != Race.DWARF)
			htmltext = "002.htm";
		else if(classId == ClassId.DWARVEN_FIGHTER)
			htmltext = "003f.htm";
		else if(classId.isOfLevel(ClassLevel.SECOND))
			htmltext = "004.htm";
		else
			htmltext = "005.htm";

		((VillageMasterInstance) npc).showChatWindow(pl, "villagemaster/32171/" + prefix + htmltext);
	}

	public void onTalk32213()
	{
		onTalk32199();
	}

	public void onChange32213(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32214()
	{
		onTalk32199();
	}

	public void onChange32214(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32217()
	{
		onTalk32199();
	}

	public void onChange32217(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32218()
	{
		onTalk32199();
	}

	public void onChange32218(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32221()
	{
		onTalk32199();
	}

	public void onChange32221(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32222()
	{
		onTalk32199();
	}

	public void onChange32222(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32205()
	{
		onTalk32199();
	}

	public void onChange32205(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32206()
	{
		onTalk32199();
	}

	public void onChange32206(String[] args)
	{
		onChange32199(args);
	}

	public void onTalk32147()
	{
		onTalk30037();
	}

	public void onTalk32150()
	{
		onTalk30565();
	}

	public void onTalk32153()
	{
		onTalk30037();
	}

	public void onTalk32154()
	{
		onTalk30066();
	}

	public void onTalk32226()
	{
		onTalk32199();
	}

	public void onTalk32225()
	{
		onTalk32199();
	}

	public void onTalk32230()
	{
		onTalk32199();
	}

	public void onTalk32229()
	{
		onTalk32199();
	}

	public void onTalk32233()
	{
		onTalk32199();
	}

	public void onTalk32234()
	{
		onTalk32199();
	}

	public void onTalk32202()
	{
		onTalk32199();
	}

	public void onTalk32210()
	{
		onTalk32199();
	}

	public void onTalk32209()
	{
		onTalk32199();
	}
}