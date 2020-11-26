package com.github.ibatis.statement.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void camelCaseToUnderscore() {
        Assert.assertEquals(StringUtils.camelCaseToUnderscore("TaskService") ,"task_service");
        Assert.assertEquals(StringUtils.camelCaseToUnderscore("TaskService_1") ,"task_service_1");
    }

    @Test
    public void camelUnderscoreToCase() {
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("task_service") ,"taskService");
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("task_service" ,true) ,"TaskService");
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("task_serviceImpl") ,"taskServiceImpl");
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("in_eq" ,true) ,"InEq");
    }

}