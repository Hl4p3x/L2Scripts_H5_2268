package l2s.gameserver.config.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.net.HostInfo;
import l2s.gameserver.GameServer;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;

/**
 * @author Bonux
**/
public final class HostsConfigParser extends AbstractParser<HostsConfigHolder>
{
	private static final Logger _log = LoggerFactory.getLogger(HostsConfigParser.class);

	private static final HostsConfigParser _instance = new HostsConfigParser();

	public static HostsConfigParser getInstance()
	{
		return _instance;
	}

	protected HostsConfigParser()
	{
		super(HostsConfigHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File("config/hostsconfig.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "hostsconfig.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			if("authserver".equalsIgnoreCase(element.getName()))
			{
				final String address = element.attributeValue("address");
				final int port = Integer.parseInt(element.attributeValue("port"));
				getHolder().setAuthServerHost(new HostInfo(address, port));
			}
			else if("gameserver".equalsIgnoreCase(element.getName()))
			{
				for(Iterator<Element> subIterator = element.elementIterator("host"); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();

					final int id = Integer.parseInt(subElement.attributeValue("id"));
					final String address = GameServer.DEVELOP ? "127.0.0.1" : subElement.attributeValue("address");
					final int port = Integer.parseInt(subElement.attributeValue("port"));
					final String key = subElement.attributeValue("key");

					HostInfo hostInfo = new HostInfo(id, address, port, key);
					if(!GameServer.DEVELOP)
					{
						for(Iterator<Element> advancedIterator = subElement.elementIterator("advanced"); advancedIterator.hasNext();)
						{
							Element advancedElement = advancedIterator.next();

							final String advanced_address = advancedElement.attributeValue("address");
							final String advanced_subnet = advancedElement.attributeValue("subnet");
							hostInfo.addSubnet(advanced_address, advanced_subnet);
						}
					}
					getHolder().addGameServerHost(hostInfo);
				}
			}
		}
	}

	@Override
	protected void afterParseActions()
	{
		if(getHolder().getAuthServerHost() == null)
		{
			_log.error("Could not load authserver host config. Configure your hostsconfig.xml!");
			Runtime.getRuntime().exit(0);
		}

		if(getHolder().getGameServerHosts().length == 0)
		{
			_log.error("Could not load gameserver host config. Configure your hostsconfig.xml!");
			Runtime.getRuntime().exit(0);
		}
	}
}