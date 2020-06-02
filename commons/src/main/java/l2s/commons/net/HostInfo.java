package l2s.commons.net;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.net.utils.Net;

/**
 * @author Bonux
**/
public class HostInfo
{
	private static final Logger _log = LoggerFactory.getLogger(HostInfo.class);

	private final int _id;
	private final String _address;
	private final int _port;
	private final String _key;
	private final Map<Net, String> _subnets = new TreeMap<Net, String>();

	public HostInfo(int id, String address, int port, String key)
	{
		_id = id;
		_address = address;
		_port = port;
		_key = key;
	}

	public HostInfo(String address, int port)
	{
		_id = 0;
		_address = address;
		_port = port;
		_key = null;
	}

	public int getId()
	{
		return _id;
	}

	public String getAddress()
	{
		return _address;
	}

	public int getPort()
	{
		return _port;
	}

	public String getKey()
	{
		return _key;
	}

	public void addSubnet(String address, String subnet)
	{
		try
		{
			_subnets.put(Net.valueOf(subnet), address);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public void addSubnet(String address, byte[] subnetAddress, byte[] subnetMask)
	{
		try
		{
			_subnets.put(Net.valueOf(subnetAddress, subnetMask), address);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public Map<Net, String> getSubnets()
	{
		return _subnets;
	}

	public String checkAddress(String address)
	{
		for(Entry<Net, String> m : getSubnets().entrySet())
		{
			if(m.getKey().matches(address))
				return m.getValue();
		}
		return getAddress();
	}
}