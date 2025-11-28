/*
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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import dk.clanie.mongo.convert.DateToZonedDateTimeConverter;
import dk.clanie.mongo.convert.ZonedDateTimeToDateConverter;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Mongo.
 */
@AutoConfiguration
public class ClanieMongoAutoConfiguration {


	@Bean
	DateToZonedDateTimeConverter dateToZonedDateTimeConverter() {
		return new DateToZonedDateTimeConverter();
	}


	@Bean
	ZonedDateTimeToDateConverter zonedDateTimeToDateConverter() {
		return new ZonedDateTimeToDateConverter();
	}


}
