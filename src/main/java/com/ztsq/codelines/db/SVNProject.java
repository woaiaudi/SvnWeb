package com.ztsq.codelines.db;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by zyuework on 2018/1/15.
 */
public class SVNProject extends Model<SVNProject> {
    public static final SVNProject dao = new SVNProject().dao();

}