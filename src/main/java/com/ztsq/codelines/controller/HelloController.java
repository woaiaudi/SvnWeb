package com.ztsq.codelines.controller;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.jfinal.core.Controller;

import java.util.ArrayList;
import java.util.List;

public class HelloController extends Controller {

    public void index(){
        renderText("<<>><<<>>>sdsdsd<<>?<><>??");
    }

    public void save(){
        renderJson(new ArrayList<String>());
    }

    public void segword(){

        String orgStr = getPara("str");

        JiebaSegmenter segmenter = new JiebaSegmenter();

        List<SegToken> regList = segmenter.process(orgStr, JiebaSegmenter.SegMode.SEARCH);

        if(null != regList && regList.size() > 0){

            List<String> wordList = new ArrayList<String>();

            for (SegToken item:regList) {
                wordList.add(item.word);
            }
            renderJson(wordList);
        }else {
            renderJson("ERROR");
        }

    }

}
