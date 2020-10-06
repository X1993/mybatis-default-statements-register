package com.github.ibatis.statement.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void camelCaseToUnderscore() {
        Assert.assertEquals(StringUtils.camelCaseToUnderscore("TaskService") ,"task_service");
    }
}