package com.hussainkarafallah.domain;

import java.util.List;

import lombok.Getter;

public enum Instrument {


    MTLCA,
    MGDTH,
    TOOL,
    OPETH,
    IRNMDN,
    JDSPRST,
    THRSH_MTL(List.of(MTLCA, MGDTH)),
    HVY_MTL(List.of(IRNMDN , JDSPRST));

    @Getter
    private List<Instrument> components;

    Instrument(){
        this.components = List.of();
    }

    Instrument(List<Instrument> components){
        this.components = components;
    }

    public boolean isComposite(){
        return !components.isEmpty();
    }
}
