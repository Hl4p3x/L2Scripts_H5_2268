package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 14:27/08.03.2011
 */
public abstract class SysMsgContainer<T extends SysMsgContainer<T>> extends L2GameServerPacket
{
	public static enum Types
	{
		TEXT, //0
		NUMBER, //1
		NPC_NAME, //2
		ITEM_NAME, //3
		SKILL_NAME, //4
		RESIDENCE_NAME, //5
		LONG, //6
		ZONE_NAME, //7
		ITEM_NAME_WITH_AUGMENTATION, //8
		ELEMENT_NAME, //9
		INSTANCE_NAME, //10  d
		STATIC_OBJECT_NAME, //11
		PLAYER_NAME, //12 S
		SYSTEM_STRING, //13 d
		NPCSTRING, // 14
		CLASS_NAME, // 15
		HP_CHANGE, // 16
		NUMBER_UNK,	// 17
		UNK_18,	// 18
		BYTE_UNK,	// 19
		BYTE,	// 20
		SHORT,	// 21
		UNK_22,	// 22
		UNK_23,	// 23
		FACTION_NAME;	// 24
	}

	protected SystemMsg _message;
	protected List<IArgument> _arguments;
	protected List<IArgument> _argumentsHF;

	//@Deprecated
	protected SysMsgContainer(int messageId)
	{
		this(SystemMsg.valueOf(messageId));
	}

	protected SysMsgContainer(SystemMsg message)
	{
		if(message == null)
			throw new IllegalArgumentException("SystemMsg is null");

		_message = message;
		_arguments = new ArrayList<IArgument>(_message.size());
		_argumentsHF = new ArrayList<IArgument>(_message.size());
	}

	protected void writeElements()
	{
		if(_message.size() > _arguments.size())
			throw new IllegalArgumentException("Wrong count of arguments: " + _message);

		if(this instanceof ConfirmDlgPacket)
		{
			writeD(_message.getId());
			writeD(_arguments.size());
		}
		else
		{
			writeH(_message.getId());
			writeC(_arguments.size());
		}
		for(IArgument argument : _arguments)
			argument.write(this);
	}

	protected void writeElementsHF()
	{
		if(_message.size() > _argumentsHF.size())
			throw new IllegalArgumentException("Wrong count of arguments: " + _message);

		writeD(_message.getId());
		writeD(_argumentsHF.size());
		for(IArgument argument : _argumentsHF)
			argument.write(this);
	}

	//==================================================================================================
	public T addName(GameObject object)
	{
		if(object == null)
			return add(new StringArgument(null));

		if(object.isNpc())
		{
			NpcInstance npc = (NpcInstance) object;
			if(Config.SERVER_SIDE_NPC_NAME || npc.getTemplate().displayId != 0 || !npc.getName().equals(npc.getTemplate().name))
				return add(new StringArgument(npc.getName()));
			return add(new NpcNameArgument(npc.getNpcId() + 1000000));
		}
		else if(object.isServitor())
		{
			Servitor servitor = (Servitor) object;
			if(!servitor.getName().equals(servitor.getTemplate().name))
				return add(new StringArgument(servitor.getName()));
			return add(new NpcNameArgument(servitor.getNpcId() + 1000000));
		}
		else if(object.isItem())
			return add(new ItemNameArgument(((ItemInstance) object).getItemId()));
		else if(object.isPlayer())
			return add(new PlayerNameArgument((Player) object));
		else if(object.isDoor())
			return add(new StaticObjectNameArgument(((DoorInstance) object).getDoorId()));
		else if(object instanceof StaticObjectInstance)
			return add(new StaticObjectNameArgument(((StaticObjectInstance) object).getUId()));

		return add(new StringArgument(object.getName()));
	}

	public T addInstanceName(int id)
	{
		return add(new InstanceNameArgument(id));
	}

	public T addSysString(int id)
	{
		return add(new SysStringArgument(id));
	}

	public T addSkillName(Skill skill)
	{
		return addSkillName(skill.getDisplayId(), skill.getDisplayLevel());
	}

	public T addSkillName(int id, int level)
	{
		return add(new SkillArgument(id, level));
	}

	public T addItemName(int item_id)
	{
		return add(new ItemNameArgument(item_id));
	}

	public T addItemNameWithAugmentation(ItemInstance item)
	{
		return add(new ItemNameWithAugmentationArgument(item.getItemId(), item.getVariation1Id(), item.getVariation2Id()));
	}

	public T addZoneName(Creature c)
	{
		return addZoneName(c.getX(), c.getY(), c.getZ());
	}

	public T addZoneName(Location loc)
	{
		return add(new ZoneArgument(loc.x, loc.y, loc.z));
	}

	public T addZoneName(int x, int y, int z)
	{
		return add(new ZoneArgument(x, y, z));
	}

	public T addResidenceName(Residence r)
	{
		return add(new ResidenceArgument(r.getId()));
	}

	public T addResidenceName(int i)
	{
		return add(new ResidenceArgument(i));
	}

	public T addElementName(int i)
	{
		return add(new ElementNameArgument(i));
	}

	public T addElementName(Element i)
	{
		return add(new ElementNameArgument(i.getId()));
	}

	public T addFactionName(int i)
	{
		return add(new FactionNameArgument(i));
	}

	public T addClassName(int i)
	{
		return add(new ClassNameArgument(i));
	}

	public T addClassName(ClassId i)
	{
		return add(new ClassNameArgument(i.getId()));
	}

	public T addInteger(double i)
	{
		return add(new IntegerArgument((int) i));
	}

	public T addLong(long i)
	{
		return add(new LongArgument(i));
	}

	public T addByte(byte i)
	{
		return add(new ByteArgument(i));
	}

	public T addString(String t)
	{
		return add(new StringArgument(t));
	}

	public T addHpChange(int targetId, int attackerId, int damage)
	{
		return add(new HpChangeArgument(targetId, attackerId, damage));
	}

	public T addNpcString(NpcString npcString, String... arg)
	{
		return add(new NpcStringArgument(npcString, arg));
	}

	@SuppressWarnings("unchecked")
	protected T add(IArgument arg)
	{
		_arguments.add(arg);

		if(arg.canWriteHF())
			_argumentsHF.add(arg);

		return (T) this;
	}

	//==================================================================================================
	// Суппорт классы, собственна реализация (не L2jFree)
	//==================================================================================================

	public static abstract class IArgument
	{
		void write(SysMsgContainer<?> m)
		{
			if(m.isHF() || m instanceof ConfirmDlgPacket)
				m.writeD(getType().ordinal());
			else
				m.writeC(getType().ordinal());

			writeData(m);
		}

		boolean canWriteHF()
		{
			return getType().ordinal() <= Types.BYTE.ordinal();
		}

		abstract Types getType();

		abstract void writeData(SysMsgContainer<?> message);
	}

	public static class IntegerArgument extends IArgument
	{
		private final int _data;

		public IntegerArgument(int da)
		{
			_data = da;
		}

		@Override
		public void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_data);
		}

		@Override
		Types getType()
		{
			return Types.NUMBER;
		}
	}

	public static class NpcNameArgument extends IntegerArgument
	{
		public NpcNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.NPC_NAME;
		}
	}

	public static class ItemNameArgument extends IntegerArgument
	{
		public ItemNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME;
		}
	}

	public static class ItemNameWithAugmentationArgument extends IArgument
	{
		private final int _itemId;
		private final int _variation1Id;
		private final int _variation2Id;

		public ItemNameWithAugmentationArgument(int itemId, int variation1Id, int variation2Id)
		{
			_itemId = itemId;
			_variation1Id = variation1Id;
			_variation2Id = variation2Id;
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME_WITH_AUGMENTATION;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_itemId);
			if(message.isHF())
			{
				message.writeH(_variation1Id);
				message.writeH(_variation2Id);
			}
			else
				message.writeC(_variation1Id > 0 || _variation2Id > 0);
		}
	}

	public static class InstanceNameArgument extends IntegerArgument
	{
		public InstanceNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.INSTANCE_NAME;
		}
	}

	public static class SysStringArgument extends IArgument
	{
		private final int _stringId;

		public SysStringArgument(int stringId)
		{
			_stringId = stringId;
		}

		@Override
		public void writeData(SysMsgContainer<?> message)
		{
			if(message.isHF())
				message.writeD(_stringId);
			else
				message.writeH(_stringId);
		}

		@Override
		Types getType()
		{
			return Types.SYSTEM_STRING;
		}
	}

	public static class ResidenceArgument extends IntegerArgument
	{
		public ResidenceArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.RESIDENCE_NAME;
		}
	}

	public static class StaticObjectNameArgument extends IntegerArgument
	{
		public StaticObjectNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.STATIC_OBJECT_NAME;
		}
	}

	public static class LongArgument extends IArgument
	{
		private final long _data;

		public LongArgument(long da)
		{
			_data = da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeQ(_data);
		}

		@Override
		Types getType()
		{
			return Types.LONG;
		}
	}

	public static class ByteArgument extends IArgument
	{
		private final byte _data;

		public ByteArgument(byte da)
		{
			_data = da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeC(_data);
		}

		@Override
		Types getType()
		{
			return Types.BYTE;
		}
	}

	public static class ShortArgument extends IArgument
	{
		private final short _data;

		public ShortArgument(short da)
		{
			_data = da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeH(_data);
		}

		@Override
		Types getType()
		{
			return Types.SHORT;
		}
	}

	public static class StringArgument extends IArgument
	{
		private final String _data;

		public StringArgument(String da)
		{
			_data = da == null ? "null" : da;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeS(_data);
		}

		@Override
		Types getType()
		{
			return Types.TEXT;
		}
	}

	public static class SkillArgument extends IArgument
	{
		private final int _skillId;
		private final int _skillLevel;

		public SkillArgument(int t1, int t2)
		{
			_skillId = t1;
			_skillLevel = t2;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_skillId);
			message.writeD(_skillLevel);
		}

		@Override
		Types getType()
		{
			return Types.SKILL_NAME;
		}
	}

	public static class ZoneArgument extends IArgument
	{
		private final int _x;
		private final int _y;
		private final int _z;

		public ZoneArgument(int t1, int t2, int t3)
		{
			_x = t1;
			_y = t2;
			_z = t3;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_x);
			message.writeD(_y);
			message.writeD(_z);
		}

		@Override
		Types getType()
		{
			return Types.ZONE_NAME;
		}
	}

	public static class ElementNameArgument extends IArgument
	{
		private final int _elementId;

		public ElementNameArgument(int elementId)
		{
			_elementId = elementId;
		}

		@Override
		public void writeData(SysMsgContainer<?> message)
		{
			if(message.isHF())
				message.writeD(_elementId);
			else
				message.writeC(_elementId);
		}

		@Override
		Types getType()
		{
			return Types.ELEMENT_NAME;
		}
	}

	public static class FactionNameArgument extends ByteArgument
	{
		public FactionNameArgument(int type)
		{
			super((byte) type);
		}

		@Override
		public Types getType()
		{
			return Types.FACTION_NAME;
		}
	}

	public static class PlayerNameArgument extends IArgument
	{
		private final Player _player;

		public PlayerNameArgument(Player player)
		{
			_player = player;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeS(_player.getVisibleName(message.getClient().getActiveChar()));
		}

		@Override
		Types getType()
		{
			return Types.PLAYER_NAME;
		}
	}

	public static class ClassNameArgument extends IntegerArgument
	{
		public ClassNameArgument(int classId)
		{
			super(classId);
		}

		@Override
		Types getType()
		{
			return Types.CLASS_NAME;
		}
	}

	public static class HpChangeArgument extends IArgument
	{
		private int _targetId;
		private int _attackerId;
		private int _hp;

		public HpChangeArgument(int targetId, int attackerId, int hp)
		{
			_targetId = targetId;
			_attackerId = attackerId;
			_hp = hp;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_targetId);
			message.writeD(_attackerId);
			message.writeD(_hp);
		}

		@Override
		Types getType()
		{
			return Types.HP_CHANGE;
		}
	}

	public static class NpcStringArgument extends IArgument
	{
		private final NpcString _npcString;
		private final String[] _parameters;

		public NpcStringArgument(NpcString npcString, String... arg)
		{
			_npcString = npcString;
			_parameters = arg;
		}

		@Override
		void writeData(SysMsgContainer<?> message)
		{
			message.writeD(_npcString.getId());
			for(String st : _parameters)
				message.writeS(st);
		}

		@Override
		Types getType()
		{
			return Types.NPCSTRING;
		}
	}
}
