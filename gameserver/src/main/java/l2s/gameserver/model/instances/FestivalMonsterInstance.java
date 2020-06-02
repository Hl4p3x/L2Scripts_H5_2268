package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;


public class FestivalMonsterInstance extends MonsterInstance
{
	protected int _bonusMultiplier = 1;

	/**
	 * Constructor<?> of L2FestivalMonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2FestivalMonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public FestivalMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_hasRandomWalk = false;
	}

	public void setOfferingBonus(int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		List<Player> pl = World.getAroundPlayers(this);
		if(pl.isEmpty())
			return;
		List<Player> alive = new ArrayList<Player>(9);
		for(Player p : pl)
			if(!p.isDead())
				alive.add(p);
		if(alive.isEmpty())
			return;

		Player target = alive.get(Rnd.get(alive.size()));
		getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
	}

	/**
	 * Actions:
	 * <li>Check if the killing object is a player, and then find the party they belong to.</li>
	 * <li>Add a blood offering item to the leader of the party.</li>
	 * <li>Update the party leader's inventory to show the new item addition.</li>
	 */
	@Override
	public void rollRewards(RewardList list, final Creature lastAttacker, Creature topDamager)
	{
		super.rollRewards(list, lastAttacker, topDamager);

		if(list.getType() != RewardType.RATED_GROUPED)
			return;
		if(!topDamager.isPlayable())
			return;

		Player topDamagerPlayer = topDamager.getPlayer();
		Party associatedParty = topDamagerPlayer.getParty();

		if(associatedParty == null)
			return;

		Player partyLeader = associatedParty.getPartyLeader();
		if(partyLeader == null)
			return;

		ItemInstance bloodOfferings = ItemFunctions.createItem(SevenSignsFestival.FESTIVAL_BLOOD_OFFERING);

		bloodOfferings.setCount(_bonusMultiplier);
		partyLeader.getInventory().addItem(bloodOfferings);
		partyLeader.sendPacket(SystemMessagePacket.obtainItems(SevenSignsFestival.FESTIVAL_BLOOD_OFFERING, _bonusMultiplier, 0));
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1000;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}