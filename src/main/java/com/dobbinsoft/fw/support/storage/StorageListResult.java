package com.dobbinsoft.fw.support.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StorageListResult {

    private String marker;

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {

        private String key;

        private Long size;

    }

}
