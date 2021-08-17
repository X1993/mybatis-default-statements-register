package com.github.ibatis.statement.util;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
import com.github.ibatis.statement.mapper.EntityType;
import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import java.lang.reflect.*;
import java.util.*;

public class TypeUtilsTest {

    interface Interface1<T ,C extends MapperMethodMateData, X> extends KeyTableMapper<T ,C> {}

    interface Interface2<T> extends List<String>, KeyTableMapper<T, MapperMethodMateData> {}

    interface Interface3 extends List<String>, KeyTableMapper<String, MapperMethodMateData> {}

    interface Interface4<K ,T> extends KeyTableMapper<K ,Map<String ,T>> {}

    interface Interface5 extends Interface4<String, List<Integer>> {}

    interface Interface6<T> extends KeyTableMapper<String ,T[]> {}

    interface Interface7 extends Interface6<List<String>> {}

    interface Interface8<T> extends Interface6<List<T[]>[]> {}

    interface Interface9<T> extends EntityType<T>
    {
        <K> T method(K key ,List<? extends Map<? extends T ,K>> list);
    }

    interface Interface10 extends Interface9<List<String>> {}

    interface Interface11<T extends Interface9<List<String>>,T1 extends String ,D extends T1 ,T2 extends List<D[]>>{}
    //<T::L_InterfaceF<L_List<L_String;>;>;T1:L_String;D:TT1;T2::L_List<[TD;>;>L_Object;

    @Test
    public void tryReplaceTypeVariable() throws NoSuchMethodException
    {
        Method method = Interface9.class.getMethod("method", Object.class, List.class);
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Assert.assertEquals(TypeUtils.tryReplaceTypeVariable(genericParameterTypes[1] ,"T" ,
                TypeUtils.parseSuperTypeVariable(Interface10.class ,EntityType.class ,"T"))
                .getTypeName() ,"java.util.List<? extends java.util.Map<? extends java.util.List<java.lang.String>, K>>");
    }

    @Test
    public void parseBaseClassTypeVariable()
    {
       Assert.assertEquals(MapperMethodMateData.class , TypeUtils.parseSuperTypeVariable(Interface3.class ,
               EntityType.class ,"T"));
        Assert.assertEquals(MapperMethodMateData.class , TypeUtils.parseSuperTypeVariable(Interface2.class ,
                EntityType.class.getTypeParameters()[0]));
        Assert.assertTrue(TypeUtils.parseSuperTypeVariable(Interface1.class ,
                EntityType.class ,"T") instanceof TypeVariable);

        ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(Map.class, new Type[]{
                        String.class, ParameterizedTypeImpl.make(List.class, new Type[]{Integer.class}, null)},
                null);

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface5.class ,
                EntityType.class.getTypeParameters()[0]) , parameterizedType);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface5.class ,
                KeyTableMapper.class ,"T") , parameterizedType);

        GenericArrayType genericArrayType = GenericArrayTypeImpl.make(ParameterizedTypeImpl.make(List.class,
                new Type[]{String.class}, null));

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface7.class, EntityType.class,
                "T") ,genericArrayType);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface8.class,
                EntityType.class.getTypeParameters()[0]).getTypeName() ,"java.util.List<T[]>[][]");
    }

    interface Interface12<T1 ,T2>{

        Collection<? extends Collection<?>> collection();

        List<? extends List<?>> list();

    }

    interface Interface13 extends Interface12<Collection<String> ,List<String>> {

        @Override
        List<? extends List<?>> collection();

        @Override
        List<? extends List<?>> list();

    }

    interface Interface14 extends Interface12<Map<String ,List[]> ,HashMap<String ,ArrayList[]>> {

    }

    interface Interface15 extends Interface12<Map<String ,? extends List[]> ,HashMap<String ,ArrayList[]>> {

    }

    interface Interface16 extends Interface12<Map<String ,? extends List> ,HashMap<String ,ArrayList>> {

    }

    interface Interface17 extends Interface12<Map<String ,? extends List> ,HashMap<String ,ArrayList>> {

    }

    interface Interface18 extends Interface12<Map<String ,? super List> ,HashMap<String ,ArrayList>> {

    }

    interface Interface19 extends Interface12<Map<String ,List> ,HashMap<String ,? super ArrayList>> {

    }

    interface Interface20 extends Interface12<Map<String ,? super ArrayList> ,HashMap<String ,List>> {

    }

    @Test
    public void isAssignableFrom() throws NoSuchMethodException
    {
        Method collectionMethod = Interface12.class.getMethod("collection");
        Method listMethod = Interface12.class.getMethod("list");
        // Collection<? extends Collection<?>>
        Type collectionReturnType = collectionMethod.getGenericReturnType();
        // List<? extends List<?>>
        Type listReturnType = listMethod.getGenericReturnType();
        Assert.assertFalse(TypeUtils.isAssignableFrom(collectionReturnType ,listReturnType));

        ParameterizedType parentType = (ParameterizedType) Interface13.class.getGenericInterfaces()[0];
        //Collection<String> ,List<String>
        Type[] actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[1] ,actualTypeArguments[0]));

        parentType = (ParameterizedType) Interface14.class.getGenericInterfaces()[0];
        // Map<String ,List[]> ,HashMap<String ,ArrayList[]>
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[1] ,actualTypeArguments[0]));

        // Map<String ,? extends List[]> ,HashMap<String ,ArrayList[]>
        parentType = (ParameterizedType) Interface15.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[1] ,actualTypeArguments[0]));

        // Map<String ,? extends List> ,HashMap<String ,ArrayList>
        parentType = (ParameterizedType) Interface16.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));

        // Map<String ,? extends List> ,HashMap<String ,ArrayList>
        parentType = (ParameterizedType) Interface17.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));

        // Map<String ,? super List> ,HashMap<String ,ArrayList>
        parentType = (ParameterizedType) Interface18.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertTrue(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));

        // Map<String ,List> ,HashMap<String ,? super ArrayList>
        parentType = (ParameterizedType) Interface19.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));

        // Map<String ,? super ArrayList> ,HashMap<String ,List>
        parentType = (ParameterizedType) Interface20.class.getGenericInterfaces()[0];
        actualTypeArguments = parentType.getActualTypeArguments();
        Assert.assertFalse(TypeUtils.isAssignableFrom(actualTypeArguments[0] ,actualTypeArguments[1]));
    }

}
