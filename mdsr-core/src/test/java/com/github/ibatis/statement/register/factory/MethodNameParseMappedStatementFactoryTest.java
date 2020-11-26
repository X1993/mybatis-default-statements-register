package com.github.ibatis.statement.register.factory;

import org.junit.Assert;
import org.junit.Test;
import java.util.List;

public class MethodNameParseMappedStatementFactoryTest {

    MethodNameParseMappedStatementFactory factory = new MethodNameParseMappedStatementFactory();

    @Test
    public void parseExpression()
    {
        List<List<MethodNameParseMappedStatementFactory.Card>> sentences = factory.expressionParticiple(
                "selectByEqNameInCodeOrderByTimeDesc", "name", "code", "time");
        Assert.assertEquals(sentences.size() ,1);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Eq Name In Code OrderBy Time Desc");

        sentences = factory.expressionParticiple("selectByEqNameInInEqOrderByTimeDesc" ,"name","in_eq","time");
        Assert.assertEquals(sentences.size() ,1);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Eq Name In InEq OrderBy Time Desc");

        sentences = factory.expressionParticiple("selectEqInCodeOrderByTimeDesc" ,"name","code","time");
        Assert.assertEquals(sentences.size() ,0);

        sentences = factory.expressionParticiple("selectEqNameCodeOrderByTimeDesc" ,"name","code","time");
        Assert.assertEquals(sentences.size() ,0);

        sentences = factory.expressionParticiple("selectByEqNameOrLikeCodeOrderByTimeCodeDesc" ,
                "name","code","time");
        Assert.assertEquals(sentences.size() ,1);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Eq Name Or Like Code OrderBy Time Code Desc");

        sentences = factory.expressionParticiple("selectByEqNameInCodeOrderByTimeDesc" ,
                "name","code","name_in_code","time");
        Assert.assertEquals(sentences.size() ,2);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Eq Name In Code OrderBy Time Desc");
        Assert.assertEquals(sentences.get(1).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Eq NameInCode OrderBy Time Desc");

        sentences = factory.expressionParticiple("selectOrderByTimeDesc" ,"name","code","time");
        Assert.assertEquals(sentences.size() ,1);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select OrderBy Time Desc");

        sentences = factory.expressionParticiple("selectByNameAndEqCodeOrderByTimeDesc" ,"name","code","time");
        Assert.assertEquals(sentences.size() ,1);
        Assert.assertEquals(sentences.get(0).stream()
                .map(card -> card.value)
                .reduce((value1 ,value2) -> value1 + " " + value2)
                .get() ,"select By Name And Eq Code OrderBy Time Desc");
    }
}