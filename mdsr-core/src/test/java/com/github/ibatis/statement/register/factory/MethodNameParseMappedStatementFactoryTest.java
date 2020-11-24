package com.github.ibatis.statement.register.factory;

import org.junit.Test;

public class MethodNameParseMappedStatementFactoryTest {

    MethodNameParseMappedStatementFactory factory = new MethodNameParseMappedStatementFactory();

    @Test
    public void parseExpression() {
        factory.parseExpression("selectByEqNameInCodeOrderByTimeDesc" ,"name","code","time");
        factory.parseExpression("selectByEqNameInInEqOrderByTimeDesc" ,"name","inEq","time");
        factory.parseExpression("selectEqNameInCodeOrderByTimeDesc" ,"name","code","time");
        factory.parseExpression("selectEqInCodeOrderByTimeDesc" ,"name","code","time");
        factory.parseExpression("selectEqNameCodeOrderByTimeDesc" ,"name","code","time");
        factory.parseExpression("selectEqNameOrLikeCodeOrderByTimeCodeDesc" ,"name","code","time");
//        factory.parseExpression("selectByEqNameInCodeOrderByTimeDesc" ,"name","code","time");
//        factory.parseExpression("selectByEqNameInCodeOrderByTimeDesc" ,"name","code","time");
    }
}