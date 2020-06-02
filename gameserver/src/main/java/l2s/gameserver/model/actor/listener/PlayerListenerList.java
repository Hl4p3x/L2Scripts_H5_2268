package l2s.gameserver.model.actor.listener;

import l2s.commons.listener.Listener;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import l2s.gameserver.listener.actor.player.OnPlayerChatMessageReceive;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2s.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2s.gameserver.listener.actor.player.OnPlayerSummonServitorListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.network.l2.components.ChatType;

/**
 * @author G1ta0
 */
public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}

	public void onExit()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
	}

	public void onPartyInvite()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onLevelChange(int oldLvl, int newLvl)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);
	}

	public void onPartyLeave()
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}

	public void onSummonServitor(Servitor servitor)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);
	}

	public void onChatMessageReceive(ChatType type, String charName, String text)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnPlayerChatMessageReceive.class.isInstance(listener))
					((OnPlayerChatMessageReceive) listener).onChatMessageReceive(getActor(), type, charName, text);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerChatMessageReceive.class.isInstance(listener))
					((OnPlayerChatMessageReceive) listener).onChatMessageReceive(getActor(), type, charName, text);
	}
}
