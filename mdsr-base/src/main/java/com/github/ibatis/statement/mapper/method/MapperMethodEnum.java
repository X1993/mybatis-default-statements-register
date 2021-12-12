package com.github.ibatis.statement.mapper.method;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.mapper.DynamicSelectMapper;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.mapper.SelectMapper;
import com.github.ibatis.statement.mapper.TableMapper;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author jie
 * @date 2021/12/11
 * @description
 */
public enum MapperMethodEnum{

    /**
     * @see KeyTableMapper#deleteBatchByPrimaryKey(Collection)
     */
    DELETE_BATCH_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "deleteBatchByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#deleteBatchByPrimaryKeyOnPhysical(Collection)
     */
    PHYSICAL_DELETE_BATCH_METHOD_NAME(true) {
        @Override
        public String methodName() {
            return "deleteBatchByPrimaryKeyOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#deleteByPrimaryKey(Object)
     */
    DELETE_BY_PRIMARY_KEY(true) {
        @Override
        public String methodName() {
            return "deleteByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#deleteByPrimaryKeyOnPhysical(Object)
     */
    DELETE_BY_PRIMARY_KEY_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "deleteByPrimaryKeyOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see  TableMapper#deleteSelective(Object)
     */
    DELETE_SELECTIVE(true) {
        @Override
        public String methodName() {
            return "deleteSelective";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see  TableMapper#deleteSelectiveOnPhysical(Object)
     */
    DELETE_SELECTIVE_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "deleteSelectiveOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see TableMapper#insert(Object)
     */
    INSERT(true) {
        @Override
        public String methodName() {
            return "insert";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see TableMapper#insertSelective(Object)
     */
    INSERT_SELECTIVE(true) {
        @Override
        public String methodName() {
            return "insertSelective";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#countByPrimaryKeys(Collection)
     */
    COUNT_BY_PRIMARY_KEYS(true) {
        @Override
        public String methodName() {
            return "countByPrimaryKeys";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#countByPrimaryKeysOnPhysical(Collection)
     */
    COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "countByPrimaryKeysOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#selectBatchByPrimaryKey(Collection)
     */
    SELECT_BATCH_BY_PRIMARY_KEY(true) {
        @Override
        public String methodName() {
            return "selectBatchByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return ParameterizedTypeImpl.make(List.class,
                    new Type[]{entityMateData.getEntityClass()}, null);
        }
    },
    /**
     * @see KeyTableMapper#selectBatchByPrimaryKeyOnPhysical(Collection)
     */
    SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL(true) {

        @Override
        public String methodName() {
            return "selectBatchByPrimaryKeyOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return ParameterizedTypeImpl.make(List.class,
                    new Type[]{entityMateData.getEntityClass()}, null);
        }
    },
    /**
     * @see KeyTableMapper#getExistPrimaryKeys(Collection)
     */
    GET_EXIST_PRIMARY_KEYS(true) {
        @Override
        public String methodName() {
            return "getExistPrimaryKeys";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return ParameterizedTypeImpl.make(Set.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null);
        }
    },
    /**
     * @see KeyTableMapper#getExistPrimaryKeysOnPhysical(Collection)
     */
    GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "getExistPrimaryKeysOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return ParameterizedTypeImpl.make(Set.class,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()}, null);
        }
    },
    /**
     * @see KeyTableMapper#selectByPrimaryKey(Object)
     */
    SELECT_BY_PRIMARY_KEY(true) {
        @Override
        public String methodName() {
            return "selectByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return entityMateData.getEntityClass();
        }
    },
    /**
     * @see KeyTableMapper#selectByPrimaryKeyOnPhysical(Object)
     */
    SELECT_BY_PRIMARY_KEY_ON_PHYSICAL(true) {
        @Override
        public String methodName() {
            return "selectByPrimaryKeyOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return entityMateData.getEntityClass();
        }
    },
    /**
     * @see {@link SelectMapper#selectSelective(Object, boolean)}
     */
    SELECT_SELECTIVE(true) {
        @Override
        public String methodName() {
            return "selectSelective";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass() ,boolean.class};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return ParameterizedTypeImpl.make(List.class, new Type[]{entityMateData.getEntityClass()}, null);
        }
    },
    /**
     * @see {@link SelectMapper#totalSelective(Object, boolean)}
     */
    TOTAL_SELECTIVE(true) {
        @Override
        public String methodName() {
            return "totalSelective";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass() ,boolean.class};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see  KeyTableMapper#updateByPrimaryKey(Object)
     */
    UPDATE_BY_PRIMARY_KEY(true) {
        @Override
        public String methodName() {
            return "updateByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see  KeyTableMapper#updateByPrimaryKeySelective(Object)
     */
    UPDATE_BY_PRIMARY_KEY_SELECTIVE(true) {
        @Override
        public String methodName() {
            return "updateByPrimaryKeySelective";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#updateBatchSameValue(Collection, Object)
     */
    UPDATE_BATCH_SAME_VALUE(true) {
        @Override
        public String methodName() {
            return "updateBatchSameValue";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class ,
                    new Type[]{entityMateData.getReasonableKeyParameterClass()} ,null) ,
                    entityMateData.getEntityClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },

    /*---------------------------------- 非标准sql ---------------------------------------------*/

    /**
     * @see KeyTableMapper#selectMaxPrimaryKey()
     */
    SELECT_MAX_PRIMARY_KEY(false) {
        @Override
        public String methodName() {
            return "selectMaxPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[0];
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return entityMateData.getReasonableKeyParameterClass();
        }
    },
    /**
     * @see DynamicSelectMapper#countByDynamicParams(DynamicParams)
     */
    SELECT_COUNT_METHOD_NAME(false) {
        @Override
        public String methodName() {
            return "countByDynamicParams";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{DynamicParams.class};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see TableMapper#insertBatch(Collection)
     */
    INSERT_BATCH(false) {
        @Override
        public String methodName() {
            return "insertBatch";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class ,
                    new Type[]{entityMateData.getEntityClass()} ,null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#updateBatch(Collection)
     */
    UPDATE_BATCH(false) {
        @Override
        public String methodName() {
            return "updateBatch";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{ParameterizedTypeImpl.make(Collection.class ,
                    new Type[]{entityMateData.getEntityClass()} ,null)};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return int.class;
        }
    },
    /**
     * @see KeyTableMapper#existByPrimaryKey(Object)
     */
    EXIST_BY_PRIMARY_KEY(false) {
        @Override
        public String methodName() {
            return "existByPrimaryKey";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return boolean.class;
        }
    },
    /**
     * @see KeyTableMapper#existByPrimaryKeyOnPhysical(Object)
     */
    EXIST_BY_PRIMARY_KEY_ON_PHYSICAL(false) {
        @Override
        public String methodName() {
            return "existByPrimaryKeyOnPhysical";
        }

        @Override
        public Type[] parameterType(EntityMateData entityMateData) {
            return new Type[]{entityMateData.getReasonableKeyParameterClass()};
        }

        @Override
        public Type returnType(EntityMateData entityMateData) {
            return boolean.class;
        }
    },
    ;

    private final boolean common;

    MapperMethodEnum(boolean common) {
        this.common = common;
    }


    public abstract String methodName();

    public abstract Type[] parameterType(EntityMateData entityMateData);

    public abstract Type returnType(EntityMateData entityMateData);

    public MethodSignature methodSignature(EntityMateData entityMateData){
        return new MethodSignature(returnType(entityMateData) , methodName() ,parameterType(entityMateData));
    }

}
