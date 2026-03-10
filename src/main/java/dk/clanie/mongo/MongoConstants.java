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

import static dk.clanie.core.Utils.asUuid;

import java.time.LocalDate;
import java.util.UUID;

public final class MongoConstants {


	private MongoConstants() {
		// Not meant to be instantiated
	}


	public static final UUID ADMIN_TENANT_ID = asUuid("00000000-0000-0000-0000-000000000000");


	/**
	 * A LocalDate far in the future.
	 * <p>
	 * It is used to represent the end of a validity period that has no end date.
	 * </p>
	 * Use this in favor of LocalDate.MAX because LocalDate.MAX is out of range
	 * for the Date class, so the converter used for MongoDB will fail. 
	 */
	public static final LocalDate MAX_LOCAL_DATE = LocalDate.of(9999, 12, 31);


	/**
	 * A LocalDate far in the past.
	 * <p>
	 * It is used to represent the end of a validity period that has no start date.
	 * </p>
	 * Use this in favor of LocalDate.MIN because LocalDate.MIN is out of range
	 * for the Date class, so the converter used for MongoDB will fail. 
	 */
	public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(0001, 01, 01);


}
