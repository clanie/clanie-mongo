/*
 * Copyright (C) 2026, Claus Nielsen, clausn999@gmail.com
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

import org.springframework.context.annotation.Import;

/**
 * Enables MongoDB change stream monitoring for cascade delete support.
 *
 * <p>Place this annotation on a Spring {@code @Configuration} class to activate
 * change stream monitoring. This is required for {@link CascadeDelete} to function.</p>
 *
 * <p>Change stream monitoring is <strong>not</strong> enabled by default; this
 * annotation must be explicitly added to opt in.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;Configuration
 * &#64;EnableChangeStreamMonitoring
 * public class MyMongoConfig {
 * }
 * </pre>
 *
 * @see CascadeDelete
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ChangeStreamMonitoringConfiguration.class)
public @interface EnableChangeStreamMonitoring {
}
