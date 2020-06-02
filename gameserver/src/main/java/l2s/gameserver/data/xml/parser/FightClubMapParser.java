package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import javolution.util.FastMap;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FightClubMapHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubMap;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;

public final class FightClubMapParser extends AbstractParser<FightClubMapHolder>
{
	private static final FightClubMapParser _instance = new FightClubMapParser();

	public static FightClubMapParser getInstance()
	{
		return _instance;
	}

	protected FightClubMapParser()
	{
		super(FightClubMapHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/fight_club_maps/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "maps.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("map"); iterator.hasNext(); )
		{
			Element eventElement = iterator.next();
			String name = eventElement.attributeValue("name");

			MultiValueSet<String> set = new MultiValueSet<String>();
			set.set("name", name);

			for(Iterator<Element> parameterIterator = eventElement.elementIterator("parameter"); parameterIterator.hasNext(); )
			{
				Element parameterElement = parameterIterator.next();
				set.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
			}

			Map<Integer, Location[]> teamSpawns = null;
			Map<Integer, Map<String, ZoneTemplate>> territories = null;
			Map<Integer, Map<Integer, Location[]>> npcWaypath = null;
			Location[] keyLocations = null;

			for(Iterator<Element> objectIterator = eventElement.elementIterator("objects"); objectIterator.hasNext(); )
			{
				Element objectElement = objectIterator.next();
				String objectsName = objectElement.attributeValue("name");

				int team = Integer.parseInt(objectElement.attributeValue("team", "-1"));
				int index = Integer.parseInt(objectElement.attributeValue("index", "-1"));

				if(objectsName.equals("teamSpawns"))
				{
					if(teamSpawns == null)
						teamSpawns = new FastMap<Integer, Location[]>();
					teamSpawns.put(team, parseLocations(objectElement));
				}
				else if(objectsName.equals("territory"))
				{
					if(territories == null)
						territories = new FastMap<Integer, Map<String, ZoneTemplate>>();
					territories.put(team, parseTerritory(objectElement));
				}
				else if(objectsName.equals("npcWaypath"))
				{
					if(npcWaypath == null)
						npcWaypath = new FastMap<Integer, Map<Integer, Location[]>>();

					if(npcWaypath.get(team) == null)
						npcWaypath.put(team, new FastMap<Integer, Location[]>());

					npcWaypath.get(team).put(index, parseLocations(objectElement));
				}
				else if(objectsName.equals("keyLocations"))
					keyLocations = parseLocations(objectElement);
			}

			getHolder().addMap(new FightClubMap(set, teamSpawns, territories, npcWaypath, keyLocations));
		}
	}

	private Location[] parseLocations(Element element)
	{
		List<Location> locs = new ArrayList<Location>();
		for(Iterator<Element> objectsIterator = element.elementIterator(); objectsIterator.hasNext(); )
		{
			Element objectsElement = objectsIterator.next();
			String nodeName = objectsElement.getName();

			if(nodeName.equalsIgnoreCase("point"))
				locs.add(Location.parse(objectsElement));
		}

		Location[] locArray = new Location[locs.size()];
		for(int i = 0; i < locs.size(); i++)
			locArray[i] = locs.get(i);

		return locArray;
	}

	private Map<String, ZoneTemplate> parseTerritory(Element element)
	{
		Map<String, ZoneTemplate> territories = new HashMap<String, ZoneTemplate>();
		for(Iterator<Element> objectsIterator = element.elementIterator(); objectsIterator.hasNext(); )
		{
			Element objectsElement = objectsIterator.next();
			String nodeName = objectsElement.getName();

			if(nodeName.equalsIgnoreCase("zone"))
			{
				ZoneTemplate template = ZoneHolder.getInstance().getTemplate(objectsElement.attributeValue("name"));
				territories.put(template.getName(), template);
			}
		}

		return territories;
	}
}