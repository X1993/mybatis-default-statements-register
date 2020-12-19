package com.github.ibatis.statement.base.core.matedata;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyColumnUsageTest {

    @Test
    public void compareTo() {
        KeyColumnUsage keyColumnUsage1 = new KeyColumnUsage();
        keyColumnUsage1.setOrdinalPosition(1);
        KeyColumnUsage keyColumnUsage2 = new KeyColumnUsage();
        keyColumnUsage2.setOrdinalPosition(2);
        List<KeyColumnUsage> keyColumnUsageList = new ArrayList();
        keyColumnUsageList.add(keyColumnUsage2);
        keyColumnUsageList.add(keyColumnUsage1);
        Collections.sort(keyColumnUsageList);
        Assert.assertTrue(keyColumnUsageList.get(0).getOrdinalPosition() == 1);
        Assert.assertTrue(keyColumnUsageList.get(1).getOrdinalPosition() == 2);
    }

}