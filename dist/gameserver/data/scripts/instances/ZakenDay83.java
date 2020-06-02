package instances;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.utils.Location;

/**
 * Класс контролирует высшего дневного Закена
 */

public class ZakenDay83 extends Reflection {
	private DeathListener _deathListener = new DeathListener();
	private List<Player> playerininstance = new ArrayList<Player>();


	private static final int Anchor = 32468;
	private static Location[] zakenTp = {new Location(55272, 219080, -2952),
			new Location(55272, 219080, -3224),
			new Location(55272, 219080, -3496),};
	public static long _savedTime;

	@Override
	protected void onCreate() {
		super.onCreate();
		addSpawnWithoutRespawn(Anchor, zakenTp[Rnd.get(zakenTp.length)], 0);
		_savedTime = System.currentTimeMillis();
	}

	@Override
	public void onPlayerEnter(Player player) {
		super.onPlayerEnter(player);
		ZakenDeathListener();
		player.sendMessage(" "+ playerininstance);

		player.sendPacket(new ExSendUIEventPacket(player, false, true, (int) (System
				.currentTimeMillis() - _savedTime) / 1000, 0,
				NpcString.ELAPSED_TIME));
		playerininstance.add(player);
	}

	private void ZakenDeathListener()
	{
		for(NpcInstance npc : getNpcs())
			npc.addListener(_deathListener);
	}


	@Override
	public void onPlayerExit(Player player) {
		super.onPlayerExit(player);
		player.sendPacket(new ExSendUIEventPacket(player, true, true, 0, 0));
	}
	   // Контроллер смерти Закена
	private class DeathListener implements OnDeathListener {

		@Override
		public void onDeath(Creature self, Creature killer) {
			if(self.isNpc() && self.getNpcId() == 29181) {
				if(killer.isPlayer()) {
					//После убийства закена ставим откат инста
					for (Player member : playerininstance) {
						member.setInstanceReuse(getInstancedZone().getId(), System.currentTimeMillis());
					}
				}
			}
		}
	}
}