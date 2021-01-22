package com.github.ibatis.statement.base.core;

import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 *
 * 列引用表达式解析
 * <p>
 *     &{columnName} + 1  -----解析----->  `columnName` + 1
 *     &{column or col} + 1  -----解析----->  `默认使用类字段映射的列（如果存在的话）` + 1
 *     &{illegalColumn} + 1  -----解析----->  `illegalColumn` + 1
 * </p>
 * @see DefaultColumnValueParser#expressionParser
 * @see DefaultColumnConditionParser#expressionParser
 * @Author: X1993
 * @Date: 2020/8/3
 */
public class ColumnExpressionParser implements ExpressionParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(ColumnExpressionParser.class);

    public static final ColumnExpressionParser INSTANT = new ColumnExpressionParser();

    @Override
    public String parse(String mappingColumnName ,String expression ,EntityMateData entityMateData) {
        if (expression == null || expression.length() == 0){
            return expression;
        }
        char[] chars = expression.toCharArray();
        int length = chars.length;
        char leftQuotationMark = 0;
        StringBuilder result = new StringBuilder();
        Map<String, ColumnMateData> columnMateDataMap = entityMateData.getTableMateData().getColumnMateDataMap();
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            if ('\'' == c || '\"' == c) {
                if (leftQuotationMark == 0) {
                    //字符串起始位置
                    leftQuotationMark = c;
                } else if (leftQuotationMark == c) {
                    if (i + 1 < length) {
                        if (chars[i + 1] == c){
                            //转义符号
                            result.append(c);
                            i++;
                        }else {
                            //字符串结束位置
                            leftQuotationMark = 0;
                        }
                    }else {
                        //已到表达式结尾
                        leftQuotationMark = 0;
                    }
                }
                result.append(c);
            }else if ('&' == c && i + 1 < length && chars[i + 1] == '{' && leftQuotationMark == 0){
                //变量符起始位置(如果表达式包含在字符串内不解析)
                StringBuilder quote = new StringBuilder();
                boolean complete = false;
                for (i = i + 2; i < length; i++) {
                    char c1 = chars[i];
                    if (c1 != '}'){
                        quote.append(c1);
                    }else {
                        String quoteValue = quote.toString();
                        ColumnMateData columnMateData = columnMateDataMap.get(quoteValue);
                        if (columnMateData != null){
                            result.append(columnMateData.getEscapeColumnName());
                        }else if ("column".equals(quoteValue) || "col".equals(quoteValue)){
                            result.append("`").append(mappingColumnName).append("`");
                        }else {
                            LOGGER.warn("illegal variable #{{}}" ,quoteValue);
                            result.append("&{").append(quoteValue).append("}");
                        }
                        complete = true;
                        break;
                    }
                }
                if (!complete){
                    LOGGER.warn("illegal expression {} ,illegal variable reference" ,expression);
                    return expression;
                }
            }else {
                result.append(c);
            }
        }
        if (leftQuotationMark > 0){
            LOGGER.warn("illegal expression {} ,illegal string" ,expression);
            return expression;
        }
        return result.toString();
    }

}
