package io.arlas.subscriptions.dao;


import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.UserSubscriptionMapper;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoUserSubscriptionDAOImpl implements UserSubscriptionDAO {

    private ArlasSubscriptionManagerConfiguration configuration;
    private MongoDatabase mongoDatabase;

    public MongoUserSubscriptionDAOImpl(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged) throws ArlasSubscriptionsException {
            this.mongoDatabase=mongoDBManaged.
                    mongoClient.
                    getDatabase(configuration.getMongoDBConnection().database);
            this.configuration =configuration;
            this.initSubscriptionsDatabase();
    }

    @Override
    public  List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {

        List<UserSubscription> userSubscriptionFind = new ArrayList<>();
        try(MongoCursor<Document> userSubscriptions = this.mongoDatabase.getCollection("arlas-subscription").find().iterator()) {
            while (userSubscriptions.hasNext()) {
                final Document userSubscription = userSubscriptions.next();
                userSubscriptionFind.add(UserSubscriptionMapper.map(userSubscription));
            }
        }
        return userSubscriptionFind;
    }

    @Override
    public UUID postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        return null;
    }

    private void initSubscriptionsDatabase() throws ArlasSubscriptionsException {
        boolean collectionExists = this.mongoDatabase.listCollectionNames()
                .into(new ArrayList<>()).contains("arlas-subscription");
        if(!collectionExists){
            this.mongoDatabase.createCollection("arlas-subscription");
        }
    }
}
