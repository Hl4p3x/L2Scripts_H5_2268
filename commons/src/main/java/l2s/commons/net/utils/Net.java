package l2s.commons.net.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Net implements Comparable<Net>
{
	private final byte[] _address;
	private final byte[] _mask;
	private final boolean _isIPv4;

	private Net(byte[] address, byte[] mask)
	{
		_address = address;
		_mask = mask;
		_isIPv4 = _address.length == 4;
	}

	public byte[] getAddress()
	{
		return _address;
	}

	public byte[] getMask()
	{
		return _mask;
	}

	public boolean applyMask(byte[] addr)
	{
		// V4 vs V4 or V6 vs V6 checks
		if(_isIPv4 == (addr.length == 4))
		{
			for(int i = 0; i < _address.length; i++)
			{
				if((addr[i] & _mask[i]) != _address[i])
					return false;
			}
		}
		else
		{
			// check for embedded v4 in v6 addr (not done !)
			if(_isIPv4)
			{
				// my V4 vs V6
				for(int i = 0; i < _address.length; i++)
				{
					if((addr[i + 12] & _mask[i]) != _address[i])
						return false;
				}
			}
			else
			{
				// my V6 vs V4
				for(int i = 0; i < _address.length; i++)
				{
					if((addr[i] & _mask[i + 12]) != _address[i + 12])
						return false;
				}
			}
		}
		return true;
	}

	public boolean matches(Object o)
	{
		return equals(o);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;

		if(o instanceof Net)
			return applyMask(((Net) o).getAddress());

		if(o instanceof InetAddress)
			return applyMask(((InetAddress) o).getAddress());

		if(o instanceof String)
		{
			try
			{
				return applyMask(InetAddress.getByName((String) o).getAddress());
			}
			catch(UnknownHostException e)
			{
				//
			}
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return _address.hashCode() + _mask.hashCode() * 13;
	}

	@Override
	public String toString()
	{
		int size = 0;
		for(byte element : _mask)
			size += Integer.bitCount((element & 0xFF));

		try
		{
			return InetAddress.getByAddress(_address).toString() + "/" + size;
		}
		catch (UnknownHostException e)
		{
			return "Invalid";
		}
	}

	@Override
	public int compareTo(Net o)
	{
		long m1 = parseLong(getMask());
		long m2 = parseLong(o.getMask());
		if(m1 == m2)
		{
			long a1 = parseLong(getAddress());
			long a2 = parseLong(o.getAddress());
			return Long.compare(a1, a2);
		}
		return Long.compare(m1, m2);
	}

	public static long parseLong(byte[] bytes)
	{
		long result = 0L;
		for(byte b : bytes)
		{
			result = result * 256L + b;
		}
		return result;
	}

	private static final byte[] getMask(int n, int maxLength) throws UnknownHostException
	{
		if((n > (maxLength << 3)) || (n < 0))
			throw new UnknownHostException("Invalid netmask: " + n);

		final byte[] result = new byte[maxLength];

		for(int i = 0; i < maxLength; i++)
			result[i] = (byte) 0xFF;

		for(int i = (maxLength << 3) - 1; i >= n; i--)
			result[i >> 3] = (byte) (result[i >> 3] << 1);

		return result;
	}

	public static Net valueOf(String input) throws UnknownHostException
	{
		byte[] address, mask;
		int idx = input.indexOf("/");
		if(idx > 0)
		{
			address = InetAddress.getByName(input.substring(0, idx)).getAddress();
			mask = getMask(Integer.parseInt(input.substring(idx + 1)), address.length);
		}
		else
		{
			address = InetAddress.getByName(input).getAddress();
			mask = getMask(address.length * 8, address.length); // host, no need to check mask
		}

		Net net = new Net(address, mask);

		if(!net.applyMask(address))
			throw new UnknownHostException(input);

		return net;
	}

	public static Net valueOf(byte[] address, byte[] mask) throws UnknownHostException
	{
		Net net = new Net(address, mask);

		if(!net.applyMask(address))
			throw new UnknownHostException(net.toString());

		return net;
	}

	public static Net valueOf(InetAddress addr, int mask) throws UnknownHostException
	{
		byte[] address = addr.getAddress();
		Net net = new Net(address, getMask(mask, address.length));

		if(!net.applyMask(address))
			throw new UnknownHostException(net.toString());

		return net;
	}
}