package com.ztsq.codelines.db;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by zyuework on 2018/1/5.
 */
public class SVNChangePathsLog extends Model<SVNChangePathsLog> {
    public static final SVNChangePathsLog dao = new SVNChangePathsLog().dao();
}