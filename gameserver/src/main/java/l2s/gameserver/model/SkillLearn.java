package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.templates.item.data.ItemData;

/**
 * @author VISTALL
 */
public final class SkillLearn implements Comparable<SkillLearn>
{
	private final int _id;
	private final int _level;
	private final int _minLevel;
	private final int _cost;
	private final int _itemId;
	private final long _itemCount;
	private final boolean _clicked;
	private final ClassLevel _classLevel;
	private final List<ItemData> _additionalRequiredItems = new ArrayList<ItemData>(); // Ð�Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¸Ñ‚ÐµÐ¼Ñ‹ (ÐºÐ½Ð¸Ð³Ð¸) Ð½Ðµ Ð²Ð»Ð¸Ñ�ÐµÑ‚ ÐºÐ¾Ð½Ñ„Ð¸Ð³ Ð½Ð° Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½Ð¸Ðµ Ð½ÐµÐ¾Ð±Ñ…Ð¾Ð´Ð¸Ð¼Ð¾Ñ�Ñ‚Ð¸ ÐºÐ½Ð¸Ð³.
	private final List<ItemData> _allRequiredItems = new ArrayList<ItemData>(); // Ð’Ñ�Ðµ Ñ‚Ñ€ÐµÐ±ÑƒÐµÐ¼Ñ‹Ðµ Ð¸Ñ‚ÐµÐ¼Ñ‹ (ÐºÐ½Ð¸Ð³Ð¸).

	public SkillLearn(int id, int lvl, int minLvl, int cost, int itemId, long itemCount, boolean clicked, ClassLevel classLevel)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_cost = cost;

		_itemId = itemId;
		_itemCount = itemCount;
		if(itemId > 0 && itemCount > 0)
			_allRequiredItems.add(new ItemData(itemId, itemCount));
		_clicked = clicked;
		_classLevel = classLevel;
	}

	public SkillLearn(int id, int lvl, int minLvl, int cost, int itemId, long itemCount, boolean clicked)
	{
		this(id, lvl, minLvl, cost, itemId, itemCount, clicked, ClassLevel.NONE);
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getCost()
	{
		return _cost;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public boolean isClicked()
	{
		return _clicked;
	}

	public ClassLevel getClassLevel()
	{
		return _classLevel;
	}

	public boolean isFreeAutoGet(AcquireType type)
	{
		return /*isAutoGet() && */getCost() == 0 && !haveRequiredItemsForLearn(type);
	}

	public void addAdditionalRequiredItem(int id, long count)
	{
		if(id > 0 && count > 0)
		{
			ItemData item = new ItemData(id, count);
			_additionalRequiredItems.add(item);
			_allRequiredItems.add(item);
		}
	}

	public void addAdditionalRequiredItems(List<ItemData> items)
	{
		_additionalRequiredItems.addAll(items);
		_allRequiredItems.addAll(items);
	}

	public List<ItemData> getAdditionalRequiredItems()
	{
		return _additionalRequiredItems;
	}

	public List<ItemData> getRequiredItemsForLearn(AcquireType type)
	{
		if(Config.DISABLED_SPELLBOOKS_FOR_ACQUIRE_TYPES.contains(type))
			return _additionalRequiredItems;
		return _allRequiredItems;
	}

	public boolean haveRequiredItemsForLearn(AcquireType type)
	{
		return !getRequiredItemsForLearn(type).isEmpty();
	}

	@Override
	public int compareTo(SkillLearn o)
	{
		if(getId() == o.getId())
			return getLevel() - o.getLevel();
		else
			return getId() - o.getId();
	}
}