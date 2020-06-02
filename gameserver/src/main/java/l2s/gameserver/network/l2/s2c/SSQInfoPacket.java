package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.SevenSigns;

/**
 * Seven Signs Info
 *
 * packet id 0x73
 * format: cc
 *
 * Пример пакета с оффа (828 протокол):
 * 73 01 01
 *
 * Возможные варианты использования данного пакета:
 * 0 0 - Обычное небо???
 * 1 1 - Dusk Sky
 * 2 2 - Dawn Sky???
 * 3 3 - Небо постепенно краснеет (за 10 секунд)
 *
 * Возможно и другие вариации, эффект не совсем понятен.
 * 1 0
 * 0 1
 * @author SYS
 */
public class SSQInfoPacket extends L2GameServerPacket
{
	private int _state = 0;

	public SSQInfoPacket()
	{
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		if(SevenSigns.getInstance().isSealValidationPeriod())
			if(compWinner == SevenSigns.CABAL_DAWN)
				_state = 2;
			else if(compWinner == SevenSigns.CABAL_DUSK)
				_state = 1;
	}

	public SSQInfoPacket(int state)
	{
		_state = state;
	}

	@Override
	protected final void writeImpl()
	{
		switch(_state)
		{
			case 1:
				writeH(257);
				break;
			case 2:
				writeH(258);
				break;
			default:
				writeH(256);
				break;
		}
	}
}