package io.micronaut.data.mongodb.transaction;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.micronaut.transaction.SynchronousTransactionManager;

public interface MongoSynchronousTransactionManager extends SynchronousTransactionManager<ClientSession> {

    MongoClient getClient();

}
