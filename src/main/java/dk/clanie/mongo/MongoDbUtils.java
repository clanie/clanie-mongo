/**
 * Copyright (C) 2025, Claus Nielsen, clausn999@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
