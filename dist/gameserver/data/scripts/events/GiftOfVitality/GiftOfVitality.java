package events.GiftOfVitality;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

/**
 *
 * @author n0nam3, pchayka
 * @date 12/10/2010 20:06
 *
 * http://www.lineage2.com/archive/2010/06/gift_of_vitalit.html
 *
 */
public class GiftOfVitality extends Functions implements ScriptFile
{
	private static final String EVENT_NAME = "GiftOfVitality";
	private static final int REUSE_HOURS = 24; // reuse
	private static final int EVENT_MANAGER_ID = 109; // npc id
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static final Logger _log = LoggerFactory.getLogger(GiftOfVitality.class);

	private final static int[][] _mageBuff = new int[][] {
			{ 5627, 1 }, // windwalk
			{ 5628, 1 }, // shield
			{ 5637, 1 }, // Magic Barrier 1
			{ 5633, 1 }, // blessthesoul
			{ 5634, 1 }, // acumen
			{ 5635, 1 }, // concentration
			{ 5636, 1 }, // empower
	};

	private final static int[][] _warrBuff = new int[][] {
			{ 5627, 1 }, // windwalk
			{ 5628, 1 }, // shield
			{ 5637, 1 }, // Magic Barrier 1
			{ 5629, 1 }, // btb
			{ 5630, 1 }, // vampirerage
			{ 5631, 1 }, // regeneration
			{ 5632, 1 }, // haste 2
	};

	private final static int[][] _summonBuff = new int[][] {
			{ 5627, 1 }, // windwalk
			{ 5628, 1 }, // shield
			{ 5637, 1 }, // Magic Barrier 1
			{ 5629, 1 }, // btb
			{ 5633, 1 }, // vampirerage
			{ 5630, 1 }, // regeneration
			{ 5634, 1 }, // blessthesoul
			{ 5631, 1 }, // acumen
			{ 5635, 1 }, // concentration
			{ 5632, 1 }, // empower
			{ 5636, 1 }, // haste 2
	};

	public enum BuffType
	{
		PLAYER,
		SUMMON,
		VITALITY,
	}

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = {
				{ -119494, 44882, 360, 24576 }, //Kamael Village
				{ -82687, 243157, -3734, 4096 }, // Talking Island Village
				{ 45538, 48357, -3056, 18000 }, //Elven Village
				{ 9929, 16324, -4568, 62999 }, //Dark Elven Village
				{ 115096, -178370, -880, 0 }, //Dwarven Village
				{ -45372, -114104, -240, 16384 }, //Orc Village
				{ -83156, 150994, -3120, 0 }, //Gludin
				{ -13727, 122117, -2984, 16384 }, //Gludio
				{ 16111, 142850, -2696, 16000 }, //Dion
				{ 111176, 220968, -3544, 16384 }, // Heine
				{ 82792, 149448, -3494, 0 }, // Giran
				{ 81083, 56118, -1552, 32768 }, //Oren
				{ 117016, 77240, -2688, 49151 }, // Hunters Village
				{ 147016, 25928, -2038, 16384 }, // Aden
				{ 43966, -47709, -792, 49999 }, //Rune
				{ 148088, -55416, -2728, 49151 }, // Goddart
				{ 87080, -141336, -1344, 0 }, // Schutgard
		};

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive(EVENT_NAME);
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive(EVENT_NAME, true))
		{
			spawnEventManagers();
			System.out.println("Event: 'Gift Of Vitality' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.GiftOfVitality.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Gift Of Vitality' already started.");

		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive(EVENT_NAME, false))
		{
			unSpawnEventManagers();
			System.out.println("Event: 'Gift Of Vitality' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.GiftOfVitality.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event: 'Gift Of Vitality' not started.");

		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		if(isActive())
		{
			spawnEventManagers();
			_log.info("Loaded Event: Gift Of Vitality [state: activated]");
		}
		else
			_log.info("Loaded Event: Gift Of Vitality [state: deactivated]");
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}

	private void buffMe(BuffType type)
	{
		if(getSelf() == null || getNpc() == null || getSelf().getPlayer() == null)
			return;
		String htmltext = null;
		Player player = getSelf().getPlayer();
		if(player.isInOlympiadMode())
			return;
		NpcInstance npc = getNpc();
		String var = player.getVar("govEventTime");

		switch(type)
		{
			case VITALITY:
				if(var != null && Long.parseLong(var) > System.currentTimeMillis() || player.getBaseClassId() != player.getActiveClassId())
					htmltext = "jack-notime.htm";
				else
				{
					npc.broadcastPacket(new MagicSkillUse(npc, player, 23179, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(23179, 1));
					player.setVar("govEventTime", String.valueOf(System.currentTimeMillis() + REUSE_HOURS * 60 * 60 * 1000L), -1);
					player.setVitality(Config.VITALITY_LEVELS[4]);
					htmltext = "jack-okvitality.htm";
				}
				break;
			case SUMMON:
				if(player.getLevel() < 76)
					htmltext = "jack-nolevel.htm";
				else if(player.getServitor() == null || !player.getServitor().isSummon())
					htmltext = "jack-nosummon.htm";
				else
				{
					for(int[] buff : _summonBuff)
					{
						npc.broadcastPacket(new MagicSkillUse(npc, player.getServitor(), buff[0], buff[1], 0, 0));
						player.altOnMagicUseTimer(player.getServitor(), SkillHolder.getInstance().getSkill(buff[0], buff[1]));
					}
					htmltext = "jack-okbuff.htm";
				}
				break;
			case PLAYER:
				if(player.getLevel() < 76)
					htmltext = "jack-nolevel.htm";
				else
				{
					if(!player.isMageClass() || player.getTemplate().getRace() == Race.ORC)
						for(int[] buff : _warrBuff)
						{
							npc.broadcastPacket(new MagicSkillUse(npc, player, buff[0], buff[1], 0, 0));
							player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(buff[0], buff[1]));
						}
					else
						for(int[] buff : _mageBuff)
						{
							npc.broadcastPacket(new MagicSkillUse(npc, player, buff[0], buff[1], 0, 0));
							player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(buff[0], buff[1]));
						}
					htmltext = "jack-okbuff.htm";
				}
				break;
		}
		show("scripts/events/GiftOfVitality/" + htmltext, getSelf().getPlayer());
	}

	public void buffVitality()
	{
		buffMe(BuffType.VITALITY);
	}

	public void buffSummon()
	{
		buffMe(BuffType.SUMMON);
	}

	public void buffPlayer()
	{
		buffMe(BuffType.PLAYER);
	}
}