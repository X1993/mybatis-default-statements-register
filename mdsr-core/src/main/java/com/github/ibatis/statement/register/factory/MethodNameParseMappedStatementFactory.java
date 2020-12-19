package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
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
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.github.ibatis.statement.mapper.param.ConditionRule.*;
import static com.github.ibatis.statement.register.factory.MethodNameParseMappedStatementFactory.Card.START_CARD;

/**
 * 特定规则的方法
 * @see <a href="https://github.com/X1993/mybatis-default-statements-register/blob/master/mdsr-core/method-name-parse-rule.png">方法名解析规则</a>
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
     * 根据方法解析SqlSource
     * @param mappedStatementMateData
     * @return
     */
    private Optional<SqlSource> resolvedSqlNode(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        Map<Card, List<Edge>> syntaxMap = syntaxMap(entityMateData);
        List<Edge> sentence = expressionParticiple(mappedStatementMateData ,syntaxMap);
        if (sentence == null || sentence.isEmpty()){
            return Optional.empty();
        }

        DynamicParamsContext dynamicParamsContext = new DynamicParamsContext(mappedStatementMateData);
        for (Edge edge : sentence) {
            edge.dynamicParamsFunction.accept(dynamicParamsContext);
        }
        DynamicParams dynamicParams = dynamicParamsContext.dynamicParams;

        List<SqlNode> sqlNodes = new ArrayList<>();
        SqlNode baseSqlNode = new StaticTextSqlNode(new StringBuilder("SELECT ")
                .append(dynamicParamsContext.selectContext)
                .append(" FROM `")
                .append(entityMateData.getTableMateData().getTableName())
                .append("` WHERE ")
                .toString());
        sqlNodes.add(baseSqlNode);

        List<ConditionParam> params = dynamicParams.getWhereConditions().getParams();
        for (ConditionParam param : params) {
            sqlNodes.add(new StaticTextSqlNode(new StringBuilder(" `")
                    .append(param.getKey()).append("` ")
                    .append(param.getRule().expression).toString()));
            sqlNodes.add((SqlNode) param.getValue());
            sqlNodes.add(new StaticTextSqlNode(param.isOr() ? " OR " : " AND "));
        }
        sqlNodes.add(new StaticTextSqlNode(" 1 = 1 "));

        OrderRule[] orderRules = dynamicParams.getOrderRules()
                .toArray(new OrderRule[dynamicParams.getOrderRules().size()]);
        int length = orderRules.length;
        if (length > 0) {
            sqlNodes.add(new StaticTextSqlNode(" ORDER BY "));
            for (int i = 0; i < length - 1; i++) {
                OrderRule orderRule = orderRules[i];
                sqlNodes.add(new StaticTextSqlNode(new StringBuilder(" `")
                        .append(orderRule.getKey()).append("` ")
                        .append(orderRule.getRule().name()).append(",").toString()));
            }
            OrderRule orderRule = orderRules[length - 1];
            sqlNodes.add(new StaticTextSqlNode(new StringBuilder(" `")
                    .append(orderRule.getKey()).append("` ")
                    .append(orderRule.getRule().name()).toString()));
        }

        Configuration configuration = mappedStatementMateData.getConfiguration();
        return Optional.of(new DynamicSqlSource(configuration ,new MixedSqlNode(sqlNodes)));
    }

    /**
     * 根据语法对表达式分词
     * @param mappedStatementMateData
     * @param syntaxMap
     * @return
     */
    List<Edge> expressionParticiple(MappedStatementMateData mappedStatementMateData ,Map<Card, List<Edge>> syntaxMap)
    {
        Context context = new Context(new HashMap<>() ,mappedStatementMateData);
        long startTimeMillis = System.currentTimeMillis();
        String expression = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        List<List<Edge>> sentences = parseParticiple(expression, Edge.START_EDGE, context, syntaxMap);

        for (List<Edge> sentence : sentences) {
            //removed Card.START_CARD
            sentence.remove(0);
        }

        LOGGER.debug("expression [{}] reasonable word segmentation plan: {} ,time consuming {} ms" ,
                expression , sentences.stream()
                .map(sentence -> "[" + sentence.stream()
                        .map(edge -> edge.card.toString())
                        .reduce((card1 ,card2) -> card1 + "," + card2)
                        .get() + "]")
                .reduce((value1 ,value2) -> value1 + " or " + value2)
                .orElse("don't have reasonable participle program") ,
                System.currentTimeMillis() - startTimeMillis);

        return reasonableSentence(sentences ,expression);
    }

    /**
     * 从多个合理的分词方案中获取最合适的一个
     * @param sentences
     * @param expression
     * @return
     */
    private List<Edge> reasonableSentence(List<List<Edge>> sentences ,String expression){
        if (sentences.size() > 1){
            throw new IllegalArgumentException(MessageFormat.format(
                    "expression [{0}] reasonable word segmentation plan: {1}" ,expression ,
                    sentences.stream()
                            .map(sentence -> "[" + sentence.stream()
                                    .map(edge -> edge.card.toString())
                                    .reduce((card1 ,card2) -> card1 + "," + card2)
                                    .get() + "]")
                            .reduce((value1 ,value2) -> value1 + " or " + value2)
                            .get()));
        }
        return sentences.isEmpty() ? Collections.EMPTY_LIST : sentences.get(0);
    }

    /**
     * 根据语法表分词
     * @param expression
     * @param currentEdge
     * @param content
     * @param syntaxMap
     * @return
     */
    private List<List<Edge>> parseParticiple(String expression, Edge currentEdge,
                                             Context content, Map<Card, List<Edge>> syntaxMap)
    {
        List<List<Edge>> sentences = new ArrayList<>();
        Card currentCard = currentEdge.card;
        String cardValue = currentCard.value;
        if (expression.startsWith(cardValue) && currentEdge.isConform.test(content)){
            String subExpression = expression.substring(cardValue.length());
            Context newContext = currentEdge.updateContext.apply(content);
            if (expression.length() == cardValue.length()){
                if (currentEdge.isTermination.test(content) && !newContext.hasEnoughParams(1)) {
                    //允许作为终结词且没有多余的参数
                    List<Edge> sentence = new ArrayList<>();
                    sentence.add(currentEdge);
                    sentences.add(sentence);
                }
                return sentences;
            }
            List<Edge> edges = syntaxMap.get(currentCard);
            if (edges != null && edges.size() > 0) {
                //TODO 可以使用前缀树提高匹配效率
                for (Edge edge : edges) {
                    List<List<Edge>> afterSentences = parseParticiple(subExpression, edge, newContext, syntaxMap);
                    for (List<Edge> afterSentence : afterSentences) {
                        afterSentence.add(0, currentEdge);
                        sentences.add(afterSentence);
                    }
                }
            }
        }
        return sentences;
    }

    private String standard(String str){
        return StringUtils.camelUnderscoreToCase(str ,true);
    }

    private final String conditionRuleKey = "conditionRule";

    private final String conditionValueKey = "conditionValue";

    private final String operatorKey = "operator";

    private final String ordering = "ordering";

    private final String filtering = "filtering";

    private final String orderColumnsKey = "orderColumns";

    private final Predicate<Context> orderPredicate = context -> context.match(operatorKey ,ordering);

    private final Function<Context ,Context> orderOperator = context -> context.cloneAndPut(operatorKey ,ordering);

    private final Predicate<Context> wherePredicate = context -> context.match(operatorKey ,filtering);

    private final Function<Context ,Context> whereOperator = context -> context.cloneAndPut(operatorKey ,filtering);

    private final Predicate<Context> truePredicate = context -> true;

    private final Map<CacheKey ,Map<Card ,List<Edge>>> edgeMapCache = new WeakHashMap<>();

    class CacheKey{

        private Class<?> entityClass;

        public CacheKey(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(entityClass, cacheKey.entityClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityClass);
        }
    }

    /**
     * 语法规则
     * @see <a href="https://github.com/X1993/mybatis-default-statements-register/blob/master/mdsr-core/method-name-parse-rule.png">方法名解析规则</a>
     * @param entityMateData
     * @return
     */
    private Map<Card ,List<Edge>> syntaxMap(EntityMateData entityMateData)
    {
        CacheKey cacheKey = new CacheKey(entityMateData.getEntityClass());
        Map<Card, List<Edge>> syntaxTable = edgeMapCache.get(cacheKey);
        if (syntaxTable != null){
            return syntaxTable;
        }
        syntaxTable = new HashMap<>();

        Set<ColumnCard> columnCards = entityMateData
                .getTableMateData().getColumnMateDataList()
                .stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .map(columnName -> new ColumnCard(standard(columnName) ,columnName))
                .collect(Collectors.toSet());

        for (ColumnPropertyMapping columnPropertyMapping : entityMateData.getColumnPropertyMappings().values()) {
            columnCards.add(new ColumnCard(standard(columnPropertyMapping.getPropertyName()) ,columnPropertyMapping.getColumnName()));
        }

        Card select = new Card("select");
        Card count = new Card("Count");
        Card find = new Card("find");
        Card by = new Card("By");
        Card and = new Card("And");
        Card or = new Card("Or");
        Card orderBy = new Card("OrderBy");
        Card desc = new Card("Desc");
        Card asc = new Card("Asc");

        Card[] conditionCards = Stream.of(ConditionRule.values())
                .map(conditionRule -> new Card(standard(conditionRule.name().toLowerCase())))
                .toArray(length -> new Card[length]);

        List<Edge> startPossibleEdges = new ArrayList<>();
        startPossibleEdges.add(new Edge().nextCard(select));
        startPossibleEdges.add(new Edge().nextCard(find));
        syntaxTable.put(Card.START_CARD ,startPossibleEdges);

        Consumer<DynamicParamsContext> selectColumnsConsumer = context ->
                context.selectContext = entityMateData.getBaseColumnListSqlContent().toString();

        List<Edge> selectPossibleEdges = new ArrayList<>();
        // select / find -> by
        selectPossibleEdges.add(new Edge().nextCard(by).updateContext(whereOperator)
                .dynamicParamsFunction(selectColumnsConsumer));
        // select / find -> count
        selectPossibleEdges.add(new Edge().nextCard(count)
                .dynamicParamsFunction(context -> context.selectContext = "COUNT(0)"));
        // select / find -> orderBy
        Edge nextOrderByEdge = new Edge().nextCard(orderBy).updateContext(orderOperator)
                .dynamicParamsFunction(selectColumnsConsumer);
        selectPossibleEdges.add(nextOrderByEdge);

        syntaxTable.put(select ,selectPossibleEdges);
        syntaxTable.put(find ,selectPossibleEdges);

        List<Edge> countPossibleEdges = new ArrayList<>();
        // count -> by
        countPossibleEdges.add(new Edge().nextCard(by).updateContext(whereOperator));
        syntaxTable.put(count ,countPossibleEdges);

        List<Edge> byPossibleEdges = new ArrayList<>();
        // by -> condition
        List<Edge> toConditionEdges = toConditionEdges();
        byPossibleEdges.addAll(toConditionEdges);
        for (ColumnCard columnCard : columnCards) {
            // by -> column
            byPossibleEdges.add(andToColumn(columnCard));
        }

        syntaxTable.put(by ,byPossibleEdges);

        List<Edge> andPossibleEdges = new ArrayList<>();
        List<Edge> orPossibleEdges = new ArrayList<>();
        syntaxTable.put(and ,andPossibleEdges);
        syntaxTable.put(or ,orPossibleEdges);

        // and -> condition
        andPossibleEdges.addAll(toConditionEdges);
        // or -> condition
        orPossibleEdges.addAll(toConditionEdges()
                .stream()
                .map(edge -> edge.dynamicParamsFunction(edge.dynamicParamsFunction.andThen(dynamicParamsContext ->
                        Optional.ofNullable(dynamicParamsContext.dynamicParams.getWhereConditions().getParams())
                                .ifPresent(params -> params.get(params.size() - 1).setOr(true)))))
                .collect(Collectors.toList()));

        for (ColumnCard columnCard : columnCards) {
            // and -> column
            andPossibleEdges.add(andToColumn(columnCard));
            // or -> column
            orPossibleEdges.add(orToColumn(columnCard));
        }

        List<Edge> columnPossibleEdges = new ArrayList<>();
        // column -> condition
        columnPossibleEdges.addAll(toConditionEdges);

        // column -> and
        columnPossibleEdges.add(columnToAndEdge());
        // column -> or
        columnPossibleEdges.add(columnToOrEdge());

        // column -> desc
        columnPossibleEdges.add(toOrderRuleEdge(desc ,OrderRule.Rule.DESC));
        // column -> asc
        columnPossibleEdges.add(toOrderRuleEdge(asc ,OrderRule.Rule.ASC));
        // column -> orderBy
        columnPossibleEdges.add(nextOrderByEdge);
        for (ColumnCard columnCard : columnCards) {
            // column -> column
            columnPossibleEdges.add(columnToColumnEdge(columnCard));
        }

        List<Edge> conditionPossibleEdges = new ArrayList<>();
        List<Edge> orderRulePossibleEdges = new ArrayList<>();
        List<Edge> orderPossibleEdges = new ArrayList<>();

        for (ColumnCard columnCard : columnCards) {
            // orderBy -> column
            orderPossibleEdges.add(orderToColumnsEdge(columnCard));
            // desc / asc -> column
            orderRulePossibleEdges.add(orderRuleToColumnEdge(columnCard));
            // condition -> column
            conditionPossibleEdges.add(conditionToColumnEdge(columnCard));
            // column -> ?
            syntaxTable.put(columnCard ,columnPossibleEdges);
        }

        for (Card conditionCard : conditionCards) {
            syntaxTable.put(conditionCard ,conditionPossibleEdges);
        }

        syntaxTable.put(desc ,orderRulePossibleEdges);
        syntaxTable.put(asc ,orderRulePossibleEdges);
        syntaxTable.put(orderBy ,orderPossibleEdges);

        edgeMapCache.put(cacheKey ,syntaxTable);
        return syntaxTable;
    }

    /**
     * or -> column
     * @param columnCard
     * @return
     */
    private Edge orToColumn(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(dynamicParamsContext ->
                        Optional.ofNullable(nextColumnDynamicParamsFunction(columnCard ,dynamicParamsContext)
                                .dynamicParams.getWhereConditions().getParams())
                                .ifPresent(params -> params.get(params.size() - 1).setOr(true)))
                .isTermination(truePredicate);
    }

    /**
     * and -> column
     * @param columnCard
     * @return
     */
    private Edge andToColumn(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(context -> nextColumnDynamicParamsFunction(columnCard ,context))
                .isTermination(truePredicate);
    }

    /**
     * column -> And
     * @return
     */
    private Edge columnToAndEdge(){
        return new Edge().nextCard(new Card(standard("And")))
                .isConform(wherePredicate);
    }

    /**
     * column -> Or
     * @return
     */
    private Edge columnToOrEdge(){
        return new Edge().nextCard(new Card(standard("Or")))
                .dynamicParamsFunction(dynamicParamsContext ->
                        Optional.ofNullable(dynamicParamsContext.dynamicParams.getWhereConditions().getParams())
                                .ifPresent(params -> params.get(params.size() - 1).setOr(true)))
                .isConform(wherePredicate);
    }

    /**
     * ? -> asc / desc
     * @param card
     * @param rule
     * @return
     */
    private Edge toOrderRuleEdge(Card card ,OrderRule.Rule rule){
        return new Edge().nextCard(card)
                .dynamicParamsFunction(dynamicParamsContext ->
                        Optional.ofNullable(dynamicParamsContext.remove(orderColumnsKey))
                                .ifPresent(columnNames -> dynamicParamsContext.dynamicParams.addOrderRule(rule ,
                                        ((String) columnNames).split(","))))
                .isTermination(truePredicate)
                .isConform(orderPredicate);
    }

    /**
     * column -> column
     * @param columnCard
     * @return
     */
    private Edge columnToColumnEdge(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .isConform(orderPredicate)
                .dynamicParamsFunction(dynamicParamsContext ->
                        orderColumnSpliceConsumer(columnCard.columnName ,dynamicParamsContext));
    }

    /**
     * condition -> column
     * @param columnCard
     * @return
     */
    private Edge conditionToColumnEdge(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .isConform(wherePredicate)
                .dynamicParamsFunction(context -> nextColumnDynamicParamsFunction(columnCard ,context))
                .isTermination(truePredicate);
    }

    /**
     * desc / asc -> column
     * @param columnCard
     * @return
     */
    private Edge orderRuleToColumnEdge(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .dynamicParamsFunction(dynamicParamsContext ->
                        orderColumnSpliceConsumer(columnCard.columnName ,dynamicParamsContext));
    }

    /**
     * order by  -> column
     * @param columnCard
     * @return
     */
    private Edge orderToColumnsEdge(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .dynamicParamsFunction(dynamicParamsContext ->
                        orderColumnSpliceConsumer(columnCard.columnName ,dynamicParamsContext));
    }

    /**
     * ? -> condition
     * @return
     */
    private List<Edge> toConditionEdges()
    {
        List<Edge> byPossibleEdges = new ArrayList<>();
        ConditionRule[] conditionRules = new ConditionRule[]{EQ ,NOT_EQ ,LT ,GT ,LE ,GE};
        for (ConditionRule conditionRule : conditionRules) {
            byPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .dynamicParamsFunction(context -> context.put(conditionRuleKey, conditionRule)
                            .put(conditionValueKey ,new StaticTextSqlNode(
                                    new StringBuilder(context.getParamPlaceholder())
                                    .toString()))));
        }

        conditionRules = new ConditionRule[]{LIKE ,NOT_LIKE};
        for (ConditionRule conditionRule : conditionRules) {
            byPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .dynamicParamsFunction(context -> context.put(conditionRuleKey, conditionRule)
                            .put(conditionValueKey ,new StaticTextSqlNode(
                                    new StringBuilder("CONCAT('%',")
                                    .append(context.getParamPlaceholder())
                                    .append(",'%')").toString()))));
        }

        byPossibleEdges.add(new Edge()
                .nextCard(new Card(standard(LIKE_LEFT.name().toLowerCase())))
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(context -> context.put(conditionRuleKey, LIKE_LEFT)
                        .put(conditionValueKey ,new StaticTextSqlNode(
                                new StringBuilder("CONCAT('%',")
                                .append(context.getParamPlaceholder())
                                .append(")").toString()))));

        byPossibleEdges.add(new Edge()
                .nextCard(new Card(standard(LIKE_RIGHT.name().toLowerCase())))
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(context -> context.put(conditionRuleKey, LIKE_RIGHT)
                        .put(conditionValueKey ,new StaticTextSqlNode(
                                new StringBuilder("CONCAT(")
                                .append(context.getParamPlaceholder())
                                .append(",'%')").toString()))));

        conditionRules = new ConditionRule[]{NE ,IS_NULL ,NOT_NULL};
        for (ConditionRule conditionRule : conditionRules) {
            byPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .dynamicParamsFunction(context -> context.put(conditionRuleKey, conditionRule)
                            .put(conditionValueKey ,new StaticTextSqlNode(""))));
        }

        conditionRules = new ConditionRule[]{BETWEEN ,NOT_BETWEEN};
        for (ConditionRule conditionRule : conditionRules) {
            byPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(2)))
                    .updateContext(context -> context.addEnoughParams(2))
                    .dynamicParamsFunction(context -> context.put(conditionRuleKey, conditionRule)
                            .put(conditionValueKey ,new StaticTextSqlNode(
                                    new StringBuilder(context.getParamPlaceholder())
                                    .append(" AND ")
                                    .append(context.getParamPlaceholder())
                                    .toString()))));
        }

        conditionRules = new ConditionRule[]{IN ,NOT_IN};
        for (ConditionRule conditionRule : conditionRules) {
            byPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .dynamicParamsFunction(context -> context.put(conditionRuleKey, conditionRule)
                            .put(conditionValueKey ,conditionInSqlNode(context))));
        }

        return byPossibleEdges;
    }

    private ForEachSqlNode conditionInSqlNode(DynamicParamsContext context){
        Method mappedMethod = context.mappedStatementMateData.getMapperMethodMateData().getMappedMethod();
        Class<?> paramType = mappedMethod.getParameterTypes()[(int) context.get(context.paramIndexKey ,0)];
        String collectionExpression = mappedMethod.getParameterCount() > 1 ? context.getParamPlaceholder().toString() :
                paramType.isArray() ? "array" : "collection";
        Configuration configuration = context.mappedStatementMateData.getConfiguration();
        return new ForEachSqlNode(configuration ,new TrimSqlNode(configuration , new StaticTextSqlNode("#{item}") ,
                null , null ,null ,",") ,collectionExpression  ,
                null , "item" ,"(" ,")" ,",");
    }

    private void orderColumnSpliceConsumer(String columnName ,DynamicParamsContext dynamicParamsContext){
        dynamicParamsContext.put(orderColumnsKey ,
                Optional.ofNullable(dynamicParamsContext.get(orderColumnsKey))
                        .map(orderColumns -> orderColumns + ",")
                        .orElse("") + columnName);
    }

    private DynamicParamsContext nextColumnDynamicParamsFunction(ColumnCard columnCard ,
                                                                 DynamicParamsContext context)
    {
        context.addWhereCondition(columnCard.columnName ,
                        //如果没有定义过滤规则，默认EQ
                        (ConditionRule) Optional.ofNullable(context.remove(conditionRuleKey)).orElse(EQ) ,
                        Optional.ofNullable((SqlNode) context.remove(conditionValueKey))
                                .orElseGet(() -> new StaticTextSqlNode(
                                        new StringBuilder(context.getParamPlaceholder())
                                                .toString())));

        return context;
    }

    /**
     * 单词
     */
    static class Card {

        static final Card START_CARD = new Card("");

        /**
         * 单词类型
         */
        public final CardType type;

        /**
         * 值
         */
        public final String value;

        public Card(String value) {
            this(CardType.KEY_WORD ,value);
        }

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
            return type == card.type &&
                    Objects.equals(value, card.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }

        public CardType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Card{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    static class ColumnCard extends Card{

        public final String columnName;

        public ColumnCard(String value ,String columnName) {
            super(CardType.COLUMN ,value);
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    enum CardType {
        KEY_WORD,
        COLUMN,
    }

    class Context{

        /**
         * 每次做了修改之前需要copy，如果使用同一个Context回溯的时候原状态可能会改变，且状态值应该确保不可变或深拷贝
         */
        private final Map status;

        protected final MappedStatementMateData mappedStatementMateData;

        final String paramIndexKey = "paramIndex";

        public Context(MappedStatementMateData mappedStatementMateData) {
            this(new HashMap<>() ,mappedStatementMateData);
        }

        public Context(Map<?, ?> status ,MappedStatementMateData MappedStatementMateData) {
            this.status = status;
            this.mappedStatementMateData = MappedStatementMateData;
        }

        public Context(Context context){
            this(new HashMap(context.status) ,context.mappedStatementMateData);
        }

        public boolean match(Object key, Object value){
            Object oldValue = status.get(key);
            if (oldValue == null){
                return value == null ? true : value.equals(oldValue);
            }
            return oldValue.equals(value);
        }

        public Context cloneAndPut(Object key , Object value){
            //每次做了修改之前需要copy，如果使用同一个Context回溯的时候原状态可能会改变
            Context newContext = new Context(this);
            newContext.status.put(key ,value);
            return newContext;
        }

        protected Context put(Object key ,Object value){
            this.status.put(key ,value);
            return this;
        }

        public Object get(Object key){
            return status.get(key);
        }

        public Object get(Object key ,Object defaultValue){
            return status.getOrDefault(key ,defaultValue);
        }

        public Object remove(Object key){
            return status.remove(key);
        }

        public Object required(Object key){
            return Optional.ofNullable(status.get(key))
                    .orElseThrow(() -> new IllegalArgumentException(
                            MessageFormat.format("required key [{0}] is null" ,key)));

        }

        public boolean hasEnoughParams(int paramCount){
            int paramIndex = (int) status.getOrDefault(paramIndexKey, -1) + paramCount;
            return paramIndex < mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getParameterCount();
        }

        public int getParamIndexAndAdd(int paramCount){
            int paramIndex = (int) status.getOrDefault(paramIndexKey, -1) + paramCount;
            put(paramIndexKey, paramIndex);
            return paramIndex;
        }

        public Context addEnoughParams(int paramCount){
            int paramIndex = (int) status.getOrDefault(paramIndexKey, -1) + paramCount;
            return cloneAndPut(paramIndexKey, paramIndex);
        }

    }

    class DynamicParamsContext extends Context{

        private DynamicParams dynamicParams;

        private String selectContext;

        public DynamicParamsContext(MappedStatementMateData mappedStatementMateData) {
            super(mappedStatementMateData);
            this.dynamicParams = new DynamicParams();
            this.dynamicParams.where(new ConditionParams());
        }

        public StringBuilder getParamPlaceholder(){
            return new StringBuilder("#{param").append(getParamIndexAndAdd(1) + 1).append("}");
        }

        /**
         * @see #put(Object, Object)
         * @param key
         * @param value
         * @return
         */
        @Override
        public Context cloneAndPut(Object key, Object value) {
            throw new IllegalStateException();
        }

        public void addWhereCondition(String columnName ,ConditionRule conditionRule ,SqlNode sqlNode ,boolean isOr){
            ConditionParam conditionParam = new ConditionParam(columnName, conditionRule, sqlNode);
            conditionParam.setOr(isOr);
            dynamicParams.getWhereConditions().addConditionParam(conditionParam);
        }

        public void addWhereCondition(String columnName ,ConditionRule conditionRule ,SqlNode sqlNode){
            addWhereCondition(columnName ,conditionRule ,sqlNode ,false);
        }

    }

    /**
     * 有向边
     */
    static class Edge{

        static Edge START_EDGE = new Edge().nextCard(START_CARD);

        /**
         * 下个单词
         */
        private Card card;

        /**
         * 是否符合语法
         * false：回溯
         */
        private Predicate<Context> isConform = context -> true;

        /**
         * 状态更新
         */
        private Function<Context ,Context> updateContext = context -> context;

        /**
         * 是否允许作为终止符
         * true：如果子表达式与{@link Edge#card}完全匹配则找到一个符合语法规则的分词方案
         */
        private Predicate<Context> isTermination = context -> false;

        /**
         * 构建dynamicParams
         */
        private Consumer<DynamicParamsContext> dynamicParamsFunction = (dynamicParamsContext) -> {};

        public Edge() {
        }

        public Edge nextCard(Card nextCard) {
            this.card = nextCard;
            return this;
        }

        public Edge isConform(Predicate<Context> isConform) {
            this.isConform = isConform;
            return this;
        }

        public Edge updateContext(Function<Context, Context> updateContext) {
            this.updateContext = updateContext;
            return this;
        }

        public Edge isTermination(Predicate<Context> isTermination) {
            this.isTermination = isTermination;
            return this;
        }

        public Edge dynamicParamsFunction(Consumer<DynamicParamsContext> dynamicParamsFunction) {
            this.dynamicParamsFunction = dynamicParamsFunction;
            return this;
        }

        @Override
        public String toString() {
            return "Edge{" +
                    "card=" + card +
                    '}';
        }
    }

}
