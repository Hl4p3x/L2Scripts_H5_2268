package l2s.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.slf4j.Logger;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @reworked VISTALL
 */
@SuppressWarnings("serial")
public class RewardList extends ArrayList<RewardGroup>
{
	public static final int MAX_CHANCE = 1000000;
	private final RewardType _type;
	private final boolean _autoLoot;

	public RewardList(RewardType rewardType, boolean a)
	{
		super(5);
		_type = rewardType;
		_autoLoot = a;
	}

	public List<RewardItem> roll(Player player)
	{
		return roll(player, 1.0, 1.0, null);
	}

	public List<RewardItem> roll(Player player, double penaltyMod, double rateMod)
	{
		return roll(player, penaltyMod, rateMod, null);
	}

	public List<RewardItem> roll(Player player, double penaltyMod, double rateMod, NpcInstance npc)
	{
		List<RewardItem> temp = new ArrayList<RewardItem>();
		for(RewardGroup g : this)
			temp.addAll(g.roll(_type, player, penaltyMod, rateMod, npc));
		return temp;
	}

	public boolean isAutoLoot()
	{
		return _autoLoot;
	}

	public RewardType getType()
	{
		return _type;
	}

	public static RewardList parseRewardList(Logger logger, org.dom4j.Element element, RewardType type, String debugString)
	{
		boolean autoLoot = element.attributeValue("auto_loot") != null && Boolean.parseBoolean(element.attributeValue("auto_loot"));
		RewardList list = new RewardList(type, autoLoot);

		for(Iterator<Element> nextIterator = element.elementIterator(); nextIterator.hasNext();)
		{
			final org.dom4j.Element nextElement = nextIterator.next();
			final String nextName = nextElement.getName();
			boolean notGroupType = type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED;
			if(nextName.equalsIgnoreCase("group"))
			{
				double enterChance = nextElement.attributeValue("chance") == null ? RewardList.MAX_CHANCE : Double.parseDouble(nextElement.attributeValue("chance")) * 10000;

				RewardGroup group = notGroupType ? null : new RewardGroup(enterChance);
				for(Iterator<org.dom4j.Element> rewardIterator = nextElement.elementIterator(); rewardIterator.hasNext();)
				{
					org.dom4j.Element rewardElement = rewardIterator.next();
					RewardData data = RewardData.parseReward(rewardElement);
					if(notGroupType)
						logger.warn("Can't load rewardlist from group: " + debugString + "; type: " + type);
					else
						group.addData(data);
				}

				if(group != null)
					list.add(group);
			}
			else if(nextName.equalsIgnoreCase("reward"))
			{
				if(!notGroupType)
				{
					logger.warn("Reward can't be without group(and not grouped): " + debugString + "; type: " + type);
					continue;
				}

				RewardData data = RewardData.parseReward(nextElement);
				RewardGroup g = new RewardGroup(RewardList.MAX_CHANCE);
				g.addData(data);
				list.add(g);
			}
		}

		return list;
	}
}