package data.xml;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
**/
public abstract class ScriptsFileParser<H extends AbstractHolder> extends AbstractParser<H> implements ScriptFile
{
	protected ScriptsFileParser(H holder)
	{
		super(holder);
	}

	@Override
	public void onLoad()
	{
		load();
	}

	@Override
	public void onReload()
	{
		reload();
	}

	@Override
	public void onShutdown()
	{
		//
	}
}
