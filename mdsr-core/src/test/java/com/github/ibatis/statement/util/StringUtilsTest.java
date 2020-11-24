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
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("task_service") ,"TaskService");
        Assert.assertEquals(StringUtils.camelUnderscoreToCase("task_serviceImpl") ,"TaskServiceImpl");
    }

}