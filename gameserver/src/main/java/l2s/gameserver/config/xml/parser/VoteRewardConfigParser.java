package l2s.gameserver.config.xml.parser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import org.dom4j.Element;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.config.xml.holder.VoteRewardConfigHolder;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.scripts.Scripts;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 13.02.2019
 * Developed for L2-Scripts.com
 **/
public final class VoteRewardConfigParser extends AbstractParser<VoteRewardConfigHolder> {
	private static final VoteRewardConfigParser _instance = new VoteRewardConfigParser();

	public static VoteRewardConfigParser getInstance() {
		return _instance;
	}

	protected VoteRewardConfigParser() {
		super(VoteRewardConfigHolder.getInstance());
	}

	@Override
	public File getXMLPath() {
		return new File("config/votereward.xml");
	}

	@Override
	public String getDTDFileName() {
		return "votereward.dtd";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void readData(Element rootElement) throws Exception {
		for(Iterator<Element> configsIterator = rootElement.elementIterator("configs"); configsIterator.hasNext();) {
			Element configsElement = configsIterator.next();
			for(Iterator<Element> configIterator = configsElement.elementIterator("config"); configIterator.hasNext();) {
				Element configElement = configIterator.next();
				String configName = configElement.attributeValue("name");
				String configValue = configElement.attributeValue("value");
				if("reward_commands".equalsIgnoreCase(configName))
					VoteRewardConfigHolder.REWARD_COMMANDS = configValue.split(";");
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("vote_site"); iterator.hasNext();) {
			Element element = iterator.next();
			String impl = element.attributeValue("impl");
			Class<VoteRewardSite> voteRewardSiteClass = null;
			try {
				voteRewardSiteClass = (Class<VoteRewardSite>) Class.forName("l2s.gameserver.model.entity.votereward.impl." + impl + "Site");
			}
			catch(ClassNotFoundException e) {
				voteRewardSiteClass = (Class<VoteRewardSite>) Scripts.getInstance().getClasses().get("votereward." + impl + "Site");
			}

			if(voteRewardSiteClass == null) {
				info("Not found impl class: " + impl);
				continue;
			}

			boolean enabled = Boolean.parseBoolean(element.attributeValue("enabled"));

			Constructor<VoteRewardSite> constructor = voteRewardSiteClass.getConstructor(MultiValueSet.class);

			MultiValueSet<String> parameters = new MultiValueSet<String>();
			parameters.set("name", impl);
			parameters.set("enabled", enabled);
			parameters.set("run_delay", element.attributeValue("run_delay") != null ? Integer.parseInt(element.attributeValue("run_delay")) : 0);

			for(Iterator<Element> parameterIterator = element.elementIterator("parameter"); parameterIterator.hasNext();) {
				Element parameterElement = parameterIterator.next();
				parameters.set(parameterElement.attributeValue("name"), parameterElement.attributeValue("value"));
			}

			VoteRewardSite voteRewardSite = constructor.newInstance(parameters);

			for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();) {
				Element subElement = subIterator.next();
				if("rewards".equalsIgnoreCase(subElement.getName())) {
					voteRewardSite.addRewardList(RewardList.parseRewardList(getLogger(), subElement, RewardType.NOT_RATED_GROUPED, impl));
				}
			}
			getHolder().addVoteRewardSite(voteRewardSite);
		}
	}
}