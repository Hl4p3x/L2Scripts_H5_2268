package l2s.gameserver.model.entity.auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.data.xml.holder.SoulCrystalHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.AuctionStorage;
import l2s.gameserver.model.items.ItemAttributes;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.c2s.RequestPrivateStoreBuy;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
//import l2s.gameserver.utils.Log;

public class AuctionManager
{
	private static AuctionManager _instance;
	private static final Logger _log = LoggerFactory.getLogger(AuctionManager.class);
	
	private final Map<Integer, Auction> _auctions = new FastMap<>();
	private final List<Integer> _deadAuctions = new ArrayList<>();
	private final Map<Integer, Long> _lastMadeAuction = new FastMap<>();
	private int _lastId = -1;
	
	private AuctionManager()
	{
		loadAuctions();
	}
	
	public Auction getAuction(int auctionId)
	{
		return _auctions.get(auctionId);
	}
	
	public Auction getAuction(ItemInstance item)
	{
		for(Auction auction : getAllAuctions())
		{
			if(auction.getItem().equals(item))
				return auction;
		}
		return null;
	}
	
	public Collection<Auction> getAllAuctions()
	{
		return _auctions.values();
	}
	
	public Collection<Auction> getMyAuctions(Player player)
	{
		return getMyAuctions(player.getObjectId());
	}
	
	public Collection<Auction> getMyAuctions(int playerObjectId)
	{
		Collection<Auction> coll = new ArrayList<>();
		for(Auction auction : getAllAuctions())
		{
			if(auction != null && auction.getSellerObjectId() == playerObjectId)
				coll.add(auction);
		}
		return coll;
	}
	
	private void loadAuctions()
	{
		AuctionStorage.getInstance();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM auctions");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("auction_id");
				int sellerObjectId = rset.getInt("seller_object_id");
				String sellerName = rset.getString("seller_name");
				int itemObjectId = rset.getInt("item_object_id");
				long pricePerItem = rset.getLong("price_per_item");
				ItemInstance item = AuctionStorage.getInstance().getItemByObjectId(itemObjectId);

				// Saving last Id
				if(id > _lastId)
					_lastId = id;
				
				if(item != null)
				{
					Auction auction = new Auction(id, sellerObjectId, sellerName, item, pricePerItem, item.getCount(), getItemGroup(item), false);
					_auctions.put(id, auction);
				}
				else
					_deadAuctions.add(id);
			}
		}
		catch(SQLException e)
		{
			_log.error("Error while loading Auctions", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void addAuctionToDatabase(Auction auction)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO auctions VALUES(?,?,?,?,?)");
			statement.setInt(1, auction.getAuctionId());
			statement.setInt(2, auction.getSellerObjectId());
			statement.setString(3, auction.getSellerName());
			statement.setInt(4, auction.getItem().getObjectId());
			statement.setLong(5, auction.getPricePerItem());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.error("Error while adding auction to database:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Adding adena by database
	 * @param sellerObjectId
	 * @param adena
	 */
	public void addAdenaToSeller(int sellerObjectId, long adena)
	{
		int objId = -1;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id FROM items WHERE item_id=" + ItemTemplate.ITEM_ID_ADENA + " AND owner_id=" + sellerObjectId + " AND loc='INVENTORY'");
			rset = statement.executeQuery();
			if(rset.next())
				objId = rset.getInt("object_id");
		}
		catch(SQLException e)
		{
			_log.error("Error while selecting adena:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		if(objId == -1)
		{
			ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), ItemTemplate.ITEM_ID_ADENA);
			item.setCount(adena);
			item.setOwnerId(sellerObjectId);
			item.setLocation(ItemLocation.INVENTORY);
			ItemsDAO.getInstance().save(item);
		}
		else
		{
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE items SET count=count+" + adena + " WHERE object_id=" + objId);
				statement.execute();
			}
			catch(SQLException e)
			{
				_log.error("Error while selecting adena:", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			ItemsDAO.getInstance().getCache().remove(objId);
		}
	}
	
	private void deleteAuctionFromDatabase(Auction auction)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM auctions WHERE auction_id = ?");
			statement.setInt(1, auction.getAuctionId());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.error("Error while deleting auction from database:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void deleteAuction(Player seller, ItemInstance item)
	{
		Auction auction = null;
		for(Auction anyAuction : getMyAuctions(seller))
		{
			if(anyAuction.getItem().equals(item))
			{
				auction = anyAuction;
				break;
			}
		}
		deleteAuction(seller, auction);
	}
	
	public void deleteAuction(Player seller, Auction auction)
	{
		if(auction == null)
		{
			sendMessage(seller, "This auction doesnt exist anymore!");
			return;
		}
		
		ItemInstance item = auction.getItem();
		
		if(!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !seller.isInPeaceZone())
			sendMessage(seller, "You cannot delete auction outside town!");

		_auctions.remove(auction.getAuctionId());
		
		PcInventory inventory = seller.getInventory();
		AuctionStorage storage = AuctionStorage.getInstance();
		inventory.writeLock();
		storage.writeLock();
		try
		{
			inventory.addItem(item);
			storage.removeItem(item);
		}
		finally
		{
			storage.writeUnlock();
			inventory.writeUnlock();
		}
		
		seller.sendChanges();
		
		deleteAuctionFromDatabase(auction);
		
		sendMessage(seller, "Auction has been removed!");
	}
	
	public Auction addNewStore(Player seller, ItemInstance item, long salePrice, long count)
	{
		int id = getNewId();
		AuctionItemTypes type = getItemGroup(item);
		return addAuction(seller, id, item, salePrice, count, type, true);
	}
	
	public void removeStore(Player seller, int auctionId)
	{
		if(!Config.AUCTION_PRIVATE_STORE_AUTO_ADDED)
			return;
		
		if(auctionId <= 0)
			return;

		Auction a = getAuction(auctionId);
		if(a == null || !a.isPrivateStore() || a.getSellerObjectId() != seller.getObjectId())
			return;

		_auctions.remove(auctionId);
	}
	
	public synchronized void removePlayerStores(Player player)
	{
		if(!Config.AUCTION_PRIVATE_STORE_AUTO_ADDED)
			return;

		int playerObjId = player.getObjectId();
		List<Integer> keysToRemove = new ArrayList<>();
		for(Entry<Integer, Auction> auction : _auctions.entrySet())
		{
			if(auction.getValue().getSellerObjectId() == playerObjId && auction.getValue().isPrivateStore())
				keysToRemove.add(auction.getKey());
		}

		for(Integer key : keysToRemove)
			_auctions.remove(key);
	}
	
	public void setNewCount(int auctionId, long newCount)
	{
		if(auctionId <= 0)
			return;

		_auctions.get(auctionId).setCount(newCount);
	}
	
	public void buyItem(Player buyer, ItemInstance item, long quantity)
	{
		Auction auction = getAuction(item);
		if(auction == null)
		{
			sendMessage(buyer, "This auction doesnt exist anymore!");
			return;
		}
		if(buyer.isBlocked())
		{
			sendMessage(buyer, "You cannot buy items while being blocked!");
			return;
		}
		if(auction.getSellerObjectId() == buyer.getObjectId())
		{
			sendMessage(buyer, "You cannot win your own auction!");
			return;
		}
		if(quantity <= 0)
		{
			sendMessage(buyer, "You need to buy at least one item!");
			return;
		}
		if(item.getCount() < quantity)
		{
			sendMessage(buyer, "You are trying to buy too many items!");
			return;
		}
		if(auction.getPricePerItem() * quantity > buyer.getAdena())
		{
			sendMessage(buyer, "You don't have enough adena!");
			return;
		}
		
		if(!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !buyer.isInPeaceZone())
		{
			sendMessage(buyer, "You cannot use buy that item outside town!");
			return;
		}
		
		if(auction.isPrivateStore())
		{
			Player seller = GameObjectsStorage.getPlayer(auction.getSellerObjectId());
			if(seller == null)
			{
				sendMessage(buyer, "This auction doesnt exist anymore !");
				return;
			}
			RequestPrivateStoreBuy.buyFromStore(seller, buyer, 1, new int[]{ item.getObjectId() }, new long[]{ quantity }, new long[]{ auction.getPricePerItem() });
			return;
		}
		
		buyer.getInventory().reduceAdena(auction.getPricePerItem() * quantity);
		boolean wholeItemBought = false;
		
		PcInventory inventory = buyer.getInventory();
		AuctionStorage storage = AuctionStorage.getInstance();
		inventory.writeLock();
		storage.writeLock();
		try
		{
			if(item.getCount() == quantity)
			{
				item.setOwnerId(buyer.getObjectId());
				storage.removeItem(item);
				inventory.addItem(item);
				
				deleteAuctionFromDatabase(auction);
				_auctions.remove(auction.getAuctionId());
				wholeItemBought = true;
			}
			else
			{
				ItemInstance newItem = copyItem(item, quantity);
				newItem.setOwnerId(buyer.getObjectId());
				storage.removeItem(item, quantity);
				inventory.addItem(newItem);
				inventory.refreshEquip();
				auction.setCount(auction.getCountToSell() - quantity);
			}
		}
		finally
		{
			storage.writeUnlock();
			inventory.writeUnlock();
		}
		
		buyer.sendChanges();
		
		Player seller = GameObjectsStorage.getPlayer(auction.getSellerObjectId());
		if(seller != null)
		{
			if(wholeItemBought)
				seller.sendMessage(item.getName() + " has been bought by " + buyer.getName() + "!");
			else
				seller.sendMessage(quantity + " " + item.getName() + (quantity > 1 ? "s" : "") + " has been bought by " + buyer.getName() + "!");
			seller.addAdena(auction.getPricePerItem() * quantity, true);
		}
		else
		{
			addAdenaToSeller(auction.getSellerObjectId(), auction.getPricePerItem() * quantity);
			//Log.item(Log.AuctionSold, buyer, auction.getSellerObjectId(), item, "Sold for " + (auction.getPricePerItem() * quantity) + " adena.");
		}
		
		buyer.sendMessage("You have bought " + item.getName());
	}
	
	public void checkAndAddNewAuction(Player seller, ItemInstance item, long quantity, long salePrice)
	{
		if(!checkIfItsOk(seller, item, quantity, salePrice))
			return;
		
		int id = getNewId();
		if(id < 0)
		{
			sendMessage(seller, "There are currently too many auctions!");
			return;
		}
		
		AuctionItemTypes type = getItemGroup(item);
		
		PcInventory inventory = seller.getInventory();
		AuctionStorage storage = AuctionStorage.getInstance();
		Auction auction = null;
		
		inventory.writeLock();
		storage.writeLock();
		try
		{
			if(item.getCount() > quantity)
			{
				ItemInstance newItem = copyItem(item, quantity);
				seller.getInventory().removeItem(item, quantity);
				inventory.refreshEquip();
				storage.addItem(newItem);
				auction = addAuction(seller, id, newItem, salePrice, quantity, type, false);
			}
			else
			{
				inventory.removeItem(item);
				item.setCount(quantity);
				storage.addFullItem(item);
				auction = addAuction(seller, id, item, salePrice, quantity, type, false);
			}
		}
		finally
		{
			storage.writeUnlock();
			inventory.writeUnlock();
		}
		
		seller.sendChanges();
		_lastMadeAuction.put(seller.getObjectId(), System.currentTimeMillis() + (Config.SECONDS_BETWEEN_ADDING_AUCTIONS * 1000));
		
		seller.getInventory().reduceAdena(Config.AUCTION_FEE);
		addAuctionToDatabase(auction);
		sendMessage(seller, "Auction has been created!");
	}
	
	private Auction addAuction(Player seller, int auctionId, ItemInstance item, long salePrice, long sellCount, AuctionItemTypes itemType, boolean privateStore)
	{
		Auction newAuction = new Auction(auctionId, seller.getObjectId(), seller.getName(), item, salePrice, sellCount, itemType, privateStore);
		_auctions.put(auctionId, newAuction);
		return newAuction;
	}
	
	public void sendMessage(Player player, String message)
	{
		player.sendMessage(message);
	}
	
	private ItemInstance copyItem(ItemInstance oldItem, long quantity)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), oldItem.getItemId());
		item.setOwnerId(oldItem.getOwnerId());
		item.setCount(quantity);
		item.setEnchantLevel(oldItem.getEnchantLevel());
		item.setLocation(ItemLocation.VOID);
		item.setLocData(-1);
		item.setCustomType1(oldItem.getCustomType1());
		item.setCustomType2(oldItem.getCustomType2());
		item.setLifeTime(oldItem.getLifeTime());
		item.setCustomFlags(oldItem.getCustomFlags());
		item.setVariation1Id(oldItem.getVariation1Id());
		item.setVariation2Id(oldItem.getVariation2Id());
		
		ItemAttributes oldAtt = oldItem.getAttributes();
		ItemAttributes att = new ItemAttributes(oldAtt.getFire(), oldAtt.getWater(), oldAtt.getWind(), oldAtt.getEarth(), oldAtt.getHoly(), oldAtt.getUnholy());
		item.setAttributes(att);
		
		item.setAgathionEnergy(oldItem.getAgathionEnergy());
		item.setLocation(ItemLocation.VOID);
		
		return item;
	}
	
	private synchronized int getNewId()
	{
		return ++_lastId;
	}
	
	private boolean checkIfItsOk(Player seller, ItemInstance item, long quantity, long salePrice)
	{
		if(seller == null)
			return false;

		if(item == null)
		{
			sendMessage(seller, "Item you are trying to sell, doesn't exist!");
			return false;
		}
		if((item.getOwnerId() != seller.getObjectId()) || (seller.getInventory().getItemByObjectId(item.getObjectId()) == null))
		{
			sendMessage(seller, "Item you are trying to sell, doesn't exist!");
			return false;
		}
		if(item.isEquipped())
		{
			sendMessage(seller, "You need to unequip that item first!");
			return false;
		}
		if(item.isAugmented())
		{
			sendMessage(seller, "You cannot sell Augmented weapons!");
			return false;
		}
		if(item.getTemplate().isQuest())
		{
			sendMessage(seller, "You can't sell quest items!");
			return false;
		}
		if(!item.canBeTraded(seller))
		{
			sendMessage(seller, "You cannot sell this item!");
			return false;
		}
		if((seller.getServitor() != null) && (item.getItemType() == EtcItemType.PET_COLLAR))
		{
			sendMessage(seller, "Please unsummon your pet before trying to sell this item.");
			return false;
		}
		if((seller.getServitor() != null) && item.isSummon() && item.getTemplate().isForPet())
		{
			sendMessage(seller, "Please unsummon your pet before trying to sell this item.");
			return false;
		}
		if(quantity < 1)
		{
			sendMessage(seller, "Quantity is too low!");
			return false;
		}
		if(item.getCount() < quantity)
		{
			sendMessage(seller, "You don't have enough items to sell!");
			return false;
		}
		if(seller.getAdena() < Config.AUCTION_FEE)
		{
			sendMessage(seller, "You don't have enough adena, to pay the fee!");
			return false;
		}
		if(salePrice <= 0)
		{
			sendMessage(seller, "Sale price is too low!");
			return false;
		}
		if(salePrice > 999999999999L)
		{
			sendMessage(seller, "Price is too high!");
			return false;
		}
		if(seller.isBlocked())
		{
			sendMessage(seller, "Cannot create auctions while being Blocked!");
			return false;
		}
		if(getMyAuctions(seller).size() >= 10)
		{
			sendMessage(seller, "You can have just 10 auctions at the time!");
			return false;
		}
		if(!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !seller.isInPeaceZone())
		{
			sendMessage(seller, "You cannot add new Auction outside town!");
			return false;
		}
		if(seller.isInStoreMode())
		{
			sendMessage(seller, "Close your store before creating new Auction!");
			return false;
		}
		if(_lastMadeAuction.containsKey(seller.getObjectId()))
		{
			if(_lastMadeAuction.get(seller.getObjectId()) > System.currentTimeMillis())
			{
				sendMessage(seller, "You cannot do it so often!");
				return false;
			}
		}
		return true;
	}
	
	private AuctionItemTypes getItemGroup(ItemInstance item)
	{
		if(item.isEquipable())
		{
			if(item.getBodyPart() == (ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR))
				return AccessoryItemType.Earring;

			if(item.getBodyPart() == (ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER))
				return AccessoryItemType.Ring;

			if(item.getBodyPart() == ItemTemplate.SLOT_NECK)
				return AccessoryItemType.Necklace;

			if((item.getBodyPart() == ItemTemplate.SLOT_L_BRACELET) || (item.getBodyPart() == ItemTemplate.SLOT_R_BRACELET))
				return AccessoryItemType.Bracelet;

			if((item.getBodyPart() == ItemTemplate.SLOT_HAIR) || (item.getBodyPart() == ItemTemplate.SLOT_HAIRALL) || (item.getBodyPart() == ItemTemplate.SLOT_DHAIR))
				return AccessoryItemType.Accessory;
		}
		
		if(item.isArmor())
		{
			if(item.getBodyPart() == ItemTemplate.SLOT_HEAD)
				return ArmorItemType.Helmet;

			if(item.getBodyPart() == ItemTemplate.SLOT_CHEST)
				return ArmorItemType.Chest;

			if(item.getBodyPart() == ItemTemplate.SLOT_LEGS)
				return ArmorItemType.Legs;

			if(item.getBodyPart() == ItemTemplate.SLOT_GLOVES)
				return ArmorItemType.Gloves;

			if(item.getBodyPart() == ItemTemplate.SLOT_FEET)
				return ArmorItemType.Shoes;

			if(item.getTemplate().isCloak())
				return ArmorItemType.Cloak;

			if(item.getTemplate().isUnderwear())
				return ArmorItemType.Shirt;

			if(item.getTemplate().isBelt())
				return ArmorItemType.Belt;
		}
		if(item.getTemplate().isEnchantScroll())
			return EtcAuctionItemType.Enchant;

		if(item.getTemplate().isLifeStone())
			return EtcAuctionItemType.Life_stone;

		if(item.getTemplate().isAttributeCrystal() || item.getTemplate().isAttributeStone())
			return EtcAuctionItemType.Attribute;

		if(item.getTemplate().isCodexBook())
			return EtcAuctionItemType.Codex;

		if(item.getTemplate().isForgottenScroll())
			return EtcAuctionItemType.Forgotten_scroll;

		if(SoulCrystalHolder.getInstance().getCrystal(item.getItemId()) != null)
			return EtcAuctionItemType.SA_crystal;
		
		if(item.isPet())
			return PetItemType.Pet;

		if(item.getItemType() == EtcItemType.PET_COLLAR)
			return PetItemType.Pet;

		if(item.getTemplate().isForPet())
			return PetItemType.Gear;

		if(isBabyFoodOrShot(item.getItemId()))
			return PetItemType.Other;

		if(item.getItemType() == EtcItemType.POTION)
			return SuppliesItemType.Elixir;

		if(HennaHolder.getInstance().isHenna(item.getItemId()))
			return SuppliesItemType.Dye;

		if(item.getItemType() == EtcItemType.SCROLL)
			return SuppliesItemType.Scroll;

		if(item.getTemplate().isKeyMatherial())
			return SuppliesItemType.Key_Material;

		if(item.getTemplate().isRecipe())
			return SuppliesItemType.Recipe;

		if(item.getItemType() == EtcItemType.MATERIAL)
			return SuppliesItemType.Material;

		if(item.getItemType() instanceof EtcItemType)
			return SuppliesItemType.Miscellaneous;
		
		if(item.isWeapon())
		{
			if(item.getItemType() == WeaponType.SWORD)
				return WeaponItemType.Sword;

			if(item.getItemType() == WeaponType.ANCIENTSWORD)
				return WeaponItemType.Ancient_sword;

			if(item.getItemType() == WeaponType.BIGSWORD)
				return WeaponItemType.Big_sword;

			if(item.getItemType() == WeaponType.BLUNT)
				return WeaponItemType.Blunt;

			if(item.getItemType() == WeaponType.BIGBLUNT)
				return WeaponItemType.Big_blunt;

			if(item.getItemType() == WeaponType.DAGGER)
				return WeaponItemType.Dagger;

			if(item.getItemType() == WeaponType.DUALDAGGER)
				return WeaponItemType.Dual_dagger;

			if(item.getItemType() == WeaponType.BOW)
				return WeaponItemType.Bow;

			if(item.getItemType() == WeaponType.CROSSBOW)
				return WeaponItemType.Crossbow;

			if(item.getItemType() == WeaponType.POLE)
				return WeaponItemType.Pole;

			if(item.getItemType() == WeaponType.DUALFIST)
				return WeaponItemType.Fists;

			if(item.getItemType() == WeaponType.RAPIER)
				return WeaponItemType.Rapier;

			return WeaponItemType.Other;
		}
		
		if(item.getBodyPart() == ItemTemplate.SLOT_L_HAND)
		{
			if(item.getItemType() == ArmorType.SIGIL)
				return ArmorItemType.Sigil;
			return ArmorItemType.Shield;
		}
		
		return SuppliesItemType.Miscellaneous;
	}
	
	private static final int[] PET_FOOD_OR_SHOT =
	{
		6316,
		2515,
		4038,
		5168,
		5169,
		7582,
		9668,
		10425,
		6645,
		20332,
		20329,
		20326,
		10515,
		6647,
		6646,
		20334,
		20333,
		20331,
		20330,
		20329,
		20327,
		10517,
		10516
	};
	
	private boolean isBabyFoodOrShot(int id)
	{
		for(int i : PET_FOOD_OR_SHOT)
		{
			if(i == id)
				return true;
		}
		return false;
	}
	
	public static AuctionManager getInstance()
	{
		if(_instance == null)
			_instance = new AuctionManager();
		return _instance;
	}
}
