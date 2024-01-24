package cn.cetasas.db.resp;

import java.util.List;

public class CESQueryResp {

    private int returncode;

    private ReturnData returndata;

    public int getReturncode() {
        return returncode;
    }

    public void setReturncode(int returncode) {
        this.returncode = returncode;
    }

    public ReturnData getReturndata() {
        return returndata;
    }

    public void setReturndata(ReturnData returndata) {
        this.returndata = returndata;
    }

    public static class ReturnData {

        private List<DataNode> datanodes;

        private int freshsort;

        private int hasdatacount;

        private List<WDNode> wdnodes;

        public List<DataNode> getDatanodes() {
            return datanodes;
        }

        public void setDatanodes(List<DataNode> datanodes) {
            this.datanodes = datanodes;
        }

        public int getFreshsort() {
            return freshsort;
        }

        public void setFreshsort(int freshsort) {
            this.freshsort = freshsort;
        }

        public int getHasdatacount() {
            return hasdatacount;
        }

        public void setHasdatacount(int hasdatacount) {
            this.hasdatacount = hasdatacount;
        }

        public List<WDNode> getWdnodes() {
            return wdnodes;
        }

        public void setWdnodes(List<WDNode> wdnodes) {
            this.wdnodes = wdnodes;
        }
    }

    public static class DataNode {

        private String code;

        private Data data;

        private List<WD> wds;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public List<WD> getWds() {
            return wds;
        }

        public void setWds(List<WD> wds) {
            this.wds = wds;
        }
    }

    public static class Data {

        private double data;

        private int dotcount;

        private boolean hasdata;

        private String strdata;

        public double getData() {
            return data;
        }

        public void setData(double data) {
            this.data = data;
        }

        public int getDotcount() {
            return dotcount;
        }

        public void setDotcount(int dotcount) {
            this.dotcount = dotcount;
        }

        public boolean isHasdata() {
            return hasdata;
        }

        public void setHasdata(boolean hasdata) {
            this.hasdata = hasdata;
        }

        public String getStrdata() {
            return strdata;
        }

        public void setStrdata(String strdata) {
            this.strdata = strdata;
        }
    }

    public static class WD {

        private String wdcode;

        private String valuecode;

        public String getWdcode() {
            return wdcode;
        }

        public void setWdcode(String wdcode) {
            this.wdcode = wdcode;
        }

        public String getValuecode() {
            return valuecode;
        }

        public void setValuecode(String valuecode) {
            this.valuecode = valuecode;
        }
    }

    public static class WDNode {
        private List<Node> nodes;

        private String wdcode;

        private String wdname;

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
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

        private String cname;

        private String code;

        private int dotcount;

        private String exp;

        private boolean ifshowcode;

        private String memo;

        private String name;

        private String nodesort;

        private int sortcode;

        private String tag;

        private String unit;

        public String getCname() {
            return cname;
        }

        public void setCname(String cname) {
            this.cname = cname;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getDotcount() {
            return dotcount;
        }

        public void setDotcount(int dotcount) {
            this.dotcount = dotcount;
        }

        public String getExp() {
            return exp;
        }

        public void setExp(String exp) {
            this.exp = exp;
        }

        public boolean isIfshowcode() {
            return ifshowcode;
        }

        public void setIfshowcode(boolean ifshowcode) {
            this.ifshowcode = ifshowcode;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNodesort() {
            return nodesort;
        }

        public void setNodesort(String nodesort) {
            this.nodesort = nodesort;
        }

        public int getSortcode() {
            return sortcode;
        }

        public void setSortcode(int sortcode) {
            this.sortcode = sortcode;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
