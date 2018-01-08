package com.ztsq.codelines.utils.svn.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by zyuework on 2018/1/8.
 */
public class StrOutputStrean extends OutputStream {

    public List<String> lineList;

    public StrOutputStrean(List<String> s) {
        this.lineList = s;
    }

    public void write(int b) throws IOException {

    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        String temp = new String(b);
        if (null != lineList){
            lineList.add(temp);
        }
    }
}
