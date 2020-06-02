package l2s.gameserver.data.xml.holder;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2s.gameserver.Config;
import l2s.gameserver.model.ProductItem;
import l2s.gameserver.model.ProductItemComponent;

public class ProductHolder
{
	public static final String RECENT_PRDCT_LIST_VAR = "recentProductList";
	public static int MAX_ITEMS_IN_RECENT_LIST;
	public static int PAYMENT_ITEM_ID;

	private static Logger _log = LoggerFactory.getLogger(ProductHolder.class.getName());
	TreeMap<Integer, ProductItem> _itemsList;

	private static ProductHolder _instance = new ProductHolder();

	public static ProductHolder getInstance()
	{
		if(_instance == null)
			_instance = new ProductHolder();
		return _instance;
	}

	public void reload()
	{
		_instance = new ProductHolder();
	}

	private ProductHolder()
	{
		_itemsList = new TreeMap<Integer, ProductItem>();

		try
		{
			File file = new File(Config.DATAPACK_ROOT, "data/item-mall.xml");
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			Document doc1 = factory1.newDocumentBuilder().parse(file);
			int isStock = 0;

			for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if("list".equalsIgnoreCase(n1.getNodeName()))
					for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if("config".equalsIgnoreCase(d1.getNodeName()))
						{
							PAYMENT_ITEM_ID = d1.getAttributes().getNamedItem("payment_item_id") == null ? -1 : Integer.parseInt(d1.getAttributes().getNamedItem("payment_item_id").getNodeValue());
							MAX_ITEMS_IN_RECENT_LIST = d1.getAttributes().getNamedItem("recent_list_size") == null ? 5 : Integer.parseInt(d1.getAttributes().getNamedItem("recent_list_size").getNodeValue());
						}
						else if("product".equalsIgnoreCase(d1.getNodeName()))
						{
							Node onSaleNode = d1.getAttributes().getNamedItem("on_sale");
							Boolean onSale = onSaleNode != null && Boolean.parseBoolean(onSaleNode.getNodeValue());

							int productId = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());

							Node categoryNode = d1.getAttributes().getNamedItem("category");
							int category = categoryNode != null ? Integer.parseInt(categoryNode.getNodeValue()) : 5;

							if(category > 5 || category < 1)
							{
								category = 5;
							}
							Node priceNode = d1.getAttributes().getNamedItem("price");
							int price = priceNode != null ? Integer.parseInt(priceNode.getNodeValue()) : 0;

							Node isEventNode = d1.getAttributes().getNamedItem("is_event");
							Boolean isEvent = isEventNode != null && Boolean.parseBoolean(isEventNode.getNodeValue());

							Node isBestNode = d1.getAttributes().getNamedItem("is_best");
							Boolean isBest = isBestNode != null && Boolean.parseBoolean(isBestNode.getNodeValue());

							Node isNewNode = d1.getAttributes().getNamedItem("is_new");
							Boolean isNew = isNewNode != null && Boolean.parseBoolean(isNewNode.getNodeValue());

							Node maxStockNode = d1.getAttributes().getNamedItem("max_stock");
							int maxStock = maxStockNode != null ? Integer.parseInt(maxStockNode.getNodeValue()) : -1;

							if(maxStock > 0)
							{
								isStock = 0;

							}
							int tabId = getProductTabId(isEvent, isBest, isNew);

							Node startTimeNode = d1.getAttributes().getNamedItem("sale_start_date");
							long startTimeSale = startTimeNode != null ? getMillisecondsFromString(startTimeNode.getNodeValue()) : 0;

							Node endTimeNode = d1.getAttributes().getNamedItem("sale_end_date");
							long endTimeSale = endTimeNode != null ? getMillisecondsFromString(endTimeNode.getNodeValue()) : 0;

							ArrayList<ProductItemComponent> components = new ArrayList<ProductItemComponent>();
							ProductItem pr = new ProductItem(productId, category, price, tabId, startTimeSale, endTimeSale, onSale, isStock, maxStock);
							for(Node t1 = d1.getFirstChild(); t1 != null; t1 = t1.getNextSibling())
								if("component".equalsIgnoreCase(t1.getNodeName()))
								{
									int item_id = Integer.parseInt(t1.getAttributes().getNamedItem("item_id").getNodeValue());
									int count = Integer.parseInt(t1.getAttributes().getNamedItem("count").getNodeValue());
									ProductItemComponent component = new ProductItemComponent(item_id, count);
									components.add(component);
								}

							pr.setComponents(components);
							_itemsList.put(productId, pr);
						}

			_log.info(String.format("ProductItemTable: Loaded %d product item on sale.", _itemsList.size()));
		}
		catch(Exception e)
		{
			_log.warn("ProductItemTable: Lists could not be initialized.");
			e.printStackTrace();
		}
	}

	private static int getProductTabId(boolean isEvent, boolean isBest, boolean isNew)
	{
		//TODO: Заюзать isNew

		if(isEvent)
			return 1;

		if(isBest)
			return 2;


		if(isNew)
		{
			return 3;
		}

		return 0;
	}

	private static long getMillisecondsFromString(String datetime)
	{
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		try
		{ 
			Date time = df.parse(datetime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);

			return calendar.getTimeInMillis();
		} 
		catch(Exception e) 
		{ 
			e.printStackTrace();
		}

		return 0;
	}

	public Collection<ProductItem> getAllItems()
	{
		return _itemsList.values();
	}

	public Collection<ProductItem> getItemsOnSale()
	{
		TreeMap<Integer, ProductItem> products = new TreeMap<Integer, ProductItem>();
		for(ProductItem product : _itemsList.values())
			if(product.isOnSale() && (System.currentTimeMillis() >= product.getStartTimeSale()) && (System.currentTimeMillis() <= product.getEndTimeSale()))
				products.put(products.size(), product);
		return products.values();
	}

	public ProductItem getProduct(int id)
	{
		return _itemsList.get(id);
	}
}
