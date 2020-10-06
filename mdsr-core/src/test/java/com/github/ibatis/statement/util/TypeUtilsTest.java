package com.github.ibatis.statement.util;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.base.core.matedata.RootMapperMethodMateData;
import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.util.reflect.GenericArrayTypeImpl;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.*;
import java.util.*;

public class TypeUtilsTest {

    interface InterfaceA<T ,C extends MapperMethodMateData, X> extends KeyTableMapper<T ,C> {}

    interface InterfaceB<T> extends List<String>, KeyTableMapper<T, RootMapperMethodMateData> {}

    interface InterfaceC extends List<String>, KeyTableMapper<String, RootMapperMethodMateData> {}

    interface InterfaceD<K ,T> extends KeyTableMapper<K ,Map<String ,T>> {}

    interface InterfaceD2 extends InterfaceD<String, List<Integer>> {}

    interface InterfaceE<T> extends KeyTableMapper<String ,T[]> {}

    interface InterfaceE2 extends InterfaceE<List<String>>{}

    interface InterfaceE3<T> extends InterfaceE<List<T[]>[]>{}

    interface InterfaceF<T> extends EntityType<T> {

        <K> T method(K key ,List<? extends Map<? extends T ,K>> list);

    }

    interface InterfaceF2 extends InterfaceF<List<String>>{}

    interface InterfaceH1<T extends InterfaceF<List<String>> ,T1 extends String ,D extends T1 ,T2 extends List<D[]>>{}
    //<T::L_InterfaceF<L_List<L_String;>;>;T1:L_String;D:TT1;T2::L_List<[TD;>;>L_Object;

    @Test
    public void tryReplaceTypeVariable() throws NoSuchMethodException
    {
        Method method = InterfaceF.class.getMethod("method", Object.class, List.class);
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Assert.assertEquals(TypeUtils.tryReplaceTypeVariable(genericParameterTypes[1] ,"T" ,
                TypeUtils.parseSuperTypeVariable(InterfaceF2.class ,EntityType.class ,"T"))
                .getTypeName() ,"java.util.List<? extends java.util.Map<? extends java.util.List<java.lang.String>, K>>");
    }

    @Test
    public void parseBaseClassTypeVariable()
    {
       Assert.assertEquals(RootMapperMethodMateData.class , TypeUtils.parseSuperTypeVariable(InterfaceC.class ,
               EntityType.class ,"T"));
        Assert.assertEquals(RootMapperMethodMateData.class , TypeUtils.parseSuperTypeVariable(InterfaceB.class ,
                EntityType.class.getTypeParameters()[0]));
        Assert.assertTrue(TypeUtils.parseSuperTypeVariable(InterfaceA.class ,
                EntityType.class ,"T") instanceof TypeVariable);

        ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(Map.class, new Type[]{
                        String.class, ParameterizedTypeImpl.make(List.class, new Type[]{Integer.class}, null)},
                null);

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(InterfaceD2.class ,
                EntityType.class.getTypeParameters()[0]) , parameterizedType);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(InterfaceD2.class ,
                KeyTableMapper.class ,"T") , parameterizedType);

        GenericArrayType genericArrayType = GenericArrayTypeImpl.make(ParameterizedTypeImpl.make(List.class,
                new Type[]{String.class}, null));

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(InterfaceE2.class, EntityType.class,
                "T") ,genericArrayType);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(InterfaceE3.class,
                EntityType.class.getTypeParameters()[0]).getTypeName() ,"java.util.List<T[]>[][]");
    }

    interface InterfaceG1<T1 ,T2>{

        Collection<? extends Collection<?>> collection();

        List<? extends List<?>> list();

    }

    interface InterfaceG2 extends InterfaceG1<Collection<String> ,List<String>>{

        @Override
        List<? extends List<?>> collection();

        @Override
        List<? extends List<?>> list();

    }

    interface InterfaceG3 extends InterfaceG1<Map<String ,List[]> ,HashMap<String ,ArrayList[]>>{

    }

    interface InterfaceG4 extends InterfaceG1<Map<String ,? extends List[]> ,HashMap<String ,ArrayList[]>>{

    }

    @Test
    public void isAssignableFrom() throws NoSuchMethodException
    {
        Method collectionMethod = InterfaceG1.class.getMethod("collection");
        Method listMethod = InterfaceG1.class.getMethod("list");
        Type collectionReturnType = collectionMethod.getGenericReturnType();
        Type listReturnType = listMethod.getGenericReturnType();
        Assert.assertTrue(TypeUtils.isAssignableFrom(collectionReturnType ,listReturnType));
        Assert.assertFalse(TypeUtils.isAssignableFrom(listReturnType ,collectionReturnType));

        ParameterizedType parentType = (ParameterizedType) InterfaceG2.class.getGenericInterfaces()[0];
        Type[] actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[1] ,actualTypeArguments[0]));

        ParameterizedType parentType2 = (ParameterizedType) InterfaceG3.class.getGenericInterfaces()[0];
        Type[] actualTypeArguments2 = parentType2.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments2[0] ,actualTypeArguments2[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments2[1] ,actualTypeArguments2[0]));

        ParameterizedType parentType3 = (ParameterizedType) InterfaceG4.class.getGenericInterfaces()[0];
        Type[] actualTypeArguments3 = parentType3.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments3[0] ,actualTypeArguments3[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments3[1] ,actualTypeArguments3[0]));

        ParameterizedType parentType4 = (ParameterizedType) InterfaceG4.class.getGenericInterfaces()[0];
        Type[] actualTypeArguments4 = parentType4.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments4[0] ,actualTypeArguments4[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments4[1] ,actualTypeArguments4[0]));
    }

}
