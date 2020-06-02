package npc.model;

import java.util.List;
import java.util.concurrent.Future;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.PositionUtils;
import manager.FourSepulchersManager;
import manager.FourSepulchersSpawn;
import manager.FourSepulchersSpawn.GateKeeper;

public class SepulcherNpcInstance extends NpcInstance
{
	protected Future<?> _closeTask = null, _spawnMonsterTask = null;

	private final static String HTML_FILE_PATH = "four_sepulchers/";

	private final static int HALLS_KEY = 7260;

	public SepulcherNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDelete()
	{
		if(_closeTask != null)
		{
			_closeTask.cancel(false);
			_closeTask = null;
		}
		if(_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(false);
			_spawnMonsterTask = null;
		}
		super.onDelete();
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(isDead())
		{
			player.sendActionFailed();
			return;
		}

		if(getNpcId() >= 31468 && getNpcId() <= 31487) {
			doDie(player);
			if(_spawnMonsterTask != null)
				_spawnMonsterTask.cancel(false);
			_spawnMonsterTask = ThreadPoolManager.getInstance().schedule(new SpawnMonster(getNpcId()), 3500);
			return;
		} else if(getNpcId() >= 31455 && getNpcId() <= 31467) {
			Party party = player.getParty();
			if (!hasPartyAKey(player) && (party != null && party.isLeader(player) || player.isGM())) {
				ItemFunctions.addItem(player, HALLS_KEY, 1, "Give items on talk with npc SepulcherNpcInstance");
				doDie(player);
			}
			return;
		}

		super.showChatWindow(player, val);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = String.valueOf(npcId);
		else
			pom = npcId + "-" + val;
		return HTML_FILE_PATH + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("open_gate"))
		{
			ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if(hallsKey == null)
				showHtmlFile(player, "Gatekeeper-no.htm");
			else if(FourSepulchersManager.isAttackTime())
			{
				switch(getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						if(!FourSepulchersSpawn.isShadowAlive(getNpcId()))
							FourSepulchersSpawn.spawnShadow(getNpcId());
				}

				// Moved here from switch-default
				openNextDoor(getNpcId());

				Party party = player.getParty();
				if(party != null) {
					for (Player mem : party.getPartyMembers()) {
						hallsKey = mem.getInventory().getItemByItemId(HALLS_KEY);
						if (hallsKey != null)
							ItemFunctions.deleteItem(mem, HALLS_KEY, hallsKey.getCount());
					}
				}
				else if (hallsKey != null)
					ItemFunctions.deleteItem(player, HALLS_KEY, hallsKey.getCount());
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	public void openNextDoor(int npcId)
	{
		GateKeeper gk = FourSepulchersManager.getHallGateKeeper(npcId);
		gk.door.openMe();

		if(_closeTask != null)
			_closeTask.cancel(false);
		_closeTask = ThreadPoolManager.getInstance().schedule(new CloseNextDoor(gk), 10000);
	}

	private class CloseNextDoor extends RunnableImpl
	{
		private final GateKeeper _gk;
		private int state = 0;

		public CloseNextDoor(GateKeeper gk)
		{
			_gk = gk;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(state == 0)
			{
				try
				{
					_gk.door.closeMe();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				state++;
				_closeTask = ThreadPoolManager.getInstance().schedule(this, 10000);
			}
			else if(state == 1)
			{
				FourSepulchersSpawn.spawnMysteriousBox(_gk.template.npcId);
				_closeTask = null;
			}
		}
	}

	private class SpawnMonster extends RunnableImpl
	{
		private final int _NpcId;

		public SpawnMonster(int npcId)
		{
			_NpcId = npcId;
		}

		@Override
		public void runImpl() throws Exception
		{
			FourSepulchersSpawn.spawnMonster(_NpcId);
		}
	}

	public void sayInShout(NpcString npcString, String... params)
	{
		if(npcString == null)
			return; //wrong usage

		List<Player> knownPlayers = GameObjectsStorage.getAllPlayers();
		if(knownPlayers == null || knownPlayers.isEmpty())
			return;

		NSPacket sm = new NSPacket(this, ChatType.SHOUT, npcString, params);
		for(Player player : knownPlayers)
		{
			if(player == null)
				continue;

			if(PositionUtils.checkIfInRange(15000, player, this, true))
				player.sendPacket(sm);
		}
	}

	public void showHtmlFile(Player player, String file)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile("four_sepulchers/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private boolean hasPartyAKey(Player player)
	{
		Party party = player.getParty();
		if(party != null) {
			for (Player m : party.getPartyMembers())
				if (ItemFunctions.getItemCount(m, HALLS_KEY) > 0)
					return true;
		} else if(player.isGM()) {
			if (ItemFunctions.getItemCount(player, HALLS_KEY) > 0)
				return true;
		}
		return false;
	}
}