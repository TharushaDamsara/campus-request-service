package com.campusflow.requestservice.util;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.annotation.Id;

/**
 * Generates sequential request IDs in the format REQ-XXXX.
 */
@Component
public class RequestIdGenerator {

    private final MongoOperations mongoOperations;

    public RequestIdGenerator(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public String generateRequestId() {
        Counter counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is("request_id")),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Counter.class
        );

        long seq = counter != null ? counter.getSeq() : 1;
        return String.format("REQ-%04d", seq);
    }

    @Document(collection = "counters")
    static class Counter {
        @Id
        private String id;
        private long seq;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public long getSeq() { return seq; }
        public void setSeq(long seq) { this.seq = seq; }
    }
}
