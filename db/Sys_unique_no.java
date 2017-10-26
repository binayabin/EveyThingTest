package com.frkj.pda.db;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by binyaya on 2015/8/9.
 */
public class Sys_unique_no {

    @JSONField(name = "bar_code")
    String bar_code;

    @JSONField(name = "unique_no")
    String unique_no;

    @JSONField(serialize = false)
    int is_used;

    @JSONField(name = "bar_code")
    public void setBar_code(String bar_code) {
        this.bar_code = bar_code;
    }

    @JSONField(name = "unique_no")
    public void setUnique_no(String unique_no) {
        this.unique_no = unique_no;
    }

    @JSONField(serialize = false)
    public void setIs_used(int is_used) {
        this.is_used = is_used;
    }

    @JSONField(name = "bar_code")
    public String getBar_code() {
        return bar_code;
    }

    @JSONField(name = "unique_no")
    public String getUnique_no() {
        return unique_no;
    }

    @JSONField(serialize = false)
    public int getIs_used() {
        return is_used;
    }
}
