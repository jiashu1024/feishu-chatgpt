package com.zjs.feishubot.entity;

public enum Model {
    GPT_3_5("GPT-3.5", "text-davinci-002-render-sha"),
    PLUS_4_DEFAULT("GPT-4", "gpt-4"),
    PLUS_GPT_4_BROWSING("GPT-4-BROWSING", "gpt-4-browsing"),
    PLUS_GPT_4_MOBILE("GPT-4-MOBILE", "gpt-4-mobile"),
    ;

    public final String key;
    public final String value;


//    public static final String GPT_3_5 = "text-davinci-002-render-sha";
//    public static final String PLUS_4_DEFAULT = "gpt-4";
//    public static final String PLUS_GPT_4_BROWSING = "gpt-4-browsing";

    Model(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
