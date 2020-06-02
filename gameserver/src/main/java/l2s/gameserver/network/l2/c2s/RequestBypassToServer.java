package l2s.gameserver.network.l2.c2s;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.CommunityBoardManager;
import l2s.gameserver.handler.bbs.ICommunityBoardHandler;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2s.gameserver.instancemanager.OfflineBufferManager;
import l2s.gameserver.instancemanager.OlympiadHistoryManager;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.OlympiadManagerInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.utils.MulticlassUtils;

public class RequestBypassToServer extends L2GameClientPacket
{
	//Format: cS
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);
	private String bypass;

	@Override
	protected void readImpl()
	{
		bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		final DecodedBypass bp = activeChar.decodeBypass(bypass);
		if(bp == null)
			return;
		try
		{
			NpcInstance npc = activeChar.getLastNpc();
			GameObject target = activeChar.getTarget();
			if(npc == null && target != null && target.isNpc())
				npc = (NpcInstance) target;

			if(bp.bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			else if(bp.bypass.equals("come_here") && activeChar.isGM())
				comeHere(getClient());
			else if(bp.bypass.startsWith("player_help "))
			{
				playerHelp(activeChar, bp.bypass.substring(12));
			}
			else if(bp.bypass.startsWith("scripts_"))
			{
				String command = bp.bypass.substring(8).trim();
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					_log.warn("Bad Script bypass!");
					return;
				}

				Map<String, Object> variables = null;
				if(npc != null)
				{
					variables = new HashMap<String, Object>(1);
					variables.put("npc", npc.getRef());
				}

				if(word.length == 1)
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
				else
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[] { args }, variables);
			}
			else if(bp.bypass.startsWith("htmbypass_"))
			{
				String command = bp.bypass.substring(10).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();

				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
				{
					try
					{
						b.getValue().invoke(b.getKey(), activeChar, npc, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
					}
					catch(Exception e)
					{
						_log.error("Exception: " + e, e);
					}
				}
				else
					_log.warn("Cannot find html bypass: " + command);
			}
			else if(bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
					vch.useVoicedCommand(word, activeChar, args);
				else
					_log.warn("Unknow voiced command '" + word + "'");
			}
			else if(bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
					id = bp.bypass.substring(4, endOfId);
				else
					id = bp.bypass.substring(4);
				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.checkInteractionDistance(object))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
			}
			else if(bp.bypass.startsWith("_olympiad?"))
			{
				String[] ar = bp.bypass.replace("_olympiad?", "").split("&");
				String firstVal = ar[0].split("=")[1];
				String secondVal = ar[1].split("=")[1];

				if(firstVal.equalsIgnoreCase("move_op_field"))
				{
					if(!Config.ENABLE_OLYMPIAD_SPECTATING)
						return;

					// Переход в просмотр олимпа разрешен только от менеджера или с арены.
					if ((activeChar.getLastNpc() instanceof OlympiadManagerInstance && activeChar.getLastNpc().checkInteractionDistance(activeChar)) || activeChar.getOlympiadObserveGame() != null)
						Olympiad.addSpectator(Integer.parseInt(secondVal) - 1, activeChar);
				}
			}
			else if(bp.bypass.startsWith("_diary"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
			}
			else if(bp.bypass.startsWith("_match"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);

				OlympiadHistoryManager.getInstance().showHistory(activeChar,  heroclass, heropage);
			}
			else if(bp.bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				GameObject object = activeChar.getTarget();
				if(object != null && object.isNpc())
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
			}
			else if(bp.bypass.startsWith("multisell "))
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0);
			else if(bp.bypass.startsWith("menu_select?"))
			{
				if(npc != null && activeChar.checkInteractionDistance(npc))
				{
					String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
					StringTokenizer st = new StringTokenizer(params, "&");
					int ask = Integer.parseInt(st.nextToken().split("=")[1]);
					int reply = Integer.parseInt(st.nextToken().split("=")[1]);
					npc.onMenuSelect(activeChar, ask, reply);

					for(ListenerHook hook : npc.getTemplate().getListenerHooks(ListenerHookType.NPC_ASK))
						hook.onNpcAsk(npc, ask, reply, activeChar);
				}
			}
			else if(bp.bypass.startsWith("Quest "))
			{
				_log.warn("Trying to call Quest bypass: " + bp.bypass + ", player: " + activeChar);
			}
			else if(bp.bypass.startsWith("BuffStore"))
			{
				try
				{
					OfflineBufferManager.getInstance().processBypass(activeChar, bp.bypass);
				}
				catch (Exception ex) {}
			}
			else if(bp.bypass.startsWith("multiclass?"))
			{
				MulticlassUtils.onBypass(activeChar, bp.bypass.substring(11).trim());
			}
			else if(bp.bbs)
			{
				if(!Config.BBS_ENABLED)
					activeChar.sendPacket(new SystemMessage(SystemMessage.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
				else
				{
					if(activeChar.isGM())
						activeChar.sendMessage("Request community bypass: \"" + bp.bypass + "\"");
					ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(bp.bypass);
					if(handler != null)
						 handler.onBypassCommand(activeChar,bp.bypass);
					else
					{
						handler = CommunityBoardManager.getInstance().getCommunityHandler("_bbsnotfound");
						if(handler != null)
							 handler.onBypassCommand(activeChar, "_bbsnotfound");
					}
				}
			}
		}
		catch(Exception e)
		{
			//_log.error("", e);
			String st = "Bad RequestBypassToServer: " + bp.bypass;
			GameObject target = activeChar.getTarget();
			if(target != null && target.isNpc())
				st = st + " via NPC #" + ((NpcInstance) target).getNpcId();
			_log.error(st, e);
		}
	}

	private static void comeHere(GameClient client)
	{
		GameObject obj = client.getActiveChar().getTarget();
		if(obj != null && obj.isNpc())
		{
			NpcInstance temp = (NpcInstance) obj;
			Player activeChar = client.getActiveChar();
			temp.setTarget(activeChar);
			temp.moveToLocation(activeChar.getLoc(), 0, true);
		}
	}

	private static void playerHelp(Player activeChar, String path)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(5);
		html.setFile(path);
		activeChar.sendPacket(html);
	}
}