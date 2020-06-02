package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.templates.item.RecipeTemplate;
import l2s.gameserver.templates.item.RecipeTemplate.RecipeComponent;

/**
 * @author Bonux
**/
public final class RecipeParser extends AbstractParser<RecipeHolder>
{
	private static final RecipeParser _instance = new RecipeParser();

	public static RecipeParser getInstance()
	{
		return _instance;
	}

	private RecipeParser()
	{
		super(RecipeHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/recipes.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "recipes.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			final int id = Integer.parseInt(element.attributeValue("id"));
			final int level = Integer.parseInt(element.attributeValue("level"));
			final int mpConsume = element.attributeValue("mp_consume") == null ? 0 : Integer.parseInt(element.attributeValue("mp_consume"));
			final int successRate = Integer.parseInt(element.attributeValue("success_rate"));
			final int itemId = Integer.parseInt(element.attributeValue("item_id"));
			final boolean isCommon = element.attributeValue("is_common") == null ? false : Boolean.parseBoolean(element.attributeValue("is_common"));

			RecipeTemplate recipe = new RecipeTemplate(id, level, mpConsume, successRate, itemId, isCommon);
			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();

				if("materials".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if("item".equalsIgnoreCase(e.getName()))
						{
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							recipe.addMaterial(new RecipeComponent(item_id, count));
						}
					}
				}
				else if("products".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if("item".equalsIgnoreCase(e.getName()))
						{
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							int chance = Integer.parseInt(e.attributeValue("chance"));
							recipe.addProduct(new RecipeComponent(item_id, count, chance));
						}
					}
				}
				else if("npc_fee".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
					{
						if("item".equalsIgnoreCase(e.getName()))
						{
							int item_id = Integer.parseInt(e.attributeValue("id"));
							long count = Long.parseLong(e.attributeValue("count"));
							recipe.addNpcFee(new RecipeComponent(item_id, count));
						}
					}
				}
			}
			getHolder().addRecipe(recipe);
		}
	}
}