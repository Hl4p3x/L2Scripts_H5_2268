package npc.model.events;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.collections.CollectionUtils;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.UndergroundColiseumEvent;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 15:40/12.07.2011
 *
 * events/kerthang_manager004.htm  - не лидер пати, но в пати
 * events/kerthang_manager008.htm  - нету пати
 * events/kerthang_manager011.htm  - C1 непохдодит уровнем
 */
@SuppressWarnings("serial")
public class ColiseumManagerInstance extends ColiseumHelperInstance
{
	private String _startHtm;
	private int _coliseumId;

	public ColiseumManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_startHtm = getParameter("start_htm", StringUtils.EMPTY);
		_coliseumId = getParameter("coliseum_id", 0);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		UndergroundColiseumEvent coliseumEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, _coliseumId);
		if(coliseumEvent == null)
			return;
		if(command.equals("register"))
		{
			Party party = player.getParty();
			if(party == null)
				showChatWindow(player, "events/kerthang_manager008.htm");
			else if(party.getPartyLeader() != player)
				showChatWindow(player, "events/kerthang_manager004.htm");
			else
			{
				for(Player $player : party)
				{
					if($player.getLevel() < coliseumEvent.getMinLevel() || $player.getLevel() > coliseumEvent.getMaxLevel())
					{
						showChatWindow(player, "events/kerthang_manager011.htm", "%name%", $player.getName());
						return;
					}
				}
			}
		}
		else if(command.equals("viewTeams"))
		{

			List<Player> reg = coliseumEvent.getRegisteredPlayers();

			NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(player, this);
			msg.setFile("events/kerthang_manager003.htm");
			for(int i = 0; i < 5; i++)
			{
				Player $player = CollectionUtils.safeGet(reg, i);

				msg.replace("%team" + i + "%", $player == null ? StringUtils.EMPTY : $player.getName());
			}

			player.sendPacket(msg);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... ar)
	{
		showChatWindow(player, _startHtm);
	}
}
