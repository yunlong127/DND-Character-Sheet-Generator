package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "`choose`")
public class Choose {
    @Id
    private Integer id;

    private String extend;

    @Column(name = "`choose`")
    private String choose;

    private Integer cnt;

    private String list;
    // getters and setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getExtend() { return extend; }
    public void setExtend(String extend) { this.extend = extend; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public Integer getCnt() { return cnt; }
    public void setCnt(Integer cnt) { this.cnt = cnt; }
    public String getList() { return list; }
    public void setList(String list) { this.list = list; }
}
