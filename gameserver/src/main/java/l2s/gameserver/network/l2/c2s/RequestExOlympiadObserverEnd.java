package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.CompType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.olympiad.OlympiadManager;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.HtmlUtils;
/**
 * @author: Kolobrodik
 * @date: 21:36/07.05.2012
 * @description: Пакет вызывает окошко переключения между аренами в режиме просмотра олимпиады.
 *
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x2F
 */
public class RequestExOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(!activeChar.isInObserverMode())
			return;

		NpcHtmlMessagePacket reply = new NpcHtmlMessagePacket(0);
		StringBuilder msg = new StringBuilder(StringUtils.EMPTY);
		msg.append("!Обзор боев Великой Олимпиады<br>");
		OlympiadManager manager = Olympiad._manager;
		if(manager != null)
		{
			for(int i = 0; i < Olympiad.STADIUMS.length; i++)
			{
				OlympiadGame game = manager.getOlympiadInstance(i);
				if(game != null && game.getState() > 0)
				{
					if(game.getType() == CompType.TEAM)
					{
						msg.append("<br1>Арена " + (i + 1) + ":&nbsp;<a action=\"bypass -h _olympiad?=move_op_field&=" + i + "\">Team vs Team:</a>");
						msg.append("<br1>- " + game.getTeamName1() + "<br1>- " + game.getTeamName2());
					}
					else
						msg.append("<br1>Арена " + (i + 1) + ":&nbsp;<a action=\"bypass -h _olympiad?=move_op_field&=" + i + "\">" + manager.getOlympiadInstance(i).getTeamName1() + " vs " + manager.getOlympiadInstance(i).getTeamName2() + "</a>");

					msg.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
				}
			}
		}

		reply.setHtml(HtmlUtils.bbParse(msg.toString()));
		activeChar.sendPacket(reply);
	}
}