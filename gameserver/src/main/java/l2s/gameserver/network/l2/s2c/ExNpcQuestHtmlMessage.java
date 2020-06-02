package l2s.gameserver.network.l2.s2c;

import java.util.regex.Matcher;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;

/**
 * @author VISTALL
 * @date 16:25/24.04.2011
 */
public class ExNpcQuestHtmlMessage extends NpcHtmlMessagePacket
{
	private int _questId;

	public ExNpcQuestHtmlMessage(int npcObjId, int questId)
	{
		super(npcObjId);
		_questId = questId;
	}

	@Override
	protected void writeImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_file != null) //TODO может быть не очень хорошо здесь это делать...
		{
			if(player.isGM())
				Functions.sendDebugMessage(player, "HTML: " + _file);
			String content = HtmCache.getInstance().getHtml(_file, player);
			String content2 = HtmCache.getInstance().getIfExists(_file, player);
			if(content2 == null)
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			else
				setHtml(content);
		}

		for(int i = 0; i < _replaces.size(); i += 2)
			_html = _html.replaceAll(_replaces.get(i), _replaces.get(i + 1));

		if(_html == null)
			return;

		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		_html = playername.matcher(_html).replaceAll(player.getName());

		player.cleanBypasses(false);
		_html = player.encodeBypasses(_html, false);

		writeD(_npcObjId);
		writeS(_html);
		writeD(_questId);
	}
}
