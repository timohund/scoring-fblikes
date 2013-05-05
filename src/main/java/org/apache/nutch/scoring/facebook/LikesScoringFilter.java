package org.apache.nutch.scoring.facebook;

import facebook4j.*;
import facebook4j.auth.AccessToken;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.scoring.ScoreDatum;
import org.apache.nutch.scoring.ScoringFilter;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class LikesScoringFilter implements ScoringFilter {

    private final static Logger LOG = LoggerFactory.getLogger(LikesScoringFilter.class);

    private final static Set<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

    private Configuration conf;

    private Facebook facebook;

    static {
        FIELDS.add(WebPage.Field.METADATA);
        FIELDS.add(WebPage.Field.SCORE);
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        if(this.facebook == null) {
            String appId        = conf.get("db.score.facebook.appId");
            String appSecret    = conf.get("db.score.facebook.appSecret");
            String permissions  = conf.get("db.score.facebook.commaSeparatedPermissions");
            String accessToken  = conf.get("db.score.facebook.accessToken");

            facebook = new FacebookFactory().getInstance();
            facebook.setOAuthAppId(appId, appSecret);
            facebook.setOAuthPermissions(permissions);
            facebook.setOAuthAccessToken(new AccessToken(accessToken, null));
        }
    }

    @Override
    public void injectedScore(String s, org.apache.nutch.storage.WebPage webPage) throws ScoringFilterException {}

    @Override
    public void initialScore(String s, org.apache.nutch.storage.WebPage webPage) throws ScoringFilterException {}

    @Override
    public float generatorSortValue(String s, org.apache.nutch.storage.WebPage webPage, float v) throws ScoringFilterException {
        return 0;
    }

    @Override
    public void distributeScoreToOutlinks(String s, org.apache.nutch.storage.WebPage webPage, Collection<ScoreDatum> scoreDatums, int i) throws ScoringFilterException {}

    @Override
    public void updateScore(String s, org.apache.nutch.storage.WebPage webPage, List<ScoreDatum> scoreDatums) throws ScoringFilterException {
        float oldScore = webPage.getScore();
        String url = webPage.getReprUrl().toString();
        try {
            ResponseList<Like> likes = facebook.getLinkLikes(url);
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(likes.size());

            LOG.info("Determined "+likes.size()+" likes for document with url "+url);
            webPage.putToMetadata(new Utf8("fb_likes"),b);
        } catch (FacebookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public float indexerScore(String s, NutchDocument entries, org.apache.nutch.storage.WebPage webPage, float v) throws ScoringFilterException {
        return 0;
    }

    @Override
    public Collection<org.apache.nutch.storage.WebPage.Field> getFields() {
        return null;
    }
}
