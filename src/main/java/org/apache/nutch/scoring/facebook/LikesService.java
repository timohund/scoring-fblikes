package org.apache.nutch.scoring.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 09.05.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class LikesService {

    private FacebookClient facebook;

    private final static Logger LOG = LoggerFactory.getLogger(LikesScoringFilter.class);

    private String appKey;

    private String appSecret;

    public LikesService(String appKey, String appSecret) {

        FacebookClient.AccessToken accessToken =
            new DefaultFacebookClient().obtainAppAccessToken(
                appKey,
                appSecret
        );

        LOG.info("Getting new access token "+accessToken.getAccessToken());
        facebook = new DefaultFacebookClient(accessToken.getAccessToken());
    }

    public LikesResult getLikes(String url) throws UnexpectedLikeResultException {
        String query = "SELECT url, " +
                "normalized_url, " +
                "share_count, " +
                "like_count, " +
                "comment_count, " +
                "total_count, " +
                "commentsbox_count, " +
                "comments_fbid, " +
                "click_count FROM link_stat WHERE url='" + url + "'";
        List<LikesResult> results = facebook.executeFqlQuery(query, LikesResult.class);

        if(results.size() != 1) {
            String message ="Facebook did not return exactly one like resulkt "+results.size();
            throw new UnexpectedLikeResultException(message);
        }

        return results.get(0);
    }
}
