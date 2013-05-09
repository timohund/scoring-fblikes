package org.apache.nutch.scoring.facebook;

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
 * This scoring filter is attaching the amount of facebook
 * likes to a document.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class LikesScoringFilter implements ScoringFilter {

    private final static Logger LOG = LoggerFactory.getLogger(LikesScoringFilter.class);

    private final static Set<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

    private Configuration conf;

    private LikesService likesService;

    static {
        FIELDS.add(WebPage.Field.METADATA);
        FIELDS.add(WebPage.Field.SCORE);
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        if(this.likesService == null) {
            String appKey       = conf.get("db.score.facebook.appKey");
            String appSecret    = conf.get("db.score.facebook.appSecret");
            this.likesService   = new LikesService(appKey,appSecret);
        }
    }

    @Override
    public void injectedScore(String s, org.apache.nutch.storage.WebPage webPage) throws ScoringFilterException {
    }

    @Override
    public void initialScore(String s, org.apache.nutch.storage.WebPage webPage) throws ScoringFilterException {
    }

    @Override
    public float generatorSortValue(String s, org.apache.nutch.storage.WebPage webPage, float v) throws ScoringFilterException {
        return 0;
    }

    @Override
    public void distributeScoreToOutlinks(String s, org.apache.nutch.storage.WebPage webPage, Collection<ScoreDatum> scoreDatums, int i) throws ScoringFilterException {
    }

    @Override
    public void updateScore(String url, org.apache.nutch.storage.WebPage webPage, List<ScoreDatum> scoreDatums) throws ScoringFilterException {
        try {
            float oldScore = webPage.getScore();
            LikesResult likes = likesService.getLikes(url);
            ByteBuffer b = ByteBuffer.allocate(4);
            int likeCount = likes.getLikeCount();
            b.putInt(likeCount);
            LOG.info("Current score is "+oldScore);
            LOG.info("Determined " + likeCount + " likes for document with url " + url);
            webPage.putToMetadata(new Utf8("fb_likes"), b);
        } catch (UnexpectedLikeResultException e) {
            LOG.info("Likes service did not retrieve single like result for "+url);
        } catch (Exception e) {
            LOG.info("Error during score update " + e.getMessage());
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
