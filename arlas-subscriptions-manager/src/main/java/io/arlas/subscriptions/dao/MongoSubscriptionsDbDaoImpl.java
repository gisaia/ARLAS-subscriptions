package io.arlas.subscriptions.dao;

import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;

import java.util.List;
import java.util.UUID;

public class MongoSubscriptionsDbDaoImpl implements SubscriptionsDbDAO {

    public MongoSubscriptionsDbDaoImpl(ArlasSubscriptionsConfiguration configuration) throws ArlasSubscriptionsException {

    }

    @Override
    public void initSubscriptionsDatabase() throws ArlasSubscriptionsException {

    }

    @Override
    public UserSubscription getUserSubscription(String id) throws ArlasSubscriptionsException {
        return null;
    }

    @Override
    public List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {
        return null;
    }

    @Override
    public UUID postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        return null;
    }
}
