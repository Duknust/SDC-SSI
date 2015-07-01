/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import java.io.Serializable;
import java.util.HashMap;
import org.bsl.classes.Project;
import org.bsl.classes.User;
import org.bsl.types.requests.ReqActProj;
import org.bsl.types.requests.ReqAddEuros;
import org.bsl.types.requests.ReqAddProj;
import org.bsl.types.requests.ReqLogin;
import org.bsl.types.requests.ReqMapProj;
import org.bsl.types.requests.ReqNotifEuros;
import org.bsl.types.requests.ReqProj;
import org.bsl.types.requests.ReqRegister;
import org.bsl.types.requests.ReqReqRetry;
import org.bsl.types.responses.RepWrongLogin;
import org.bsl.types.responses.RepLogin;
import org.bsl.types.responses.RepMapProj;
import org.bsl.types.responses.RepProj;
import org.bsl.types.responses.RepRegister;

@XStreamInclude({ReqActProj.class, ReqAddEuros.class, ReqAddProj.class, ReqLogin.class, ReqMapProj.class, ReqNotifEuros.class, ReqProj.class, ReqRegister.class, ReqReqRetry.class, RepWrongLogin.class, RepLogin.class, RepMapProj.class, RepProj.class, RepRegister.class, RepWrongLogin.class})
public abstract class Message implements Serializable {

    @XStreamAlias("type")
    @XStreamAsAttribute
    protected TypeOP type;
    @XStreamAlias("project")
    protected Project proj;
    @XStreamAlias("user")
    protected User user;
    @XStreamAlias("projects")
    protected HashMap<String, Project> mp;
    @XStreamAlias("string1")
    @XStreamAsAttribute
    protected String string1;
    @XStreamAlias("string2")
    @XStreamAsAttribute
    protected String string2;
    @XStreamAlias("value1")
    @XStreamAsAttribute
    protected int value1;
    @XStreamAlias("value2")
    @XStreamAsAttribute
    protected int value2;

    public Message() {
        this.type = TypeOP.NULL;
        this.proj = null;
        this.user = null;
        this.mp = new HashMap<>();
        this.string1 = "";
        this.string2 = "";
        this.value1 = -1;
        this.value2 = -1;
    }

    public Message(Message p) {
        this.type = p.getType();
        this.proj = p.getProj();
        this.user = p.getUser();
        this.mp = p.getMp();
        this.string1 = p.getString1();
        this.string2 = p.getString2();
        this.value1 = p.getValue1();
        this.value2 = p.getValue2();
    }

    public Message(TypeOP type, Project proj, User user, HashMap<String, Project> mp, int value1, int value2, String str, String str2) {
        this.type = type;
        this.proj = proj;
        this.user = user;
        this.mp.putAll(mp);

        this.string1 = str;
        this.string2 = str2;
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public Project getProj() {
        return proj;
    }

    public void setProj(Project proj) {
        this.proj = proj;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HashMap<String, Project> getMp() {
        return mp;
    }

    public void setMp(HashMap<String, Project> mp) {
        this.mp.clear();
        this.mp.putAll(mp);
    }

    public TypeOP getType() {
        return type;
    }

    public void setType(TypeOP type) {
        this.type = type;
    }

    public int getValue1() {
        return value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    //----------------
    @Override
    public String toString() {
        return "Package{" + "type=" + this.type + ", proj=" + this.proj + ", user=" + this.user + ", mp=" + this.mp + ", value1=" + this.value1 + ", value2=" + this.value2 + ", string=" + '}';
    }

}
