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
package dk.clanie.mongo.cascade;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a foreign key reference that triggers cascade deletion.
 *
 * <p>When the {@link #parent()} document is deleted, all documents containing this
 * field with the matching parent ID will also be deleted.</p>
 *
 * <p><strong>Requires</strong> MongoDB change stream monitoring to be enabled by
 * placing {@link EnableChangeStreamMonitoring} on a Spring {@code @Configuration}
 * class in the application.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;Indexed
 * &#64;CascadeDelete(parent = Portfolio.class)
 * String portfolioId;
 * </pre>
 *
 * @see EnableChangeStreamMonitoring
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CascadeDelete {

    /**
     * The parent document class whose deletions should be cascaded to the owning document.
     */
    Class<?> parent();

}
