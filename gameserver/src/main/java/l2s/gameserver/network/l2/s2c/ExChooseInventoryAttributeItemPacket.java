package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

public class ExChooseInventoryAttributeItemPacket extends L2GameServerPacket
{
	private int _itemId;
	private boolean _disableFire;
	private boolean _disableWater;
	private boolean _disableEarth;
	private boolean _disableWind;
	private boolean _disableDark;
	private boolean _disableHoly;
	private int _stoneLvl;

	public ExChooseInventoryAttributeItemPacket(ItemInstance item)
	{
		_itemId = item.getItemId();
		_disableFire = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.FIRE;
		_disableWater = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.WATER;
		_disableWind = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.WIND;
		_disableEarth = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.EARTH;
		_disableHoly = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.HOLY;
		_disableDark = ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.UNHOLY;
		_stoneLvl = item.getTemplate().isAttributeCrystal() ? 6 : 3;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_itemId);
		writeQ(0); // Ertheia UNK.
		writeD(_disableFire ? 1 : 0);  //fire
		writeD(_disableWater ? 1 : 0); // water
		writeD(_disableWind ? 1 : 0);  //wind
		writeD(_disableEarth ? 1 : 0);  //earth
		writeD(_disableHoly ? 1 : 0); //holy
		writeD(_disableDark ? 1 : 0); //dark
		writeD(_stoneLvl); //max enchant lvl
		writeD(0); //equipable items count
		/*for(int itemObjId : _attributableItems.toArray())
		{
			writeD(itemObjId); //itemObjId
		}
		*/
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_itemId);
		writeD(_disableFire ? 1 : 0);  //fire
		writeD(_disableWater ? 1 : 0); // water
		writeD(_disableWind ? 1 : 0);  //wind
		writeD(_disableEarth ? 1 : 0);  //earth
		writeD(_disableHoly ? 1 : 0); //holy
		writeD(_disableDark ? 1 : 0); //dark
		writeD(_stoneLvl); //max enchant lvl
	}
}