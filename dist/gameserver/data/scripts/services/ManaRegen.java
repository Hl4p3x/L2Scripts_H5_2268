package services;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class ManaRegen extends Functions
{
	private static final int ADENA = 57;
	private static final long PRICE = 5; //5 аден за 1 МП

	public void DoManaRegen()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if (npc == null || npc.getReflection().getId() != player.getReflection().getId() || !player.checkInteractionDistance(npc))
			return;

		long mp = (long) Math.floor(player.getMaxMp() - player.getCurrentMp());
		long fullCost = mp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(ItemFunctions.getItemCount(player, ADENA) >= fullCost)
		{
			ItemFunctions.deleteItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(mp));
			player.setCurrentMp(player.getMaxMp());
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}
}