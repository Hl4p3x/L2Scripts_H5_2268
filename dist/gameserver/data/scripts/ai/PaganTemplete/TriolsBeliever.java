package ai.PaganTemplete;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * @author PaInKiLlEr
 *         - AI для монстра Triols Believer (22143) и Triols Priest (22146) и Triols Priest (22151).
 *         - Если увидел игрока в радиусе 500, если его пати состовляет больше 4 мемберов.
 *         - Тогда выбрасывает на рандомные координаты первого увидевшего игрока.
 *         - AI проверен и работает.
 */
public class TriolsBeliever extends Mystic
{
	private boolean _tele = true;

	public static final Location[] locs = {new Location( -16128, -35888, -10726), new Location( -16397, -44970, -10724), new Location( -15729, -42001, -10724)};

	public TriolsBeliever(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		for(Player player : World.getAroundPlayers(actor, 500, 500))
		{
			if(player == null || !player.isInParty())
				continue;

			if(player.getParty().getMemberCount() >= 5 && _tele)
			{
				_tele = false;
				player.teleToLocation(Rnd.get(locs));
			}
		}

		return true;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tele = true;
		super.onEvtDead(killer);
	}
}