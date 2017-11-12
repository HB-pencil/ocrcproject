package com.example.shinelon.ocrcamera.helper;

import java.util.List;

/**
 * Created by Shinelon on 2017/11/8.
 */

public class TentcentRs {

    /**
     * data : {"items":[{"itemstring":"手机","itemcoord":{"x":0,"y":100,"width":40,"height":20},"words":[{"character":"手","confidence":90.9},{"character":"机","confidence":93.9}]}],"session_id":""}
     * code : 0
     * message : OK
     */

    private DataBean data;
    private int code;
    private String message;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        /**
         * items : [{"itemstring":"手机","itemcoord":{"x":0,"y":100,"width":40,"height":20},"words":[{"character":"手","confidence":90.9},{"character":"机","confidence":93.9}]}]
         * session_id :
         */

        private String session_id;
        private List<ItemsBean> items;

        public String getSession_id() {
            return session_id;
        }

        public void setSession_id(String session_id) {
            this.session_id = session_id;
        }

        public List<ItemsBean> getItems() {
            return items;
        }

        public void setItems(List<ItemsBean> items) {
            this.items = items;
        }

        public static class ItemsBean {
            /**
             * itemstring : 手机
             * itemcoord : {"x":0,"y":100,"width":40,"height":20}
             * words : [{"character":"手","confidence":90.9},{"character":"机","confidence":93.9}]
             */

            private String itemstring;
            private ItemcoordBean itemcoord;
            private List<WordsBean> words;

            public String getItemstring() {
                return itemstring;
            }

            public void setItemstring(String itemstring) {
                this.itemstring = itemstring;
            }

            public ItemcoordBean getItemcoord() {
                return itemcoord;
            }

            public void setItemcoord(ItemcoordBean itemcoord) {
                this.itemcoord = itemcoord;
            }

            public List<WordsBean> getWords() {
                return words;
            }

            public void setWords(List<WordsBean> words) {
                this.words = words;
            }

            public static class ItemcoordBean {
                /**
                 * x : 0
                 * y : 100
                 * width : 40
                 * height : 20
                 */

                private int x;
                private int y;
                private int width;
                private int height;

                public int getX() {
                    return x;
                }

                public void setX(int x) {
                    this.x = x;
                }

                public int getY() {
                    return y;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            public static class WordsBean {
                /**
                 * character : 手
                 * confidence : 90.9
                 */

                private String character;
                private double confidence;

                public String getCharacter() {
                    return character;
                }

                public void setCharacter(String character) {
                    this.character = character;
                }

                public double getConfidence() {
                    return confidence;
                }

                public void setConfidence(double confidence) {
                    this.confidence = confidence;
                }
            }
        }
    }
}
