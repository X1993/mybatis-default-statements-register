package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.util.Sorter;

import java.util.Optional;

/**
 * 解析实体类映射的表来源信息
 * @author X1993
 * @date 2020/2/23
 */
public interface TableSourceParser extends Sorter {

    /**
     * 解析
     * @param entityClass
     * @return
     */
    Optional<Source> parse(Class<?> entityClass);

    /**
     * 表来源
     * @author X1993
     * @date 2020/9/26
     */
    class Source {

        /**
         * 表名
         */
        private String tableName;

        /**
         * 表结构解析策略
         */
        private TableSchemaResolutionStrategy tableSchemaResolutionStrategy;

        public Source(String tableName, TableSchemaResolutionStrategy tableSchemaResolutionStrategy) {
            this.tableName = tableName;
            this.tableSchemaResolutionStrategy = tableSchemaResolutionStrategy;
        }

        public Source(String tableName) {
            this.tableName = tableName;
        }

        public Source() {
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public TableSchemaResolutionStrategy getTableSchemaResolutionStrategy() {
            return tableSchemaResolutionStrategy;
        }

        public void setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy tableSchemaResolutionStrategy) {
            this.tableSchemaResolutionStrategy = tableSchemaResolutionStrategy;
        }

    }

}
