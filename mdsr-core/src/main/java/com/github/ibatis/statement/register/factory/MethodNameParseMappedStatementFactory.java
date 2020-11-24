package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import com.github.ibatis.statement.util.StringUtils;
import org.apache.ibatis.mapping.SqlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: junjie
 * @Date: 2020/11/23
 */
public class MethodNameParseMappedStatementFactory extends AbstractSelectMappedStatementFactory{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodNameParseMappedStatementFactory.class);

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData) {
        return false;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        return null;
    }

    /**
     * 单词
     */
    static class Card {

        static Card START_CARD = new Card(Type.KEY_WORD ,"");

        /**
         * 单词类型
         */
        public final Type type;

        /**
         * 值
         */
        public final String value;

        public Card(Type type, String value) {
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

    enum Type {
        /**
         * 关键字
         */
        KEY_WORD,
        /**
         * 列
         */
        COLUMN,
    }

    private Card getKeyWord(String keyWord){
        return new Card(Type.KEY_WORD ,keyWord);
    }

    private void parseMethodName(String methodName ,TableMateData tableMateData)
    {
        String[] columnNames = tableMateData.getColumnMateDataList().stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .toArray(length -> new String[length]);

        List<List<Card>> sentences = parseExpression(methodName, columnNames);

    }

    List<List<Card>> parseExpression(String expression ,String ... columnNames)
    {
        Map<Card ,List<NextCard>> syntaxTable = syntaxTable(columnNames);
        Map<String ,String> statusMap = new HashMap<>();

        long startTimeMillis = System.currentTimeMillis();
        List<List<Card>> sentences = parseSentences(expression, Card.START_CARD,
                statusMap, syntaxTable ,false);
        LOGGER.debug("expression [{}] ,{} ,time consuming {} ms" ,expression , sentences
                .stream()
                .map(sentence -> MessageFormat.format("possible semantics:[{0}]" ,sentence.stream()
                        .map(card -> card.value)
                        .reduce((value1 ,value2) -> value1 + " " + value2)
                        .get()))
                .reduce((value1 ,value2) -> value1 + " or " + value2)
                .orElse("don't have semantics") ,System.currentTimeMillis() - startTimeMillis);

        return sentences;
    }

    private List<List<Card>> parseSentences(String expression, Card currentCard, Map<String ,String> statusMap,
                                            Map<Card ,List<NextCard>> syntaxTable ,boolean isTermination)
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
                Map<String, String> nextStatusMap = nextCard.statusFunction.apply(statusMap);
                if (nextStatusMap != null && Collections.EMPTY_MAP != nextStatusMap){
                    List<List<Card>> afterSentences = parseSentences(expression,
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

    /**
     * 语法规则
     * @param columnNames
     * @return
     */
    private Map<Card ,List<NextCard>> syntaxTable(String ... columnNames)
    {
        Card select = getKeyWord("select");
        Card find = getKeyWord("find");

        Card by = getKeyWord("By");

        Card and = getKeyWord("And");
        Card or = getKeyWord("Or");

        Card eq = getKeyWord("Eq");
        Card notEq = getKeyWord("NotEq");
        Card between = getKeyWord("Between");
        Card notBetween = getKeyWord("NotBetween");
        Card gt = getKeyWord("Gt");
        Card lt = getKeyWord("Lt");
        Card ge = getKeyWord("Ge");
        Card in = getKeyWord("In");
        Card notIn = getKeyWord("NotIn");
        Card isNull = getKeyWord("IsNull");
        Card isNotNull = getKeyWord("IsNotNull");
        Card le = getKeyWord("Le");
        Card ne = getKeyWord("Ne");
        Card like = getKeyWord("Like");
        Card notLike = getKeyWord("NotLike");
        Card likeLeft = getKeyWord("LikeLeft");
        Card likeRight = getKeyWord("LikeRight");

        Card[] conditionKeyWords = new Card[]{eq ,notEq ,notBetween ,gt ,lt ,ge ,in ,between ,
                notIn ,isNull ,isNotNull ,le ,ne ,like ,notLike ,likeLeft ,likeRight};

        Card orderBy = getKeyWord("OrderBy");
        Card desc = getKeyWord("Desc");
        Card asc = getKeyWord("Asc");

        Card[] columnCards = Arrays.stream(columnNames)
                .distinct()
                .map(name -> StringUtils.camelUnderscoreToCase(name))
                .map(name -> new Card(Type.COLUMN, name))
                .toArray(s -> new Card[s]);

        Map<Card ,List<NextCard>> syntaxTable = new HashMap<>();
        Function<Map<String ,String> ,Map<String ,String>> noOperator = map -> map;

        List<NextCard> startPossibleNextCards = new ArrayList<>();
        startPossibleNextCards.add(new NextCard(select ,noOperator));
        startPossibleNextCards.add(new NextCard(find ,noOperator));
        syntaxTable.put(Card.START_CARD ,startPossibleNextCards);

        List<NextCard> selectPossibleNextCards = new ArrayList<>();
        // select /find -> by
        selectPossibleNextCards.add(new NextCard(by ,noOperator));
        for (Card conditionKeyWord : conditionKeyWords) {
            // by -> conditions
            selectPossibleNextCards.add(new NextCard(conditionKeyWord));
        }

        syntaxTable.put(select ,selectPossibleNextCards);
        syntaxTable.put(find ,selectPossibleNextCards);

        List<NextCard>  byPossibleNextCards = new ArrayList<>();
        for (Card conditionKeyWord : conditionKeyWords) {
            // by -> conditions
            byPossibleNextCards.add(new NextCard(conditionKeyWord ,noOperator));
        }
        syntaxTable.put(by ,byPossibleNextCards);

        List<NextCard>  andOrPossibleNextCards = new ArrayList<>();
        for (Card conditionKeyWord : conditionKeyWords) {
            andOrPossibleNextCards.add(new NextCard(conditionKeyWord ,noOperator));
        }
        // and -> conditions
        syntaxTable.put(and ,andOrPossibleNextCards);
        // or -> conditions
        syntaxTable.put(or ,andOrPossibleNextCards);

        List<NextCard>  columnPossibleNextCards = new ArrayList<>();
        String operatorStatus = "operator";
        String whereOperator = "where";

        Function<Map<String ,String> ,Map<String ,String>> checkOperatorWhere = statusMap ->
                whereOperator.equals(statusMap.get(operatorStatus)) ? statusMap : Collections.EMPTY_MAP;
        for (Card conditionKeyWord : conditionKeyWords) {
            // column -> condition
            columnPossibleNextCards.add(new NextCard(conditionKeyWord ,checkOperatorWhere));
        }

        Function<Map<String ,String> ,Map<String ,String>> setOperatorWhere = statusMap -> {
            Map<String ,String> copyMap = new HashMap<>(statusMap);
            copyMap.put(operatorStatus ,whereOperator);
            return copyMap;
        };
        // column -> and
        columnPossibleNextCards.add(new NextCard(and ,setOperatorWhere));
        // column -> or
        columnPossibleNextCards.add(new NextCard(or ,setOperatorWhere));

        String orderOperator = "order";
        Function<Map<String ,String> ,Map<String ,String>> checkOperatorOrder = statusMap ->
                orderOperator.equals(statusMap.get(operatorStatus)) ? statusMap : Collections.EMPTY_MAP;
        // column -> desc
        columnPossibleNextCards.add(new NextCard(desc ,true ,checkOperatorOrder));
        // column -> asc
        columnPossibleNextCards.add(new NextCard(asc ,true ,checkOperatorOrder));
        // column -> orderBy
        columnPossibleNextCards.add(new NextCard(orderBy ,statusMap -> {
            if ("1".equals(statusMap.get("orderFlag"))){
                return statusMap;
            }
            Map<String ,String> copyMap = new HashMap<>(statusMap);
            copyMap.put("orderFlag" ,"1");
            copyMap.put(operatorStatus ,orderOperator);
            return copyMap;
        }));

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
