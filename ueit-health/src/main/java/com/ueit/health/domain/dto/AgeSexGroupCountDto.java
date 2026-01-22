package com.ueit.health.domain.dto;

/**
 * 年龄,性别分类数据  各个年龄段的人数和男女人数
 */
public class AgeSexGroupCountDto {
    // 6-14岁 人数
    private Integer a;
    // 15-19岁 人数
    private Integer b;
    // 20-39岁 人数
    private Integer c;
    // 40-59岁 人数
    private Integer d;
    // 60岁以上 人数
    private Integer e;
    // 未知性别 人数
    private Integer nono;
    // 女人 人数
    private Integer woman;
    // 男人 人数
    private Integer man;

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }

    public Integer getNono() {
        return nono;
    }

    public void setNono(Integer nono) {
        this.nono = nono;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }

    public Integer getWoman() {
        return woman;
    }

    public void setWoman(Integer woman) {
        this.woman = woman;
    }

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public Integer getE() {
        return e;
    }

    public void setE(Integer e) {
        this.e = e;
    }

    public Integer getMan() {
        return man;
    }

    public void setMan(Integer man) {
        this.man = man;
    }

    @Override
    public String toString() {
        return "AgeSexGroupCountDto{" +
                "a=" + a +
                ", nono=" + nono +
                ", b=" + b +
                ", c=" + c +
                ", woman=" + woman +
                ", d=" + d +
                ", e=" + e +
                ", man=" + man +
                '}';
    }
}

