package com.github.mdsr.sample.controller;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.parse.EntityMateDataParser;
import com.github.ibatis.statement.mapper.DynamicSelectMapper;
import com.github.ibatis.statement.mapper.param.*;
import com.github.ibatis.statement.util.TypeUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

/**
 * 通用查询
 * @Author: junjie
 * @Date: 2020/10/18
 */
@RestController
public class GeneralController implements ApplicationContextAware{

    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralController.class);

    /**
     * 记录所以继承了{@link DynamicSelectMapper}的mapper接口
     * key: mapper接口支持的实体类映射的table_name
     */
    private final Map<String ,TypeWrapper> typeMapperHashMap = new HashMap<>();

    /**
     * 排序字段url参数名
     */
    public static final String SORT_FIELD = "sort_field";

    /**
     * 排序字段规则参数名
     */
    public static final String SORT_RULE = "sort_rule";

    /**
     * 分页下标参数名
     */
    public static final String PAGE_INDEX = "page_index";

    /**
     * 分页大小参数名
     */
    public static final String PAGE_SIZE = "page_size";

    /**
     * 是否逻辑查询参数名
     */
    public static final String LOGICAL_SELECT = "logical_select";

    enum ResultType {
        unique ,
        list ,
        count
    }

    /**
     * 动态参数查询
     * @param entityType 查询的类型
     * @param resultType 定义以什么样的数据结构返回
     * @param sortField 排序字段，由type决定
     * @param sortOrder 排序规则
     * @param pageIndex 分页下标
     * @param pageSize 分页大小
     * @param logicalSelect 是否逻辑查询
     * @param propertyValues 自定义查询规则
     *                       paramName: `columnName` + "_"+ {@link ConditionRule#name().toLowerCase()}
     * @return
     */
    @GetMapping("/general/type/{entity_type}/{result_type}")
    public Object dynamicSelect(
            @PathVariable("entity_type") String entityType ,
            @PathVariable("result_type") ResultType resultType,
            @RequestParam(value = SORT_FIELD, required = false) String sortField,
            @RequestParam(value = SORT_RULE, required = false) String sortOrder,
            @RequestParam(value = PAGE_INDEX, required = false) Integer pageIndex,
            @RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = LOGICAL_SELECT, required = false ,defaultValue = "true") boolean logicalSelect,
            @RequestParam Map<String, String> propertyValues)
    {
        TypeWrapper typeWrapper = typeMapperHashMap.get(entityType);
        if (typeWrapper == null){
            throw new IllegalArgumentException(MessageFormat.format("unknown entity type code {0}" ,entityType));
        }

        propertyValues.remove(SORT_FIELD);
        propertyValues.remove(SORT_RULE);
        propertyValues.remove(PAGE_INDEX);
        propertyValues.remove(PAGE_SIZE);
        propertyValues.remove(LOGICAL_SELECT);

        EntityMateData entityMateData = typeWrapper.getEntityMateData();
        ConditionParams conditionParams = buildConditionParams(propertyValues ,entityMateData);

        if (!propertyValues.isEmpty()){
            StringBuilder unrecognizedParamValues = new StringBuilder();
            for (Map.Entry<String, String> entry : propertyValues.entrySet()) {
                unrecognizedParamValues.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            unrecognizedParamValues.deleteCharAt(unrecognizedParamValues.length() - 1);
            LOGGER.warn("can't recognized paramValues {}" ,unrecognizedParamValues);
        }

        DynamicParams dynamicParams = new DynamicParams();
        dynamicParams.where(conditionParams);
        dynamicParams.logical(logicalSelect);

        if (resultType == ResultType.count){
            int count = typeWrapper.getDynamicSelectMapper().countByDynamicParams(dynamicParams);
            return count;
        }

        dynamicParams.addOrderRule(buildOrderRule(sortField, sortOrder));

        if (pageIndex != null && pageSize != null) {
            dynamicParams.page1(pageIndex ,pageSize);
        }

        List list = typeWrapper.getDynamicSelectMapper().selectByDynamicParams(dynamicParams);
        if (resultType == ResultType.unique){
            if (list.size() > 1){
                throw new IllegalStateException("There are multiple results that meet the conditions");
            }
            return list.isEmpty() ? null : list.get(0);
        }

        return list;
    }

    /**
     * 构建查询条件
     * @param propertyValues
     * @param entityMateData
     * @return
     */
    ConditionParams buildConditionParams(Map<String, String> propertyValues ,EntityMateData entityMateData)
    {
        Map<String, ColumnMateData> columnMateDataMap = entityMateData.getTableMateData().getColumnMateDataMap();

        ConditionParams conditionParams = new ConditionParams();
        for (Map.Entry<String, ColumnMateData> entry : columnMateDataMap.entrySet())
        {
            if (propertyValues.isEmpty()) {
                break;
            }
            String columnName = entry.getKey();
            if (propertyValues.containsKey(columnName)) {
                String value = propertyValues.remove(columnName);
                conditionParams.addConditionParam(columnName, ConditionRule.EQ, value);
                continue;
            }

            for (ConditionRule rule : ConditionRule.values()) {
                if (propertyValues.isEmpty()){
                    break;
                }
                String keyRule = columnName + "_" + rule.name().toLowerCase();
                if (propertyValues.containsKey(keyRule)) {
                    String value = propertyValues.remove(keyRule);
                    Object ruleValue = null;
                    switch (rule) {
                        case LIKE:
                        case NOT_LIKE:
                            ruleValue = "%" + value + "%";
                            break;
                        case LIKE_LEFT:
                            ruleValue = "%" + value;
                            break;
                        case LIKE_RIGHT:
                            ruleValue = value + "%";
                            break;
                        case IN:
                        case NOT_IN:
                            ruleValue = value.split(",");
                            break;
                        case IS_NULL:
                        case NOT_NULL:
                        case NE:
                            ruleValue = null;
                            break;
                        case BETWEEN:
                            int index = value.indexOf(",");
                            if (index <= 0) {
                                throw new IllegalArgumentException(MessageFormat.format("param {0} value illegal", columnName));
                            }
                            ruleValue = new BetweenParam(value.substring(0, index), value.substring(index + 1, value.length()));
                            break;
                        default:
                            ruleValue = value;
                            break;
                    }
                    conditionParams.addConditionParam(new ConditionParam(columnName, rule, ruleValue));
                }
            }
        }

        return conditionParams;
    }

    /**
     * 解析排序规则
     * @param sortField
     * @param sortOrder
     * @return
     */
    List<OrderRule> buildOrderRule(String sortField ,String sortOrder)
    {
        List<OrderRule> orderRules = new ArrayList<>();
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortOrder))
        {
            String[] fields = sortField.split(",");
            String[] rules = sortOrder.split(",");
            if (fields.length != rules.length) {
                throw new IllegalStateException("order params illegal");
            }
            for (int i = 0; i < fields.length; i++) {
                orderRules.add(new OrderRule(fields[i], OrderRule.Rule.valueOf(rules[i])));
            }
        }
        return orderRules;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        EntityMateDataParser entityMateDataParser = applicationContext.getBean(EntityMateDataParser.class);
        SqlSession sqlSession = applicationContext.getBean(SqlSession.class);
        Collection<DynamicSelectMapper> tableMappers = applicationContext.getBeansOfType(DynamicSelectMapper.class).values();
        for (DynamicSelectMapper tableMapper : tableMappers) {
            Type entityType = TypeUtils.parseSuperTypeVariable(tableMapper.getClass(), DynamicSelectMapper.class.getTypeParameters()[0]);
            if (entityType instanceof Class){
                Class entityClass = (Class) entityType;
                EntityMateData entityMateData = entityMateDataParser.parse(entityClass, sqlSession).orElse(null);
                if (entityMateData != null) {
                    TypeWrapper typWrapper = new TypeWrapper();
                    String type = entityMateData.getTableName();
                    typWrapper.setType(entityMateData.getTableName());
                    typWrapper.setEntityMateData(entityMateData);
                    typWrapper.setDynamicSelectMapper(tableMapper);
                    typeMapperHashMap.put(type, typWrapper);
                }
            }
        }
    }

    class TypeWrapper<T>{

        /**
         * 实体类映射的表名
         */
        private String type;

        /**
         * 支持实体类动态查询的mapper接口
         */
        private DynamicSelectMapper<T> dynamicSelectMapper;

        /**
         * 实体类-表映射元数据
         */
        private EntityMateData entityMateData;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public DynamicSelectMapper<T> getDynamicSelectMapper() {
            return dynamicSelectMapper;
        }

        public void setDynamicSelectMapper(DynamicSelectMapper<T> dynamicSelectMapper) {
            this.dynamicSelectMapper = dynamicSelectMapper;
        }

        public EntityMateData getEntityMateData() {
            return entityMateData;
        }

        public void setEntityMateData(EntityMateData entityMateData) {
            this.entityMateData = entityMateData;
        }
    }

}
