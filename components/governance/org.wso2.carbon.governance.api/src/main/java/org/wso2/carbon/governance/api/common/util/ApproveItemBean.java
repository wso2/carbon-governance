package org.wso2.carbon.governance.api.common.util;

import java.util.ArrayList;
import java.util.List;

public class ApproveItemBean {
    private String name;
    private int order;
    private String status;
    private int requiredVotes;
    private int votes;
    private List<String> voters = new ArrayList<String>();
    private Boolean value = Boolean.FALSE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRequiredVotes() {
        return requiredVotes;
    }

    public void setRequiredVotes(int requiredVotes) {
        this.requiredVotes = requiredVotes;
    }

    public int getVotes() {
        return votes;
    }

    public List<String> getVoters() {
        return voters;
    }

    public void setVoters(List<String> voters) {
        this.voters = voters;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
