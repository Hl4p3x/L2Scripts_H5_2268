package l2s.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.SoDManager;
import l2s.gameserver.instancemanager.SoIManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.ExChangeClientEffectInfo;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.stats.Stats;

public class AdminAdmin implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_admin,
		admin_play_sounds,
		admin_play_sound,
		admin_silence,
		admin_tradeoff,
		admin_cfg,
		admin_config,
		admin_show_html,
		admin_setnpcstate,
		admin_setareanpcstate,
		admin_showmovie,
		admin_setzoneinfo,
		admin_eventtrigger,
		admin_debug,
		admin_uievent,
		admin_opensod,
		admin_closesod,
		admin_setsoistage,
		admin_soinotify,
		admin_forcenpcinfo,
		admin_undying
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		StringTokenizer st;

		if(activeChar.getPlayerAccess().Menu)
		{
			switch(command)
			{
				case admin_admin:
					activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/admin.htm"));
					break;
				case admin_play_sounds:
					if(wordList.length == 1)
						activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/songs/songs.htm"));
					else
						try
						{
							activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
						}
						catch(StringIndexOutOfBoundsException e)
						{
						}
					break;
				case admin_play_sound:
					try
					{
						playAdminSound(activeChar, wordList[1]);
					}
					catch(StringIndexOutOfBoundsException e)
					{
					}
					break;
				case admin_silence:
					if(activeChar.getMessageRefusal()) // already in message refusal
					// mode
					{
						activeChar.unsetVar("gm_silence");
						activeChar.setMessageRefusal(false);
						activeChar.sendPacket(Msg.MESSAGE_ACCEPTANCE_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					else
					{
						if(Config.SAVE_GM_EFFECTS)
							activeChar.setVar("gm_silence", "true", -1);
						activeChar.setMessageRefusal(true);
						activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					break;
				case admin_tradeoff:
					try
					{
						if(wordList[1].equalsIgnoreCase("on"))
						{
							activeChar.setTradeRefusal(true);
							Functions.sendDebugMessage(activeChar, "tradeoff enabled");
						}
						else if(wordList[1].equalsIgnoreCase("off"))
						{
							activeChar.setTradeRefusal(false);
							Functions.sendDebugMessage(activeChar, "tradeoff disabled");
						}
					}
					catch(Exception ex)
					{
						if(activeChar.getTradeRefusal())
							Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
						else
							Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
					}
					break;
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/" + html));
						else
							Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					catch(Exception npe)
					{
						Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					break;
				case admin_setnpcstate:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
						return false;
					}
					int state;
					GameObject target = activeChar.getTarget();
					try
					{
						state = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify state");
						return false;
					}
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "You must target an NPC");
						return false;
					}
					NpcInstance npc = (NpcInstance) target;
					npc.setNpcState(state);
					break;
				case admin_setareanpcstate:
					try
					{
						final String val = fullString.substring(15).trim();

						String[] vals = val.split(" ");
						int range = NumberUtils.toInt(vals[0], 0);
						int astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

						for(NpcInstance n : activeChar.getAroundNpc(range, 200))
							n.setNpcState(astate);
					}
					catch(Exception e)
					{
						Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
					}
					break;
				case admin_showmovie:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
						return false;
					}
					int id;
					try
					{
						id = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.showQuestMovie(id);
					break;
				case admin_setzoneinfo:
					if(wordList.length < 4)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo type key value");
						return false;
					}
					int type, key, value;
					try
					{
						type = Integer.parseInt(wordList[1]);
						key = Integer.parseInt(wordList[2]);
						value = Integer.parseInt(wordList[3]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo type key value");
						return false;
					}
					activeChar.broadcastPacket(new ExChangeClientEffectInfo(type, key, value));
					break;
				case admin_eventtrigger:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
						return false;
					}
					int triggerid;
					try
					{
						triggerid = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new EventTriggerPacket(triggerid, true));
					break;
				case admin_debug:
					GameObject ob = activeChar.getTarget();
					if(ob == null || !ob.isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Only player target is allowed");
						return false;
					}
					Player pl = ob.getPlayer();
					List<String> _s = new ArrayList<String>();
					_s.add("==========TARGET STATS:");
					_s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
					_s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1, null, null));
					_s.add("==Skill Power: " + pl.calcStat(Stats.SKILL_POWER, 1, null, null));
					_s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1, null, null));

					_s.add("==========Powers:");
					_s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1, null, null));
					_s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1, null, null));
					_s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1, null, null));
					_s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1, null, null));
					_s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1, null, null));
					_s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1, null, null));
					_s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1, null, null));
					_s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1, null, null));
					_s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1, null, null));

					_s.add("==========PvP Stats:");
					_s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null));
					_s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null));
					_s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null));
					_s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null));
					_s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null));
					_s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null));

					_s.add("==========Reflects:");
					_s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
					_s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));

					_s.add("==========MP Consume Rate:");
					_s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1, null, null));
					_s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1, null, null));
					_s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1, null, null));

					_s.add("==========Shield:");
					_s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
					_s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
					_s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));

					_s.add("==========Etc:");
					_s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
					_s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.PSKILL_EVASION, null, null));
					_s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
					_s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
					_s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1, null, null));
					_s.add("==========DONE.");

					for(String s : _s)
						Functions.sendDebugMessage(activeChar, s);
					break;
				case admin_uievent:
					if(wordList.length < 5)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
						return false;
					}
					boolean hide;
					boolean increase;
					int startTime;
					int endTime;
					String text;
					try
					{
						hide = Boolean.parseBoolean(wordList[1]);
						increase = Boolean.parseBoolean(wordList[2]);
						startTime = Integer.parseInt(wordList[3]);
						endTime = Integer.parseInt(wordList[4]);
						text = wordList[5];
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "Invalid format");
						return false;
					}
					activeChar.broadcastPacket(new ExSendUIEventPacket(activeChar, hide, increase, startTime, endTime, text));
					break;
				case admin_opensod:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //opensod minutes");
						return false;
					}
					SoDManager.openSeed(Integer.parseInt(wordList[1]) * 60 * 1000L);
					break;
				case admin_closesod:
					SoDManager.closeSeed();
					break;
				case admin_setsoistage:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setsoistage stage[1-5]");
						return false;
					}
					SoIManager.setCurrentStage(Integer.parseInt(wordList[1]));
					break;
				case admin_soinotify:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //soinotify [1-3]");
						return false;
					}
					switch(Integer.parseInt(wordList[1]))
					{
						case 1:
							SoIManager.notifyCohemenesKill();
							break;
						case 2:
							SoIManager.notifyEkimusKill();
							break;
						case 3:
							SoIManager.notifyHoEDefSuccess();
							break;
					}
					break;
				case admin_forcenpcinfo:
					GameObject obj2 = activeChar.getTarget();
					if(!obj2.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
						return false;
					}
					((NpcInstance) obj2).broadcastCharInfo();
					break;
				case admin_undying:
					if(activeChar.isGMUndying())
					{
						activeChar.setGMUndying(false);
						Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
					}
					else
					{
						activeChar.setGMUndying(true);
						Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
					}
					break;
			}
			return true;
		}

		if(activeChar.getPlayerAccess().CanTeleport)
		{
			switch(command)
			{
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							if(html.startsWith("tele"))
								activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/" + html));
							else
								activeChar.sendMessage(new CustomMessage("common.Admin.AccessDenied", activeChar));
						else
							activeChar.sendMessage(new CustomMessage("common.Admin.Html404", activeChar));
					}
					catch(Exception npe)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Html404", activeChar));
					}
					break;
			}
			return true;
		}

		return false;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void playAdminSound(Player activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySoundPacket(sound));
		activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/admin.htm"));
		activeChar.sendMessage(new CustomMessage("common.Admin.PlaySound", activeChar).addString(sound));
	}
}