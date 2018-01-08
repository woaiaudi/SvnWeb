package com.ztsq.codelines.db;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by zyuework on 2018/1/5.
 */
public class SVNCommitLog extends Model<SVNCommitLog> {
    public static final SVNCommitLog dao = new SVNCommitLog().dao();
}
