package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.mapper.param.*;
import com.github.ibatis.statement.util.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 特定规则的方法
 * @see <a href="https://github.com/X1993/mybatis-default-statements-register/blob/3.1.0-SNAPSHOT/mdsr-core/method-name-parse-rule.png">方法名解析规则</a>
 * @Author: junjie
 * @Date: 2020/11/23
 */
public class MethodNameParseMappedStatementFactory extends AbstractSelectMappedStatementFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodNameParseMappedStatementFactory.class);

    @Override
    public Optional<MappedStatement> tryBuild(MappedStatementMateData mappedStatementMateData) {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        MapperMethodMateData mapperMethodMateData = mappedStatementMateData.getMapperMethodMateData();
        String mappedStatementId = mapperMethodMateData.getMappedStatementId();
        return resolvedSqlNode(mappedStatementMateData)
                .map(sqlSource -> customBuilder(mappedStatementMateData ,new MappedStatement.Builder(configuration,
                        mappedStatementId, sqlSource, sqlCommandType(mappedStatementMateData))
                        .resource(resource(mappedStatementMateData))
                        .statementType(StatementType.PREPARED)
                        .databaseId(configuration.getDatabaseId())
                        .resultSetType(configuration.getDefaultResultSetType()))
                        .build());
    }

    @Override
    public int order() {
        return super.order() + 100;
    }

    /**
     * @param mappedStatementMateData 元数据
     * @return
     * @deprecated {@link AbstractMappedStatementFactory#tryBuild(MappedStatementMateData)}被重写,这个方法不会被调用
     */
    @Override
    @Deprecated
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData) {
        return true;
    }

    /**
     * @param mappedStatementMateData
     * @return
     * @deprecated {@link AbstractMappedStatementFactory#tryBuild(MappedStatementMateData)}被重写,这个方法不会被调用
     */
    @Deprecated
    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        return null;
    }

    /**
     * 单词
     */
    static class Card {

        static Card START_CARD = new Card(null ,"");

        /**
         * 单词类型
         */
        public final CardType type;

        /**
         * 值
         */
        public final String value;

        public Card(CardType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Card card = (Card) o;
            return Objects.equals(value, card.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Card{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    static class NextCard{

        /**
         * 下个单词
         */
        public final Card card;

        /**
         * 状态校验/设置函数
         */
        public final Function<Map<String ,String> ,Map<String ,String>> statusFunction;

        /**
         * 是否允许作为语法结束符
         */
        public final boolean isTermination;

        public NextCard(Card card) {
            this(card,false , map -> map);
        }

        public NextCard(Card card, Function<Map<String ,String> ,Map<String ,String>> statusFunction) {
            this(card,false ,statusFunction);
        }

        public NextCard(Card card, boolean isTermination , Function<Map<String ,String> ,Map<String ,String>> statusFunction) {
            this.card = card;
            this.isTermination = isTermination;
            if (statusFunction == null){
                statusFunction = map -> map;
            }
            this.statusFunction = statusFunction;
        }

        @Override
        public String toString() {
            return "NextCard{" +
                    "card=" + card +
                    ", isTermination=" + isTermination +
                    '}';
        }
    }

    enum CardType {
        SELECT,
        FIND,
        BY,
        EQ,
        NOT_EQ,
        LT,
        GT,
        LE,
        GE,
        NOT_LIKE,
        LIKE,
        LIKE_LEFT,
        LIKE_RIGHT,
        NOT_IN,
        IN,
        IS_NULL,
        NOT_NULL,
        BETWEEN,
        NOT_BETWEEN,
        NE,
        AND,
        OR,
        DESC,
        ASC,
        ORDER_BY,
        COLUMN,
    }

    /**
     * 根据方法解析SqlSource
     * @param mappedStatementMateData
     * @return
     */
    private Optional<SqlSource> resolvedSqlNode(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        String[] columnNames = entityMateData
                .getTableMateData().getColumnMateDataList()
                .stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .toArray(length -> new String[length]);

        Method mappedMethod = mappedStatementMateData.getMapperMethodMateData().getMappedMethod();
        Class<?>[] parameterTypes = mappedMethod.getParameterTypes();
        String methodName = mappedMethod.getName();

        List<List<Card>> sentences = expressionParticiple(methodName, columnNames);
        Configuration configuration = mappedStatementMateData.getConfiguration();

        List<Card> suitableSentence = null;
        int maxSuitability = -1;
        for (List<Card> sentence : sentences) {
            int suitability = suitability(mappedStatementMateData, sentence);
            if (suitability > maxSuitability){
                suitableSentence = sentence;
            }
        }

        if (suitableSentence == null){
            LOGGER.debug("method {} does not meet the registration rules" ,mappedMethod);
            return Optional.empty();
        }

        ConditionRule conditionRule = ConditionRule.EQ;
        String operator = null;
        int size = suitableSentence.size();
        List<SqlNode> conditionSqlNodes = new ArrayList<>();
        StringBuilder orderSql = new StringBuilder();

        Map<String, String> columnNameMappings = Arrays.stream(columnNames)
                .collect(Collectors.toMap(name -> standard(name), name -> name));

        int argIndex = 1;
        for (int i = 0; i < size; i++) {
            Card card = suitableSentence.get(i);
            switch (card.type){
                case BY:
                    operator = "where";
                    break;
                case ORDER_BY:
                    operator = "order";
                    break;
                case AND:
                case OR:
                    //默认
                    conditionRule = ConditionRule.EQ;
                    conditionSqlNodes.add(new StaticTextSqlNode(" " + card.type.name() + " "));
                    break;
                case EQ:
                case NOT_EQ:
                case LT:
                case GT:
                case LE:
                case GE:
                case NOT_LIKE:
                case LIKE:
                case LIKE_LEFT:
                case LIKE_RIGHT:
                case NOT_IN:
                case IN:
                case IS_NULL:
                case NOT_NULL:
                case BETWEEN:
                case NOT_BETWEEN:
                case NE:
                    conditionRule = ConditionRule.valueOf(card.type.name());
                    break;
                case COLUMN:
                    String columnName = columnNameMappings.get(card.value);
                    if ("where".equals(operator)){
                        StringBuilder value = new StringBuilder("#{param").append(argIndex).append("}");
                        SqlNode valueSqlNode = null;
                        switch (conditionRule){
                            case IN:
                            case NOT_IN:
                                /*
                                ，
                                mybatis <foreach>标签collectionExpression属性如果方法参数只有一个，不是很清楚这里为什么不能通过下标获取方法参数值(param1),
                                只可以使用array或者collection，如果有多个参数可以通过下标取值
                                 */
                                valueSqlNode = new ForEachSqlNode(configuration ,new StaticTextSqlNode("#{item}") ,
                                        mappedMethod.getParameterCount() > 1 ? value.toString() :
                                                parameterTypes[argIndex - 1].isArray() ? "array" : "collection" ,
                                        null, "item" ,"(" ,")" ,",");
                                argIndex++;
                                break;
                            case LIKE:
                            case NOT_LIKE:
                                valueSqlNode = new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                        .append(value).append(",'%')").toString());
                                argIndex++;
                                break;
                            case LIKE_LEFT:
                                valueSqlNode = new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                        .append(value).toString());
                                argIndex++;
                                break;
                            case LIKE_RIGHT:
                                valueSqlNode = new StaticTextSqlNode(new StringBuilder("CONCAT(")
                                        .append(value).append(",'%')").toString());
                                argIndex++;
                                break;
                            case NE:
                            case IS_NULL:
                            case NOT_NULL:
                                valueSqlNode = new StaticTextSqlNode("");
                                break;
                            case BETWEEN:
                            case NOT_BETWEEN:
                                valueSqlNode = new StaticTextSqlNode(value.append(" AND #{param")
                                        .append(++argIndex).append("}").toString());
                                argIndex++;
                                break;
                            default:
                                valueSqlNode = new StaticTextSqlNode(value.toString());
                                argIndex++;
                                break;
                        }
                        conditionSqlNodes.add(new StaticTextSqlNode(new StringBuilder("`")
                                .append(columnName).append("` ")
                                .append(conditionRule.expression).append(" ").toString()));
                        conditionSqlNodes.add(valueSqlNode);
                    }else if ("order".equals(operator)){
                        orderSql.append("`").append(columnName).append("`,");
                    }
                    break;
                case ASC:
                case DESC:
                    orderSql.deleteCharAt(orderSql.length() - 1).append(" ").append(card.type);
            }
        }

        if (orderSql.length() > 0){
            orderSql.insert(0 ," ORDER BY ");
        }
        if (conditionSqlNodes.size() > 0){
            conditionSqlNodes.add(0 ,new StaticTextSqlNode(" WHERE "));
        }

        SqlNode selectSqlNode = new StaticTextSqlNode(new StringBuilder("SELECT ")
                .append(entityMateData.getBaseColumnListSqlContent())
                .append(" FROM `").append(entityMateData.getTableName())
                .append("`").toString());

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            conditionSqlNodes.add(new StaticTextSqlNode(logicalColumnMateData.equalSqlContent(true)
                    .insert(0 ," AND ").toString()));
        }

        List<SqlNode> sqlNodes = new ArrayList<>();
        sqlNodes.add(selectSqlNode);
        sqlNodes.addAll(conditionSqlNodes);
        sqlNodes.add(new StaticTextSqlNode(orderSql.toString()));

        return Optional.of(new DynamicSqlSource(configuration ,new MixedSqlNode(sqlNodes)));
    }

    /**
     * 获取方法与分词匹配值，值越高匹配可能性越高
     * @param mappedStatementMateData
     * @param sentence
     * @return -1 不匹配
     */
    private int suitability(MappedStatementMateData mappedStatementMateData ,List<Card> sentence)
    {
        Method mappedMethod = mappedStatementMateData.getMapperMethodMateData().getMappedMethod();
        Class[] parameterTypes = mappedMethod.getParameterTypes();

        int size = sentence.size();
        List<ConditionParam> conditionParams = new ArrayList<>(parameterTypes.length + 1);
        String operator = null;
        ConditionRule conditionRule = ConditionRule.EQ;
        for (int i = 0; i < size; i++) {
            Card card = sentence.get(i);
            switch (card.type){
                case BY:
                    operator = "where";
                    break;
                case ORDER_BY:
                    operator = "order";
                    break;
                case EQ:
                case NOT_EQ:
                case LT:
                case GT:
                case LE:
                case GE:
                case NOT_LIKE:
                case LIKE:
                case LIKE_LEFT:
                case LIKE_RIGHT:
                case NOT_IN:
                case IN:
                case IS_NULL:
                case NOT_NULL:
                case BETWEEN:
                case NOT_BETWEEN:
                case NE:
                    conditionRule = ConditionRule.valueOf(card.type.name());
                    break;
                case AND:
                case OR:
                    //默认
                    conditionRule = ConditionRule.EQ;
                    break;
                case COLUMN:
                    if ("where".equals(operator)){
                        if (ConditionRule.IS_NULL.equals(conditionRule)
                            || ConditionRule.NOT_NULL.equals(conditionRule)
                            || ConditionRule.NE.equals(conditionRule))
                        {
                            //没有值
                            break;
                        }else if (ConditionRule.BETWEEN.equals(conditionRule)
                                || ConditionRule.NOT_BETWEEN.equals(conditionRule))
                        {
                            //两个值
                            conditionParams.add(new ConditionParam(card.value ,conditionRule ,"1"));
                            conditionParams.add(new ConditionParam(card.value ,conditionRule ,"2"));
                        }else {
                            conditionParams.add(new ConditionParam(card.value, conditionRule, null));
                        }
                    }
            }
        }

        if (conditionParams.size() != parameterTypes.length){
            return -1;
        }

        int value = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            ConditionParam conditionParam = conditionParams.get(i);
            ConditionRule rule = conditionParam.getRule();
            Class<?> parameterType = parameterTypes[i];
            if (ConditionRule.IN.equals(rule) || ConditionRule.NOT_IN.equals(rule)){
                if (!Collection.class.isAssignableFrom(parameterType) && !parameterType.isArray()){
                    return -1;
                }
            }
        }

        return value;
    }

    /**
     * 根据语法对表达式分词
     * @see <a href="https://github.com/X1993/mybatis-default-statements-register/blob/3.1.0-SNAPSHOT/mdsr-core/method-name-parse-rule.png">方法名解析规则</a>
     * @param expression
     * @param columnNames
     * @return
     */
    List<List<Card>> expressionParticiple(String expression , String ... columnNames)
    {
        Map<Card ,List<NextCard>> syntaxTable = syntaxTable(columnNames);
        Map<String ,String> content = new HashMap<>();

        long startTimeMillis = System.currentTimeMillis();
        List<List<Card>> sentences = parseParticiple(expression, Card.START_CARD, content, syntaxTable ,false);

        for (List<Card> sentence : sentences) {
            sentence.remove(0);
        }

        LOGGER.debug("expression [{}] reasonable participle program: {} ,time consuming {} ms" ,expression , sentences
                .stream()
                .map(sentence -> MessageFormat.format("[{0}]" ,sentence.stream()
                        .map(card -> card.value)
                        .reduce((value1 ,value2) -> value1 + " " + value2)
                        .get())
                )
                .reduce((value1 ,value2) -> value1 + " or " + value2)
                .orElse("don't have reasonable participle program") ,System.currentTimeMillis() - startTimeMillis);

        return sentences;
    }

    /**
     * 根据语法表分词
     * @param expression
     * @param currentCard
     * @param content
     * @param syntaxTable
     * @param isTermination
     * @return
     */
    private List<List<Card>> parseParticiple(String expression, Card currentCard, Map<String ,String> content,
                                             Map<Card ,List<NextCard>> syntaxTable , boolean isTermination)
    {
        List<List<Card>> sentences = new ArrayList<>();
        if (expression.startsWith(currentCard.value)){
            if (expression.length() == currentCard.value.length() && isTermination){
                List<Card> sentence = new ArrayList<>();
                sentence.add(currentCard);
                sentences.add(sentence);
                return sentences;
            }
            List<NextCard> nextCards = syntaxTable.get(currentCard);
            expression = expression.substring(currentCard.value.length());
            for (NextCard nextCard : nextCards) {
                Map<String, String> nextStatusMap = nextCard.statusFunction.apply(content);
                if (nextStatusMap != null && Collections.EMPTY_MAP != nextStatusMap){
                    List<List<Card>> afterSentences = parseParticiple(expression,
                            nextCard.card, nextStatusMap ,syntaxTable ,nextCard.isTermination);
                    if (afterSentences != null){
                        for (List<Card> afterSentence : afterSentences) {
                            afterSentence.add(0 ,currentCard);
                            sentences.add(afterSentence);
                        }
                    }
                }
            }
        }
        return sentences;
    }

    private String standard(String str){
        return StringUtils.camelUnderscoreToCase(str ,true);
    }

    /**
     * 语法规则
     * @param columnNames
     * @return
     */
    private Map<Card ,List<NextCard>> syntaxTable(String ... columnNames)
    {
        Card select = new Card(CardType.SELECT ,"select");
        Card find = new Card(CardType.FIND ,"find");

        Card by = new Card(CardType.BY ,"By");

        Card and = new Card(CardType.AND ,"And");
        Card or = new Card(CardType.OR ,"Or");

        Card[] conditionKeyWords = Stream.of(ConditionRule.values())
                .map(conditionRule -> new Card(CardType.valueOf(conditionRule.name()) ,
                        standard(conditionRule.name().toLowerCase())))
                .toArray(length -> new Card[length]);

        Card orderBy = new Card(CardType.ORDER_BY, "OrderBy");
        Card desc = new Card(CardType.DESC, "Desc");
        Card asc = new Card(CardType.ASC, "Asc");

        Card[] columnCards = Arrays.stream(columnNames)
                .distinct()
                .map(columnName -> new Card(CardType.COLUMN, standard(columnName)))
                .toArray(s -> new Card[s]);

        Map<Card ,List<NextCard>> syntaxTable = new HashMap<>();
        Function<Map<String ,String> ,Map<String ,String>> noOperator = map -> map;

        List<NextCard> startPossibleNextCards = new ArrayList<>();
        startPossibleNextCards.add(new NextCard(select ,noOperator));
        startPossibleNextCards.add(new NextCard(find ,noOperator));
        syntaxTable.put(Card.START_CARD ,startPossibleNextCards);

        List<NextCard> selectPossibleNextCards = new ArrayList<>();
        // select / find -> by
        selectPossibleNextCards.add(new NextCard(by ,noOperator));
        String order = "order";
        String operator = "operator";
        String where = "where";

        Function<Map<String ,String> ,Map<String ,String>> orderOperator = content -> {
            if ("1".equals(content.get("orderFlag"))){
                return content;
            }
            Map<String ,String> copyMap = new HashMap<>(content);
            copyMap.put("orderFlag" ,"1");
            copyMap.put(operator ,order);
            return copyMap;
        };

        Function<Map<String ,String> ,Map<String ,String>> checkOperatorWhere = content ->
                where.equals(content.get(operator)) ? content : Collections.EMPTY_MAP;

        Function<Map<String ,String> ,Map<String ,String>> setOperatorWhere = content -> {
            Map<String ,String> copyMap = new HashMap<>(content);
            copyMap.put(operator ,where);
            return copyMap;
        };

        Function<Map<String ,String> ,Map<String ,String>> checkOperatorOrder = content ->
                order.equals(content.get(operator)) ? content : Collections.EMPTY_MAP;

        NextCard nextOrderByCard = new NextCard(orderBy, orderOperator);
        selectPossibleNextCards.add(nextOrderByCard);

        syntaxTable.put(select ,selectPossibleNextCards);
        syntaxTable.put(find ,selectPossibleNextCards);

        List<NextCard>  byPossibleNextCards = new ArrayList<>();
        for (Card conditionKeyWord : conditionKeyWords) {
            // by -> conditions
            byPossibleNextCards.add(new NextCard(conditionKeyWord ,noOperator));
        }
        for (Card columnCard : columnCards) {
            byPossibleNextCards.add(new NextCard(columnCard ,true ,setOperatorWhere));
        }

        syntaxTable.put(by ,byPossibleNextCards);

        List<NextCard>  andOrPossibleNextCards = new ArrayList<>();
        for (Card conditionKeyWord : conditionKeyWords) {
            andOrPossibleNextCards.add(new NextCard(conditionKeyWord ,noOperator));
        }
        for (Card columnCard : columnCards) {
            andOrPossibleNextCards.add(new NextCard(columnCard ,checkOperatorWhere));
        }
        // and -> conditions
        syntaxTable.put(and ,andOrPossibleNextCards);
        // or -> conditions
        syntaxTable.put(or ,andOrPossibleNextCards);

        List<NextCard>  columnPossibleNextCards = new ArrayList<>();

        for (Card conditionKeyWord : conditionKeyWords) {
            // column -> condition
            columnPossibleNextCards.add(new NextCard(conditionKeyWord ,checkOperatorWhere));
        }
        // column -> and
        columnPossibleNextCards.add(new NextCard(and ,checkOperatorWhere));
        // column -> or
        columnPossibleNextCards.add(new NextCard(or ,checkOperatorWhere));
        // column -> desc
        columnPossibleNextCards.add(new NextCard(desc ,true ,checkOperatorOrder));
        // column -> asc
        columnPossibleNextCards.add(new NextCard(asc ,true ,checkOperatorOrder));
        // column -> orderBy
        columnPossibleNextCards.add(nextOrderByCard);

        for (Card columnCard : columnCards) {
            // column -> column
            columnPossibleNextCards.add(new NextCard(columnCard ,checkOperatorOrder));
            syntaxTable.put(columnCard ,columnPossibleNextCards);
        }

        List<NextCard>  conditionPossibleNextCards = new ArrayList<>();
        for (Card columnCard : columnCards) {
            // condition -> column
            conditionPossibleNextCards.add(new NextCard(columnCard ,true ,setOperatorWhere));
        }
        for (Card conditionKeyWord : conditionKeyWords) {
            syntaxTable.put(conditionKeyWord ,conditionPossibleNextCards);
        }

        List<NextCard>  descPossibleNextCards = new ArrayList<>();
        for (Card columnCard : columnCards) {
            // desc / asc -> column
            descPossibleNextCards.add(new NextCard(columnCard ,noOperator));
        }
        syntaxTable.put(desc ,descPossibleNextCards);
        syntaxTable.put(asc ,descPossibleNextCards);

        List<NextCard>  orderPossibleNextCards = new ArrayList<>();
        for (Card columnCard : columnCards) {
            // OrderBy -> column
            orderPossibleNextCards.add(new NextCard(columnCard ,noOperator));
        }
        syntaxTable.put(orderBy ,orderPossibleNextCards);

        return syntaxTable;
    }

}
