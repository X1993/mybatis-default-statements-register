package com.github.ibatis.statement.register.mysql;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;

/**
 * @author jie
 * @date 2021/12/11
 * @description
 */
public class AdapterProperties {

    public final static String[] DATA_BASE_PRODUCT_NAMES = new String[]{"MYSQL" ,"MariaDB"};

    public final static boolean matchDatabase(String sqlMode)
    {
        if (sqlMode == null || "".equals(sqlMode)){
            return false;
        }
        for (String dataBaseProductName : DATA_BASE_PRODUCT_NAMES) {
            if (dataBaseProductName.equalsIgnoreCase(sqlMode)){
                return true;
            }
        }
        return false;
    }

    public final static boolean matchDatabase(EntityMateData entityMateData){
        String sqlMode = entityMateData.getTableMateData().getSqlMode();
        return matchDatabase(sqlMode);
    }

}
