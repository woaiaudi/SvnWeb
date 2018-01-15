package com.ztsq.codelines;

import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.ztsq.codelines.controller.HelloController;
import com.ztsq.codelines.controller.ProjectController;
import com.ztsq.codelines.controller.SvnController;
import com.ztsq.codelines.db.SVNAuth;
import com.ztsq.codelines.db.SVNChangePathsLog;
import com.ztsq.codelines.db.SVNCommitLog;
import com.ztsq.codelines.db.SVNProject;

/**
 * Created by zyuework on 2018/1/8.
 */
public class JFinalConfig extends com.jfinal.config.JFinalConfig {
    public void configConstant(Constants constants) {
        constants.setDevMode(true);
    }

    public void configRoute(Routes routes) {
        routes.add("/Hello", HelloController.class);
        routes.add("/svnlog/code", SvnController.class);
        routes.add("/svnlog/project", ProjectController.class);
    }

    public void configEngine(Engine engine) {

    }

    public void configPlugin(Plugins plugins) {
        DruidPlugin dp = new DruidPlugin("jdbc:mysql://192.168.2.156/ztmalaysiadb", "ztsq", "ztsq!@#123");
        plugins.add(dp);
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dp); plugins.add(arp);
        arp.addMapping("svnt_auth", SVNAuth.class);
        arp.addMapping("svnt_commit_log", SVNCommitLog.class);
        arp.addMapping("svnt_changed_paths_log", SVNChangePathsLog.class);
        arp.addMapping("svnt_project", SVNProject.class);

    }

    public void configInterceptor(Interceptors interceptors) {

    }

    public void configHandler(Handlers handlers) {

    }
}
