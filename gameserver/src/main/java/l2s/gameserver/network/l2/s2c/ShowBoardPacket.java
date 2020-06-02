package l2s.gameserver.network.l2.s2c;

import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.FakePlayersTable;

public class ShowBoardPacket extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ShowBoardPacket.class);

	public static L2GameServerPacket CLOSE = new ShowBoardPacket();

	private boolean _show;
	private String _htmlCode;
	private String _id;
	private List<String> _arg;
	private String _addFav = "";

	public static void separateAndSend(String html, Player player)
	{
		if(html == null || html.isEmpty())
			return;

		if(html.length() < 8180)
		{
			player.sendPacket(new ShowBoardPacket(html, "101", player));
			player.sendPacket(new ShowBoardPacket(null, "102", player));
			player.sendPacket(new ShowBoardPacket(null, "103", player));
		}
		else if(html.length() < 8180 * 2)
		{
			player.sendPacket(new ShowBoardPacket(html.substring(0, 8180), "101", player));
			player.sendPacket(new ShowBoardPacket(html.substring(8180, html.length()), "102", player));
			player.sendPacket(new ShowBoardPacket(null, "103", player));
		}
		else if(html.length() < 8180 * 3)
		{
			player.sendPacket(new ShowBoardPacket(html.substring(0, 8180), "101", player));
			player.sendPacket(new ShowBoardPacket(html.substring(8180, 8180 * 2), "102", player));
			player.sendPacket(new ShowBoardPacket(html.substring(8180 * 2, html.length()), "103", player));
		}
	}

	public ShowBoardPacket(String htmlCode, String id, Player player)
	{
		if(htmlCode != null && htmlCode.length() > 8192) // html code must not exceed 8192 bytes
		{
			_log.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
			_htmlCode = "<html><body>Html was too long</body></html>";
			return;
		}
		_show = true;
		_id = id;

		if(player.getSessionVar("add_fav") != null)
			_addFav = "bypass _bbsaddfav_List";

		if(htmlCode != null)
		{
			if(id.equalsIgnoreCase("101"))
				player.cleanBypasses(true);

			_htmlCode = player.encodeBypasses(htmlCode, true);
			_htmlCode = _htmlCode.replace("<?copyright?>", Config.BBS_COPYRIGHT);
			_htmlCode = _htmlCode.replace("<?total_online?>", String.valueOf(GameObjectsStorage.getAllPlayersCount() + FakePlayersTable.getActiveFakePlayersCount()));

			Matcher m = ImagesCache.HTML_PATTERN.matcher(_htmlCode);
			while(m.find())
			{
				String imageName = m.group(1);
				int imageId = ImagesCache.getInstance().getImageId(imageName);
				_htmlCode = _htmlCode.replaceAll("%image:" + imageName + "%", "Crest.crest_" + Config.REQUEST_ID + "_" + imageId);
				byte[] image = ImagesCache.getInstance().getImage(imageId);
				if(image != null)
					player.sendPacket(new PledgeCrestPacket(imageId, image));
			}
		}
		else
			_htmlCode = null;
	}

	public ShowBoardPacket(List<String> arg)
	{
		_show = true;
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
	}

	private ShowBoardPacket()
	{
		_show = false;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_show ? 0x01 : 0x00); //c4 1 to show community 00 to hide
		if(_show)
		{
			writeS("bypass _bbshome");
			writeS("bypass _bbsgetfav");
			writeS("bypass _bbsloc");
			writeS("bypass _bbsclan");
			writeS("bypass _bbsmemo");
			writeS("bypass _maillist_0_1_0_");
			writeS("bypass _friendlist_0_");
			writeS(_addFav);
			String str = _id + "\u0008";
			if(!_id.equals("1002"))
			{
				if(_htmlCode != null)
					str += _htmlCode;
			}
			else
			{
				for(String arg : _arg)
					str += arg + " \u0008";
			}
			writeS(str);
		}
	}
}