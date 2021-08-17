package com.github.ibatis.statement.util;

import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import java.lang.reflect.*;
import java.util.*;

public class TypeUtilsTest {

    interface Interface0<T>{}

    interface Interface1<T0 ,T1>{}

    interface Interface2<T> extends Interface0<String>, Interface1<T, Integer> {}

    interface Interface3 extends Interface0<String>, Interface1<String, Integer> {}

    interface Interface4<K ,T> extends Interface1<K ,Interface1<String ,T>> {}

    interface Interface5 extends Interface4<String, Interface0<Integer>> {}

    interface Interface6<T> extends Interface1<String ,T[]> {}

    interface Interface7 extends Interface6<Interface0<String>> {}

    interface Interface8<T> extends Interface6<Interface0<T[]>[]> {}

    interface Interface9<T> extends Interface0<T>
    {
        <K> T method(K key ,Interface0<? extends Map<? extends T ,K>> interface0);
    }

    interface Interface10 extends Interface9<Interface0<String>> {}

    interface Interface11<T extends Interface9<Interface0<String>>,T1 extends String ,D extends T1 ,T2 extends Interface0<D[]>>{}
    //<T::L_InterfaceF<L_Interface0<L_String;>;>;T1:L_String;D:TT1;T2::L_Interface0<[TD;>;>L_Object;

    interface Interface22<T ,C extends Integer, X> extends Interface1<T ,C> {}

    @Test
    public void tryReplaceTypeVariable() throws NoSuchMethodException
    {
        Method method = Interface9.class.getMethod("method", Object.class, Interface0.class);
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Assert.assertEquals(TypeUtils.tryReplaceTypeVariable(genericParameterTypes[1] ,"T" ,
                TypeUtils.parseSuperTypeVariable(Interface10.class ,Interface0.class ,"T"))
                .getTypeName() ,
                "com.github.ibatis.statement.util.TypeUtilsTest$Interface0<? extends java.util.Map" +
                        "<? extends com.github.ibatis.statement.util.TypeUtilsTest$Interface0<java.lang.String>, K>>");
    }

    @Test
    public void parseBaseClassTypeVariable()
    {
       Assert.assertEquals(String.class , TypeUtils.parseSuperTypeVariable(Interface3.class ,
               Interface0.class ,"T"));
        Assert.assertEquals(String.class , TypeUtils.parseSuperTypeVariable(Interface2.class ,
                Interface0.class.getTypeParameters()[0]));
        Assert.assertTrue(TypeUtils.parseSuperTypeVariable(Interface1.class ,
                Interface0.class ,"T") instanceof TypeVariable);

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface5.class ,
                Interface1.class.getTypeParameters()[0]) , String.class);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface5.class ,
                Interface1.class ,"T0") , String.class);

        GenericArrayType genericArrayType = GenericArrayTypeImpl.make(ParameterizedTypeImpl.make(Interface0.class,
                new Type[]{String.class}, null));

        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface7.class, Interface1.class,
                "T1") ,genericArrayType);
        Assert.assertEquals(TypeUtils.parseSuperTypeVariable(Interface8.class,
                Interface1.class.getTypeParameters()[1]).getTypeName() ,
                "com.github.ibatis.statement.util.TypeUtilsTest$Interface0<T[]>[][]");
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
