package ai.residences.dominion;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import l2s.gameserver.Config;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;

/**
 * @author VISTALL
 * @date 3:35/23.06.2011
 */
public class Catapult extends DefaultAI
{
	private static final IntObjectMap<NpcString[]> MESSAGES = new HashIntObjectMap<NpcString[]>(9);

	static
	{
		MESSAGES.put(81, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_GLUDIO, NpcString.THE_CATAPULT_OF_GLUDIO_HAS_BEEN_DESTROYED});
		MESSAGES.put(82, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_DION, NpcString.THE_CATAPULT_OF_DION_HAS_BEEN_DESTROYED});
		MESSAGES.put(83, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_GIRAN, NpcString.THE_CATAPULT_OF_GIRAN_HAS_BEEN_DESTROYED});
		MESSAGES.put(84, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_OREN, NpcString.THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED});
		MESSAGES.put(85, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_ADEN, NpcString.THE_CATAPULT_OF_ADEN_HAS_BEEN_DESTROYED});
		MESSAGES.put(86, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_INNADRIL, NpcString.THE_CATAPULT_OF_INNADRIL_HAS_BEEN_DESTROYED});
		MESSAGES.put(87, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_GODDARD, NpcString.THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED});
		MESSAGES.put(88, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_RUNE, NpcString.THE_CATAPULT_OF_RUNE_HAS_BEEN_DESTROYED});
		MESSAGES.put(89, new NpcString[] {NpcString.PROTECT_THE_CATAPULT_OF_SCHUTTGART, NpcString.THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED});
	}

	private class OnPlayerEnterListenerImpl implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			NpcInstance actor = getActor();
			DominionSiegeEvent siegeEvent = actor.getEvent(DominionSiegeEvent.class);
			if(siegeEvent == null)
				return;

			if(player.getEvent(DominionSiegeEvent.class) != siegeEvent)
				return;

			Quest q = QuestHolder.getInstance().getQuest(729);

			QuestState questState = q.newQuestState(player);
			questState.setCond(1, false);
		}
	}

	private final OnPlayerEnterListener _listener = new OnPlayerEnterListenerImpl();

	public Catapult(NpcInstance actor)
	{
		super(actor);
	}

		@Override
	public boolean thinkActive()
	{
		return false;
	}

	@Override
	public  void onEvtAttacked(Creature attacker, int dam)
	{
		NpcInstance actor = getActor();

		DominionSiegeEvent siegeEvent = actor.getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return;

		boolean first = actor.getParameter("dominion_first_attack", true);
		if(first)
		{
			actor.setParameter("dominion_first_attack", false);
			NpcString msg = MESSAGES.get(siegeEvent.getId())[0];
			Quest q = QuestHolder.getInstance().getQuest(729);
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(player.getEvent(DominionSiegeEvent.class) == siegeEvent)
				{
					player.sendPacket(new ExShowScreenMessage(msg, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER));

					QuestState questState = player.getQuestState(q);
					if(questState == null)
					{
						questState = q.newQuestState(player);
						questState.setCond(1, false);
					}
				}
			}
		}
	}

	@Override
	public  void onEvtAggression(Creature attacker, int d)
	{
		//
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		super.onEvtDead(killer);
		DominionSiegeEvent siegeEvent = actor.getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return;

		NpcString msg = MESSAGES.get(siegeEvent.getId())[1];
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.getEvent(DominionSiegeEvent.class) == siegeEvent)
			{
				player.sendPacket(new ExShowScreenMessage(msg, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER));

				QuestState questState = player.getQuestState(729);
				if(questState != null)
					questState.abortQuest();
			}
		}

		siegeEvent.doorAction(DominionSiegeEvent.CATAPULT_DOORS, true);

		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(player.getParty() == null)
		{
			DominionSiegeEvent siegeEvent2 = player.getEvent(DominionSiegeEvent.class);
			if(siegeEvent2 == null || siegeEvent2 == siegeEvent)
				return;
			siegeEvent2.addReward(player, DominionSiegeEvent.STATIC_BADGES, 15);
		}
		else
		{
			for(Player $member : player.getParty())
			{
				if($member.isInRange(player, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				{
					DominionSiegeEvent siegeEvent2 = $member.getEvent(DominionSiegeEvent.class);
					if(siegeEvent2 == null || siegeEvent2 == siegeEvent)
						continue;
					siegeEvent2.addReward($member, DominionSiegeEvent.STATIC_BADGES, 15);
				}
			}
		}
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();

		getActor().setParameter("dominion_first_attack", true);

		PlayerListenerList.addGlobal(_listener);
	}

	@Override
	public void onEvtDeSpawn()
	{
		super.onEvtDeSpawn();

		PlayerListenerList.removeGlobal(_listener);
	}
}
