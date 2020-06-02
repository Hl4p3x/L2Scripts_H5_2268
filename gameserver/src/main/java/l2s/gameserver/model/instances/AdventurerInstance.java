package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowQuestInfoPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.network.l2.s2c.ShowPCCafeCouponShowUI;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

public class AdventurerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final Logger _log = LoggerFactory.getLogger(AdventurerInstance.class);

	static int[][] _warriorPcCafeBuff = new int[][]{
		// minlevel maxlevel skill
		{1, 40, 4352, 1}, // Berserker Spirit level 1 - 300
		{1, 40, 4345, 1}, // Might level 1 - 200
		{1, 40, 4323, 1}, // Shield level 1 - 100
		{1, 40, 4322, 1}, // Wind Walk level 1
		{1, 40, 4359, 1}, // Focus level 1
		{1, 40, 4360, 1}, // Death Whisper level 1
		{1, 40, 4358, 1}, // Guidance level 1 - 200
		{1, 40, 4353, 1}, // Bless Shield level 1 - 100
		{1, 40, 4328, 1}, // Blessed Soul ? Bless the Soul
		{1, 40, 4357, 1}, // Haste level 1 - 400
		{1, 40, 4406, 1}, // Agility level 1 - 200
		{1, 40, 4354, 1}, // Vampiric Rage level 1 - 200
	};

	static int[][] _wizzardPcCafeBuff = new int[][]{
		// minlevel maxlevel skill
		{1, 40, 4352, 1}, // Berserker Spirit level 1 - 300
		{1, 40, 4323, 1}, // Shield level 1 - 100
		{1, 40, 4322, 1}, // Wind Walk level 1 - 200
		{1, 40, 4324, 1}, // Bless the Body level 1 - 200
		{1, 40, 4328, 1}, // Bless the Soul level 1 - 200
		{1, 40, 5637, 1}, // Magic Barrier level 1 - 300
		{1, 40, 4331, 1}, // Empower level 1 - 200
		{1, 40, 4329, 1}, // Acumen level 1 - 400
	};

	static int[][] _summonWarriorPcCafeBuff = new int[][]{
		// minlevel maxlevel skill
		{1, 40, 4352, 1}, // Berserker Spirit level 1 - 300
		{1, 40, 4345, 2}, // Might level 2 - 300
		{1, 40, 4323, 2}, // Shield level 2 - 150
		{1, 40, 4322, 2}, // Wind Walk level 2 - 300
		{1, 40, 4359, 2}, // Focus level 2 - 650
		{1, 40, 4360, 2}, // Death Whisper level 2 - 800
		{1, 40, 4358, 2}, // Guidance level 2 - 300
		{1, 40, 4353, 2}, // Bless Shield level 2 - 150
		{1, 40, 4324, 3}, // Bless the Body level 3 - 300
		{1, 40, 4357, 1}, // Haste level 1 - 400
		{1, 40, 4406, 2}, // Agility level 2 - 300
		{1, 40, 4354, 2}, // Vampiric Rage level 2 - 300
	};

	static int[][] _summonWizzardPcCafeBuff = new int[][]{
		// minlevel maxlevel skill
		{1, 40, 4352, 1}, // Berserker Spirit level 1 - 300
		{1, 40, 5637, 1}, // Magic Barrier level 1 - 300
		{1, 40, 4323, 2}, // Shield level 2 - 150
		{1, 40, 4322, 2}, // Wind Walk level 2 - 300
		{1, 40, 4328, 3}, // Bless the Soul level 3 - 300
		{1, 40, 4331, 2}, // Empower level 2 - 300
		{1, 40, 4329, 2}, // Acumen level 2 - 600
	};

	public AdventurerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		List<Creature> target = new ArrayList<Creature>();
		target.add(player);

		if(command.startsWith("npcfind_byid"))
		{
			try
			{
				int bossId = Integer.parseInt(command.substring(12).trim());
				switch(RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId))
				{
					case ALIVE:
					case DEAD:
						Spawner spawn = RaidBossSpawnManager.getInstance().getSpawnTable().get(bossId);

						Location loc = spawn.getCurrentSpawnRange().getRandomLoc(spawn.getReflection().getGeoIndex(), false);

						// Убираем и ставим флажок на карте и стрелку на компасе
						player.sendPacket(new RadarControlPacket(2, 2, loc), new RadarControlPacket(0, 1, loc));
						break;
					case UNDEFINED:
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2AdventurerInstance.BossNotInGame", player).addNumber(bossId));
						break;
				}
			}
			catch(NumberFormatException e)
			{
				_log.warn("AdventurerInstance: Invalid Bypass to Server command parameter.");
			}
		}	
		else if(command.startsWith("raidInfo"))
		{
			int bossLevel = Integer.parseInt(command.substring(9).trim());

			String filename = "adventurer_guildsman/raid_info/info.htm";
			if(bossLevel != 0)
				filename = "adventurer_guildsman/raid_info/level" + bossLevel + ".htm";

			showChatWindow(player, filename);
		}
		else if(command.equalsIgnoreCase("questlist"))
			player.sendPacket(ExShowQuestInfoPacket.STATIC);
		else if(command.equalsIgnoreCase("pccafe_coupon_use"))
		{
			if(player.getVarInt("PCC_CODE_ATTEMPTS") < Config.ALT_PCBANG_POINTS_MAX_CODE_ENTER_ATTEMPTS)
			{
				player.sendPacket(new ShowPCCafeCouponShowUI());
			}
			else
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/max_attempts.htm");
			}
		}
		else if(command.equalsIgnoreCase("receive_lottery_tiket"))
		{
			if(player.getVarInt("pc_lottery") != 1)
			{
				player.setVar("pc_lottery", 1, System.currentTimeMillis() / 1000L + 86400000);
				ItemFunctions.addItem(player, 15358, 1, "Receive lottery ticket");
			}
		else
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/already_get_tiket.htm");
				return;
			}
		}
		else if(command.equalsIgnoreCase("won"))
		{
			int tiketId = Integer.parseInt(command.substring(3).trim());

			if(ItemFunctions.getItemCount(player, tiketId) < 1)
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/no_tiket.htm");
				return;
			}
			else
			{
				giveLotteryRevard(player, tiketId);
			}
		}
		else if(command.equalsIgnoreCase("pet_warrior_buff_set"))
		{
			if(player.isCursedWeaponEquipped())
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/cursed_weapon.htm");
				return;
			}

			if(player.getLevel() > 40)
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/to_high_level.htm");
				return;
			}

			if(player.getPcBangPoints() < 4000)
			{
				player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
				return;
			}

			if(player.getServitor() != null && !player.getServitor().isDead())
			{
				target.clear();
				target.add(player.getServitor());

				for(int[] buff : _summonWarriorPcCafeBuff)
				{
					if(player.getLevel() >= buff[0] && player.getLevel() <= buff[1])
					{
						broadcastPacket(new MagicSkillUse(this, player.getServitor(), buff[2], buff[3], 0, 0));
						callSkill(SkillHolder.getInstance().getSkill(buff[2], buff[3]), target, true);
					}
				}
				player.reducePcBangPoints(4000, true);
			}
		}
		else if(command.equalsIgnoreCase("pet_wizard_buff_set"))
		{
			if(player.isCursedWeaponEquipped())
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/cursed_weapon.htm");
				return;
			}

			if(player.getLevel() > 40)
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/to_high_level.htm");
				return;
			}

			if(player.getPcBangPoints() < 2100)
			{
				player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
				return;
			}

			if(player.getServitor() != null && !player.getServitor().isDead())
			{
				target.clear();
				target.add(player.getServitor());

				for(int[] buff : _summonWizzardPcCafeBuff)
				{
					if(player.getLevel() >= buff[0] && player.getLevel() <= buff[1])
					{
						broadcastPacket(new MagicSkillUse(this, player.getServitor(), buff[2], buff[3], 0, 0));
						callSkill(SkillHolder.getInstance().getSkill(buff[2], buff[3]), target, true);
					}
				}
				player.reducePcBangPoints(2100, true);
			}
		}
		else if(command.equalsIgnoreCase("player_warrior_buff_set"))
		{
			if(player.isCursedWeaponEquipped())
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/cursed_weapon.htm");
				return;
			}

			if(player.getLevel() > 40)
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/to_high_level.htm");
				return;
			}

			if(player.getPcBangPoints() < 1600)
			{
				player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
				return;
			}

			for(int[] buff : _warriorPcCafeBuff)
			{
				if(player.getLevel() >= buff[0] && player.getLevel() <= buff[1])
				{
					broadcastPacket(new MagicSkillUse(this, player, buff[2], buff[3], 0, 0));
					callSkill(SkillHolder.getInstance().getSkill(buff[2], buff[3]), target, true);
				}
			}
			player.reducePcBangPoints(1600, true);
		}
		else if(command.equalsIgnoreCase("player_wizard_buff_set"))
		{
			if(player.isCursedWeaponEquipped())
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/cursed_weapon.htm");
				return;
			}

			if(player.getLevel() > 40)
			{
				showChatWindow(player, "adventurer_guildsman/pc_cafe/to_high_level.htm");
				return;
			}

			if(player.getPcBangPoints() < 3100)
			{
				player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
				return;
			}

			for(int[] buff : _wizzardPcCafeBuff)
			{
				if(player.getLevel() >= buff[0] && player.getLevel() <= buff[1])
				{
					broadcastPacket(new MagicSkillUse(this, player, buff[2], buff[3], 0, 0));
					callSkill(SkillHolder.getInstance().getSkill(buff[2], buff[3]), target, true);
				}
			}
			player.reducePcBangPoints(3100, true);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void giveLotteryRevard(Player player, int tiketId)
	{
		if(player == null)
		{
			return;
		}

		if(player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300)
		{
			return;
		}

		if(ItemFunctions.getItemCount(player, tiketId) <= 0)
		{
			showChatWindow(player, "adventurer_guildsman/pc_cafe/no_tiket.htm");
			return;
		}
		else
		{
			switch(tiketId)
			{
				case 15363:
					ItemFunctions.deleteItem(player, tiketId, 1);
					player.addPcBangPoints(100, false, true);
					break;
				case 15362:
					ItemFunctions.deleteItem(player, tiketId, 1);
					player.addPcBangPoints(1000, false, true);
					break;
				case 15361:
					ItemFunctions.deleteItem(player, tiketId, 1);
					player.addPcBangPoints(2000, false, true);
					break;
				case 15360:
					ItemFunctions.deleteItem(player, tiketId, 1);
					player.addPcBangPoints(10000, false, true);
					break;
				case 15359:
					ItemFunctions.deleteItem(player, tiketId, 1);
					player.addPcBangPoints(100000, false, true);
					break;
			}
		}
		showChatWindow(player, "adventurer_guildsman/pc_cafe/used_tiket.htm");
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "adventurer_guildsman/" + pom + ".htm";
	}
}