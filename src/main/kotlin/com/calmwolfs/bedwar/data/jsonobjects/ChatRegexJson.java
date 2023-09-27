package com.calmwolfs.bedwar.data.jsonobjects;

import com.google.gson.annotations.Expose;

public class ChatRegexJson {
    @Expose
    public String gameEndPattern;

    @Expose
    public String bedBreakPattern;

    @Expose
    public String finalKillPattern;

    @Expose
    public String killPattern;

    @Expose
    public String voidPattern;
    @Expose
    public String teamEliminatedPattern;
}