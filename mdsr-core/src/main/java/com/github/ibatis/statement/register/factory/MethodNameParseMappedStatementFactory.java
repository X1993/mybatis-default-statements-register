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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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

        SyntaxTable syntaxTable = syntaxMap(entityMateData);
        List<Edge> sentence = expressionParticiple(mappedStatementMateData ,syntaxTable);
        if (sentence == null || sentence.isEmpty()){
            return Optional.empty();
        }

        DynamicParamsContext dynamicParamsContext = new DynamicParamsContext(mappedStatementMateData);
        String expression = getExpression(mappedStatementMateData);
        dynamicParamsContext.putExpression(expression);
        for (Edge edge : sentence) {
            edge.dynamicParamsFunction.accept(dynamicParamsContext);
            expression = expression.substring(edge.card.value.length());
            dynamicParamsContext.putExpression(expression);
        }

        List<SqlNode> sqlNodes = new ArrayList<>();
        SqlNode baseSqlNode = new StaticTextSqlNode(new StringBuilder("SELECT ")
                .append(dynamicParamsContext.selectContext)
                .append(" FROM `")
                .append(entityMateData.getTableMateData().getTableName())
                .append("` WHERE 1 = 1 ")
                .toString());
        sqlNodes.add(baseSqlNode);
        sqlNodes.addAll(dynamicParamsContext.conditionSqlNodes);

        OrderRule[] orderRules = dynamicParamsContext.orderRules
                .stream()
                .toArray(size -> new OrderRule[size]);

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

        SqlNode limitSqlNode = dynamicParamsContext.limitSqlNode;
        if (limitSqlNode != null){
            sqlNodes.add(limitSqlNode);
        }

        Configuration configuration = mappedStatementMateData.getConfiguration();
        return Optional.of(new DynamicSqlSource(configuration ,new MixedSqlNode(sqlNodes)));
    }

    private String getExpression(MappedStatementMateData mappedStatementMateData){
        return mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName().toUpperCase();
    }

    /**
     * 根据语法对表达式分词
     * @param mappedStatementMateData
     * @param syntaxTable
     * @return
     */
    List<Edge> expressionParticiple(MappedStatementMateData mappedStatementMateData ,SyntaxTable syntaxTable)
    {
        Context context = new Context(new HashMap<>() ,mappedStatementMateData);
        long startTimeMillis = System.currentTimeMillis();
        Map<Card, List<Edge>> cardEdgeMap = syntaxTable.getSyntaxTable();
        String expression = getExpression(mappedStatementMateData);
        List<List<Edge>> sentences = parseParticiple(expression, Edge.START_EDGE, context, cardEdgeMap);

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
            content.putExpression(expression);
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
                String subExpression = expression.substring(cardValue.length());
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

    private final String columnNameKey = "columnName";

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

    private Reference<SyntaxTable> syntaxTableCache;

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
     * @param entityMateData
     * @return
     */
    private SyntaxTable syntaxMap(EntityMateData entityMateData)
    {
        if (syntaxTableCache != null){
            SyntaxTable syntaxTable = syntaxTableCache.get();
            if (syntaxTable != null && entityMateData.getEntityClass().equals(syntaxTable.getEntityClass())){
                return syntaxTable;
            }
        }

        Map<Card ,List<Edge>> cardEdgeMap = new HashMap<>();

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

        Consumer<DynamicParamsContext> selectColumnsConsumer = context ->
                context.selectContext = entityMateData.getBaseColumnListSqlContent().toString();

        List<Edge> startPossibleEdges = new ArrayList<>();
        List<Edge> selectPossibleEdges = new ArrayList<>();
        List<Edge> countPossibleEdges = new ArrayList<>();
        List<Edge> conditionPossibleEdges = new ArrayList<>();
        List<Edge> columnPossibleEdges = new ArrayList<>();
        List<Edge> orderRulePossibleEdges = new ArrayList<>();
        List<Edge> orderPossibleEdges = new ArrayList<>();
        List<Edge> andPossibleEdges = new ArrayList<>();
        List<Edge> orPossibleEdges = new ArrayList<>();
        List<Edge> byPossibleEdges = new ArrayList<>();

        cardEdgeMap.put(Card.START_CARD ,startPossibleEdges);
        cardEdgeMap.put(find ,selectPossibleEdges);
        cardEdgeMap.put(count ,countPossibleEdges);
        cardEdgeMap.put(and ,andPossibleEdges);
        cardEdgeMap.put(or ,orPossibleEdges);
        cardEdgeMap.put(by ,byPossibleEdges);
        cardEdgeMap.put(desc ,orderRulePossibleEdges);
        cardEdgeMap.put(asc ,orderRulePossibleEdges);
        cardEdgeMap.put(orderBy ,orderPossibleEdges);
        cardEdgeMap.put(select ,selectPossibleEdges);

        startPossibleEdges.add(new Edge().nextCard(select));
        startPossibleEdges.add(new Edge().nextCard(find));
        startPossibleEdges.add(new Edge().nextCard(count));

        // select / find -> by
        selectPossibleEdges.add(new Edge().nextCard(by).updateContext(whereOperator)
                .dynamicParamsFunction(selectColumnsConsumer));
        // select / find -> count
        selectPossibleEdges.add(new Edge().nextCard(count));
        // select / find -> orderBy
        selectPossibleEdges.add(new Edge().nextCard(orderBy).updateContext(orderOperator)
                .dynamicParamsFunction(selectColumnsConsumer));
        // select / find -> limit
        Edge toLimitEdge = toLimitEdge(false);
        selectPossibleEdges.add(toLimitEdge);

        // count -> by
        countPossibleEdges.add(new Edge().nextCard(by).updateContext(whereOperator)
                .dynamicParamsFunction(context -> context.selectContext = "COUNT(0)"));

        // column -> and
        columnPossibleEdges.add(columnToAndEdge(and));
        // column -> or
        columnPossibleEdges.add(columnToOrEdge(or));
        // column -> condition
        columnPossibleEdges.addAll(toConditionEdges());
        // column -> orderBy
        columnPossibleEdges.add(columnToOrderEdge(orderBy));
        // column -> desc
        columnPossibleEdges.add(toOrderRuleEdge(desc ,OrderRule.Rule.DESC));
        // column -> asc
        columnPossibleEdges.add(toOrderRuleEdge(asc ,OrderRule.Rule.ASC));
        // column -> limit
        columnPossibleEdges.add(toLimitEdge(true));

        // asc / desc -> limit
        orderRulePossibleEdges.add(toLimitEdge);

        // condition -> or
        conditionPossibleEdges.add(new Edge().nextCard(or).isConform(wherePredicate));
        // condition -> and
        conditionPossibleEdges.add(new Edge().nextCard(and).isConform(wherePredicate));
        // condition -> orderBy
        conditionPossibleEdges.add(new Edge().nextCard(orderBy)
                .isConform(wherePredicate).updateContext(orderOperator));
        // condition -> limit
        conditionPossibleEdges.add(toLimitEdge);

        for (ColumnCard columnCard : columnCards) {
            // and -> column
            andPossibleEdges.add(andToColumn(columnCard));
            // or -> column
            orPossibleEdges.add(orToColumn(columnCard));
            // by -> column
            byPossibleEdges.add(andToColumn(columnCard));
            // column -> column
            columnPossibleEdges.add(columnToColumnEdge(columnCard));
            // orderBy -> column
            orderPossibleEdges.add(orderToColumnsEdge(columnCard));
            // desc / asc -> column
            orderRulePossibleEdges.add(orderRuleToColumnEdge(columnCard));
            // column -> ?
            cardEdgeMap.put(columnCard ,columnPossibleEdges);
        }

        for (Card conditionCard : conditionCards) {
            cardEdgeMap.put(conditionCard ,conditionPossibleEdges);
        }

        SyntaxTable syntaxTable = new SyntaxTable(entityMateData.getEntityClass(), cardEdgeMap);
        syntaxTableCache = new WeakReference<>(syntaxTable);

        return syntaxTable;
    }

    private Edge toLimitEdge(boolean fromColumn)
    {
        return new Edge().nextCard(new Card("Limit"))
                .isConform(context -> context.hasEnoughParams(fromColumn ? 2 : 1))
                .updateContext(context -> context.addEnoughParams(fromColumn ? 2 : 1))
                .isTermination(truePredicate)
                .dynamicParamsFunction(context -> {
                    if (fromColumn){
                        singleParameterCondition(EQ ,
                                i -> new StaticTextSqlNode(new StringBuilder(
                                        context.getParamPlaceholder(i))
                                        .toString()) ,
                                context);
                    }

                    context.addEnoughParams(1);
                    int paramIndex = context.getParamIndex();
                    Method mappedMethod = context.mappedStatementMateData.getMapperMethodMateData().getMappedMethod();

                    Class<?> paramType = mappedMethod.getParameterTypes()[paramIndex];
                    StringBuilder limit = new StringBuilder(" LIMIT ");
                    if (LimitParam.class.isAssignableFrom(paramType)){
                        limit.append("#{param").append(paramIndex + 1)
                                .append(".index} ,#{param").append(paramIndex + 1)
                                .append(".size}");
                    }else {
                        limit.append("#{param").append(paramIndex + 1).append("}");
                    }

                    SqlNode limitSqlNode = new StaticTextSqlNode(limit.toString());

                    If ifAnnotation = mappedMethod.getParameters()[paramIndex].getAnnotation(If.class);
                    if (ifAnnotation != null){
                        String defaultValue = ifAnnotation.otherwise();
                        String test = ifAnnotation.test();
                        test = test.replaceAll(StringUtils.escapeExprSpecialWord(If.PARAM_PLACEHOLDER),
                                context.getParamName().toString());
                        limitSqlNode = new IfSqlNode(limitSqlNode, test);
                        if (!If.NULL.equals(defaultValue)){
                            // choose 标签
                            limitSqlNode = new ChooseSqlNode(Arrays.asList(limitSqlNode) ,
                                    new StaticTextSqlNode(" LIMIT " + defaultValue));
                        }
                    }

                    context.limitSqlNode = limitSqlNode;
                });
    }

    /**
     * or -> column
     * @param columnCard
     * @return
     */
    private Edge orToColumn(ColumnCard columnCard){
        return toColumnEdge(columnCard ,true);
    }

    /**
     * and -> column
     * @param columnCard
     * @return
     */
    private Edge andToColumn(ColumnCard columnCard){
        return toColumnEdge(columnCard ,false);
    }

    private Edge toColumnEdge(ColumnCard columnCard ,boolean isOr){
        return new Edge().nextCard(columnCard)
                .isConform(wherePredicate)
                .updateContext(context -> {
                    if (columnCard.value.equals(context.getExpression())){
                        return context.addEnoughParams(1);
                    }
                    return context;
                })
                .isTermination(wherePredicate)
                .dynamicParamsFunction(context -> {
                    context.setOr(isOr).put(columnNameKey ,columnCard.columnName);
                    if (columnCard.value.equals(context.getExpression())){
                        singleParameterCondition(EQ ,i ->
                                new StaticTextSqlNode(new StringBuilder(
                                context.getParamPlaceholder(i)).toString()) ,context);
                    }
                });
    }

    /**
     * column -> And，default condition = EQ
     * @return
     */
    private Edge columnToAndEdge(Card card){
        return new Edge().nextCard(card)
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(context -> singleParameterCondition(
                        EQ ,i -> new StaticTextSqlNode(new StringBuilder(
                                context.getParamPlaceholder(i))
                                .toString()) ,context)
                        .setOr(false)
                );
    }

    /**
     * column -> Or，default condition = EQ
     * @return
     */
    private Edge columnToOrEdge(Card card){
        return new Edge().nextCard(card)
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .dynamicParamsFunction(context -> singleParameterCondition(
                        EQ ,i -> new StaticTextSqlNode(new StringBuilder(
                                context.getParamPlaceholder(i))
                                .toString()) ,context)
                        .setOr(true)
                );
    }

    /**
     * column -> OrderBy，default condition = EQ
     * @return
     */
    private Edge columnToOrderEdge(Card card)
    {
        return new Edge().nextCard(card)
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(orderOperator.andThen(context -> context.addEnoughParams(1)))
                .dynamicParamsFunction(context -> singleParameterCondition(
                        EQ ,i -> new StaticTextSqlNode(new StringBuilder(
                                context.getParamPlaceholder(i))
                                .toString()) ,context)
                        .setOr(false)
                );
    }

    /**
     * ? -> asc / desc
     * @param card
     * @param rule
     * @return
     */
    private Edge toOrderRuleEdge(Card card ,OrderRule.Rule rule){
        return new Edge().nextCard(card)
                .isConform(orderPredicate)
                .dynamicParamsFunction(dynamicParamsContext ->
                        Optional.ofNullable(dynamicParamsContext.remove(orderColumnsKey))
                                .ifPresent(columnNames -> dynamicParamsContext.addOrderRule(rule ,
                                        ((String) columnNames).split(","))))
                .isTermination(orderPredicate.and(truePredicate));
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
     * desc / asc -> column
     * @param columnCard
     * @return
     */
    private Edge orderRuleToColumnEdge(ColumnCard columnCard){
        return new Edge().nextCard(columnCard)
                .isConform(orderPredicate)
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
     * ? -> condition ,构建SqlNode
     * @return
     */
    private List<Edge> toConditionEdges()
    {
        List<Edge> toConditionPossibleEdges = new ArrayList<>();
        ConditionRule[] conditionRules = new ConditionRule[]{EQ ,NOT_EQ ,LT ,GT ,LE ,GE};
        for (ConditionRule conditionRule : conditionRules) {
            toConditionPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .isTermination(truePredicate)
                    .dynamicParamsFunction(context -> singleParameterCondition(
                            conditionRule ,
                            i -> new StaticTextSqlNode(new StringBuilder(
                                    context.getParamPlaceholder(i))
                                    .toString()) ,
                            context)
                    )
            );
        }

        conditionRules = new ConditionRule[]{LIKE ,NOT_LIKE};
        for (ConditionRule conditionRule : conditionRules) {
            toConditionPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .isTermination(truePredicate)
                    .dynamicParamsFunction(context -> singleParameterCondition(
                            conditionRule ,
                            i -> new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                    .append(context.getParamPlaceholder(i))
                                    .append(",'%')").toString()) ,
                            dv -> new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                    .append(dv)
                                    .append(",'%')").toString()) ,
                            context)
                    )
            );
        }

        toConditionPossibleEdges.add(new Edge()
                .nextCard(new Card(standard(LIKE_LEFT.name().toLowerCase())))
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .isTermination(truePredicate)
                .dynamicParamsFunction(context -> singleParameterCondition(
                        LIKE_LEFT ,
                        i -> new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                .append(context.getParamPlaceholder(i))
                                .append(")").toString()) ,
                        dv -> new StaticTextSqlNode(new StringBuilder("CONCAT('%',")
                                .append(dv)
                                .append(")").toString()),
                        context)
                )
        );

        toConditionPossibleEdges.add(new Edge()
                .nextCard(new Card(standard(LIKE_RIGHT.name().toLowerCase())))
                .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                .updateContext(context -> context.addEnoughParams(1))
                .isTermination(truePredicate)
                .dynamicParamsFunction(context -> singleParameterCondition(
                        LIKE_LEFT ,
                        i -> new StaticTextSqlNode(new StringBuilder("CONCAT(")
                                .append(context.getParamPlaceholder(i))
                                .append(",'%')").toString()) ,
                        dv -> new StaticTextSqlNode(new StringBuilder("CONCAT(")
                                .append(dv)
                                .append(",'%')").toString()) ,
                        context)
                )
        );

        conditionRules = new ConditionRule[]{NE ,IS_NULL ,NOT_NULL};
        for (ConditionRule conditionRule : conditionRules) {
            toConditionPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isTermination(truePredicate)
                    .dynamicParamsFunction(context -> context.addCondition(new StaticTextSqlNode(
                            new StringBuilder(context.isOr ? " OR " : "AND")
                                    .append(" `")
                                    .append(context.remove(columnNameKey))
                                    .append("` ")
                                    .append(conditionRule.expression)
                                    .toString()))));
        }

        Predicate<Context> isBetweenParamType = context -> BetweenParam.class.isAssignableFrom(
                context.mappedStatementMateData.getMapperMethodMateData().getMappedMethod()
                .getParameterTypes()[context.getParamIndex()]);

        conditionRules = new ConditionRule[]{BETWEEN ,NOT_BETWEEN};
        for (ConditionRule conditionRule : conditionRules) {
            toConditionPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(
                            isBetweenParamType.test(context) ? 1 : 2
                            )
                    ))
                    .updateContext(context -> context.addEnoughParams(
                            isBetweenParamType.test(context) ? 1 : 2
                            )
                    )
                    .isTermination(truePredicate)
                    .dynamicParamsFunction(context -> {
                        if (isBetweenParamType.test(context)){
                            singleParameterCondition(conditionRule ,
                                    i -> new StaticTextSqlNode(new StringBuilder(" #{param").append(i + 1)
                                            .append(".minVal} AND #{param").append(i + 1)
                                            .append(".maxVal}").toString()),
                                    context);
                        }else {
                            singleParameterCondition(
                                    null,
                                    i -> new StaticTextSqlNode(new StringBuilder(
                                            context.getParamPlaceholder(i))
                                            .toString()),
                                    //看作两个condition来处理
                                    singleParameterCondition(conditionRule,
                                            i -> new StaticTextSqlNode(new StringBuilder(
                                                    context.getParamPlaceholder(i))
                                                    .toString()),
                                            context).setOr(false)
                            );
                        }
                    })
            );
        }

        conditionRules = new ConditionRule[]{IN ,NOT_IN};
        for (ConditionRule conditionRule : conditionRules) {
            toConditionPossibleEdges.add(new Edge()
                    .nextCard(new Card(standard(conditionRule.name().toLowerCase())))
                    .isConform(wherePredicate.and(context -> context.hasEnoughParams(1)))
                    .updateContext(context -> context.addEnoughParams(1))
                    .isTermination(truePredicate)
                    .dynamicParamsFunction(context -> singleParameterCondition(
                            conditionRule ,
                            i -> new ForEachSqlNode(context.mappedStatementMateData.getConfiguration() ,
                                    new StaticTextSqlNode("#{item}"), collectionExpression(context ,i),
                                    null , "item" ,"(" ,")" ,",") ,
                            i -> collectionExpression(context ,i),
                            dv -> new StaticTextSqlNode(dv),
                            context)
                    )
            );
        }

        return toConditionPossibleEdges;
    }

    private String collectionExpression(DynamicParamsContext context ,int paramIndex)
    {
        Method mappedMethod = context.mappedStatementMateData.getMapperMethodMateData().getMappedMethod();
        Class<?> paramType = mappedMethod.getParameterTypes()[paramIndex];
        return mappedMethod.getParameterCount() > 1
                ? context.getParamName(paramIndex).toString() : paramType.isArray()
                ? "array" : "collection";
    }

    private void orderColumnSpliceConsumer(String columnName ,DynamicParamsContext dynamicParamsContext){
        dynamicParamsContext.put(orderColumnsKey ,
                Optional.ofNullable(dynamicParamsContext.get(orderColumnsKey))
                        .map(orderColumns -> orderColumns + ",")
                        .orElse("") + columnName);
    }

    private DynamicParamsContext singleParameterCondition(ConditionRule conditionRule ,
                                                          Function<Integer ,SqlNode> valueSqlNodeFunction,
                                                          Function<Integer ,String> testParamExpressionFunction,
                                                          Function<String ,SqlNode> defaultValueFunction,
                                                          DynamicParamsContext context)
    {
        StaticTextSqlNode keyRuleSqlNode = new StaticTextSqlNode(
                new StringBuilder(context.isOr ? " OR " : " AND ")
                        .append(Optional.ofNullable(context.remove(columnNameKey))
                                .map(column -> "`" + column + "` ")
                                .orElse(" "))
                        .append(Optional.ofNullable(conditionRule)
                                .map(rule -> rule.expression)
                                .orElse(" "))
                        .toString());

        context.addEnoughParams(1);
        int paramIndex = context.getParamIndex();
        If ifAnnotation = context.mappedStatementMateData.getMapperMethodMateData()
                .getMappedMethod().getParameters()[paramIndex].getAnnotation(If.class);

        SqlNode valueSqlNode = valueSqlNodeFunction.apply(paramIndex);
        SqlNode sqlNode = new MixedSqlNode(Arrays.asList(keyRuleSqlNode, valueSqlNode));
        if (ifAnnotation != null){
            String defaultValue = ifAnnotation.otherwise();
            String test = ifAnnotation.test();
            test = test.replaceAll(StringUtils.escapeExprSpecialWord(If.PARAM_PLACEHOLDER),
                    testParamExpressionFunction.apply(paramIndex));
            if (If.NULL.equals(defaultValue)){
                //if 标签
                sqlNode = new IfSqlNode(sqlNode, test);
            }else {
                // choose 标签
                sqlNode = new MixedSqlNode(Arrays.asList(keyRuleSqlNode,
                        new ChooseSqlNode(Arrays.asList(new IfSqlNode(valueSqlNode ,test)) ,
                                defaultValueFunction.apply(defaultValue))));
            }
        }

        context.addCondition(sqlNode);
        return context;
    }

    private DynamicParamsContext singleParameterCondition(ConditionRule conditionRule ,
                                                          Function<Integer ,SqlNode> valueSqlNodeFunction,
                                                          Function<String ,SqlNode> defaultValueFunction,
                                                          DynamicParamsContext context)
    {
        return singleParameterCondition(conditionRule, valueSqlNodeFunction,
                i -> context.getParamName(i).toString(), defaultValueFunction, context);
    }

    private DynamicParamsContext singleParameterCondition(ConditionRule conditionRule ,
                                                          Function<Integer ,SqlNode> valueSqlNodeFunction,
                                                          DynamicParamsContext context)
    {
        return singleParameterCondition(conditionRule ,valueSqlNodeFunction ,
                i -> context.getParamName(i).toString() ,dv -> new StaticTextSqlNode(dv) ,context);
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
            this.value = value.toUpperCase();
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

    class SyntaxTable {

        final Class<?> entityClass;

        final Map<Card ,List<Edge>> syntaxTable;

        public SyntaxTable(Class<?> entityClass, Map<Card, List<Edge>> syntaxTable) {
            this.entityClass = entityClass;
            this.syntaxTable = syntaxTable;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }

        public Map<Card, List<Edge>> getSyntaxTable() {
            return syntaxTable;
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

        final String expressionKey = "expression";

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
            if (paramCount == 0){
                return true;
            }
            int paramIndex = (int) status.getOrDefault(paramIndexKey, -1) + paramCount;
            return paramIndex < mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getParameterCount();
        }

        public int getParamIndex(){
            return (int) status.getOrDefault(paramIndexKey ,0);
        }

        public Context addEnoughParams(int paramCount){
            if (paramCount == 0){
                return this;
            }
            int paramIndex = (int) status.getOrDefault(paramIndexKey, -1) + paramCount;
            return cloneAndPut(paramIndexKey, paramIndex);
        }

        public Context putExpression(String expression) {
            return put(expressionKey ,expression);
        }

        public String getExpression(){
            return (String) required(expressionKey);
        }

    }

    class DynamicParamsContext extends Context{

        private String selectContext;

        private boolean isOr;

        private final List<SqlNode> conditionSqlNodes = new ArrayList<>();

        private final List<OrderRule> orderRules = new ArrayList<>();

        private SqlNode limitSqlNode;

        public DynamicParamsContext(MappedStatementMateData mappedStatementMateData) {
            super(mappedStatementMateData);
        }

        public boolean isOr() {
            return isOr;
        }

        public DynamicParamsContext setOr(boolean or) {
            isOr = or;
            return this;
        }

        public StringBuilder getParamName(int paramIndex){
            return new StringBuilder("param").append(paramIndex + 1);
        }

        public StringBuilder getParamName(){
            return getParamName(getParamIndex());
        }

        public StringBuilder getParamPlaceholder(int paramIndex){
            return new StringBuilder("#{").append(getParamName(paramIndex)).append("}");
        }

        public StringBuilder getParamPlaceholder(){
            return getParamPlaceholder(getParamIndex());
        }

        @Override
        public Context cloneAndPut(Object key, Object value) {
            return put(key ,value);
        }

        public DynamicParamsContext addCondition(SqlNode sqlNode){
            conditionSqlNodes.add(sqlNode);
            return this;
        }

        public DynamicParamsContext addOrderRule(OrderRule.Rule rule, String ... columns) {
            for (String column : columns) {
                this.orderRules.add(new OrderRule(column ,rule));
            }
            return this;
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
