package com.ztsq.codelines.controller;

import com.jfinal.core.Controller;
import com.ztsq.codelines.db.SVNProject;
import com.ztsq.codelines.utils.RespBaseBean;
import com.ztsq.codelines.utils.ZTSVNConstanst;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyuework on 2018/1/15.
 */
public class ProjectController extends Controller {

    private static Logger logger = Logger.getLogger(ProjectController.class);

    public void all(){
        String searchText = (null == getPara("searchText","")?"":getPara("searchText",""));
        List<SVNProject> allProject = SVNProject.dao.find("SELECT * FROM svnt_project WHERE name like '%"+searchText+"%' OR path like '%"+searchText+"%' ;");
        if (null == allProject) allProject = new ArrayList<SVNProject>();
        RespBaseBean resp = RespBaseBean.createSuccessResp(allProject);
        renderJson(resp);
    }


    public void add(){
        String projName =  (null == getPara("name")?"":getPara("name"));
        String projPath =  (null == getPara("path")?"":getPara("path"));

        if (projName.length() <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请执行项目的名称"));
            return;
        }

        if (projPath.length() <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请执行项目的路径"));
            return;
        }

        try {
            projName = URLDecoder.decode(projName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            projPath = URLDecoder.decode(projPath,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (projPath.indexOf(ZTSVNConstanst.path) != 0){
            renderJson(RespBaseBean.createErrorResp(44,"请输入合法的svn路径"));
            return;
        }

        SVNProject tmpProj = SVNProject.dao.findFirst("SELECT * FROM svnt_project WHERE path = '"+projPath+"' ;");
        if (null != tmpProj){
            renderJson(RespBaseBean.createErrorResp(44,"项目路径已经存在,请勿重复添加!"));
            return;
        }

        boolean saveFlag = false;
        try {
            saveFlag = new SVNProject()
                    .set("name", projName)
                    .set("path", projPath)
                    .set("mark", "")
                    .save();
        }catch (Exception e){
            logger.error(e);
        }

        if (saveFlag){
            renderJson(RespBaseBean.createSuccessResp("项目添加成功！"));
            return;
        }else {
            renderJson(RespBaseBean.createErrorResp(43,"项目添加失败！"));
            return;
        }

    }


    public void edit(){
        int projId = (null == getParaToInt("id")?0:getParaToInt("id"));
        String projName =  (null == getPara("name")?"":getPara("name"));
        String projPath =  (null == getPara("path")?"":getPara("path"));

        if (projId <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请指定要修改的项目id"));
            return;
        }

        if (projName.length() <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请执行项目的名称"));
            return;
        }

        if (projPath.length() <= 0){
            renderJson(RespBaseBean.createErrorResp(44,"请执行项目的路径"));
            return;
        }

        try {
            projName = URLDecoder.decode(projName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            projPath = URLDecoder.decode(projPath,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (projPath.indexOf(ZTSVNConstanst.path) != 0){
            renderJson(RespBaseBean.createErrorResp(44,"请输入合法的svn路径"));
            return;
        }



        SVNProject svnProject = SVNProject.dao.findFirst("SELECT * FROM svnt_project WHERE id = '"+projId+"' ;");

        if (null == svnProject){
            renderJson(RespBaseBean.createErrorResp(44,"没有查询到ID为"+projId+"的项目！"));
            return;
        }

        SVNProject tmpProj = SVNProject.dao.findFirst("SELECT * FROM svnt_project WHERE path = '"+projPath+"' ;");
        if (null != tmpProj && tmpProj.getInt("id") != svnProject.getInt("id")){
            //这个项目路径已经存在，并且不是当前要编辑的 项目，要提示
            renderJson(RespBaseBean.createErrorResp(44,"项目路径已经存在,请勿重复添加!"));
            return;
        }

        boolean updateFlage = svnProject.set("name",projName)
                .set("path",projPath).update();

        if (updateFlage){
            renderJson(RespBaseBean.createSuccessResp("项目修改成功！"));
            return;
        }else {
            renderJson(RespBaseBean.createErrorResp(43,"项目修改失败！"));
            return;
        }
    }



}
