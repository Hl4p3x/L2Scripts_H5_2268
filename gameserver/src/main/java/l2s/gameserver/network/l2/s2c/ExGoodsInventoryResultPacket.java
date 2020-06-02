package l2s.gameserver.network.l2.s2c;

/**
 * @author VISTALL
 * @date 11:19/03.07.2011
 */
public class ExGoodsInventoryResultPacket extends L2GameServerPacket
{
	public static L2GameServerPacket NOTHING = new ExGoodsInventoryResultPacket(1);
	public static L2GameServerPacket SUCCESS = new ExGoodsInventoryResultPacket(2);
	public static L2GameServerPacket ERROR = new ExGoodsInventoryResultPacket(-1);
	public static L2GameServerPacket TRY_AGAIN_LATER = new ExGoodsInventoryResultPacket(-2);
	public static L2GameServerPacket INVENTORY_FULL = new ExGoodsInventoryResultPacket(-3);
	public static L2GameServerPacket NOT_CONNECT_TO_PRODUCT_SERVER = new ExGoodsInventoryResultPacket(-4);
	public static L2GameServerPacket CANT_USE_AT_TRADE_OR_PRIVATE_SHOP = new ExGoodsInventoryResultPacket(-5);
	public static L2GameServerPacket NOT_EXISTS = new ExGoodsInventoryResultPacket(-6);
	public static L2GameServerPacket TO_MANY_USERS_TRY_AGAIN_INVENTORY = new ExGoodsInventoryResultPacket(-101);
	public static L2GameServerPacket TO_MANY_USERS_TRY_AGAIN = new ExGoodsInventoryResultPacket(-102);
	public static L2GameServerPacket PREVIOS_REQUEST_IS_NOT_COMPLETE = new ExGoodsInventoryResultPacket(-103);
	public static L2GameServerPacket NOTHING2 = new ExGoodsInventoryResultPacket(-104);
	public static L2GameServerPacket ALREADY_RETRACTED = new ExGoodsInventoryResultPacket(-105);
	public static L2GameServerPacket ALREADY_RECIVED = new ExGoodsInventoryResultPacket(-106);
	public static L2GameServerPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_SERVER = new ExGoodsInventoryResultPacket(-107);
	public static L2GameServerPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_PLAYER = new ExGoodsInventoryResultPacket(-108);

	private int _result;

	private ExGoodsInventoryResultPacket(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}
