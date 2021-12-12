package com.github.ibatis.statement.util;

import com.github.ibatis.statement.base.core.MethodSignature;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author X1993
 * @date 2020/2/22
 */
public class TypeUtils {

    /**
     * <p>
     *     public interface EntityType<T> {
     *     }
     *
     *     public interface SelectMapper<T> extends EntityType<T>{
     *
     *     }
     *
     *     public interface EntityMapper extends SelectMapper<User>{
     *
     *     }
     *
     *     TypeUtils.parseSuperTypeVariable(EntityMapper.class ,EntityType.class.getTypeParameters()[0]) //User.class
     * </p>
     *
     * 根据继承关系分析类申明的类型变量类型
     * @param targetClass 定义了类型变量实际类型的类
     * @param matchClassTypeVariable 需要解析的的类型变量
     * @return
     */
    public static Type parseSuperTypeVariable(Class<?> targetClass,
                                              TypeVariable<? extends Class<?>> matchClassTypeVariable)
    {
        if (targetClass == null || matchClassTypeVariable == null){
            throw new IllegalArgumentException("params is null");
        }
        if (targetClass.equals(matchClassTypeVariable.getGenericDeclaration())){
            return matchClassTypeVariable;
        }

        Type[] superTypes = getSuperTypes(targetClass);
        for (Type type : superTypes) {
            if (type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    Type variableType = parseSuperTypeVariable(parameterizedType.getActualTypeArguments(),
                            (Class<?>) rawType, matchClassTypeVariable);
                    if (variableType != null && !matchClassTypeVariable.equals(variableType)){
                        return variableType;
                    }
                }
            }else if (type instanceof Class){
                Type variableType = parseSuperTypeVariable((Class<?>) type, matchClassTypeVariable);
                if (variableType != null && !matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }
        }
        return matchClassTypeVariable;
    }

    /**
     * <p>
     *     public interface EntityType<T> {
     *     }
     *
     *     public interface SelectMapper<T> extends EntityType<T>{
     *
     *     }
     *
     *     public interface EntityMapper extends SelectMapper<User>{
     *
     *     }
     *
     *     TypeUtils.parseSuperTypeVariable(EntityMapper.class ,EntityType.class ,"T") //User.class
     * </p>
     *
     * 根据继承关系分析类申明的类型变量类型
     * @param targetClass 定义了类型变量实际类型的类
     * @param matchRawClass 需要解析的泛型变量定义的类
     * @param matchTypeVariableName 类型变量名
     * @return
     */
    public static Type parseSuperTypeVariable(Class<?> targetClass,
                                              Class<?> matchRawClass,
                                              String matchTypeVariableName)
    {
        TypeVariable<? extends Class<?>> matchClassTypeVariable = null;
        for (TypeVariable<? extends Class<?>> classTypeVariable : matchRawClass.getTypeParameters()) {
            if (classTypeVariable.getName().equals(matchTypeVariableName)){
                matchClassTypeVariable = classTypeVariable;
                break;
            }
        }
        if (matchClassTypeVariable == null){
            throw new IllegalArgumentException("not found TypeVariable \"" + matchTypeVariableName
                    + "\" on " + matchRawClass);
        }
        return parseSuperTypeVariable(targetClass ,matchClassTypeVariable);
    }

    private static Type[] getSuperTypes(Class<?> clazz){
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        Type genericSuperclass = clazz.getGenericSuperclass();
        Type[] superTypes = genericInterfaces;
        if (!Object.class.equals(genericSuperclass)){
            superTypes = Arrays.copyOf(genericInterfaces, genericInterfaces.length + 1);
            superTypes[superTypes.length - 1] = genericSuperclass;
        }
        return superTypes;
    }

    private static Type parseSuperTypeVariable(Type[] actualTypeArguments,
                                               Class<?> clazz,
                                               TypeVariable<? extends Class<?>> matchClassTypeVariable)
    {
        TypeVariable<? extends Class<?>>[] classTypeVariables = clazz.getTypeParameters();
        int len = classTypeVariables.length;
        if (len != actualTypeArguments.length){
            throw new IllegalArgumentException();
        } else if (len == 0){
            return matchClassTypeVariable;
        } else if (clazz.equals(matchClassTypeVariable.getGenericDeclaration())){
            for (int i = 0; i < len; i++) {
                TypeVariable<? extends Class<?>> classTypeVariable = classTypeVariables[i];
                if (matchClassTypeVariable.equals(classTypeVariable)){
                    return actualTypeArguments[i];
                }
            }
        }

        Type[] superTypes = getSuperTypes(clazz);

        for (Type type : superTypes) {
            if (type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class rawClass = (Class) parameterizedType.getRawType();
                Type[] superActualTypeArguments = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < superActualTypeArguments.length; i++) {
                    for (int j = 0; j < classTypeVariables.length; j++) {
                        superActualTypeArguments[i] = tryReplaceTypeVariable(superActualTypeArguments[i] ,
                                classTypeVariables[j] ,actualTypeArguments[j]);
                    }
                }
                Type variableType = parseSuperTypeVariable(superActualTypeArguments, rawClass, matchClassTypeVariable);
                if (!matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }else if (type instanceof Class){
                Type variableType = parseSuperTypeVariable((Class<?>) type, matchClassTypeVariable);
                if (!matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }
        }
        return matchClassTypeVariable;
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: TypeVariable T 匹配规则
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有匹配的的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param typeVariablePredicate 判断类型变量是否满足替换条件
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,
                                              Predicate<TypeVariable<? extends GenericDeclaration>> typeVariablePredicate,
                                              Type replaceType)
    {
        if (type == null || typeVariablePredicate == null || replaceType == null){
            throw new IllegalArgumentException("replace type is null");
        }
        if (type instanceof TypeVariable){
            if (typeVariablePredicate.test((TypeVariable) type)){
                return replaceType;
            }
        }else if (type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            boolean replace = false;
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type newType = tryReplaceTypeVariable(actualTypeArguments[i], typeVariablePredicate, replaceType);
                if (actualTypeArguments[i] != newType){
                    //有类型变量被替换
                    replace = true;
                    actualTypeArguments[i] = newType;
                }
            }
            if (replace){
                return ParameterizedTypeImpl.make((Class<?>) parameterizedType.getRawType(),actualTypeArguments ,null);
            }
        }else if (type instanceof GenericArrayType){
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            Type newType = tryReplaceTypeVariable(genericComponentType, typeVariablePredicate, replaceType);
            if (newType != genericArrayType){
                //有类型变量被替换
                return GenericArrayTypeImpl.make(newType);
            }
        }else if (type instanceof WildcardType){
            WildcardType wildcardType = (WildcardType) type;
            boolean replace = false;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            for (int i = 0; i < lowerBounds.length; i++) {
                Type newType = tryReplaceTypeVariable(lowerBounds[i], typeVariablePredicate, replaceType);
                if (newType != lowerBounds[i]){
                    //有类型变量被替换
                    replace = true;
                    lowerBounds[i] = newType;
                }
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            for (int i = 0; i < upperBounds.length; i++) {
                Type newType = tryReplaceTypeVariable(upperBounds[i], typeVariablePredicate, replaceType);
                if (newType != upperBounds[i]){
                    //有类型变量被替换
                    replace = true;
                    upperBounds[i] = newType;
                }
            }
            if (replace){
                return new WildcardTypeImpl(upperBounds ,lowerBounds);
            }
        }
        return type;
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: "T"
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有指定的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param matchTypeVariableName 匹配的类型变量名
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,String matchTypeVariableName, Type replaceType){
        return tryReplaceTypeVariable(type ,
                typeVariable -> matchTypeVariableName.equals(typeVariable.getName()) ,replaceType);
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: T
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有指定的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param matchTypeVariable 匹配的类型变量
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,
                                              TypeVariable<? extends GenericDeclaration> matchTypeVariable,
                                              Type replaceType)
    {
        return tryReplaceTypeVariable(type ,typeVariable -> matchTypeVariable.equals(typeVariable) ,replaceType);
    }

    /**
     * <p>
     *      public interface EntityType<T> {
     *      }
     *
     *      public interface TableMapper<T> extends EntityType<T> {
     *          int insert(T t);
     *      }
     *
     *      public interface UserMapper implement TableMapper<User> {
     *
     *      }
     *
     *      TypeUtils.methodSignature(insertMethod ,UserMapper.class); // methodSignature: int.class insert(User.class)
     * </p>
     * 生成方法签名，如果定义了实际类型，会尝试替换引用自类上声明的类型变量
     * @param method 方法
     * @param definitionTypeVariableClass 申明了方法上引用的类型变量的类
     * @return
     */
    public static MethodSignature methodSignature(Method method ,Class<?> definitionTypeVariableClass)
    {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Type genericReturnType = method.getGenericReturnType();

        Class<?> declaringClass = method.getDeclaringClass();
        TypeVariable<? extends Class<?>>[] classTypeVariables = declaringClass.getTypeParameters();

        if (declaringClass.isAssignableFrom(definitionTypeVariableClass) && !declaringClass.equals(definitionTypeVariableClass))
        {
            for (int i = 0; i < classTypeVariables.length; i++) {
                TypeVariable<? extends Class<?>> classTypeVariable = classTypeVariables[i];
                Type replaceType = TypeUtils.parseSuperTypeVariable(definitionTypeVariableClass, classTypeVariable);
                if (replaceType != null && !(replaceType instanceof TypeVariable))
                {
                    for (int j = 0; j < genericParameterTypes.length; j++) {
                        genericParameterTypes[j] = TypeUtils.tryReplaceTypeVariable(genericParameterTypes[j] ,
                                classTypeVariable ,replaceType);
                    }
                    genericReturnType = TypeUtils.tryReplaceTypeVariable(genericReturnType ,
                            classTypeVariable ,replaceType);
                }
            }
        }
        return new MethodSignature(genericReturnType ,method.getName() ,genericParameterTypes);
    }

    /**
     * <p>
     *     TypeUtils.isAssignableFrom(Integer.class ,int.class); //true
     *     ``````省略其他基本类型
     *
     *     TypeUtils.isAssignableFrom(Object ,*); //true
     *     TypeUtils.isAssignableFrom(List ,ArrayList.class); //true
     *     TypeUtils.isAssignableFrom(List[] ,ArrayList[].class); //true
     *     TypeUtils.isAssignableFrom(List ,ArrayList<T>); //true
     *     TypeUtils.isAssignableFrom(List ,< ? extend ArrayList>); //true
     *     TypeUtils.isAssignableFrom(List<List<String>> ,List<ArrayList<String>>); //true
     *     TypeUtils.isAssignableFrom(List<String>[] ,ArrayList<String>[]); //true
     *
     *     TypeUtils.isAssignableFrom(< ?> ,*); //true
     *     TypeUtils.isAssignableFrom(< ? > ,*); //true
     *     TypeUtils.isAssignableFrom(< ? super List> ,ArrayList); //true
     *     TypeUtils.isAssignableFrom(< ? super Array> ,ArrayList); //true
     *
     *     TypeUtils.isAssignableFrom(< ? extend List> ,ArrayList); //false
     *     TypeUtils.isAssignableFrom(< ? extend List> ,< ? extend ArrayList>); //false
     *     ...
     * </p>
     * 类型是否兼容
     * @param parentType 父类型
     * @param subType 子类型
     * @return
     */
    public static boolean isAssignableFrom(Type parentType ,Type subType)
    {
        if (Object.class.equals(parentType) || parentType.equals(subType)){
            return true;
        }

        if (parentType instanceof WildcardType){
            WildcardType parentWildcardType = (WildcardType) parentType;
            if (parentWildcardType.getLowerBounds().length > 0){
                //使用了下边界 <? super T>
                return Stream.of(parentWildcardType.getLowerBounds())
                        .allMatch(lowerBound -> isAssignableFrom(lowerBound ,subType));
            }else if (parentWildcardType.getUpperBounds().length > 0){
                //使用了上边界<? extend T>
                if (Stream.of(parentWildcardType.getUpperBounds())
                        .allMatch(upperBound -> upperBound instanceof Class &&
                                Modifier.isFinal(((Class) upperBound).getModifiers()))){
                    //都是final类
                    return Stream.of(parentWildcardType.getUpperBounds())
                            .allMatch(upperBound -> isAssignableFrom(upperBound ,subType));
                }
                return false;
            }else {
                // <?> 等价于 Object.class
                return isAssignableFrom(Object.class ,subType);
            }
        }

        if (subType instanceof WildcardType){
            WildcardType wildcardType = (WildcardType) subType;
            if (wildcardType.getLowerBounds().length > 0){
                //使用了下边界 <? super T> ,等价于 Object.class
                return isAssignableFrom(parentType ,Object.class);
            }else if (wildcardType.getUpperBounds().length > 0){
                //使用了上边界<? extend T> ,等价于T
                return Stream.of(wildcardType.getUpperBounds())
                        .allMatch(upperBound -> isAssignableFrom(parentType ,upperBound));
            }else {
                // <?> 等价于 Object.class
                return isAssignableFrom(parentType ,Object.class);
            }
        }

        if (parentType instanceof Class){
            if (subType instanceof Class){
                if (((Class) subType).isPrimitive()){
                    if (!Number.class.isAssignableFrom((Class<?>) parentType)){
                        return false;
                    }
                    if (Byte.class == parentType && short.class == subType){
                        return true;
                    }else if (Short.class == parentType && short.class == subType){
                        return true;
                    }else if (Integer.class == parentType && int.class == subType){
                        return true;
                    }else if (Float.class == parentType && float.class == subType){
                        return true;
                    }else if (Double.class == parentType && double.class == subType){
                        return true;
                    }else if (Long.class == parentType && long.class == subType){
                        return true;
                    }else if (Character.class == parentType && char.class == subType){
                        return true;
                    }else if (Boolean.class == parentType && boolean.class == subType){
                        return true;
                    }
                }
                if (((Class) parentType).isAssignableFrom((Class<?>) subType)){
                    // List ,ArrayList
                    return true;
                }
                if (((Class) parentType).isArray() && ((Class) subType).isArray()){
                    // List[] ,ArrayList[]
                    return ((Class) parentType).getComponentType().isAssignableFrom(((Class) subType).getComponentType());
                }
                return false;
            }

            if (subType instanceof ParameterizedType){
                Type subRawType = ((ParameterizedType) subType).getRawType();
                if (subRawType instanceof Class){
                    // List ,ArrayList<T>
                    return ((Class) parentType).isAssignableFrom((Class<?>) subRawType);
                }
            }

            return false;
        }

        if (parentType instanceof ParameterizedType)
        {
            if (!(subType instanceof ParameterizedType)){
                return false;
            }

            Type parentRawType = ((ParameterizedType) parentType).getRawType();
            Type subRawType = ((ParameterizedType) subType).getRawType();

            if (isAssignableFrom(parentRawType ,subRawType)) {
                Type[] parentActualTypeArguments = ((ParameterizedType) parentType).getActualTypeArguments();
                Type[] subActualTypeArguments = ((ParameterizedType) subType).getActualTypeArguments();
                if (parentActualTypeArguments.length == subActualTypeArguments.length){
                    for (int i = 0; i < parentActualTypeArguments.length; i++) {
                        if (!isAssignableFrom(parentActualTypeArguments[i] ,subActualTypeArguments[i])){
                            return false;
                        }
                    }
                    // List<List<String>> ,List<ArrayList<String>>
                    return true;
                }
            }
            return false;
        }

        if (parentType instanceof GenericArrayType)
        {
            if (!(subType instanceof GenericArrayType)){
                return false;
            }
            // List<String>[] ,ArrayList<String>[]
            return isAssignableFrom(((GenericArrayType) parentType).getGenericComponentType() ,
                    ((GenericArrayType) subType).getGenericComponentType());

        }

        return false;
    }

    static class WildcardTypeImpl implements WildcardType{

        private final Type[] upperBounds;

        private final Type[] lowerBounds;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }

        @Override
        public String toString() {
            Type[] var1 = this.getLowerBounds();
            Type[] var2 = var1;
            StringBuilder var3 = new StringBuilder();
            if (var1.length > 0) {
                var3.append("? super ");
            } else {
                Type[] var4 = this.getUpperBounds();
                if (var4.length <= 0 || var4[0].equals(Object.class)) {
                    return "?";
                }

                var2 = var4;
                var3.append("? extends ");
            }

            assert var2.length > 0;

            boolean var9 = true;
            Type[] var5 = var2;
            int var6 = var2.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Type var8 = var5[var7];
                if (!var9) {
                    var3.append(" & ");
                }

                var9 = false;
                var3.append(var8.getTypeName());
            }

            return var3.toString();
        }

        @Override
        public boolean equals(Object var1) {
            if (!(var1 instanceof WildcardType)) {
                return false;
            } else {
                WildcardType var2 = (WildcardType)var1;
                return Arrays.equals(this.getLowerBounds(), var2.getLowerBounds()) && Arrays.equals(this.getUpperBounds(), var2.getUpperBounds());
            }
        }

        @Override
        public int hashCode() {
            Type[] var1 = this.getLowerBounds();
            Type[] var2 = this.getUpperBounds();
            return Arrays.hashCode(var1) ^ Arrays.hashCode(var2);
        }
    }

}
