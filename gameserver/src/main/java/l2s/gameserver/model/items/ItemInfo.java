package l2s.gameserver.model.items;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.templates.item.ItemTemplate;

public class ItemInfo
{
	private int ownerId;
	private int lastChange;
	private int type1;
	private int objectId;
	private int itemId;
	private long count;
	private int type2;
	private int customType1;
	private boolean isEquipped;
	private int bodyPart;
	private int enchantLevel;
	private int customType2;
	private int _variationStoneId;
	private int _variation1Id;
	private int _variation2Id;
	private int shadowLifeTime;
	private int attackElement = Element.NONE.getId();
	private int attackElementValue;
	private int defenceFire;
	private int defenceWater;
	private int defenceWind;
	private int defenceEarth;
	private int defenceHoly;
	private int defenceUnholy;
	private int equipSlot;
	private int temporalLifeTime;
	private int[] enchantOptions = ItemInstance.EMPTY_ENCHANT_OPTIONS;

	private boolean _isBlocked;

	private ItemTemplate item;

	public ItemInfo()
	{}

	public ItemInfo(ItemInstance item)
	{
		this(item, false);
	}

	public ItemInfo(ItemInstance item, boolean isBlocked)
	{
		setOwnerId(item.getOwnerId());
		setObjectId(item.getObjectId());
		setItemId(item.getItemId());
		setCount(item.getCount());
		setCustomType1(item.getCustomType1());
		setEquipped(item.isEquipped());
		setEnchantLevel(item.getEnchantLevel());
		setCustomType2(item.getCustomType2());
		setVariationStoneId(item.getVariationStoneId());
		setVariation1Id(item.getVariation1Id());
		setVariation2Id(item.getVariation2Id());
		setShadowLifeTime(item.getShadowLifeTime());
		setAttackElement(item.getAttackElement().getId());
		setAttackElementValue(item.getAttackElementValue());
		setDefenceFire(item.getDefenceFire());
		setDefenceWater(item.getDefenceWater());
		setDefenceWind(item.getDefenceWind());
		setDefenceEarth(item.getDefenceEarth());
		setDefenceHoly(item.getDefenceHoly());
		setDefenceUnholy(item.getDefenceUnholy());
		setIsBlocked(isBlocked);
		setEquipSlot(item.getEquipSlot());
		setTemporalLifeTime(item.getTemporalLifeTime());
		setEnchantOptions(item.getEnchantOptions());
	}

	public ItemTemplate getItem()
	{
		return item;
	}

	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	public void setLastChange(int lastChange)
	{
		this.lastChange = lastChange;
	}

	public void setType1(int type1)
	{
		this.type1 = type1;
	}

	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	public void setItemId(int itemId)
	{
		this.itemId = itemId;
		if (itemId > 0)
			item = ItemHolder.getInstance().getTemplate(getItemId());
		else
			item = null;
		if(item != null)
		{
			setType1(item.getType1());
			setType2(item.getType2ForPackets());
			setBodyPart(item.getBodyPart());
		}
	}

	public void setCount(long count)
	{
		this.count = count;
	}

	public void setType2(int type2)
	{
		this.type2 = type2;
	}

	public void setCustomType1(int customType1)
	{
		this.customType1 = customType1;
	}

	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}

	public void setBodyPart(int bodyPart)
	{
		this.bodyPart = bodyPart;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}

	public void setCustomType2(int customType2)
	{
		this.customType2 = customType2;
	}

	public void setVariationStoneId(int val)
	{
		_variationStoneId = val;
	}

	public void setVariation1Id(int val)
	{
		_variation1Id = val;
	}

	public void setVariation2Id(int val)
	{
		_variation2Id = val;
	}

	public void setShadowLifeTime(int shadowLifeTime)
	{
		this.shadowLifeTime = shadowLifeTime;
	}

	public void setAttackElement(int attackElement)
	{
		this.attackElement = attackElement;
	}

	public void setAttackElementValue(int attackElementValue)
	{
		this.attackElementValue = attackElementValue;
	}

	public void setDefenceFire(int defenceFire)
	{
		this.defenceFire = defenceFire;
	}

	public void setDefenceWater(int defenceWater)
	{
		this.defenceWater = defenceWater;
	}

	public void setDefenceWind(int defenceWind)
	{
		this.defenceWind = defenceWind;
	}

	public void setDefenceEarth(int defenceEarth)
	{
		this.defenceEarth = defenceEarth;
	}

	public void setDefenceHoly(int defenceHoly)
	{
		this.defenceHoly = defenceHoly;
	}

	public void setDefenceUnholy(int defenceUnholy)
	{
		this.defenceUnholy = defenceUnholy;
	}

	public void setEquipSlot(int equipSlot)
	{
		this.equipSlot = equipSlot;
	}

	public void setTemporalLifeTime(int temporalLifeTime)
	{
		this.temporalLifeTime = temporalLifeTime;
	}

	public void setIsBlocked(boolean val)
	{
		_isBlocked = val;
	}

	public int getOwnerId()
	{
		return ownerId;
	}

	public int getLastChange()
	{
		return lastChange;
	}

	public int getType1()
	{
		return type1;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public long getCount()
	{
		return count;
	}

	public int getType2()
	{
		return type2;
	}

	public int getCustomType1()
	{
		return customType1;
	}

	public boolean isEquipped()
	{
		return isEquipped;
	}

	public int getBodyPart()
	{
		return bodyPart;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	public int getVariationStoneId()
	{
		return _variationStoneId;
	}

	public int getVariation1Id()
	{
		return _variation1Id;
	}

	public int getVariation2Id()
	{
		return _variation2Id;
	}

	public int getShadowLifeTime()
	{
		return shadowLifeTime;
	}

	public int getCustomType2()
	{
		return customType2;
	}

	public int getAttackElement()
	{
		return attackElement;
	}

	public int getAttackElementValue()
	{
		return attackElementValue;
	}

	public int getDefenceFire()
	{
		return defenceFire;
	}

	public int getDefenceWater()
	{
		return defenceWater;
	}

	public int getDefenceWind()
	{
		return defenceWind;
	}

	public int getDefenceEarth()
	{
		return defenceEarth;
	}

	public int getDefenceHoly()
	{
		return defenceHoly;
	}

	public int getDefenceUnholy()
	{
		return defenceUnholy;
	}

	public int getEquipSlot()
	{
		return equipSlot;
	}

	public int getTemporalLifeTime()
	{
		return temporalLifeTime;
	}

	public boolean isBlocked()
	{
		return _isBlocked;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		if(getObjectId() == 0)
			return getItemId() == ((ItemInfo)obj).getItemId();
		return getObjectId() == ((ItemInfo)obj).getObjectId();
	}

	@Override     
	public int hashCode()
	{
		int hash = getItemId();
		hash = 89 * hash + getObjectId();
		return hash;
    }

	public int[] getEnchantOptions()
	{
		return enchantOptions;
	}

	public void setEnchantOptions(int[] enchantOptions)
	{
		this.enchantOptions = enchantOptions;
	}
}
