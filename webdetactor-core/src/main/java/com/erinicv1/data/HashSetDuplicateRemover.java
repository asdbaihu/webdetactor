package com.erinicv1.data;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class HashSetDuplicateRemover<ID> implements DuplicateRemover<ID> {

    private Set<ID> ids = Sets.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public boolean isDuplicate(ID id){
        return !ids.add(id);
    }
}
