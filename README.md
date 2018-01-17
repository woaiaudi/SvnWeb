

### 功能列表
- [x] 人员列表
- [x] 项目列表
- [x] 添加项目(编码，中文)
- [x] 修改项目
- [ ] 删除项目(暂不提供)
- [x] 过滤掉打分支的版本
- [x] 在UI上表示出 分支版本
- [x] 查询的时间范围不大于21天(性能不行)
- [ ] 查询的时间范围一个月时，性能调优
- [x] 查询的时间范围 从start day 00:00:00 到 end day 23:59:59
- [x] 查询某一次提交日志中的changepaths
- [x] 重新刷某一次提交日志中的changepaths
- [ ] 打分支的功能，使用 svnkit SVNCopyClient





##### 刷数据
```

    http://192.168.2.156:8080/Hello/createData?startDate=2018-01-08&endDate=2018-01-15

    http://192.168.2.156:8080/Hello/createData?startDate=2018-01-01&endDate=2018-01-08

    http://192.168.2.156:8080/Hello/createData?startDate=2017-12-25&endDate=2018-01-01

    http://192.168.2.156:8080/Hello/createData?startDate=2017-12-18&endDate=2017-12-25

    //接下来的 数据就不刷了
    http://192.168.2.156:8080/Hello/createData?startDate=2017-12-11&endDate=2017-12-18

    http://192.168.2.156:8080/Hello/createData?startDate=2017-12-04&endDate=2017-12-11

    http://192.168.2.156:8080/Hello/createData?startDate=2017-11-27&endDate=2017-12-04

    http://192.168.2.156:8080/Hello/createData?startDate=2017-11-20&endDate=2017-11-27

    http://192.168.2.156:8080/Hello/createData?startDate=2017-11-13&endDate=2017-11-20

    http://192.168.2.156:8080/Hello/createData?startDate=2017-11-06&endDate=2017-11-13
```



#### 使用说明
##### 项目设置
![](http://ogeijtkoy.bkt.clouddn.com/svn1.png)

![](http://ogeijtkoy.bkt.clouddn.com/svn2.png)

##### 统计代码
![](http://ogeijtkoy.bkt.clouddn.com/svn3.png)