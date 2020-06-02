package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.model.Manor;

/**
 * format(packet 0xFE)
 * ch cd [ddddcdcd]
 * c  - id
 * h  - sub id
 *
 * c
 * d  - size
 *
 * [
 * d  - level
 * d  - seed price
 * d  - seed level
 * d  - crop price
 * c
 * d  - reward 1 id
 * c
 * d  - reward 2 id
 * ]
 *
 */
public class ExShowManorDefaultInfoPacket extends L2GameServerPacket
{
	private List<Integer> _crops = null;

	public ExShowManorDefaultInfoPacket()
	{
		_crops = Manor.getInstance().getAllCrops();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_crops.size());
		for(int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(Manor.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(Manor.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeD(Manor.getInstance().getCropBasicPrice(cropId)); // crop price
			writeC(1); // rewrad 1 Type
			writeD(Manor.getInstance().getRewardItem(cropId, 1)); // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(Manor.getInstance().getRewardItem(cropId, 2)); // Rewrad 2 Type Item Id
		}
	}
}