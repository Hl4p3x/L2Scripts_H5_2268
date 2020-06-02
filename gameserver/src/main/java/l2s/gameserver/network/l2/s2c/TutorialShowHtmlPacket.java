package l2s.gameserver.network.l2.s2c;

import java.util.regex.Pattern;

import l2s.gameserver.model.Player;

public class TutorialShowHtmlPacket extends L2GameServerPacket
{
	public static int NORMAL_WINDOW = 0x01;
	public static int LARGE_WINDOW = 0x02;

	protected static final Pattern playername = Pattern.compile("%playername%");
	protected static final Pattern playerClassName = Pattern.compile("%className%");

	/**
	 * <html><head><body><center>
	 * <font color="LEVEL">Quest</font>
	 * </center>
	 * <br>
	 * Speak to the <font color="LEVEL"> Paagrio Priests </font>
	 * of the Temple of Paagrio. They will explain the basics of combat through quests.
	 * <br>
	 * You must visit them, for they will give you a useful gift after you complete a quest.
	 * <br>
	 * They are marked in yellow on the radar, at the upper-right corner of the screen.
	 * You must visit them if you wish to advance.
	 * <br>
	 * <a action="link tutorial_close_0">Close Window</a>
	 * </body></html>
	 *
	 * ВНИМАНИЕ!!! Клиент отсылает назад action!!! Используется как БАЙПАСС В RequestTutorialLinkHtml!!!
	 */
	private String _html;
	private int _windowType;

	public TutorialShowHtmlPacket(int windowType, String html)
	{
		_windowType = windowType;
		_html = html;
	}

	@Override
	protected final void writeImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;

		// Player name replace
		_html = playername.matcher(_html).replaceAll(player.getName());

		// Player class name replace
		_html = playerClassName.matcher(_html).replaceAll(player.getClassId().getName(player));

		writeD(_windowType);
		writeS(_html);
	}

	@Override
	protected boolean canWriteHF()
	{
		return _windowType == NORMAL_WINDOW;
	}

	@Override
	protected final void writeImplHF()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;

		// Player name replace
		_html = playername.matcher(_html).replaceAll(player.getName());

		// Player class name replace
		_html = playerClassName.matcher(_html).replaceAll(player.getClassId().getName(player));

		writeS(_html);
	}
}