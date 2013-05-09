package org.apache.nutch.scoring.facebook;


import com.restfb.Facebook;

class LikesResult {
    @Facebook
    int like_count = 0;

    public int getLikeCount() {
        return like_count;
    }

    public void setLikeCount(int like_count) {
        this.like_count = like_count;
    }
}