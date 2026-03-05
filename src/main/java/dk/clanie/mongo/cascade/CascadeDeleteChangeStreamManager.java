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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonBinarySubType;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages MongoDB change stream watchers that perform cascade deletes.
 *
 * <p>On startup this bean scans the {@code MappingContext} for fields annotated
 * with {@link CascadeDelete} and starts one virtual-thread watcher per parent
 * collection. Each watcher persists its resume token in the
 * {@value #RESUME_TOKEN_COLLECTION} collection so that no delete events are
 * missed across server restarts.</p>
 *
 * <p>Activated only when {@link EnableChangeStreamMonitoring} is present on a
 * Spring {@code @Configuration} class.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class CascadeDeleteChangeStreamManager implements SmartLifecycle {

    /** MongoDB collection used to persist change-stream resume tokens. */
    static final String RESUME_TOKEN_COLLECTION = "cascade_delete_resume_tokens";

    private static final long WATCHER_RETRY_DELAY_MS = 5_000L;

    private final MongoTemplate mongoTemplate;
    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;

    private volatile boolean running = false;
    private final List<Watcher> watchers = new ArrayList<>();


    // -------------------------------------------------------------------------
    // SmartLifecycle
    // -------------------------------------------------------------------------

    @Override
    public void start() {
        if (running) return;
        running = true;

        Map<Class<?>, List<CascadeRelation>> cascadeMap = buildCascadeMap();

        if (cascadeMap.isEmpty()) {
            log.debug("No @CascadeDelete annotations found – change stream monitoring is idle.");
            return;
        }

        cascadeMap.forEach((parentClass, relations) -> {
            MongoPersistentEntity<?> parentEntity;
            try {
                parentEntity = mappingContext.getRequiredPersistentEntity(parentClass);
            } catch (MappingException e) {
                log.warn("Parent class {} is not in the MappingContext; cascade delete will not be set up for it. "
                        + "Make sure the class is used by a Spring Data MongoDB repository.",
                        parentClass.getName());
                return;
            }
            String collectionName = parentEntity.getCollection();
            Watcher watcher = new Watcher(collectionName, relations);
            watchers.add(watcher);
            Thread.ofVirtual()
                    .name("cascade-delete-watcher-" + collectionName)
                    .start(watcher::run);
        });
    }

    @Override
    public void stop() {
        running = false;
        watchers.forEach(Watcher::stop);
        watchers.clear();
    }

    @Override
    public boolean isRunning() {
        return running;
    }


    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Scans the mapping context for fields annotated with {@link CascadeDelete}
     * and builds a map from parent class → list of cascade relations.
     */
    private Map<Class<?>, List<CascadeRelation>> buildCascadeMap() {
        Map<Class<?>, List<CascadeRelation>> result = new HashMap<>();
        for (MongoPersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
            entity.doWithProperties((MongoPersistentProperty property) -> {
                CascadeDelete annotation = property.findAnnotation(CascadeDelete.class);
                if (annotation != null) {
                    Class<?> parentClass = resolveParentClass(annotation);
                    result.computeIfAbsent(parentClass, k -> new ArrayList<>())
                          .add(new CascadeRelation(entity.getType(), property.getFieldName()));
                }
            });
        }
        return result;
    }

    /**
     * Resolves the parent class from a {@link CascadeDelete} annotation,
     * supporting both {@code @CascadeDelete(Portfolio.class)} (shorthand via {@code value})
     * and {@code @CascadeDelete(parent = Portfolio.class)}.
     *
     * @throws IllegalArgumentException if neither {@code value} nor {@code parent} is specified,
     *                                  or if both are specified with different values
     */
    private static Class<?> resolveParentClass(CascadeDelete annotation) {
        Class<?> value = annotation.value();
        Class<?> parent = annotation.parent();
        boolean valueSet = value != void.class;
        boolean parentSet = parent != void.class;
        if (valueSet && parentSet && value != parent) {
            throw new IllegalArgumentException(
                    "@CascadeDelete has conflicting 'value' and 'parent' attributes.");
        }
        if (!valueSet && !parentSet) {
            throw new IllegalArgumentException(
                    "@CascadeDelete requires either 'value' or 'parent' to be specified.");
        }
        return valueSet ? value : parent;
    }

    /** Loads the persisted resume token for the given collection, or {@code null} if none. */
    private BsonDocument loadResumeToken(String collectionName) {
        Query query = Query.query(Criteria.where("_id").is(collectionName));
        Document doc = mongoTemplate.findOne(query, Document.class, RESUME_TOKEN_COLLECTION);
        if (doc == null) return null;
        String tokenJson = doc.getString("token");
        return tokenJson != null ? BsonDocument.parse(tokenJson) : null;
    }

    /** Persists the resume token for the given collection so it survives restarts. */
    private void saveResumeToken(String collectionName, BsonDocument resumeToken) {
        if (resumeToken == null) return;
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(collectionName)),
                Update.update("token", resumeToken.toJson()),
                RESUME_TOKEN_COLLECTION);
    }

    /**
     * Converts a BSON {@code _id} value to an equivalent Java object that can be
     * used as a {@link Criteria} value when querying child collections.
     */
    private static Object extractId(BsonDocument documentKey) {
        BsonValue bsonId = documentKey.get("_id");
        if (bsonId == null) return null;

        return switch (bsonId.getBsonType()) {
            case STRING -> ((BsonString) bsonId).getValue();
            case OBJECT_ID -> ((BsonObjectId) bsonId).getValue();
            case BINARY -> {
                var binary = bsonId.asBinary();
                byte subType = binary.getType();
                byte[] data = binary.getData();
                if (data.length == 16) {
                    if (subType == BsonBinarySubType.UUID_STANDARD.getValue()) {
                        // Standard big-endian UUID bytes
                        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                        yield new UUID(byteBuffer.getLong(), byteBuffer.getLong());
                    } else if (subType == BsonBinarySubType.UUID_LEGACY.getValue()) {
                        // Java legacy: each long's bytes are reversed compared to big-endian
                        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                        yield new UUID(Long.reverseBytes(byteBuffer.getLong()), Long.reverseBytes(byteBuffer.getLong()));
                    }
                }
                yield data;
            }
            case INT32 -> ((BsonInt32) bsonId).getValue();
            case INT64 -> ((BsonInt64) bsonId).getValue();
            default -> bsonId.toString();
        };
    }


    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** Describes a child collection field that references a parent collection. */
    private record CascadeRelation(Class<?> childClass, String fieldName) {}

    /**
     * Watches a single parent collection for delete events and cascades them to
     * all registered child collections.
     */
    private class Watcher {

        private final String collectionName;
        private final List<CascadeRelation> relations;
        private volatile MongoCursor<ChangeStreamDocument<Document>> cursor;

        Watcher(String collectionName, List<CascadeRelation> relations) {
            this.collectionName = collectionName;
            this.relations = relations;
        }

        void run() {
            while (running) {
                try {
                    watchOnce();
                } catch (Exception e) {
                    if (running) {
                        log.error("Change stream watcher for '{}' failed – restarting in 5 s.", collectionName, e);
                        try {
                            Thread.sleep(WATCHER_RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        }

        /**
         * Opens a single change-stream cursor for this collection, processes events
         * until the cursor is exhausted or the manager is stopped, then closes it.
         */
        private void watchOnce() {
            BsonDocument resumeToken = loadResumeToken(collectionName);

            List<Bson> pipeline = List.of(Aggregates.match(Filters.eq("operationType", "delete")));

            ChangeStreamIterable<Document> changeStream =
                    mongoTemplate.getCollection(collectionName).watch(pipeline);

            if (resumeToken != null) {
                changeStream.resumeAfter(resumeToken);
            }

            cursor = changeStream.iterator();
            try {
                while (running && cursor.hasNext()) {
                    ChangeStreamDocument<Document> event = cursor.next();
                    handleDeleteEvent(event);
                    saveResumeToken(collectionName, event.getResumeToken());
                }
            } finally {
                closeCursor();
            }
        }

        void stop() {
            closeCursor();
        }

        private void closeCursor() {
            MongoCursor<ChangeStreamDocument<Document>> c = cursor;
            cursor = null;
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    log.debug("Error closing change stream cursor for '{}'.", collectionName, e);
                }
            }
        }

        private void handleDeleteEvent(ChangeStreamDocument<Document> event) {
            BsonDocument documentKey = event.getDocumentKey();
            if (documentKey == null) return;

            Object idValue = extractId(documentKey);
            if (idValue == null) return;

            for (CascadeRelation relation : relations) {
                try {
                    Query query = Query.query(Criteria.where(relation.fieldName()).is(idValue));
                    long deleted = mongoTemplate.remove(query, relation.childClass()).getDeletedCount();
                    if (deleted > 0) {
                        log.debug("Cascade-deleted {} document(s) from '{}' where {} = {}.",
                                deleted, relation.childClass().getSimpleName(), relation.fieldName(), idValue);
                    }
                } catch (Exception e) {
                    log.error("Failed to cascade delete from '{}' where {} = {}.",
                            relation.childClass().getSimpleName(), relation.fieldName(), idValue, e);
                }
            }
        }
    }

}
