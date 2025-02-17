package dk.clanie.mongo;

import static dk.clanie.core.Utils.mapList;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;

public class MongoDbUtils {


	private MongoDbUtils() {
		// Not meant to be instantiated.
	}

	
	/**
	 * Ads criteria that property contains given string ignoring case.
	 * 
	 * If given string is null no criteria is added.
	 */
	public static Criteria critiaAndContains(Criteria criteria, String property, String string) {
		if (hasText(string)) {
	         String regex = Pattern.quote(string);
	         criteria = criteria.and(property).regex(regex, "i");
		}
		return criteria;
	}


	/**
	 * Ads criteria that at least one of given properties contains given string ignoring case.
	 * 
	 * If given string is null no criteria is added.
	 */
	public static Criteria critiaAndContains(Criteria criteria, List<String> properties, String string) {
		if (hasText(string)) {
	         String regex = Pattern.quote(string);
	         criteria = criteria.orOperator(mapList(properties, property -> where(property).regex(regex, "i")));
		}
		return criteria;
	}


	/**
	 * Ads criteria that given property is NOT true if negativeFilter IS true.
	 * 
	 * This is typically for excluding something with a deleted, failed or broken flag or similar.
	 */
	public static Criteria critiaAndNotTrue(Criteria criteria, String property, Boolean negativeFilter) {
		if (isTrue(negativeFilter)) {
			criteria = criteria.and(property).ne(true);
		}
		return criteria;
	}


}
