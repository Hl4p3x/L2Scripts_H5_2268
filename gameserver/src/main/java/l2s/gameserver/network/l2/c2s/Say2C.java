package l2s.gameserver.network.l2.c2s;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.VarMap;

import l2s.gameserver.Config;
import l2s.gameserver.cache.ItemInfoCache;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.mods.ChatReplacer;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.utils.BotPunish;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.MapUtils;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.Util;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);

	/** RegExp для кэширования ссылок на предметы, пример ссылки: \b\tType=1 \tID=268484598 \tColor=0 \tUnderline=0 \tTitle=\u001BAdena\u001B\b */
	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

	private String _text;
	private ChatType _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = l2s.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.isntAfk();

		if(Config.ALT_ENABLE_BOTREPORT)
		{
			// Check for bot punishment
			if(activeChar.isBeingPunished())
			{
				// Check if punishment expired
				if(activeChar.getPlayerPunish().canTalk() && activeChar.getBotPunishType() == BotPunish.Punish.CHATBAN)
					activeChar.endPunishment();
				else if(activeChar.getBotPunishType() == BotPunish.Punish.CHATBAN)
				{
					activeChar.sendPacket(SystemMsg.REPORTED_10_MINS_WITHOUT_CHAT);
					return;
				}
			}
		}
		writeToChat(activeChar, _text, _type, _target);
	}

	public static void writeToChat(Player activeChar, String text, ChatType type, String target)
	{
		if(type == null || text == null || text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.getLevel() < Config.DISSALOW_GLOBAL_CHATS_UNTIL_LEVEL)
		{
			if(type == ChatType.SHOUT || type == ChatType.TRADE)
			{
				activeChar.sendMessage("You cannot use that chat until you have reached "+Config.DISSALOW_GLOBAL_CHATS_UNTIL_LEVEL+" level!");
				activeChar.sendActionFailed();
				return;
			}		
		}
		
		text = text.replaceAll("\\\\n", "\n");

		if(text.contains("\n"))
		{
			String[] lines = text.split("\n");
			text = StringUtils.EMPTY;
			for(int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(text.length() > 0)
					text += "\n  >";
				text += lines[i];
			}
		}

		if(text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Functions.isEventStarted("events.Viktorina.Viktorina"))
		{
			String answer = text.trim();
			if(answer.length() > 0)
			{
				Object[] objects = { answer, activeChar };
				Functions.callScripts("events.Viktorina.Viktorina", "checkAnswer", objects);
			}
		}

		if(text.startsWith("."))
		{
			if(Config.ALLOW_VOICED_COMMANDS)
			{
				String fullcmd = text.substring(1).trim();
				String command = fullcmd.split("\\s+")[0];
				String args = fullcmd.substring(command.length()).trim();

				if(command.length() > 0)
				{
					// then check for VoicedCommands
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					if(vch != null)
					{
						vch.useVoicedCommand(command, activeChar, args);
						return;
					}
				}
				activeChar.sendMessage(new CustomMessage("common.command404", activeChar));
				return;
			}
		}
		else if(text.startsWith("=="))
		{
			String expression = text.substring(2);
			Expression expr = null;

			if(!expression.isEmpty())
			{
				try
				{
					expr = ExpressionTree.parse(expression);
				}
				catch(ExpressionParseException epe)
				{

				}

				if(expr != null)
				{
					double result;

					try
					{
						VarMap vm = new VarMap();
						vm.setValue("adena", activeChar.getAdena());
						result = expr.eval(vm, null);
						activeChar.sendMessage(expression);
						activeChar.sendMessage("=" + Util.formatDouble(result, "NaN", false));
					}
					catch(Exception e)
					{

					}
				}
			}

			return;
		}

		boolean globalchat = type != ChatType.ALLIANCE && type != ChatType.CLAN && type != ChatType.PARTY;

		if((globalchat || ArrayUtils.contains(Config.BAN_CHANNEL_LIST, type.ordinal())) && activeChar.getNoChannel() != 0)
		{
			if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
			{
				if(activeChar.getNoChannel() > 0)
				{
					int timeRemained = Math.round(activeChar.getNoChannelRemained() / 60000);
					activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
				}
				else
					activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			activeChar.updateNoChannel(0);
		}

		if(globalchat)
			if(Config.ABUSEWORD_REPLACE)
			{
				if(Config.containsAbuseWord(text))
				{
					text = Config.ABUSEWORD_REPLACE_STRING;
					activeChar.sendActionFailed();
				}
			}
			else if(Config.ABUSEWORD_BANCHAT && Config.containsAbuseWord(text))
			{
				activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(Config.ABUSEWORD_BANTIME));
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AbuseChat", "message: "+text+"");	
				activeChar.updateNoChannel(Config.ABUSEWORD_BANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}

		// Кэширование линков предметов
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(text);
		ItemInstance item;
		int objectId;

		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

			if(item == null)
			{
				activeChar.sendActionFailed();
				break;
			}
			if(HidenItemsDAO.isHidden(item))
			{
				activeChar.sendActionFailed();
				return;
			}

			ItemInfoCache.getInstance().put(item);
		}

		String translit = activeChar.getVar("translit");
		if(translit != null)
		{
			//Исключаем из транслитерации ссылки на предметы
			m = SKIP_ITEM_LINK_PATTERN.matcher(text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(text.substring(end, end = m.end()));
			}

			text = sb.append(Strings.fromTranslit(text.substring(end, text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}

		Log.LogChat(type.name(), activeChar.getName(), target, text);

		SayPacket2 cs;
		if(activeChar.isInFightClub() && activeChar.getFightClubEvent().isHidePersonality())
			cs = new SayPacket2(0, type, "Player", text);
		else
			cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), text);

		switch(type)
		{
			case TELL:
				Player receiver = World.getPlayer(target);
				if(receiver == null && Config.ALLOW_FAKE_PLAYERS && FakePlayersTable.getActiveFakePlayers().contains(target.toLowerCase()))
				{
					cs = new SayPacket2(activeChar.getObjectId(), type, "->" + target, text);
					activeChar.sendPacket(cs);
					return;
				}				
				if(receiver != null && receiver.isInOfflineMode())
				{
					activeChar.sendMessage(new CustomMessage("common.OfflineMessage", activeChar));
					activeChar.sendActionFailed();
				}
				else if(receiver != null && !receiver.getBlockList().contains(activeChar) && !receiver.isBlockAll())
				{
					if(!receiver.getMessageRefusal())
					{
						if(!activeChar.getAntiFlood().canTell(receiver.getObjectId(), text))
							return;

						if(Config.TOW_INITED && !activeChar.isGM() && !receiver.isGM())
						{
							if(!activeChar.isSameRace(receiver))
							{
								String _newTxt = ChatReplacer.textReplace(text);
								cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);

								if(receiver.isFakePlayer())
									receiver.getListeners().onChatMessageReceive(type, activeChar.getName(), _newTxt);
								else
									receiver.sendPacket(cs);
								receiver.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
							}
							else
							{
								if(receiver.isFakePlayer())
									receiver.getListeners().onChatMessageReceive(type, activeChar.getName(), text);
								else
									receiver.sendPacket(cs);
								receiver.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
							}
						}
						else
						{
							if(receiver.isFakePlayer())
								receiver.getListeners().onChatMessageReceive(type, activeChar.getName(), text);
							else
								receiver.sendPacket(cs);
							receiver.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
						}
						cs = new SayPacket2(activeChar.getObjectId(), type, "->" + receiver.getName(), text);
						activeChar.sendPacket(cs);
						activeChar.getAntiFlood().addInterlocutorId(activeChar.getObjectId());
						Log.LogEvent(activeChar.getName(), activeChar.getIP(), "PrivateMessage", "to:", ""+receiver.getName()+" message: "+text+"");
					}
					else
						activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
				}
				else if(receiver == null)
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(target), ActionFailPacket.STATIC);
				else
					activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, ActionFailPacket.STATIC);
				break;
			case SHOUT:
				if(activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage("You cannot use this chat until you get "+Config.MIN_LEVEL_TO_USE_SHOUT+" level");
					return;
				}
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}

				if(!activeChar.getAntiFlood().canShout(text))
					return;

				if(Config.GLOBAL_SHOUT)
					announce(activeChar, cs, text, type);
				else
					shout(activeChar, cs, text, type);
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "ShoutMessage", "message: "+text+"");
				activeChar.sendPacket(cs);
				break;
			case TRADE:
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(Msg.SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if(activeChar.isInObserverMode())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
					return;
				}

				if(!activeChar.getAntiFlood().canTrade(text))
					return;

				if(Config.GLOBAL_TRADE_CHAT)
					announce(activeChar, cs, text, type);
				else
					shout(activeChar, cs, text, type);

				activeChar.sendPacket(cs);
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "TradeMessage", "message: "+text+"");
				break;
			case ALL:
				if(!activeChar.getAntiFlood().canAll(text))
					return;

				if(activeChar.isCursedWeaponEquipped())
					cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getTransformationName(), text);

				List<Player> list = null;

				if(activeChar.isInObserverMode() && activeChar.getObserverRegion() != null && activeChar.getOlympiadObserveGame() != null)
				{
					OlympiadGame game = activeChar.getOlympiadObserveGame();
					if(game != null)
						list = game.getAllPlayers();
				}
				else if(activeChar.isInOlympiadMode())
				{
					OlympiadGame game = activeChar.getOlympiadGame();
					if(game != null)
						list = game.getAllPlayers();
				}
				else if(activeChar.isInFightClub())
					list = activeChar.getFightClubEvent().getAllFightingPlayers();
				else
					list = World.getAroundPlayers(activeChar);

				if(list != null)
					for(Player player : list)
					{
						if(player == activeChar || player.getReflection() != activeChar.getReflection() || player.isBlockAll() || player.getBlockList().contains(activeChar))
							continue;
						if(Config.TOW_INITED && !activeChar.isGM() && !player.isGM())
						{
							if(!activeChar.isSameRace(player) && !activeChar.isCursedWeaponEquipped())
							{
								String _newTxt = ChatReplacer.textReplace(text);
								cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);
								player.sendPacket(cs);
							}
							else
								player.sendPacket(cs);
						}		
						else							
							player.sendPacket(cs);
					}
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "NormalMessage", "message: "+text+"");
				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if(activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "ClanMessage", "message: "+text+"");
				}	
				break;
			case ALLIANCE:
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
				{
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AllyMessage", "message: "+text+"");
				}	
				break;
			case PARTY:
				if(activeChar.isInParty())
				{
					activeChar.getParty().broadCast(cs);
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "PartyMessage", "message: "+text+"");
				}	
				break;
			case PARTY_ROOM:
				MatchingRoom r = activeChar.getMatchingRoom();
				if(r != null && r.getType() == MatchingRoom.PARTY_MATCHING)
					r.broadCast(cs);
				break;
			case COMMANDCHANNEL_ALL:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
				{
					activeChar.getParty().getCommandChannel().broadCast(cs);
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "CommandChannelMessage", "message: "+text+"");
				}	
				else
					activeChar.sendPacket(Msg.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND);
				break;
			case COMMANDCHANNEL_COMMANDER:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
					activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
				else
					activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
				break;
			case HERO_VOICE:
				if(activeChar.isHero() || activeChar.getPlayerAccess().CanAnnounce)
				{
					// Ограничение только для героев, гм-мы пускай говорят.
					if(!activeChar.getPlayerAccess().CanAnnounce)
					{
						if(!activeChar.getAntiFlood().canHero(text))
							return;
					}

					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					{
						if(player.getBlockList().contains(activeChar) || player.isBlockAll())
							continue;
							
						if(Config.TOW_INITED && !activeChar.isGM() && !player.isGM())
						{
							if(!activeChar.isSameRace(player))
							{
								String _newTxt = ChatReplacer.textReplace(text);
								cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);
								player.sendPacket(cs);
							}
							else
								player.sendPacket(cs);		
							
						}		
						else		
							player.sendPacket(cs);	
					}
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "HeroVoiceMessage", "message: "+text+"");	
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}

				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "PetitionMessage", "message: "+text+"");	
				break;
			case BATTLEFIELD:
				if (activeChar.isInFightClub())
				{
					list = activeChar.getFightClubEvent().getMyTeamFightingPlayers(activeChar);
					for(Player player : list)
						player.sendPacket(cs);
					return;
				}

				if(activeChar.getBattlefieldChatId() == 0)
					return;

				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if(player.getBlockList().contains(activeChar) && player.isBlockAll())
						continue;
					if(player.getBattlefieldChatId() != activeChar.getBattlefieldChatId())
						continue;
						
					if(Config.TOW_INITED && !activeChar.isGM() && !player.isGM())
					{
						if(!activeChar.isSameRace(player))
						{
							String _newTxt = ChatReplacer.textReplace(text);
							cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);
							player.sendPacket(cs);
						}
						else
							player.sendPacket(cs);		
							
					}		
					else		
						player.sendPacket(cs);							
				}
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "BattleFieldMessage", "message: "+text+"");		
				break;
			case MPCC_ROOM:
				MatchingRoom r2 = activeChar.getMatchingRoom();
				if(r2 != null && r2.getType() == MatchingRoom.CC_MATCHING)
					r2.broadCast(cs);
				break;
			default:
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + type.ordinal() + ".");
		}
	}

	private static void shout(Player activeChar, SayPacket2 cs, String text, ChatType type)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.getBlockList().contains(activeChar))
				continue;

			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset || activeChar.isInRangeZ(player, Config.CHAT_RANGE))
			{
				if(Config.TOW_INITED && !activeChar.isGM() && !player.isGM())
				{
					if(!activeChar.isSameRace(player))
					{
						String _newTxt = ChatReplacer.textReplace(text);
						cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);
						player.sendPacket(cs);
					}
					else
						player.sendPacket(cs);
				}		
				else			
					player.sendPacket(cs);
			}		
		}
	}

	private static void announce(Player activeChar, SayPacket2 cs, String text, ChatType type)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection() || player.isBlockAll() || player.getBlockList().contains(activeChar))
				continue;
			if(Config.TOW_INITED && !activeChar.isGM() && !player.isGM())
			{
				if(!activeChar.isSameRace(player))
				{
					String _newTxt = ChatReplacer.textReplace(text);
					cs = new SayPacket2(activeChar.getObjectId(), type, activeChar.getName(), _newTxt);
					player.sendPacket(cs);
				}
				else
					player.sendPacket(cs);
			}		
			else
				player.sendPacket(cs);
		}
	}
}