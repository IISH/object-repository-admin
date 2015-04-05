/*
 * Copyright (C) 2010-2012, International Institute of Social History
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.socialhistoryservices.security;

import com.mongodb.*;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth2 provider of tokens. Made for MongoDB
 * <p/>
 * Field expirationTokenStore will contain a token and expiration datestamp
 */
final public class MongoTokenStore implements TokenStore {

    private static final String DATABASE = "iaa";
    private static final String OAUTH_ACCESS_TOKEN = "oauth_access_token";
    private static final String OAUTH_REFRESH_TOKEN = "oauth_refresh_token";
    private final ConcurrentHashMap<String, OAuth2AccessToken> accessTokenStore = new ConcurrentHashMap<String, OAuth2AccessToken>();
    private final ConcurrentHashMap<String, OAuth2Authentication> authenticationTokenStore = new ConcurrentHashMap<String, OAuth2Authentication>();
    private final ConcurrentHashMap<String, Long> expirationTokenStore = new ConcurrentHashMap<String, Long>();
    private long sliderExpiration = 30000; // Thirty seconds
    private String database;
    private Mongo mongo;


    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        // insert into oauth_access_token (token_id, token, authentication_id, authentication, refresh_token) values (?, ?, ?, ?, ?)
        String refreshToken = null;
        if (token.getRefreshToken() != null) {
            refreshToken = token.getRefreshToken().getValue();
        }
        final String username = (authentication.getUserAuthentication() == null) ? null : authentication.getUserAuthentication().getName();
        final BasicDBObject document = new BasicDBObject();
        document.put("token_id", encodePassword(token.getValue()));
        document.put("token", serialize(token));
        document.put("authentication_id", null);
        document.put("authentication", serialize(authentication));
        document.put("refresh_token", encodePassword(refreshToken));
        document.put("username", username);
        document.put("clientId", authentication.getOAuth2Request().getClientId());
        final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
        collection.insert(document);
    }

    public OAuth2AccessToken readAccessToken(String tokenValue) {

        OAuth2AccessToken accessToken = (isFresh(tokenValue))
                ? this.accessTokenStore.get(tokenValue)
                : null;
        if (accessToken == null) {
            // select token_id, token from oauth_access_token where token_id = ?
            final BasicDBObject query = new BasicDBObject("token_id", encodePassword(tokenValue));
            final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
            DBObject document = collection.findOne(query);
            if (document != null) {
                accessToken = deserialize((byte[]) document.get("token"));
                this.accessTokenStore.put(tokenValue, accessToken);
                expiration(tokenValue);
            }
        }
        return accessToken;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {
        final BasicDBObject query = new BasicDBObject("token_id", encodePassword(oAuth2AccessToken.getValue()));
        final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
        collection.remove(query);
        this.accessTokenStore.remove(oAuth2AccessToken.getValue());
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication oAuth2Authentication) {
        final BasicDBObject document = new BasicDBObject();
        document.put("token_id", encodePassword(oAuth2RefreshToken.getValue()));
        document.put("token", serialize(oAuth2RefreshToken));
        document.put("authentication", serialize(oAuth2Authentication));
        final DBCollection collection = getCollection(OAUTH_REFRESH_TOKEN);
        collection.insert(document);
    }

    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue(), OAUTH_ACCESS_TOKEN);
    }


    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthentication(token.getValue(), OAUTH_REFRESH_TOKEN);
    }

    private OAuth2Authentication readAuthentication(String tokenValue, String collectionName) {
        OAuth2Authentication authentication = (isFresh(tokenValue))
                ? this.authenticationTokenStore.get(tokenValue)
                : null;
        if (authentication == null) {
            // select token_id, authentication from oauth_access_token where token_id = ?
            final BasicDBObject query = new BasicDBObject();
            query.put("token_id", encodePassword(tokenValue));
            final DBCollection collection = getCollection(collectionName);
            final DBObject document = collection.findOne(query);
            if (document != null) {
                authentication = deserialize((byte[]) document.get("authentication"));
                this.authenticationTokenStore.put(tokenValue, authentication);
                expiration(tokenValue);
            }
        }
        return authentication;
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication authentication = null;
        final BasicDBObject query = new BasicDBObject("token_id", encodePassword(token));
        final DBCollection collection = getCollection(OAUTH_REFRESH_TOKEN);
        final DBObject document = collection.findOne(query);
        if (document != null) {
            authentication = deserialize((byte[]) document.get("authentication"));
        }
        return authentication;
    }

    public ExpiringOAuth2RefreshToken readRefreshToken(String token) {

        // select token_id, token from oauth_refresh_token where token_id = ?
        ExpiringOAuth2RefreshToken refreshToken = null;
        final BasicDBObject query = new BasicDBObject("token_id", encodePassword(token));
        final DBCollection collection = getCollection(OAUTH_REFRESH_TOKEN);
        final DBObject document = collection.findOne(query);
        if (document != null) {
            refreshToken = deserialize((byte[]) document.get("token"));
        }
        return refreshToken;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        final BasicDBObject query = new BasicDBObject("token_id", encodePassword(oAuth2RefreshToken.getValue()));
        final DBCollection collection = getCollection(OAUTH_REFRESH_TOKEN);
        collection.remove(query);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        final BasicDBObject query = new BasicDBObject("refresh_token", encodePassword(oAuth2RefreshToken.getValue()));
        final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
        collection.remove(query);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication oAuth2Authentication) {
        final String clientId = oAuth2Authentication.getOAuth2Request().getClientId();
        final String username = oAuth2Authentication.getUserAuthentication().getName();
        final Collection<OAuth2AccessToken> tokens = findTokens(clientId, username);
        return (tokens.size() == 0) ? null : tokens.iterator().next();
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String username) {
        return findTokens(clientId, username);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return findTokens(clientId, null);
    }

    private Collection<OAuth2AccessToken> findTokens(String clientId, String username) {
        final QueryBuilder qb = QueryBuilder.start("clientId").is(clientId);
        if (username != null)
            qb.and("username").is(username);
        final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
        final DBCursor cursor = collection.find(qb.get());
        Collection<OAuth2AccessToken> list = new ArrayList<OAuth2AccessToken>(cursor.size());
        while (cursor.hasNext()) {
            list.add((OAuth2AccessToken) deserialize((byte[]) cursor.next().get("token")));
        }
        return list;
    }

    public void updateAuthentication(OAuth2AccessToken token, OAuth2Authentication authentication) {
        storeAccessToken(token, authentication);
    }

    /**
     * selectKeys
     * <p/>
     * returns all keys that belong to a user
     *
     * @param username The identifier of the principal
     * @return The OAuth2AccessToken associated with this principal
     */
    public OAuth2AccessToken selectKeys(String username) {
        final BasicDBObject query = new BasicDBObject("username", username);
        final DBCollection collection = getCollection(OAUTH_ACCESS_TOKEN);
        DBObject document = collection.findOne(query);
        return (document == null)
                ? null
                : (OAuth2AccessToken) deserialize((byte[]) document.get("token"));
    }

    /**
     * isFresh
     * <p/>
     * Determines if we can use the cache rather than hit the database...
     *
     * @param tokenValue the token to look for
     * @return Fresh when the token is requested within the sliding expiration span
     */
    private boolean isFresh(String tokenValue) {

        long expiration = (expirationTokenStore.containsKey(tokenValue))
                ? expirationTokenStore.get(tokenValue)
                : 0;
        long time = new Date().getTime();
        if (expiration > time) {
            this.expirationTokenStore.put(tokenValue, time + sliderExpiration);
            return true;
        }
        expirationTokenStore.remove(tokenValue);
        accessTokenStore.remove(tokenValue);
        authenticationTokenStore.remove(tokenValue);
        return false;
    }

    public void setSliderExpiration(long sliderExpiration) {
        this.sliderExpiration = sliderExpiration;
    }

    private void expiration(String tokenValue) {
        this.expirationTokenStore.put(tokenValue, new Date().getTime() + sliderExpiration);
    }

    private DBCollection getCollection(String collection) {

        final DB db = mongo.getDB(getDatabase());
        return db.getCollection(collection);
    }

    public String getDatabase() {
        if (database == null)
            database = DATABASE;
        return database;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setDatabase(String database) {
        this.database = database;
        final DBCollection c = getCollection(OAUTH_ACCESS_TOKEN);
        DBObject keys = new BasicDBList();
        keys.put("username", 1);
        keys.put("token_id", 1);
        c.createIndex(keys);
    }

    private static byte[] serialize(Object state) {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(state);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // eat it
                }
            }
        }
    }

    private static <T> T deserialize(byte[] byteArray) {
        ObjectInputStream oip = null;
        try {
            oip = new ObjectInputStream(new ByteArrayInputStream(byteArray));
            return (T) oip.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (oip != null) {
                try {
                    oip.close();
                } catch (IOException e) {
                    // eat it
                }
            }
        }
    }

    private static String encodePassword(String rawPass) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(rawPass);
    }
}