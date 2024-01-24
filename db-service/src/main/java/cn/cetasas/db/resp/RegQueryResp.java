package cn.cetasas.db.resp;

import java.util.List;

public class RegQueryResp {

    private int returncode;

    private List<ReturnData> returndata;

    public int getReturncode() {
        return returncode;
    }

    public void setReturncode(int returncode) {
        this.returncode = returncode;
    }

    public List<ReturnData> getReturndata() {
        return returndata;
    }

    public void setReturndata(List<ReturnData> returndata) {
        this.returndata = returndata;
    }

    public static class ReturnData {

        private boolean issj;

        private List<Node> nodes;

        private String selcode;

        private String wdcode;

        private String wdname;

        public boolean isIssj() {
            return issj;
        }

        public void setIssj(boolean issj) {
            this.issj = issj;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }

        public String getSelcode() {
            return selcode;
        }

        public void setSelcode(String selcode) {
            this.selcode = selcode;
        }

        public String getWdcode() {
            return wdcode;
        }

        public void setWdcode(String wdcode) {
            this.wdcode = wdcode;
        }

        public String getWdname() {
            return wdname;
        }

        public void setWdname(String wdname) {
            this.wdname = wdname;
        }
    }

    public static class Node {

        private String code;

        private String name;

        private String sort;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }
    }
}
