package l2s.gameserver.network.l2.s2c;


import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.Henna;

public class HennaEquipListPacket extends L2GameServerPacket
{
	private int _emptySlots;
	private long _adena;
	private List<Henna> _hennas = new ArrayList<Henna>();

	public HennaEquipListPacket(Player player)
	{
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();

		List<Henna> list = HennaHolder.getInstance().generateList(player);
		for(Henna element : list)
			if(player.getInventory().getItemByItemId(element.getDyeId()) != null)
				_hennas.add(element);
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_adena);
		writeD(_emptySlots);
		writeD(_hennas.size());
		for(Henna henna : _hennas)
		{
			writeD(henna.getSymbolId()); //symbolid
			writeD(henna.getDyeId()); //itemid of dye
			writeQ(henna.getDrawCount());
			writeQ(henna.getPrice());
			writeD(1); //meet the requirement or not
			writeD(0); // UNK
		}
	}
	
	@Override
	protected final void writeImplHF()
	{
		writeQ(_adena);
		writeD(_emptySlots);
		if(_hennas.size() != 0)
		{
			writeD(_hennas.size());
			for(Henna henna : _hennas)
			{
				writeD(henna.getSymbolId()); //symbolid
				writeD(henna.getDyeId()); //itemid of dye
				writeQ(henna.getDrawCount());
				writeQ(henna.getPrice());
				writeD(1); //meet the requirement or not
			}
		}
		else
		{
			writeD(0x01);
			writeD(0x00);
			writeD(0x00);
			writeQ(0x00);
			writeQ(0x00);
			writeD(0x00);
		}
	}
}