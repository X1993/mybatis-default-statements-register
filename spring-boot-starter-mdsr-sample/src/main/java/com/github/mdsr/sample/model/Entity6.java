package com.github.mdsr.sample.model;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.logical.Logical;

@Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")
public class Entity6 {

    private String id;

    private String id2;

    private String or;

    private String and;

    private String like;

    private String by;

    private String byAndLike;

    private String index;

    @Column(value = "lo_code")
    private String locationCode;

    public Entity6(String id, String id2) {
        this.id = id;
        this.id2 = id2;
    }

    public Entity6() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getOr() {
        return or;
    }

    public void setOr(String or) {
        this.or = or;
    }

    public String getAnd() {
        return and;
    }

    public void setAnd(String and) {
        this.and = and;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getByAndLike() {
        return byAndLike;
    }

    public void setByAndLike(String byAndLike) {
        this.byAndLike = byAndLike;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }
}