package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import org.apache.ibatis.session.SqlSession;
import java.util.function.Consumer;

/**
 * {@link DefaultStatementAutoRegister#registerDefaultMappedStatement(SqlSession, Class)}
 * 缺失{@link MappedStatementMateData}注册失败时执行
 * @Author: junjie
 * @Date: 2020/10/13
 */
public interface MappedStatementRegisterFailureConsumer extends Consumer<MappedStatementMateData> {
}
