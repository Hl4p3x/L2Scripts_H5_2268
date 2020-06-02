package l2s.gameserver.model.actor.instances.player;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterBlockListDAO;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author Bonux
**/
public class BlockList
{
	public static final int MAX_BLOCK_LIST_SIZE = 128;

	private TIntObjectMap<Block> _blockList = new TIntObjectHashMap<Block>(0);
	private final Player _owner;

	public BlockList(Player owner)
	{
		_owner = owner;
	}

	public void restore()
	{
		_blockList = CharacterBlockListDAO.getInstance().select(_owner);
	}

	public Block get(int objectId)
	{
		return _blockList.get(objectId);
	}

	public Block get(String name)
	{
		if(StringUtils.isEmpty(name))
			return null;

		for(Block b : values())
		{
			if(name.equalsIgnoreCase(b.getName()))
				return b;
		}
		return null;
	}

	public boolean contains(int objectId)
	{
		return _blockList.containsKey(objectId);
	}

	public boolean contains(Player player)
	{
		if(player == null)
			return false;
		return contains(player.getObjectId());
	}

	public boolean contains(String name)
	{
		return get(name) != null;
	}

	public int size()
	{
		return _blockList.size();
	}

	public Block[] values()
	{
		return _blockList.values(new Block[_blockList.size()]);
	}

	public Collection<Block> valueCollection()
	{
		return _blockList.valueCollection();
	}

	public boolean isEmpty()
	{
		return _blockList.isEmpty();
	}

	public void add(String name)
	{
		if(StringUtils.isEmpty(name) || name.equalsIgnoreCase(_owner.getName()) || contains(name))
		{
			_owner.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		int blockedObjId;
		Player blockedPlayer = World.getPlayer(name);
		if(blockedPlayer != null)
		{
			if(blockedPlayer.isGM())
			{
				_owner.sendPacket(SystemMsg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			blockedObjId = blockedPlayer.getObjectId();
		}
		else
		{
			blockedObjId = CharacterDAO.getInstance().getObjectIdByName(name);
			if(blockedObjId == 0)
			{
				_owner.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
				return;
			}

			if(Config.gmlist.containsKey(blockedObjId) && Config.gmlist.get(blockedObjId).IsGM)
			{
				_owner.sendPacket(SystemMsg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
		}

		_owner.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(name));

		_blockList.put(blockedObjId, new Block(blockedObjId, name));

		CharacterBlockListDAO.getInstance().insert(_owner, blockedObjId);
	}

	public void remove(String name)
	{
		if(StringUtils.isEmpty(name))
			return;

		int blockedObjId = 0;
		for(Block b : values())
		{
			if(name.equalsIgnoreCase(b.getName()))
			{
				blockedObjId = b.getObjectId();
				break;
			}
		}

		if(blockedObjId == 0)
		{
			_owner.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_);
			return;
		}

		_owner.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST).addString(name));

		_blockList.remove(blockedObjId);

		CharacterBlockListDAO.getInstance().delete(_owner, blockedObjId);
	}

	@Override
	public String toString()
	{
		return "BlockList[owner=" + _owner.getName() + "]";
	}
}
