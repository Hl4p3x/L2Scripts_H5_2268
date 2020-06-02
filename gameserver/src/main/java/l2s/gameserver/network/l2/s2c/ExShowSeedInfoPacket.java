package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.model.Manor;
import l2s.gameserver.templates.manor.SeedProduction;

/**
 * format
 * cddd[dddddc[d]c[d]]
 * cddd[dQQQdc[d]c[d]] - Gracia Final
 */
public class ExShowSeedInfoPacket extends L2GameServerPacket
{
	private List<SeedProduction> _seeds;
	private int _manorId;

	public ExShowSeedInfoPacket(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_seeds.size());
		for(SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id

			writeQ(seed.getCanProduce()); // Left to buy
			writeQ(seed.getStartProduce()); // Started amount
			writeQ(seed.getPrice()); // Sell Price
			writeD(Manor.getInstance().getSeedLevel(seed.getId())); // Seed Level

			writeC(1); // reward 1 Type
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id

			writeC(1); // reward 2 Type
			writeD(Manor.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
}