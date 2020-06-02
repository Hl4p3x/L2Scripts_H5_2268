package l2s.gameserver.model.entity.votereward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.napile.primitive.maps.IntLongMap;
import org.napile.primitive.maps.impl.HashIntLongMap;
import org.napile.primitive.pair.IntLongPair;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.logging.LoggerObject;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.VoteRewardRecordsDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 13.02.2019
 * Developed for L2-Scripts.com
 **/
public abstract class VoteRewardSite extends LoggerObject implements Runnable {
	private final String name;
	private final boolean enabled;
	private final int runDelay;
	private final List<RewardList> rewardLists = new ArrayList<>();
	private final Map<String, VoteRewardRecord> records = new ConcurrentHashMap<>();
	private final Lock lock = new ReentrantLock();

	public VoteRewardSite(MultiValueSet<String> parameters) {
		name = parameters.getString("name");
		enabled = parameters.getBool("enabled");
		runDelay = parameters.getInteger("run_delay", 0);
	}

	@Override
	public void run() {
		throw new UnsupportedOperationException(getClass().getName() + " not implemented run");
	}

	public final String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public final void addRewardList(RewardList rewardList) {
		rewardLists.add(rewardList);
	}

	public final VoteRewardRecord getRecord(String identifier) {
		VoteRewardRecord record = records.get(identifier);
		if(record == null) {
			record = new VoteRewardRecord(getName(), identifier, 0, -1);
			record.save();
			records.put(record.getIdentifier(), record);
		}
		return record;
	}

	public final Lock getLock() {
		return lock;
	}

	public void init() {
		VoteRewardRecordsDAO.getInstance().restore(records, getName());

		if(runDelay > 0 && isEnabled())
			ThreadPoolManager.getInstance().scheduleAtFixedRate(this, runDelay, runDelay * 1000L);
	}

	/**
	 * Процедура используется для получения награды через 'voice' команду.
	 * @param player
	 * @return true если награда была успешно выдана.
	 */
	public boolean tryGiveRewards(Player player) {
		return false;
	}

	protected void giveRewards(Player player, int count) {
		List<RewardItem> rolledItems = new ArrayList<>();
		for(RewardList rewardList : rewardLists) {
			for(int i = 0; i < count; i++)
				rolledItems.addAll(rewardList.roll(player));
		}

		if(rolledItems.isEmpty()) {
			player.sendMessage(new CustomMessage("votereward.reward_not_received." + getName(), player));
			return;
		}

		player.sendMessage(new CustomMessage("votereward.reward_received." + getName(), player));

		IntLongMap rewards = new HashIntLongMap();
		for(RewardItem rewardItem : rolledItems)
			rewards.put(rewardItem.itemId, rewards.get(rewardItem.itemId) + rewardItem.count);

		for(IntLongPair pair : rewards.entrySet())
			ItemFunctions.addItem(player, pair.getKey(), pair.getValue(), true, getName() + " vote reward");
	}
}
