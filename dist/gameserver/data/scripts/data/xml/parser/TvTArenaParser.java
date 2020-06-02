package data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import data.xml.ScriptsFileParser;
import data.xml.holder.TvTArenaHolder;
import l2s.gameserver.Config;
import l2s.gameserver.utils.Location;
import templates.TvTArena;

public final class TvTArenaParser extends ScriptsFileParser<TvTArenaHolder>
{
	private static final TvTArenaParser _instance = new TvTArenaParser();

	public static TvTArenaParser getInstance()
	{
		return _instance;
	}

	public TvTArenaParser()
	{
		super(TvTArenaHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/tvt_arena.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "tvt_arena.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			TvTArena arena = new TvTArena(Integer.parseInt(element.attributeValue("id")));
			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();

				if("teleport_locations".equalsIgnoreCase(subElement.getName()))
				{
					int teamId = Integer.parseInt(subElement.attributeValue("team"));
					for(Element e : subElement.elements())
					{
						int x = Integer.parseInt(e.attributeValue("x"));
						int y = Integer.parseInt(e.attributeValue("y"));
						int z = Integer.parseInt(e.attributeValue("z"));

						arena.addTeleportLocation(teamId, new Location(x, y, z));
					}
				}
				else if("zones".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						arena.addZone(e.attributeValue("name"));
				}
				else if("doors".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						arena.addDoor(Integer.parseInt(e.attributeValue("id")));
				}
			}
			getHolder().addArena(arena);
		}
	}
}